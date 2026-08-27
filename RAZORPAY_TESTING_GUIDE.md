# Razorpay Complete Flow — Endpoint Testing Guide

## Overview

There are **13 endpoints** covering the full Razorpay flow across two paths:

| Flow | Description |
|---|---|
| **Flow A — One-Time Payment** | Price preview → Create order → Verify payment → Plan activated |
| **Flow B — Autopay Subscription** | Create subscription → Client registers mandate → Auto-charged each cycle |

**Base URL (local):** `http://localhost:1010`  
**Swagger UI:** `http://localhost:1010/swagger-ui/index.html`

**Test Credentials (already in application.properties):**
```
Key ID     : rzp_test_FrS0vPNhqINoNZ
Key Secret : 1knPuqRo5maEhFWgxv7FdVDb
```
**Test Card:** `4111 1111 1111 1111` | Expiry: any future date | CVV: any 3 digits  
**Test UPI:** `success@razorpay` (success) · `failure@razorpay` (failure)

---

## FLOW A — One-Time Payment (Manual Plan Purchase)

### Step 1 · Preview pricing with GST

**`POST /plan/price-preview`**

Request:
```json
{
  "serviceKeys": ["GMB_MANAGEMENT"],
  "durationType": "MONTHLY",
  "locationCount": 5
}
```

`durationType` values: `MONTHLY` · `HALF_YEARLY` · `ANNUAL`

Response:
```json
{
  "serviceBreakdown": [
    {
      "serviceName": "GMB Management",
      "serviceKey": "GMB_MANAGEMENT",
      "pricePerLocation": 200.0,
      "subtotal": 1000.0
    }
  ],
  "locationCount": 5,
  "durationType": "MONTHLY",
  "totalAmountRupees": 1000.0,
  "totalAmountPaise": 100000,
  "gstBreakdown": {
    "baseAmountPaise": 100000,
    "cgstPaise": 9000,
    "sgstPaise": 9000,
    "totalAmountPaise": 118000,
    "cgstRate": 9.0,
    "sgstRate": 9.0
  }
}
```

---

### Step 2 · Create Razorpay order

**`POST /plan/payment/create-order`**

Request:
```json
{
  "clientId": "Acme_Corp_123456",
  "userId": "user@email.com",
  "serviceKeys": ["GMB_MANAGEMENT"],
  "durationType": "MONTHLY",
  "locationCount": 5
}
```

Response:
```json
{
  "razorpayOrderId": "order_XXXXXXXXXXXXXXXX",
  "amountPaise": 118000,
  "currency": "INR",
  "keyId": "rzp_test_FrS0vPNhqINoNZ",
  "clientId": "Acme_Corp_123456",
  "gstBreakdown": { ... }
}
```

> Pass `razorpayOrderId` and `keyId` to the Razorpay Checkout JS on the frontend to open the payment modal.

---

### Step 3 · Verify payment and activate plan

After the customer completes checkout, Razorpay returns `razorpay_payment_id`, `razorpay_order_id`, and `razorpay_signature` to your frontend. Pass these to:

**`POST /plan/payment/verify`**

Request:
```json
{
  "razorpayPaymentId": "pay_XXXXXXXXXXXXXXXX",
  "razorpayOrderId":   "order_XXXXXXXXXXXXXXXX",
  "razorpaySignature": "<hmac-sha256-from-razorpay>",
  "clientId": "Acme_Corp_123456"
}
```

Response:
```json
{
  "success": true,
  "planId": 42,
  "message": "Plan activated successfully",
  "clientId": "Acme_Corp_123456",
  "onboarding_step": "SOCIAL_ACCOUNT_SETUP"
}
```

> On success: plan is created with `STATUS=ACTIVE`, payment is recorded in `caliper_payment` with GST amounts, and the onboarding state machine is triggered.

`onboarding_step` values: `SOCIAL_ACCOUNT_SETUP` · `CAMPAIGN_SETUP` · `COMPLETED`

---

### Step 4 · Confirm plan is active

**`GET /plan/subscription/{clientId}`**

```
GET /plan/subscription/Acme_Corp_123456
```

Response:
```json
{
  "planName": "Monthly Plan",
  "renewalDate": "2026-07-25",
  "amount": 1180.00,
  "locationCount": 5,
  "purchasedModules": ["GMB_MANAGEMENT"],
  "expiresOn": "2026-07-25",
  "status": "ACTIVE",
  "durationType": "MONTHLY"
}
```

---

### Step 5 · View payment history

**`GET /plan/history/{clientId}?page=0&size=10`**

```
GET /plan/history/Acme_Corp_123456?page=0&size=10
```

Response contains paginated list of all past payments with amount, status, and billing date.

---

## FLOW B — Autopay Subscription (Recurring Billing)

> **Pre-requisite:** The `razorpay_plan_mapping` table must be seeded with Razorpay Plan IDs matching your `plan_service_master` pricing. See the Razorpay Autopay README for the seed SQL.

---

### Step 1 · Find the caliper_service_id

**`GET /plan/allplans`**

Returns the full list of `PlanServiceMaster`. Note the `id` field of the service you want — this is the `caliper_service_id` for the next step.

---

### Step 2 · Create Autopay subscription

**`POST /razorpay/subscription/create`**

Request:
```json
{
  "client_id": "Acme_Corp_123456",
  "caliper_service_id": 1,
  "billing_interval": "monthly",
  "total_count": 12
}
```

