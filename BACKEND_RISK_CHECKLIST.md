# Backend Risk Checklist

This checklist tracks the backend OLTP risks that were still worth validating after the main polish pass.

## Fixed

- VNPay callback no longer reports success on failed transactions.
- Payment confirmation is blocked for cancelled orders.
- GHN failures no longer break the shipment page or the staff flow.
- Shipment refresh now falls back safely when GHN is unavailable or not configured.

## VNPAY Modes

- `vnpay.mock-mode=true`
  - local mock gateway for localhost development
  - useful when you do not have a public callback or sandbox credentials
- `vnpay.mock-mode=false`
  - real VNPAY sandbox flow
  - requires valid `vnpay.tmn-code`, `vnpay.hash-secret`, `vnpay.return-url`
  - the app redirects to the external VNPAY URL and expects the callback to return to `/payments/vnpay/return`

## Needs Test

- `POST /orders/place` with COD
- `POST /orders/place` with VNPay mock success
- `POST /orders/place` with VNPay mock failure
- `GET /payments/{id}/vnpay/return` on success
- `GET /payments/{id}/vnpay/return` on failure
- `POST /payments/{id}/confirm` on a cancelled order
- `GET /orders/{id}/tracking`
- `POST /orders/{id}/tracking/refresh`
- `GET /employee/orders/{id}`
- `POST /employee/orders/{id}/status`
- `POST /employee/orders/{id}/receive`
- `GET /admin/orders/{id}`
- `POST /admin/orders/{id}/status`

## Still Risky

- Real GHN sandbox response formats may differ from the local assumptions.
- Real VNPay sandbox responses may vary if the sandbox settings or return URL are changed.
- Very old or malformed orders may expose edge cases in report and tracking pages.
- External service outages should still be treated as normal failure cases, not app failures.

## Validation Rule

If a failure comes from an external service, the app should:

1. keep the internal order state consistent
2. show a clear message to the user
3. avoid crashing the page
