# Razorpay Autopay (Subscriptions) — Integration Guide

## Overview

This module adds automated recurring billing to Caliper using **Razorpay Subscriptions (Autopay)**. Once a client registers a payment mandate (UPI / NACH / card), Razorpay automatically charges them on each billing cycle. Your backend receives webhook events to update plan status and record transactions — no manual renewal needed.

---

## How It Works

```
Admin creates subscription
        │
        ▼
Razorpay returns auth_link ──► Email sent to client
        │
        ▼
Client opens auth_link and registers mandate (UPI / Card / NACH)
        │
        ▼
Razorpay activates subscription ──► Webhook: subscription.activated
        │                                     Plan status → ACTIVE
        ▼
Every billing cycle:
Razorpay auto-debits ──► Webhook: subscription.charged
                                  Payment recorded in caliper_payment
                                  Plan status stays ACTIVE
        │
        ▼ (if payment fails repeatedly)
Razorpay halts subscription ──► Webhook: subscription.halted
                                         Plan status → INACTIVE
                                         Alert email sent to client
```

---

## Project Structure

```
src/main/java/com/caliper/razorpay/
├── config/
│   └── RazorpayConfig.java              # Spring Bean: initializes RazorpayClient
├── controller/
│   ├── RazorpaySubscriptionController.java  # REST APIs (create / get / cancel)
│   └── RazorpayWebhookController.java       # POST /razorpay/webhook (public)
├── dto/
│   ├── CreateSubscriptionRequest.java
│   ├── SubscriptionResponse.java
│   └── CancelSubscriptionRequest.java
├── entity/
│   ├── RazorpaySubscription.java        # DB table: razorpay_subscription
│   └── RazorpayPlanMapping.java         # DB table: razorpay_plan_mapping
├── repository/
│   ├── RazorpaySubscriptionRepository.java
│   └── RazorpayPlanMappingRepository.java
└── service/
    ├── RazorpaySubscriptionService.java  # Core business logic
    └── RazorpayWebhookService.java       # Signature verification + event handlers
```

---

## Database Tables

Two new tables are auto-created by Hibernate on startup.

### `razorpay_subscription`

Stores one row per client representing their current subscription state.

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Internal ID |
| `client_id` | VARCHAR | Links to `client.client_id` |
| `plan_id` | BIGINT | Links to `plan.id` |
| `razorpay_subscription_id` | VARCHAR | Razorpay's `sub_XXXX` ID |
| `razorpay_plan_id` | VARCHAR | Razorpay's `plan_XXXX` ID |
| `status` | VARCHAR | `created` / `authenticated` / `active` / `halted` / `cancelled` / `expired` |
| `auth_link` | TEXT | Mandate authorization URL sent to client |
| `total_count` | INT | Total billing cycles planned |
| `paid_count` | INT | Successfully charged cycles so far |
| `current_start` | DATETIME | Start of current billing period |
| `current_end` | DATETIME | End of current billing period |
| `next_charge_at` | DATETIME | Next scheduled debit date |
| `billing_interval` | VARCHAR | `monthly` or `yearly` |
| `amount` | DOUBLE | Amount per cycle in INR |
| `created_at` | DATETIME | Row creation time |
| `updated_at` | DATETIME | Last update time |

### `razorpay_plan_mapping`

Maps your Caliper service tiers to Razorpay Plan IDs. **Must be seeded manually** before creating subscriptions.

| Column | Type | Description |
|---|---|---|
| `id` | BIGINT PK | Internal ID |
| `caliper_service_id` | BIGINT | FK to `plan_service_master.id` |
| `billing_interval` | VARCHAR | `monthly` or `yearly` |
| `razorpay_plan_id` | VARCHAR | Razorpay Plan ID (e.g., `plan_ABC123`) |
| `amount_paise` | LONG | Price in paise (₹499 = 49900) |
| `currency` | VARCHAR | `INR` |
| `created_at` | DATETIME | |

---

## Configuration