| Field | Required | Notes |
|---|---|---|
| `client_id` | Yes | Must exist in `client` table |
| `caliper_service_id` | Yes | ID from `plan_service_master` |
| `billing_interval` | Yes | `monthly` or `yearly` |
| `total_count` | No | Defaults: 12 (monthly), 3 (yearly) |

Response:
```json
{
  "status": 201,
  "message": "Subscription created. Auth link sent to client email.",
  "client_id": "Acme_Corp_123456",
  "razorpay_subscription_id": "sub_XXXXXXXXXXXXXXXX",
  "auth_link": "https://rzp.io/i/XXXXXX",
  "subscription_status": "created",
  "billing_interval": "monthly",
  "base_amount_rupees": 999.00,
  "cgst_amount_rupees": 89.91,
  "sgst_amount_rupees": 89.91,
  "total_amount_rupees": 1178.82,
  "cgst_rate": 9.0,
  "sgst_rate": 9.0,
  "paid_count": 0,
  "total_count": 12,
  "timestamp": "2026-06-25T10:00:00.000+00:00"
}
```

> The `auth_link` is automatically emailed to the client. The client opens it and registers their UPI / Card / NACH mandate.

---

### Step 3 · Client completes mandate (external step)

The client opens the `auth_link` and authenticates. Razorpay fires a `subscription.activated` webhook to your server → plan status becomes `ACTIVE`.

To test this manually: use the **Razorpay Dashboard → Webhooks → Test** to fire a `subscription.activated` event.

---

### Step 4 · Check subscription status

**`GET /razorpay/subscription/{clientId}`**

```
GET /razorpay/subscription/Acme_Corp_123456
```

Response is live-synced from Razorpay and includes `next_charge_at`, `paid_count`, and the full GST breakdown.

---

### Step 5 · Cancel subscription (if needed)

**`POST /razorpay/subscription/cancel`**

Request:
```json
{
  "client_id": "Acme_Corp_123456",
  "cancel_at_cycle_end": true
}
```

| `cancel_at_cycle_end` | Effect |
|---|---|
| `true` | Client keeps access until end of current billing period |
| `false` | Cancels immediately |

---

## WEBHOOK ENDPOINTS

> These are called by Razorpay, not by your frontend. Register them in the Razorpay Dashboard.

### One-Time Payment Webhook

**`POST /webhooks/razorpay`**

| Item | Value |
|---|---|
| Register URL | `https://your-domain.com/webhooks/razorpay` |
| Header | `X-Razorpay-Signature: <hmac-sha256>` |
| Auth | HMAC-SHA256 signature verification (no JWT) |

Events handled:

| Event | Action |
|---|---|
| `payment.captured` | Activates plan, records `caliper_payment` with GST |
| `payment.failed` | Marks order as `FAILED` |

---

### Subscription Lifecycle Webhook

**`POST /razorpay/webhook`**

| Item | Value |
|---|---|
| Register URL | `https://your-domain.com/razorpay/webhook` |
| Header | `X-Razorpay-Signature: <hmac-sha256>` |
| Auth | HMAC-SHA256 signature verification (no JWT) |

Events handled:

| Event | Action |
|---|---|
| `subscription.activated` | Plan → `ACTIVE` |
| `subscription.charged` | Records payment in `caliper_payment` with `cgst_amount`, `sgst_amount`, `webhook_event_id` |
| `subscription.halted` | Plan → `INACTIVE` + alert email to client |
| `subscription.cancelled` | Plan → `INACTIVE` |
| `payment.failed` (subscription-linked) | Records failed payment + failure email to client |

> Both webhook endpoints return `200 OK` even on errors (to prevent Razorpay retry flooding). Check server logs for errors.

---

## All Endpoints — Quick Reference

| # | Method | Endpoint | Purpose |
|---|--------|----------|---------|
| 1 | GET | `/plan/allplans` | List all service plans |
| 2 | POST | `/plan/price-preview` | Preview price with GST breakdown |
| 3 | POST | `/plan/payment/create-order` | Create Razorpay order (one-time) |
| 4 | POST | `/plan/payment/verify` | Verify payment + activate plan |
| 5 | POST | `/plan/createplan` | Legacy: create plan manually |
| 6 | POST | `/plan/createpayment` | Legacy: record payment manually |
| 7 | GET | `/plan/subscription/{clientId}` | Get active plan/subscription details |
| 8 | GET | `/plan/history/{clientId}` | Paginated payment history |
| 9 | POST | `/razorpay/subscription/create` | Create Autopay subscription |
| 10 | GET | `/razorpay/subscription/{clientId}` | Get Autopay subscription status |
| 11 | POST | `/razorpay/subscription/cancel` | Cancel Autopay subscription |
| 12 | POST | `/webhooks/razorpay` | Webhook: one-time payment events |
| 13 | POST | `/razorpay/webhook` | Webhook: subscription lifecycle events |

---

## Razorpay Dashboard Checklist

Before going live, complete these steps in the Razorpay Dashboard:

- [ ] Generate Live API keys under **Settings → API Keys**
- [ ] Create Plans matching your `plan_service_master` pricing under **Subscriptions → Plans**
- [ ] Seed `razorpay_plan_mapping` table with the generated Plan IDs
- [ ] Register webhook URL `/webhooks/razorpay` — enable `payment.captured`, `payment.failed`
- [ ] Register webhook URL `/razorpay/webhook` — enable `subscription.activated`, `subscription.charged`, `subscription.halted`, `subscription.cancelled`, `payment.failed`
- [ ] Copy webhook secret to `RAZORPAY_WEBHOOK_SECRET` environment variable
- [ ] Set `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` environment variables to Live keys
