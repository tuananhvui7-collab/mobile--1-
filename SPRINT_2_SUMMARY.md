# Sprint 2 Summary

Sprint 2 was used to turn the web app from “functional but rough” into a more coherent localhost storefront.

## Completed

- Shared responsive Thymeleaf layout
- Customer pages polished
- Staff pages unified
- Manager dashboard unified
- Manager employee management added
- VNPAY local mock flow added so payment does not get stuck on localhost
- GHN tracking flow kept in place for shipment testing

## What feels significantly better now

- The app has a consistent look across public, customer, employee, and manager pages
- The main customer journey is easier to follow
- The manager side now has more real business control
- Local payment testing is no longer blocked by external callback limitations

## What still feels old and should be improved next

- Product detail still looks more like a working admin page than a modern storefront PDP
- Cart and checkout can be made more commercial and guided
- Order tracking can become a proper timeline instead of a plain detail page
- Manager pages can be upgraded into a more polished admin console
- Some forms still feel utilitarian instead of e-commerce native

## Recommended next upgrades

1. Product detail polish
- Replace plain variant tables with a better product gallery + sticky buy box
- Show stock, price changes, and call-to-action states more clearly

2. Cart and checkout polish
- Add a stronger order summary panel
- Show shipping, discount, and total in a clearer checkout step

3. Order tracking polish
- Turn shipment tracking into a visual step timeline
- Highlight current state, last sync time, and carrier updates better

4. Manager console polish
- Make manager pages feel like a proper admin system
- Add better list filtering, action grouping, and status badges

5. Final visual cleanup
- Remove the last few “table-first” pages that still feel too plain
- Keep the same responsive design language everywhere

## Notes

- BI/DWH is still available, but the web app has been stabilized first.
- The project now has a better base for later analytics work, because the operational app is less brittle.