### Environment Variables

Set these on your server (never hard-code API keys):

```bash
RAZORPAY_KEY_ID=rzp_live_xxxxxxxxxxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxxxxxxxxxx
RAZORPAY_WEBHOOK_SECRET=xxxxxxxxxxxxxxxxxxxxxxxx
```

For local development, override in `application-local.properties`:

```properties
razorpay.key.id=rzp_test_xxxxxxxxxxxxxxxx
razorpay.key.secret=xxxxxxxxxxxxxxxxxxxxxxxx
razorpay.webhook.secret=xxxxxxxxxxxxxxxxxxxxxxxx
```

### `application.properties` keys added

```properties
razorpay.key.id=${RAZORPAY_KEY_ID:rzp_test_placeholder}
razorpay.key.secret=${RAZORPAY_KEY_SECRET:placeholder_secret}
razorpay.webhook.secret=${RAZORPAY_WEBHOOK_SECRET:placeholder_webhook_secret}
```

---

## API Reference

### 1. Create Subscription

**`POST /razorpay/subscription/create`**

Creates a Razorpay subscription and emails the auth link to the client.

**Request Body:**
```json
{
  "client_id": "Acme_Corp_123456",
  "caliper_service_id": 1,
  "billing_interval": "monthly",
  "total_count": 12
}
```

| Field | Required | Description |
|---|---|---|
| `client_id` | Yes | Caliper client ID |
| `caliper_service_id` | Yes | ID from `plan_service_master` table |
| `billing_interval` | Yes | `monthly` or `yearly` |
| `total_count` | No | Billing cycles (default: 12 monthly / 3 yearly) |

**Response `201 Created`:**
```json
{
  "status": 201,
  "message": "Subscription created. Auth link sent to client email.",
  "client_id": "Acme_Corp_123456",
  "razorpay_subscription_id": "sub_XXXXXXXXXXXXXXX",
  "auth_link": "https://rzp.io/i/XXXXXX",
  "subscription_status": "created",
  "billing_interval": "monthly",
  "amount": 4990.00,
  "paid_count": 0,
  "total_count": 12,
  "timestamp": "2026-06-24T10:00:00.000+00:00"
}
```

---

### 2. Get Subscription Status

**`GET /razorpay/subscription/{clientId}`**

Returns the current subscription state, synced live from Razorpay for active subscriptions.

**Response `200 OK`:**
```json
{
  "status": 200,
  "client_id": "Acme_Corp_123456",
  "razorpay_subscription_id": "sub_XXXXXXXXXXXXXXX",
  "subscription_status": "active",
  "billing_interval": "monthly",
  "amount": 4990.00,
  "paid_count": 3,
  "total_count": 12,
  "next_charge_at": "2026-07-24T00:00:00.000+00:00"
}
```

---

### 3. Cancel Subscription

**`POST /razorpay/subscription/cancel`**

Cancels the client's subscription immediately or at the end of the current billing cycle.

**Request Body:**
```json
{
  "client_id": "Acme_Corp_123456",
  "cancel_at_cycle_end": true
}
```

| Field | Description |
|---|---|
| `cancel_at_cycle_end` | `true` = cancel gracefully at end of cycle; `false` = cancel immediately |

**Response `200 OK`:**
```json
{
  "status": 200,
  "message": "Subscription cancelled successfully",
  "client_id": "Acme_Corp_123456",
  "subscription_status": "cancelled"
}
```

---

### 4. Webhook Endpoint

**`POST /razorpay/webhook`**

Receives all Razorpay subscription and payment events. **No JWT required** — secured via HMAC-SHA256 signature verification using `X-Razorpay-Signature` header.

> This endpoint must be registered in the Razorpay Dashboard.

---

## Webhook Events Handled

