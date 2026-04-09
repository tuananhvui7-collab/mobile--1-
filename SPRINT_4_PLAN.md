# Sprint 4 Plan

Sprint 4 is the Metabase integration sprint.

## Goal

Embed the warehouse-based reporting experience back into the web app so the project feels like one complete system, not two separate tools.

## Scope

- Keep the web app as the main entry point.
- Expose Metabase from the app through an internal report page.
- Use an `iframe` to embed the dashboard.
- Keep access limited by role.
- Make the report page responsive and consistent with the rest of the app.

## Expected Work

1. Add or finalize the report route in the web app.
2. Point that route to the Metabase dashboard URL.
3. Wrap the embedded dashboard in a clean layout.
4. Add fallback UI when the BI service is unavailable.
5. Document how to run and refresh the BI dashboard.

## Acceptance Criteria

- Manager can open the report page from the web app.
- The Metabase dashboard renders inside the app using `iframe`.
- The page works on desktop and mobile.
- Access is role-based.
- The page fails gracefully if the BI service is down.

## Current Status

- Web app integration point is ready at `/admin/reports`.
- The page already supports an embed URL through `bi.dashboard-url`.
- The app falls back gracefully when the URL is empty.
- Sprint 4 is functionally ready on the web side.
- The remaining step is to provide a real Metabase embed URL and build the dashboard there.

## Notes

- Sprint 4 does not redesign the BI layer itself.
- The warehouse, ETL, and dashboard should already exist before this sprint starts.
- The main task is integration, not rebuilding reporting logic.

## Sprint Code Flow Map

- Sprint 1:
  - web app core flow
  - auth
  - browse/search products
  - cart
  - checkout
  - order processing
  - staff and manager routing
- Sprint 2:
  - data foundation
  - mock data
  - ETL
  - DWH schema
  - Power BI measures and dashboard setup
  - Metabase dashboard setup
- Sprint 3:
  - frontend polish
  - responsive UI
  - template cleanup
  - backend stability for main web flows
- Sprint 4:
  - Metabase embedding
  - iframe integration
  - report route wiring
  - graceful fallback when BI is unavailable

## Backend Risk Checklist

See [BACKEND_RISK_CHECKLIST.md](BACKEND_RISK_CHECKLIST.md) for the remaining backend validation items.
