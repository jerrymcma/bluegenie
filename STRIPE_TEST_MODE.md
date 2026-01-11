# Stripe Test Mode - Testing Without Real Payments

## 1. Enable Test Mode

In `sparkifire-web/.env`, add:
```
VITE_STRIPE_MODE=test
```

Then restart your web server.

## 2. Use Stripe Test Cards

When checking out, use these test card numbers:

- **Success**: `4242 4242 4242 4242`
- **Decline**: `4000 0000 0000 0002`
- **Requires authentication**: `4000 0025 0000 3155`

**Any future expiry date, any CVC, any ZIP code**

Example:
- Card: `4242 4242 4242 4242`
- Expiry: `12/34`
- CVC: `123`
- ZIP: `12345`

## 3. Test the Full Flow

1. Click "Upgrade to Premium"
2. Enter test card `4242 4242 4242 4242`
3. Use expiry `12/34`, CVC `123`, ZIP `12345`
4. Complete checkout (no real charge)
5. Premium should activate via webhook

## 4. Verify No Real Charges

- Test mode transactions appear in Stripe Dashboard under "Test Mode"
- They're completely separate from live transactions
- Zero risk of real charges

---

## Switch Back to Live Mode

When ready for real payments:

1. In `sparkifire-web/.env`, change:
   ```
   VITE_STRIPE_MODE=live
   ```

2. Restart your web server

Done!