| Event | Action |
|---|---|
| `subscription.activated` | Sets `razorpay_subscription.status = active`; sets `plan.status = ACTIVE` |
| `subscription.charged` | Records row in `caliper_payment` (success); updates `paid_count`, `next_charge_at` |
| `subscription.halted` | Sets status = `halted`; sets `plan.status = INACTIVE`; sends alert email to client |
| `subscription.cancelled` | Sets status = `cancelled`; sets `plan.status = INACTIVE` |
| `payment.failed` | Records row in `caliper_payment` (failed); sends failure notification email |

---

## Razorpay Dashboard Setup (One-time)

### Step 1 — Get API Keys
1. Log in to [Razorpay Dashboard](https://dashboard.razorpay.com)
2. Go to **Settings → API Keys**
3. Generate keys for **Test** (development) and **Live** (production)

### Step 2 — Create Plans in Razorpay
For each service tier in your `plan_service_master` table, create a corresponding plan in Razorpay:

1. Go to **Subscriptions → Plans → Create Plan**
2. Set the amount (in paise), interval (`monthly` or `yearly`), and currency (`INR`)
3. Copy the generated Plan ID (e.g., `plan_ABC123XYZ`)

### Step 3 — Seed `razorpay_plan_mapping` Table

Insert a row for each service + interval combination:

```sql
INSERT INTO razorpay_plan_mapping (caliper_service_id, billing_interval, razorpay_plan_id, amount_paise, currency, created_at)
VALUES
  (1, 'monthly', 'plan_ABC123monthly', 49900, 'INR', NOW()),
  (1, 'yearly',  'plan_ABC123yearly',  499000, 'INR', NOW()),
  (2, 'monthly', 'plan_XYZ456monthly', 99900, 'INR', NOW());
```

### Step 4 — Register Webhook URL
1. Go to **Settings → Webhooks → Add New Webhook**
2. Enter URL: `https://your-domain.com/razorpay/webhook`
3. Enter the webhook secret (same value as `RAZORPAY_WEBHOOK_SECRET`)
4. Enable these events:
   - `subscription.activated`
   - `subscription.charged`
   - `subscription.halted`
   - `subscription.cancelled`
   - `payment.failed`
5. Save and note the webhook secret shown

---

## Testing with Razorpay Test Mode

1. Use test API keys (`rzp_test_...`) in `application-local.properties`
2. Create a subscription via `POST /razorpay/subscription/create`
3. Open the `auth_link` and complete the UPI/card test mandate (use Razorpay's test credentials)
4. Verify `razorpay_subscription.status` changes to `active` and `plan.status = ACTIVE`
5. Use the **Webhook Tester** in Razorpay Dashboard to simulate events:
   - Simulate `subscription.charged` → check a new row in `caliper_payment`
   - Simulate `subscription.halted` → check email is sent and `plan.status = INACTIVE`

**Razorpay test card:** `4111 1111 1111 1111` | Expiry: any future date | CVV: any 3 digits

---

## Subscription Status Flow

```
created ──► authenticated ──► active ──► cancelled
                                │
                                ▼
                             halted ──► (reactivate via dashboard) ──► active
                                │
                                ▼
                            expired
```

---

## Error Reference

| HTTP Status | Meaning |
|---|---|
| `400 Bad Request` | Missing required fields in request |
| `401 Unauthorized` | Webhook signature verification failed |
| `404 Not Found` | Client not found, or no subscription exists for client |
| `409 Conflict` | Active subscription already exists for this client |
| `502 Bad Gateway` | Razorpay API returned an error — check Razorpay Dashboard logs |
| `500 Internal Server Error` | Unexpected server error |

---

## Dependencies Added

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.razorpay</groupId>
    <artifactId>razorpay-java</artifactId>
    <version>1.4.7</version>
</dependency>
```

---

## Security Notes

- API keys are loaded from environment variables — never committed to git
- Webhook endpoint (`/razorpay/webhook`) has no JWT auth — it uses HMAC-SHA256 signature verification instead
- All webhook payloads are verified against `X-Razorpay-Signature` before processing
- Failed signature verification returns `401 Unauthorized` and the payload is ignored
