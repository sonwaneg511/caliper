# Meta Ads Integration — Implementation Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Configuration](#configuration)
   - [Facebook Account / Page Setup (Dealer-Level)](#facebook-account--page-setup-dealer-level)
5. [Database Tables](#database-tables)
6. [Campaign Lifecycle](#campaign-lifecycle)
7. [API Endpoints](#api-endpoints)
   - [Create Campaign (Client)](#1-create-campaign-client)
   - [Approve Campaign (COE)](#2-approve-campaign-coe)
   - [List Campaigns](#3-list-campaigns)
   - [Campaign Details](#4-campaign-details)
   - [Pause Campaign](#5-pause-campaign)
   - [Campaign Report (Insights)](#6-campaign-report-insights)
   - [Objective Options](#7-objective-options)
   - [Geo Location Search](#8-geo-location-search)
   - [Webhook — Verify](#9-webhook--verify)
   - [Webhook — Lead Events](#10-webhook--lead-events)
8. [Creative Types](#creative-types)
   - [Multi-Ratio Images](#multi-ratio-images)
   - [Advantage+ Creative](#advantage-creative-degrees_of_freedom)
9. [Targeting Types](#targeting-types)
10. [Objectives, Optimization Goals & Destination Types](#objectives-optimization-goals--destination-types)
11. [Budget Rules](#budget-rules)
12. [Bid Strategy](#bid-strategy)
13. [Deployment Task](#deployment-task)
14. [Insights](#insights)

---

## Overview

The Meta Ads module enables clients to create and manage Facebook/Instagram ad campaigns through the Caliper platform. The flow is:

```
Client creates campaign → COE approves → Scheduler deploys to Meta API → Insights available
```

All Meta API calls go through the **Meta Business SDK (Java)**. Campaigns are created in a **PAUSED** state in Meta and activated separately.

**Auto-derived values** — the system automatically fetches the following so clients don't need to provide them:
- `page_id` — pulled from the `facebook_page` table for the client
- `pixel_id` — pulled from the `facebook_account` table for the client
- `destination_type` — derived from the campaign objective
- `promoted_object` — built from the objective + optimization goal decision matrix

---

## Architecture

```
MetaCampaignController          — REST API surface
  └─ MetaCampaignService        — Business logic, entity creation
  └─ MetaInsightsService        — Fetches insights from Meta API

MetaAdsCampaignDeploymentTask   — Scheduled job: deploys PENDING_DEPLOYMENT campaigns
  └─ MetaAdsApiService          — Static utility: all Meta SDK calls
  └─ MetaAdsValidator           — Validates create-campaign request

MetaGeoLocationSyncTask         — Scheduled job: fetches city/region keys from Meta and stores them in DB

Entities (JPA / Hibernate auto-DDL):
  meta_campaign         — Campaign header (budget lives here)
  meta_adset            — Targeting, optimization goal, audience
  meta_ad_creative      — Ad copy, image/video URL, CTA, creative type, degrees_of_freedom flag
  meta_ad               — Links adset ↔ creative
  meta_ad_image         — Single-image upload tracking (used when image_assets is not provided)
  meta_ad_image_asset   — Per-asset records for multi-ratio image ads
  meta_ad_creative_text — Extra headline/body(primary text)/description variants beyond the primary one on meta_ad_creative
  meta_ad_carousel_card — Per-card data for CAROUSEL ads
  meta_geo_location     — Cached city/region keys fetched from Meta Targeting Search API

Supporting:
  facebook_account      — Per-client (or per-dealer) Meta Ad Account + access token + pixel_id; dealer_id=NULL means shared
  facebook_page         — Per-client (or per-dealer) Facebook Page ID; dealer_id=NULL means shared
  client_location_setup — Lat/lng/radius used for RADIUS targeting
  dealer_location       — Country code used to build pincode targeting key
```

---

## Prerequisites

### 1. Meta Developer App

1. Go to [developers.facebook.com](https://developers.facebook.com) and create an App (type: **Business**).
2. Add the **Marketing API** product to the app.
3. Generate a **System User Access Token** with these permissions:
   - `ads_management`
   - `ads_read`
   - `pages_read_engagement`
   - `leads_retrieval` (if using Lead Gen campaigns)
4. Get your **Ad Account ID** (format: `act_XXXXXXXXXX`) from [business.facebook.com](https://business.facebook.com).
5. Get your **Facebook Page ID** from the Page settings.

### 2. Webhook (Lead Gen only)

1. In your Meta App → Webhooks, subscribe to the `leadgen` field on the `page` object.
2. Set the callback URL to: `https://<your-domain>/meta-campaign/webhook`
3. Set the verify token to match `meta.webhook.verify.token` in `application.properties`.

---

## Configuration

### `application.properties`

```properties
# Meta Ads Webhook verify token — must match what you enter in the Meta Developer Console
meta.webhook.verify.token=caliper_meta_verify_token
```

### Database seeding required before first campaign

The `facebook_account` table must have a row for each client before they can create campaigns:

```sql
INSERT INTO facebook_account (account_id, client_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (123456789012345,   -- Meta Ad Account ID (without "act_" prefix)
        1,                  -- client_id in Caliper
        'My Ad Account',
        'EAAxxxx...',       -- System User Access Token
        987654321098765,    -- pixel_id (optional, used for conversion tracking)
        'admin',
        NOW());
```

The `facebook_page` table must have a row for each client (page_id is auto-fetched from here):

```sql
INSERT INTO facebook_page (facebook_page_id, client_id, page_name, ...)
VALUES (111222333444555, 1, 'My Facebook Page', ...);
```

See [Facebook Account / Page Setup (Dealer-Level)](#facebook-account--page-setup-dealer-level) below for multi-dealer configurations.

For **RADIUS targeting**, `client_location_setup` must have the dealer's location:

```sql
INSERT INTO client_location_setup (client_id, dealer_id, latitude, longitude, radius, radius_unit)
VALUES ('1', 'D001', '19.0760', '72.8777', 10, 'kilometer');
```

For **PINCODE targeting**, `dealer_location` must have a `country_code` for the dealer (used to build the `IN:400001` key format):

```sql
UPDATE dealer_location SET country_code = 'IN' WHERE dealer_id = 'D001';
```

### Facebook Account / Page Setup (Dealer-Level)

The system supports three real-world configurations. In all cases, the lookup uses a **two-step resolution**:

1. **Exact dealer match** — row with `dealer_id = campaign.dealer_id`
2. **Shared fallback** — row with `dealer_id IS NULL` (shared across all dealers for that client)
3. If neither exists → campaign creation / deployment fails with an error

> **Why `dealer_id IS NULL` and not a `findByClientId` lookup?**
> `findByClientId` returns all rows for a client — in a mixed setup this could non-deterministically return a different dealer's account as the fallback. The `IS NULL` lookup explicitly targets the shared/catch-all row only.

---

#### Pattern A — One shared account for all dealers

Use this when all of a client's dealers run ads from the same Meta Ad Account and Facebook Page. Insert a single row with `dealer_id = NULL`.

```sql
-- Shared account (all dealers fall back to this)
INSERT INTO facebook_account (account_id, client_id, dealer_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (111222333444555, 'RE_476085', NULL, 'RE Shared Account', 'EAAxx...', 987654321, 'admin', NOW());

INSERT INTO facebook_page (facebook_page_id, client_id, dealer_id, page_name, ...)
VALUES (222333444555666, 'RE_476085', NULL, 'Royal Enfield India', ...);
```

Any campaign for any dealer under `RE_476085` will resolve to this shared row.

---

#### Pattern B — Fully dealer-specific accounts

Use this when every dealer has its own Meta Ad Account and Facebook Page.

```sql
-- D001 (Agra)
INSERT INTO facebook_account (account_id, client_id, dealer_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (444555666777888, 'RE_476085', 'D001', 'RE Agra Account', 'EAAyy...', 111111111, 'admin', NOW());

INSERT INTO facebook_page (facebook_page_id, client_id, dealer_id, page_name, ...)
VALUES (555666777888999, 'RE_476085', 'D001', 'Royal Enfield Agra', ...);

-- D002 (Jaipur)
INSERT INTO facebook_account (account_id, client_id, dealer_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (777888999000111, 'RE_476085', 'D002', 'RE Jaipur Account', 'EAAzz...', 222222222, 'admin', NOW());

INSERT INTO facebook_page (facebook_page_id, client_id, dealer_id, page_name, ...)
VALUES (888999000111222, 'RE_476085', 'D002', 'Royal Enfield Jaipur', ...);
```

Each dealer resolves to its own account. If a dealer without a row tries to create a campaign, it fails with `"No Facebook Account found for client ... dealer ..."`.

---

#### Pattern C — Mixed (some dealers own, some share)

Use this when certain dealers have their own accounts but the rest should fall back to a client-level shared account.

```sql
-- Shared fallback for all dealers that don't have their own row
INSERT INTO facebook_account (account_id, client_id, dealer_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (111222333444555, 'RE_476085', NULL, 'RE Shared Account', 'EAAxx...', 987654321, 'admin', NOW());

INSERT INTO facebook_page (facebook_page_id, client_id, dealer_id, page_name, ...)
VALUES (222333444555666, 'RE_476085', NULL, 'Royal Enfield India', ...);

-- D001 has its own dedicated account
INSERT INTO facebook_account (account_id, client_id, dealer_id, account_name, access_token, pixel_id, last_modified_by, last_modified_date)
VALUES (444555666777888, 'RE_476085', 'D001', 'RE Agra Account', 'EAAyy...', 111111111, 'admin', NOW());

INSERT INTO facebook_page (facebook_page_id, client_id, dealer_id, page_name, ...)
VALUES (555666777888999, 'RE_476085', 'D001', 'Royal Enfield Agra', ...);
```

- Campaign for D001 → picks D001-specific rows.
- Campaign for D002 (no own row) → falls back to the `NULL` (shared) row.
- Campaign for any other dealer → also falls back to the shared row.

---

#### Resolution applies to

| Service | Where dealer-aware lookup happens |
|---|---|
| `MetaCampaignService` | Campaign creation — resolves `page_id` and `pixel_id` |
| `MetaAdsCampaignDeploymentTask` | Deployment — resolves Ad Account ID for Meta API calls |
| `MetaInsightsService` | Insights — resolves access token to query Meta |
| `MetaWebhookService` | Lead webhook — resolves access token to fetch lead details |
| `MetaGeoLocationSyncTask` | Geo sync — uses `findByClientId()` only (no dealer context needed) |

---

### Scheduler

The deployment task `MetaAdsCampaignDeploymentTask` is a `ParameterizedJob`. It picks up all campaigns in **Pending Deployment** status and deploys them to Meta in order. Configure the schedule in the scheduler configuration as appropriate for your environment.

---

## Database Tables

Hibernate `ddl-auto=update` creates and alters these tables automatically.

| Table | Key Columns |
|---|---|
| `meta_campaign` | `id`, `client_id`, `dealer_id`, `campaign_name`, `objective`, `daily_budget`, `total_budget`, `budget_type`, `start_time`, `stop_time`, `status`, `meta_campaign_id`, `created_by`, `destination_type`, `coe_comment`, `error_comment`, `client_comment`, `last_modified_by`, `last_modified_date` |
| `meta_adset` | `id`, `campaign_id`, `adset_name`, `meta_adset_id`, `optimization_goal`, `billing_event`, `bid_amount`, `bid_strategy`, `start_time`, `stop_time`, `targeting_type`, `latitude`, `longitude`, `radius`, `radius_unit`, `pincode`, `city`, `region`, `gender`, `age_min`, `age_max`, `publisher_platforms`, `facebook_positions`, `instagram_positions`, `promoted_object_page_id`, `promoted_object_pixel_id`, `promoted_object_custom_event_type`, `whatsapp_number`, `status` |
| `meta_ad_creative` | `id`, `campaign_id`, `meta_creative_id`, `name`, `page_id`, `headline`, `body`, `description`, `call_to_action_type`, `link_url`, `image_url`, `image_hash`, `video_url`, `video_id`, `thumbnail_url`, `thumbnail_hash`, `creative_type`, `degrees_of_freedom` |
| `meta_ad` | `id`, `campaign_id`, `adset_id`, `ad_name`, `meta_ad_id`, `creative_id`, `status` |
| `meta_ad_image` | `id`, `campaign_id`, `client_id`, `image_url`, `image_hash`, `image_name`, `status` |
| `meta_ad_image_asset` | `id`, `campaign_id`, `creative_id`, `image_url`, `image_hash`, `ratio`, `asset_order` |
| `meta_ad_creative_text` | `id`, `campaign_id`, `creative_id`, `type` (`HEADLINE`\|`BODY`\|`DESCRIPTION`), `value`, `text_order` |
| `meta_ad_carousel_card` | `id`, `campaign_id`, `creative_id`, `card_order`, `headline`, `description`, `image_url`, `image_hash`, `link_url`, `video_url`, `video_id` |
| `meta_geo_location` | `id`, `meta_key`, `name`, `location_type`, `country_code`, `region_name`, `synced_at` |

**Column notes:**

| Column | Table | Description |
|---|---|---|
| `coe_comment` | `meta_campaign` | COE approval note set when a hub user approves the campaign |
| `error_comment` | `meta_campaign` | Deployment error message set when the deployment task fails (max 500 chars) |
| `client_comment` | `meta_campaign` | Free-text note entered by the client at campaign creation time |
| `degrees_of_freedom` | `meta_ad_creative` | When `true`, Meta's Advantage+ Creative (`degrees_of_freedom_spec`) is enrolled — Meta may auto-adjust text, images, and CTA to improve performance |
| `ratio` | `meta_ad_image_asset` | Aspect ratio label for the asset e.g. `"1:1"`, `"1.91:1"`, `"9:16"`, `"4:5"` — stored for reference; Meta picks the best-fitting image per placement automatically |
| `headlines` / `bodies` / `descriptions` | request only | Optional lists (1-5 entries each) of headline / primary-text / description variants — mirrors Ads Manager's "Add text options". Takes precedence over the singular `headline`/`body`/`description` fields; not allowed for `headlines`/`descriptions` on CAROUSEL (use `carousel_cards[].headline`/`description` there instead), but `bodies` is allowed on CAROUSEL. Meta auto-tests combinations via `asset_feed_spec`. |

**Campaign status flow:**

```
Draft → Pending Deployment → Deployed
                           → Error (deployment failed)
            → Paused (manual pause)
```

---

## Campaign Lifecycle

```
1. Client  →  POST /meta-campaign/client-campaign   (status: Draft)
             • Validates request
             • Auto-fetches page_id from facebook_page table
             • Auto-fetches pixel_id from facebook_account table
             • Auto-derives destination_type from objective
             • Saves campaign, adset, creative, carousel cards (if CAROUSEL), ad records

2. COE     →  POST /meta-campaign/coe-approve        (status: Pending Deployment)

3. Scheduler → MetaAdsCampaignDeploymentTask runs:
               a. Creates Campaign in Meta (PAUSED) — budget at campaign level
               b. Creates AdSet in Meta with geo targeting + audience (PAUSED)
               c. Dispatch by creative type:
                  IMAGE (single)       → upload image → create image AdCreative
                  IMAGE (multi-ratio)  → upload all assets → create asset_feed_spec AdCreative
                  VIDEO                → upload video → poll until ready → upload thumbnail → create video AdCreative
                  CAROUSEL             → upload image per card → create carousel AdCreative
                  * degrees_of_freedom=true adds Advantage+ Creative spec to any creative type
               d. Creates Ad in Meta linking AdSet ↔ AdCreative (PAUSED)
               e. Marks campaign Deployed in DB

4. Client  →  POST /meta-campaign/campaign-report    (fetch live insights from Meta)
```

---

## API Endpoints

Base path: `/meta-campaign`

All endpoints require JWT authentication (Bearer token from `/signin`).

---

### 1. Create Campaign (Client)

**`POST /meta-campaign/client-campaign`**

Creates a campaign in **Draft** status. All creative and targeting data is captured at this point.

#### Request Body — IMAGE ad (minimal)

```json
{
  "client_id": "1",
  "dealer_id": "D001",
  "campaign_name": "Summer Sale 2026",
  "objective": "OUTCOME_TRAFFIC",
  "start_date": "2026-06-01T00:00:00.000Z",
  "end_date": "2026-06-30T23:59:59.000Z",
  "daily_budget": 500.00,
  "budget_type": "DAILY",
  "headline": "Best Deals This Summer",
  "body": "Get up to 40% off on all products. Limited time offer!",
  "description": "Shop now and save big.",
  "call_to_action_type": "LEARN_MORE",
  "link_url": "https://example.com/sale",
  "creative_type": "IMAGE",
  "image_url": "https://example.com/images/summer-banner.jpg",
  "created_by": "john.doe@example.com",
  "targeting_type": "PINCODE",
  "pincode": "400001"
}
```

#### Request Body — VIDEO ad

```json
{
  "client_id": "1",
  "dealer_id": "D001",
  "campaign_name": "Brand Video Campaign",
  "objective": "OUTCOME_AWARENESS",
  "start_date": "2026-06-01T00:00:00.000Z",
  "end_date": "2026-06-30T23:59:59.000Z",
  "daily_budget": 800.00,
  "budget_type": "DAILY",
  "headline": "Our Story",
  "body": "Watch how we serve you better.",
  "call_to_action_type": "LEARN_MORE",
  "link_url": "https://example.com/about",
  "creative_type": "VIDEO",
  "video_url": "https://example.com/videos/brand-video.mp4",
  "thumbnail_url": "https://example.com/images/brand-video-thumb.jpg",
  "created_by": "john.doe@example.com",
  "targeting_type": "CITY",
  "city": "Mumbai"
}
```

#### Request Body — WhatsApp ad

```json
{
  "client_id": "1",
  "dealer_id": "D001",
  "campaign_name": "WhatsApp Enquiry Campaign",
  "objective": "OUTCOME_TRAFFIC",
  "start_date": "2026-06-01T00:00:00.000Z",
  "end_date": "2026-06-30T23:59:59.000Z",
  "daily_budget": 500.00,
  "budget_type": "DAILY",
  "headline": "Chat With Us on WhatsApp",
  "body": "Get instant answers to all your queries. Click to chat now!",
  "call_to_action_type": "LEARN_MORE",
  "link_url": "https://example.com",
  "creative_type": "IMAGE",
  "image_url": "https://example.com/images/whatsapp-banner.jpg",
  "created_by": "john.doe@example.com",
  "targeting_type": "PINCODE",
  "pincode": "400001",
  "destination_type": "WHATSAPP",
  "whatsapp_number": "+919876543210"
}
```

#### Request Body — Multi-Ratio IMAGE ad (Advantage+ Creative)

```json
{
  "client_id": "1",
  "dealer_id": "D001",
  "campaign_name": "Multi-Format Summer Campaign",
  "objective": "OUTCOME_TRAFFIC",
  "start_date": "2026-06-01T00:00:00.000Z",
  "end_date": "2026-06-30T23:59:59.000Z",
  "daily_budget": 500.00,
  "budget_type": "DAILY",
  "headline": "Best Deals This Summer",
  "body": "Get up to 40% off on all products. Limited time offer!",
  "call_to_action_type": "LEARN_MORE",
  "link_url": "https://example.com/sale",
  "creative_type": "IMAGE",
  "image_assets": [
    { "image_url": "https://example.com/images/banner-landscape.jpg", "ratio": "1.91:1" },
    { "image_url": "https://example.com/images/banner-square.jpg",    "ratio": "1:1" },
    { "image_url": "https://example.com/images/banner-portrait.jpg",  "ratio": "4:5" },
    { "image_url": "https://example.com/images/banner-stories.jpg",   "ratio": "9:16" }
  ],
  "degrees_of_freedom": true,
  "created_by": "john.doe@example.com",
  "targeting_type": "PINCODE",
  "pincode": "400001"
}
```

> When `image_assets` is provided, Meta receives all images via `asset_feed_spec` and automatically selects the best-fitting image for each placement (Feed, Stories, Reels, etc.). Omit `image_url` — it is not needed alongside `image_assets`.
>
> `degrees_of_freedom: true` additionally allows Meta to auto-enhance the ad elements (text, brightness, aspect ratio adjustments) for better performance (Advantage+ Creative).

#### Request Body — CAROUSEL ad

```json
{
  "client_id": "1",
  "dealer_id": "D001",
  "campaign_name": "Bike Showcase Carousel",
  "objective": "OUTCOME_TRAFFIC",
  "start_date": "2026-06-01T00:00:00.000Z",
  "end_date": "2026-06-30T23:59:59.000Z",
  "daily_budget": 600.00,
  "budget_type": "DAILY",
  "headline": "Explore Our Range",
  "body": "Find the perfect bike for you.",
  "call_to_action_type": "LEARN_MORE",
  "link_url": "https://example.com/bikes",
  "creative_type": "CAROUSEL",
  "carousel_cards": [
    {
      "headline": "Classic 350",
      "description": "Timeless design, modern performance.",
      "image_url": "https://example.com/images/classic350.jpg",
      "link_url": "https://example.com/bikes/classic350"
    },
    {
      "headline": "Meteor 350",
      "description": "Built for long rides.",
      "image_url": "https://example.com/images/meteor350.jpg",
      "link_url": "https://example.com/bikes/meteor350"
    },
    {
      "headline": "Himalayan",
      "description": "Adventure starts here.",
      "image_url": "https://example.com/images/himalayan.jpg",
      "link_url": "https://example.com/bikes/himalayan"
    }
  ],
  "created_by": "john.doe@example.com",
  "targeting_type": "RADIUS"
}
```

#### Full Field Reference

| Field | Required | Description |
|---|---|---|
| `client_id` | Yes | Caliper client identifier |
| `dealer_id` | Yes | Dealer/location identifier |
| `campaign_name` | Yes | Name shown in Meta Ads Manager |
| `objective` | Yes | Campaign objective — see [Objectives](#objectives-optimization-goals--destination-types) |
| `start_date` | Yes | Campaign start (ISO 8601) |
| `end_date` | Yes | Campaign end (ISO 8601), must be after `start_date` |
| `daily_budget` | Conditional | Daily budget in local currency (e.g. ₹500). Required if `budget_type` is `DAILY` (or omitted). Minimum ₹40. Applied at **campaign level only** |
| `total_budget` | Conditional | Total/lifetime budget. Required if `budget_type` is `LIFETIME` |
| `budget_type` | No | `DAILY` (default) or `LIFETIME`. Lifetime budgets require a fixed `end_date` |
| `headline` | Yes | Ad headline text |
| `body` | Yes | Ad primary text |
| `description` | No | Additional description text |
| `call_to_action_type` | No | CTA button — `LEARN_MORE` (default), `SIGN_UP`, `CONTACT_US`, `GET_QUOTE`, `APPLY_NOW` |
| `link_url` | Yes | Default destination URL |
| `created_by` | Yes | Name or email of the person creating the campaign |
| `client_comment` | No | Internal note from client to COE |
| **Creative** | | |
| `creative_type` | No | `IMAGE` (default), `VIDEO`, or `CAROUSEL` — see [Creative Types](#creative-types) |
| `image_url` | Conditional | Publicly accessible image URL. Required when `creative_type` is `IMAGE` **and** `image_assets` is not provided |
| `image_assets` | Conditional | Array of `{image_url, ratio}` objects for multi-ratio IMAGE ads. When present, `image_url` is ignored and a `asset_feed_spec` creative is created — Meta picks the best-fitting image per placement. At least one asset required. See [Multi-Ratio Images](#multi-ratio-images) |
| `image_assets[].image_url` | Yes (per asset) | Publicly accessible image URL for this asset |
| `image_assets[].ratio` | No | Aspect ratio label for reference — e.g. `"1:1"`, `"1.91:1"`, `"9:16"`, `"4:5"`. Not sent to Meta; used for your own tracking only |
| `degrees_of_freedom` | No | `true` to enrol in **Advantage+ Creative** — Meta may auto-enhance text, image framing, and CTA placement for better performance. Applies to IMAGE, VIDEO, and CAROUSEL creatives. Default: `false` |
| `video_url` | Conditional | Publicly accessible video URL. Required when `creative_type` is `VIDEO` |
| `thumbnail_url` | Conditional | Publicly accessible thumbnail image URL. **Required when `creative_type` is `VIDEO`** — Meta enforces a video thumbnail on all video ad creatives |
| `carousel_cards` | Conditional | Array of card objects. Required (≥ 2 cards) when `creative_type` is `CAROUSEL` |
| `carousel_cards[].headline` | Yes (per card) | Card headline |
| `carousel_cards[].description` | No | Card description |
| `carousel_cards[].image_url` | Yes (per card) | Card image URL |
| `carousel_cards[].link_url` | Yes (per card) | Card destination URL |
| **Targeting** | | |
| `targeting_type` | Yes | One of: `RADIUS`, `PINCODE`, `CITY`, `REGION` — see [Targeting Types](#targeting-types) |
| `pincode` | Conditional | Required when `targeting_type` is `PINCODE`. E.g. `"400001"` |
| `city` | Conditional | Required when `targeting_type` is `CITY`. Pass the **city name** e.g. `"Mumbai"`. System resolves it to the Meta key via `meta_geo_location` table. Browse names via `GET /meta-campaign/geo-search?location_type=city` |
| `region` | Conditional | Required when `targeting_type` is `REGION`. Pass the **region name** e.g. `"Maharashtra"`. System resolves it to the Meta key via `meta_geo_location` table. Browse names via `GET /meta-campaign/geo-search?location_type=region` |
| **Audience** | | |
| `gender` | No | `ALL` (default), `MALE`, `FEMALE` |
| `age_min` | No | Minimum age. Range: 18–65. Meta default: 18 |
| `age_max` | No | Maximum age. Range: 18–65. Meta default: 65 |
| `publisher_platforms` | No | `["facebook", "instagram", "messenger", "audience_network"]`. Omit to use Meta defaults |
| `facebook_positions` | No | `["feed", "story", "reels", "marketplace", "video_feeds", "search", "right_hand_column"]` |
| `instagram_positions` | No | `["stream", "story", "explore", "reels", "profile_feed", "explore_home", "ig_search"]` |
| **Optimization** | | |
| `optimization_goal` | No | Override default optimization goal. Omit to use the default for the chosen objective |
| `destination_type` | No | Override auto-derived destination type. **Required when using WhatsApp**: set to `WHATSAPP`. Other values: `WEBSITE`, `APP`, `MESSENGER`, `INSTAGRAM_DIRECT`, `INSTANT_FORMS`, `PHONE_CALL`, `FACEBOOK_PAGE` |
| `whatsapp_number` | Conditional | Required when `destination_type` is `WHATSAPP`. Must include country code e.g. `"+919876543210"` |
| `bid_strategy` | No | `LOWEST_COST_WITHOUT_CAP` (default), `LOWEST_COST_WITH_BID_CAP`, `COST_CAP`, `LOWEST_COST_WITH_MIN_ROAS` |

> **Auto-derived fields** — `page_id`, `pixel_id`, and `promoted_object` are determined automatically by the system. `destination_type` is also auto-derived unless overridden (required for WHATSAPP).

#### Response

```json
{
  "result": "SUCCESS",
  "message": "Meta campaign created with id: 42",
  "role": "client",
  "id": 42
}
```

---

### 2. Approve Campaign (COE)

**`POST /meta-campaign/coe-approve`**

Moves a campaign from Draft to **Pending Deployment**. The deployment scheduler will pick it up on the next run.

#### Request Body

```json
{
  "campaign_id": 42,
  "comment": "Approved. Budget looks good."
}
```

| Field | Required | Description |
|---|---|---|
| `campaign_id` | Yes | Caliper campaign ID (returned from create) |
| `comment` | No | COE internal note — stored in `meta_campaign.coe_comment` |

#### Response

```json
{
  "result": "SUCCESS",
  "message": "Campaign approved for deployment: 42",
  "role": "hub_user",
  "id": 42
}
```

---

### 3. List Campaigns

**`POST /meta-campaign/all-campaigns`**

Returns a paginated list of campaigns for a client/dealer. Results are ordered newest first.

#### Request Body

```json
{
  "client_id": "1",
  "dealer_id": ["D001", "D002"],
  "page_no": 0,
  "search": ""
}
```

| Field | Required | Description |
|---|---|---|
| `client_id` | Yes | Caliper client identifier |
| `dealer_id` | Yes | List of dealer IDs to filter by |
| `page_no` | Yes | Zero-based page number (page size = 10) |
| `search` | No | Search term (not yet active) |

#### Response

```json
{
  "content": [
    {
      "campaign_id": 42,
      "campaign_name": "Summer Sale 2026",
      "objective": "OUTCOME_TRAFFIC",
      "status": "Deployed",
      "daily_budget": 500.00,
      "total_budget": null,
      "start_time": "2026-06-01T00:00:00.000+00:00",
      "stop_time": "2026-06-30T23:59:59.000+00:00",
      "dealer_id": "D001",
      "meta_campaign_id": "120200000012345678",
      "coe_comment": "Approved. Budget looks good.",
      "error_comment": null,
      "client_comment": "Please review and approve.",
      "created_by": "john.doe@example.com"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10 },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 4. Campaign Details

**`GET /meta-campaign/campaign-details/{campaignId}`**

Returns full details of a single campaign including adset, ad creative, and ad records.

#### Path Parameter

| Parameter | Description |
|---|---|
| `campaignId` | Caliper campaign ID |

#### Response

```json
{
  "campaign_id": 42,
  "campaign_name": "Summer Sale 2026",
  "objective": "OUTCOME_TRAFFIC",
  "status": "Deployed",
  "daily_budget": 500.00,
  "total_budget": null,
  "budget_type": "DAILY",
  "start_time": "2026-06-01T00:00:00.000+00:00",
  "stop_time": "2026-06-30T23:59:59.000+00:00",
  "dealer_id": "D001",
  "meta_campaign_id": "120200000012345678",
  "coe_comment": "Approved.",
  "error_comment": null,
  "client_comment": "Please review.",
  "created_by": "john.doe@example.com",
  "adset": {
    "id": 18,
    "campaignId": 42,
    "adSetName": "Summer Sale 2026 - AdSet",
    "metaAdSetId": "120200000023456789",
    "optimizationGoal": "LANDING_PAGE_VIEWS",
    "billingEvent": "IMPRESSIONS",
    "bidAmount": 0,
    "bidStrategy": "LOWEST_COST_WITHOUT_CAP",
    "startTime": "2026-06-01T00:00:00.000+00:00",
    "stopTime": "2026-06-30T23:59:59.000+00:00",
    "targetingType": "PINCODE",
    "pincode": "IN:400001",
    "city": null,
    "region": null,
    "latitude": null,
    "longitude": null,
    "radius": 0.0,
    "radiusUnit": null,
    "gender": "ALL",
    "ageMin": 25,
    "ageMax": 55,
    "publisherPlatforms": "facebook,instagram",
    "facebookPositions": "feed,story,reels",
    "instagramPositions": "stream,story,reels",
    "status": "Deployed"
  },
  "ad_creative": {
    "id": 18,
    "campaignId": 42,
    "headline": "Best Deals This Summer",
    "body": "Get up to 40% off...",
    "description": "Shop now and save big.",
    "callToActionType": "LEARN_MORE",
    "linkUrl": "https://example.com/sale",
    "imageUrl": "https://example.com/images/summer-banner.jpg",
    "imageHash": "abc123def456...",
    "videoUrl": null,
    "videoId": null,
    "thumbnailUrl": null,
    "creativeType": "IMAGE",
    "degreesOfFreedom": false
  },
  "ad": {
    "id": 18,
    "campaignId": 42,
    "adSetId": 18,
    "adName": "Summer Sale 2026 - Ad",
    "metaAdId": "120200000034567890",
    "status": "Deployed"
  }
}
```

---

### 5. Pause Campaign

**`POST /meta-campaign/pause/{campaignId}`**

Stubs a pause request (actual Meta API pause not yet wired — returns success immediately).

#### Response

```json
{
  "result": "SUCCESS",
  "message": "Pause initiated for campaign: 42",
  "role": null,
  "id": 42
}
```

---

### 6. Campaign Report (Insights)

**`POST /meta-campaign/campaign-report`**

Fetches live performance metrics from the Meta API for a deployed campaign.

#### Request Body

```json
{
  "campaign_id": 42,
  "date_preset": "LAST_30_DAYS"
}
```

| Field | Required | Description |
|---|---|---|
| `campaign_id` | Yes | Caliper campaign ID |
| `date_preset` | No | Meta date preset string. Defaults to `LAST_30_DAYS` if omitted |

**Supported `date_preset` values:**

| Value | Period |
|---|---|
| `TODAY` | Today |
| `YESTERDAY` | Yesterday |
| `LAST_7_DAYS` | Last 7 days |
| `LAST_14_DAYS` | Last 14 days |
| `LAST_30_DAYS` | Last 30 days (default) |
| `THIS_MONTH` | Current calendar month |
| `LAST_MONTH` | Previous calendar month |
| `THIS_QUARTER` | Current quarter |
| `LAST_QUARTER` | Previous quarter |
| `THIS_YEAR` | Current year |
| `LAST_YEAR` | Previous year |
| `MAXIMUM` | All time |

#### Response

```json
{
  "campaign_id": 42,
  "meta_campaign_id": "120200000012345678",
  "campaign_name": "Summer Sale 2026",
  "impressions": "84320",
  "reach": "61450",
  "clicks": "1230",
  "spend": "4820.50",
  "leads": "47",
  "date_preset": "LAST_30_DAYS"
}
```

> **Note:** CTR, CPM, and CPC are intentionally excluded. The `leads` field is populated only for Lead Generation campaigns.

---

### 7. Objective Options

**`GET /meta-campaign/objective-options`**

Returns all supported campaign objectives with their allowed optimization goals and destination types. Use this to populate frontend dropdowns.

#### Response

```json
{
  "OUTCOME_AWARENESS": {
    "optimization_goals": ["REACH", "IMPRESSIONS", "AD_RECALL_LIFT", "THRUPLAY"],
    "destination_types": []
  },
  "OUTCOME_TRAFFIC": {
    "optimization_goals": ["LANDING_PAGE_VIEWS", "LINK_CLICKS", "IMPRESSIONS", "REACH", "POST_ENGAGEMENT"],
    "destination_types": ["WEBSITE", "APP", "MESSENGER", "INSTAGRAM_DIRECT"]
  },
  "OUTCOME_LEADS": {
    "optimization_goals": ["LEAD_GENERATION", "QUALITY_LEAD", "LINK_CLICKS", "LANDING_PAGE_VIEWS"],
    "destination_types": ["INSTANT_FORMS", "WEBSITE", "MESSENGER", "PHONE_CALL"]
  },
  "OUTCOME_ENGAGEMENT": {
    "optimization_goals": ["POST_ENGAGEMENT", "VIDEO_VIEWS", "REACH", "THRUPLAY", "LINK_CLICKS"],
    "destination_types": ["FACEBOOK_PAGE", "WEBSITE", "APP", "MESSENGER"]
  },
  "OUTCOME_SALES": {
    "optimization_goals": ["OFFSITE_CONVERSIONS", "VALUE", "LINK_CLICKS", "LANDING_PAGE_VIEWS"],
    "destination_types": ["WEBSITE", "APP", "MESSENGER"]
  }
}
```

**Usage:** Show the user a dropdown of objectives. When they pick one, filter `optimization_goals` from this response for the next dropdown. Pass the chosen value as `optimization_goal` in the campaign creation request. If omitted, the system applies the default (see [Objectives section](#objectives-optimization-goals--destination-types)).

---

### 8. Geo Location Search

**`GET /meta-campaign/geo-search`**

Searches the local `meta_geo_location` database (populated by `MetaGeoLocationSyncTask`) for city or region names. Use the response to show a dropdown to the user. The `name` field is what the user passes in the `city` / `region` field of the campaign creation request — **not** the `key`.

> **Prerequisite:** Run `MetaGeoLocationSyncTask` at least once before using CITY or REGION targeting.

#### Query Parameters

| Parameter | Required | Description |
|---|---|---|
| `q` | No | Partial name search — e.g. `Mum` returns Mumbai, Mumbai Suburban. Omit to list all entries of the given type |
| `location_type` | No | `city` (default) or `region` |

#### Example — search cities

```
GET /meta-campaign/geo-search?q=Mumbai&location_type=city
```

```json
[
  { "key": "2295420", "name": "Mumbai",          "type": "city", "country_code": "IN", "region": "Maharashtra" },
  { "key": "2296005", "name": "Mumbai Suburban", "type": "city", "country_code": "IN", "region": "Maharashtra" }
]
```

The frontend displays the `name` in a dropdown. When the user selects "Mumbai", send `"city": "Mumbai"` in the campaign creation request. The system automatically resolves it to the correct Meta key.

#### Example — list all regions

```
GET /meta-campaign/geo-search?location_type=region
```

```json
[
  { "key": "3388", "name": "Maharashtra", "type": "region", "country_code": "IN", "region": "" },
  { "key": "3389", "name": "Delhi",       "type": "region", "country_code": "IN", "region": "" }
]
```

---

### 9. Webhook — Verify

**`GET /meta-campaign/webhook`**

Used by Meta to verify the webhook endpoint during setup. Handled automatically — no manual action needed.

#### Query Parameters

| Parameter | Description |
|---|---|
| `hub.mode` | Must be `subscribe` |
| `hub.verify_token` | Must match `meta.webhook.verify.token` in properties |
| `hub.challenge` | Random string echoed back to Meta |

---

### 10. Webhook — Lead Events

**`POST /meta-campaign/webhook`**

Receives real-time lead data from Meta when a user submits a Lead Gen form. Leads are stored in the `meta_lead` table.

Meta sends this automatically when a lead is captured — no manual interaction needed.

#### Headers

| Header | Description |
|---|---|
| `X-Hub-Signature-256` | HMAC-SHA256 signature (optional validation) |

---

## Creative Types

The `creative_type` field controls which ad format is used. It defaults to `IMAGE` if not specified.

| `creative_type` | Required field(s) | Description |
|---|---|---|
| `IMAGE` (single) | `image_url` | Single image ad. The image is downloaded from the URL and uploaded to Meta at deployment time |
| `IMAGE` (multi-ratio) | `image_assets` | Multiple images for different aspect ratios. All images are uploaded and assembled using Meta's `asset_feed_spec` — Meta automatically selects the best-fitting image for each placement (Feed, Stories, Reels, etc.) |
| `VIDEO` | `video_url`, `thumbnail_url` | Single video ad. The video is downloaded from the URL and uploaded to Meta at deployment time. A thumbnail image is mandatory — Meta rejects video creatives without one |
| `CAROUSEL` | `carousel_cards` (≥ 2) | Multi-image scrollable ad. Each card has its own image, headline, description, and link URL |

### Multi-Ratio Images

When `image_assets` is provided (instead of `image_url`), the system creates an `asset_feed_spec` creative that includes all the uploaded images. Meta selects the most appropriate image for each placement automatically.

Recommended ratios to cover all placements:

| Ratio | Best for |
|---|---|
| `1.91:1` | Facebook Feed, Link Ads |
| `1:1` | Facebook Feed (square), Instagram Feed |
| `4:5` | Facebook / Instagram Feed (vertical) |
| `9:16` | Stories, Reels (full-screen vertical) |

Providing all four ratios gives Meta the most flexibility to serve the ad across all placements without cropping.

### Advantage+ Creative (`degrees_of_freedom`)

When `"degrees_of_freedom": true` is sent, the deployed creative includes:

```json
"degrees_of_freedom_spec": {
  "creative_features_spec": {
    "standard_enhancements": { "enroll_status": "OPT_IN" }
  }
}
```

Meta may then automatically:
- Adjust image brightness / contrast
- Add music to videos
- Show alternative text variations
- Modify the CTA position

This is applied to IMAGE, VIDEO, and CAROUSEL creative types. Omit or set to `false` to keep the creative exactly as submitted.

### Carousel Cards

Each entry in `carousel_cards` must have:

| Field | Required | Description |
|---|---|---|
| `headline` | Yes | Card title (shown below the image) |
| `description` | No | Short supporting text |
| `image_url` | Yes | Publicly accessible image URL for this card |
| `link_url` | Yes | Destination URL when this card is clicked |

Minimum 2 cards, no hard upper limit (Meta recommends 2–10).

### Image / Video Upload

Images and videos are **not** uploaded at campaign creation time. They are uploaded to the Meta Ad Account during the deployment task run. The system:

1. Downloads the file from the public URL to a temporary file
2. Uploads via the Meta Ads API (`addUploadFile`)
3. Stores the resulting hash (image) or video ID in the database
4. Deletes the temp file

This approach is required because Meta restricts URL-based uploads at the app capability level.

For VIDEO ads, the `thumbnail_url` is uploaded to the Meta Ad Account the same way as images (download → temp file → `addUploadFile` → get hash). The resulting hash is stored in `meta_ad_creative.thumbnail_hash` and passed as `image_hash` in the `AdCreativeVideoData` payload. The thumbnail is **mandatory** — Meta rejects video creatives without one (`error_subcode 1443226`). Direct URL references are not accepted by Meta for thumbnails (`error_subcode 3858258`).

---

## Targeting Types

Exactly one `targeting_type` must be specified per campaign.

| `targeting_type` | Required field | How it works |
|---|---|---|
| `RADIUS` | None (uses `client_location_setup` record) | Targets a circular area around the dealer's lat/lng coordinate with the configured radius |
| `PINCODE` | `pincode` — e.g. `"400001"` | Targets the postal code area. The country code is fetched from `dealer_location.country_code` (defaults to `IN`) and prefixed automatically: `IN:400001` |
| `CITY` | `city` — **human-readable city name** e.g. `"Mumbai"` | System looks up the city name in `meta_geo_location` table and resolves it to the Meta key automatically. Run `MetaGeoLocationSyncTask` first. Browse available cities via `GET /meta-campaign/geo-search?location_type=city` |
| `REGION` | `region` — **human-readable region name** e.g. `"Maharashtra"` | System looks up the region name in `meta_geo_location` table and resolves it to the Meta key automatically. Run `MetaGeoLocationSyncTask` first. Browse available regions via `GET /meta-campaign/geo-search?location_type=region` |

### Pincode Format

Meta requires pincode keys in the format `{countryCode}:{pincode}` (e.g. `IN:400001`). The system builds this automatically by reading the `country_code` column from the `dealer_location` table for the given `dealer_id`. If no country code is found, it defaults to `IN`.

### City / Region Name Resolution

When `targeting_type` is `CITY` or `REGION`, the user passes the **display name** (e.g. `"Mumbai"`, `"Maharashtra"`). The system performs a case-insensitive lookup in the `meta_geo_location` table to find the corresponding Meta key, which is then stored in the `meta_adset` table and used by the deployment task.

If the name is not found in the table, campaign creation will fail with an error like:
```
City 'Pune' not found in the geo-location database.
Run the MetaGeoLocationSyncTask first, or search via
GET /meta-campaign/geo-search?q=Pune&location_type=city
```

---

## Objectives, Optimization Goals & Destination Types

### Default Optimization Goals (when `optimization_goal` is not specified)

| Objective | Default Optimization Goal | Auto-derived Destination Type |
|---|---|---|
| `OUTCOME_AWARENESS` | `REACH` | — (none) |
| `OUTCOME_TRAFFIC` | `LANDING_PAGE_VIEWS` | `WEBSITE` |
| `OUTCOME_LEADS` | `LEAD_GENERATION` | `INSTANT_FORMS` |
| `OUTCOME_ENGAGEMENT` | `POST_ENGAGEMENT` | `FACEBOOK_PAGE` |
| `OUTCOME_SALES` | `OFFSITE_CONVERSIONS` | `WEBSITE` |

### All Supported Options

| Objective | Supported Optimization Goals | Supported Destination Types |
|---|---|---|
| `OUTCOME_AWARENESS` | `REACH`, `IMPRESSIONS`, `AD_RECALL_LIFT`, `THRUPLAY` | — |
| `OUTCOME_TRAFFIC` | `LANDING_PAGE_VIEWS`, `LINK_CLICKS`, `IMPRESSIONS`, `REACH`, `POST_ENGAGEMENT` | `WEBSITE`, `APP`, `MESSENGER`, `INSTAGRAM_DIRECT`, `WHATSAPP` |
| `OUTCOME_LEADS` | `LEAD_GENERATION`, `QUALITY_LEAD`, `LINK_CLICKS`, `LANDING_PAGE_VIEWS` | `INSTANT_FORMS`, `WEBSITE`, `MESSENGER`, `PHONE_CALL`, `WHATSAPP` |
| `OUTCOME_ENGAGEMENT` | `POST_ENGAGEMENT`, `VIDEO_VIEWS`, `REACH`, `THRUPLAY`, `LINK_CLICKS` | `FACEBOOK_PAGE`, `WEBSITE`, `APP`, `MESSENGER`, `WHATSAPP` |
| `OUTCOME_SALES` | `OFFSITE_CONVERSIONS`, `VALUE`, `LINK_CLICKS`, `LANDING_PAGE_VIEWS` | `WEBSITE`, `APP`, `MESSENGER`, `WHATSAPP` |

### Promoted Object Rules (auto-built by system)

The system builds the `promoted_object` for Meta automatically based on this matrix:

| Objective | Condition | Promoted Object |
|---|---|---|
| Any | Destination = `WHATSAPP` | page_id + whatsapp_phone_number |
| `OUTCOME_AWARENESS` | — | None |
| `OUTCOME_TRAFFIC` | Destination = MESSENGER or INSTAGRAM_DIRECT | page_id |
| `OUTCOME_TRAFFIC` | Destination = WEBSITE | pixel_id + event (optional) |
| `OUTCOME_LEADS` | Destination = INSTANT_FORMS / PHONE_CALL / MESSENGER | page_id |
| `OUTCOME_LEADS` | Destination = WEBSITE | pixel_id + event = LEAD |
| `OUTCOME_ENGAGEMENT` | goal = POST_ENGAGEMENT or dest = FACEBOOK_PAGE | page_id |
| `OUTCOME_SALES` | goal = OFFSITE_CONVERSIONS or VALUE | pixel_id + event = PURCHASE |

> WhatsApp is evaluated **before** the objective switch — it takes priority over all other destination rules.

### Call-to-Action Types

| Value | Button label |
|---|---|
| `LEARN_MORE` | Learn More (default) |
| `SIGN_UP` | Sign Up |
| `CONTACT_US` | Contact Us |
| `GET_QUOTE` | Get Quote |
| `APPLY_NOW` | Apply Now |

---

## Budget Rules

- Budget is **always set at the campaign level** in Meta (Campaign Budget Optimization / CBO). AdSets do not receive a budget.
- `budget_type` controls which field is sent to Meta:
  - `DAILY` → `daily_budget` value × 100 (Meta uses micro-currency, e.g. paise for INR)
  - `LIFETIME` → `total_budget` value × 100; campaign must have a fixed `end_date`
- Minimum daily budget for INR is approximately ₹40 (validated before save).
- For LIFETIME budgets, `total_budget` must be > 0.

---

## Bid Strategy

Set via the `bid_strategy` field. Applied at the **campaign level**.

| Value | Description |
|---|---|
| `LOWEST_COST_WITHOUT_CAP` | Meta spends the budget for the lowest possible cost (default) |
| `LOWEST_COST_WITH_BID_CAP` | Meta targets the lowest cost but won't exceed a bid cap you specify |
| `COST_CAP` | Meta tries to keep average cost per result at or below the cap |
| `LOWEST_COST_WITH_MIN_ROAS` | Meta optimises for maximum return on ad spend above a minimum threshold |

If `bid_strategy` is omitted, Meta applies `LOWEST_COST_WITHOUT_CAP` by default.

---

## Deployment Task

`MetaAdsCampaignDeploymentTask` processes all campaigns with status `Pending Deployment` in sequence.

### Steps per campaign

| Step | Action | Skipped if |
|---|---|---|
| 1 | Create Campaign in Meta (PAUSED) | `meta_campaign_id` ≠ `-1` |
| 2 | Create AdSet in Meta with targeting + audience (PAUSED) | `meta_adset_id` ≠ `-1` |
| 3a | **IMAGE (single)**: Upload image from URL → store hash | `image_hash` ≠ `-1` |
| 3b | **IMAGE (single)**: Create image AdCreative | `meta_creative_id` ≠ `-1` |
| 3a | **IMAGE (multi-ratio)**: Upload each image asset → store hash per asset | per-asset `image_hash` ≠ `-1` |
| 3b | **IMAGE (multi-ratio)**: Create `asset_feed_spec` AdCreative with all image hashes | `meta_creative_id` ≠ `-1` |
| 3a | **VIDEO**: Upload video from URL → store video_id | `video_id` ≠ `-1` |
| 3b | **VIDEO**: Poll `/{video_id}?fields=status` every 10 s until `video_status = ready` (max 10 min) | — (always runs after upload) |
| 3c | **VIDEO**: Upload thumbnail from URL → store thumbnail_hash | `thumbnail_hash` ≠ `-1` |
| 3d | **VIDEO**: Create video AdCreative (uses video_id + thumbnail_hash) | `meta_creative_id` ≠ `-1` |
| 3a | **CAROUSEL**: Upload image for each card → store hash per card | per-card `image_hash` ≠ `-1` |
| 3b | **CAROUSEL**: Create carousel AdCreative | `meta_creative_id` ≠ `-1` |
| — | **Any creative type**: if `degrees_of_freedom = true`, `degrees_of_freedom_spec` is added to the creative call | `degrees_of_freedom = false` |
| 4 | Create Ad in Meta linking AdSet ↔ AdCreative (PAUSED) | `meta_ad_id` ≠ `-1` |
| 5 | Mark campaign `Deployed` in DB | — |

The `-1` sentinel value on each ID column means "not yet deployed". Steps are idempotent — if a step was already completed in a previous run (e.g. after a partial failure), it is skipped.

### Retry behaviour

| Error type | Behaviour |
|---|---|
| Rate limit (code 17) | Wait 60 seconds, retry up to 3 times |
| Transient errors (codes 1, 2, 368) | Exponential backoff (1 s → 2 s → 4 s), retry up to 3 times |
| Video not ready (subcode 1885252) | Wait 30 seconds, retry up to 3 times (safety net — `waitForVideoReady` normally prevents this) |
| All other errors | Fail immediately; campaign marked `Error` |

If a campaign fails, the error message (up to 500 chars) is stored in `meta_campaign.error_comment` and the status is set to `Error`. The `coe_comment` (approval note) is preserved separately and is not overwritten. Fix the underlying data and re-approve the campaign to retry.

---

## Geo Location Sync Task

`MetaGeoLocationSyncTask` fetches city and region keys from the Meta Targeting Search API and stores them in the `meta_geo_location` table. This must be run before any campaign uses `CITY` or `REGION` targeting.

### XML Parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `client_id` | Yes | — | Caliper client ID whose Facebook access token is used for the API call |
| `location_type` | No | `both` | `city`, `region`, or `both` |
| `country_code` | No | `IN` | ISO country code — results from other countries are filtered out |
| `queries` | No | Built-in list | Comma-separated search terms. If omitted, a predefined list of major Indian cities / states is used |

### Example XML — sync all defaults (India)

```xml
<parameters>
  <client_id>1</client_id>
  <location_type>both</location_type>
  <country_code>IN</country_code>
</parameters>
```

### Example XML — sync specific cities only

```xml
<parameters>
  <client_id>1</client_id>
  <location_type>city</location_type>
  <country_code>IN</country_code>
  <queries>Pune,Nashik,Aurangabad,Kolhapur</queries>
</parameters>
```

### Default City List (built-in)

Mumbai, Delhi, Bangalore, Chennai, Hyderabad, Kolkata, Pune, Ahmedabad, Surat, Jaipur, Lucknow, Kanpur, Nagpur, Visakhapatnam, Bhopal, Patna, Vadodara, Ludhiana, Agra, Nashik, Faridabad, Meerut, Rajkot, Varanasi, Aurangabad, Dhanbad, Amritsar, Ranchi, Howrah, Coimbatore, Jabalpur, Gwalior, Vijayawada, Jodhpur, Madurai, Raipur, Kota, Chandigarh, Guwahati, Solapur, Hubli, Mysore, Tiruchirappalli, Bareilly, Aligarh, Moradabad, Thiruvananthapuram, Noida, Thane, Indore, Bhubaneswar, Srinagar, Mangalore, Kochi, Kozhikode, Guntur

### Default Region List (built-in)

Maharashtra, Delhi, Karnataka, Tamil Nadu, Telangana, West Bengal, Gujarat, Rajasthan, Uttar Pradesh, Bihar, Madhya Pradesh, Punjab, Haryana, Kerala, Andhra Pradesh, Jharkhand, Assam, Odisha, Chhattisgarh, Uttarakhand, Himachal Pradesh, Goa, Jammu and Kashmir, Tripura, Meghalaya

### Behaviour

- Results are **upserted** — existing entries (matched by `meta_key` + `location_type`) are skipped to avoid duplicates.
- A 200 ms pause is inserted between Meta API calls to respect rate limits.
- The task logs how many entries were inserted vs skipped.
- If a search query returns results from multiple countries, only entries matching the configured `country_code` are stored.

### Scheduling recommendation

Run this task once before go-live, then weekly or monthly to pick up new cities added by Meta.

---

## Insights

Insights are fetched live from the Meta API on each request and are not stored in the database.

### Metrics returned

| Field | Description |
|---|---|
| `impressions` | Total number of times the ad was shown |
| `reach` | Number of unique accounts that saw the ad |
| `clicks` | Total link clicks on the ad |
| `spend` | Amount spent in the account currency |
| `leads` | Total lead form submissions (Lead Gen campaigns only) |

### Not included

CTR (Click-Through Rate), CPM (Cost Per Mille), and CPC (Cost Per Click) are intentionally excluded from the response.

### Error cases

- Campaign not yet deployed → HTTP 400: `"Campaign has not been deployed yet"`
- No `facebook_account` record → HTTP 404: `"Facebook account not found for client"`
- Meta API error → HTTP 400 with the Meta error message forwarded
