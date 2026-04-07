# Learning Path

This project is big, so the goal is not to learn everything at once.
The goal is to learn in layers until you can work without help on the next small piece.

## Honest answer

You will not become fully independent by reading once.
You will become independent by repeating the same flow a few times:

1. read the code
2. run the code
3. break the code
4. fix the code
5. explain the code back in your own words

That loop is what turns confusion into skill.

## What to learn first

### Layer 1: Run the system

Learn how to:
- start the Spring Boot app
- log in
- browse products
- add to cart
- place an order
- run the ETL
- open the DWH in Power BI

If you can run the flow end to end, you already understand the project better than most beginners.

### Layer 2: Read the web app by flow

Do not try to memorize every file.
Learn the flow in this order:

1. `SecurityConfig`
2. `AuthController`
3. `ProductController`
4. `CartController`
5. `OrderController`
6. `PaymentController`
7. `ShipmentController`
8. `FeedbackController`
9. `ReportController`

For each controller, ask:
- what URL does it handle
- which service does it call
- which entity does it touch
- which template does it render

`ReportController` is special: in this project, it is not just a small report page.
It is the web-side entry point for `U14`, which connects the app to the BI layer.
So when you study it, think:
- what business KPI is being exposed
- which warehouse tables are needed
- which dashboard/page should consume it

### Layer 3: Understand the data

Learn the core tables in the OLTP database:
- `users`
- `roles`
- `products`
- `product_variants`
- `carts`
- `cart_items`
- `orders`
- `order_items`
- `payments`
- `shipments`
- `reviews`
- `feedback`

Ask yourself:
- what is one row in this table
- who owns the row
- what foreign key connects it
- what business event creates it

### Layer 4: Understand BI

Learn the BI stack in this order:

1. OLTP source data
2. ETL
3. warehouse tables
4. DAX measures
5. visuals

If you understand those five steps, Power BI stops feeling magical.

In this project, `U14: Xem báo cáo kinh doanh` is basically the BI use case.
So `ReportController` and the warehouse are two parts of the same story:
- the controller exposes reporting from the web app
- the warehouse powers the real metrics and visuals

## A practical weekly rhythm

### Week 1
- open the app
- log in as customer
- place one order
- cancel one order
- open one review

### Week 2
- log in as employee
- move an order through the workflow
- open feedback and resolve it

### Week 3
- run `mockdata.py`
- run `etl_to_dwh.py`
- inspect `phoneshop_dw`

### Week 4
- open Power BI
- build 3 cards
- build 1 line chart
- build 1 table

That is enough to move from passive reading to active understanding.

## How to become independent

You do not need to stop asking for help.
You just need to shrink the size of the help.

At first, ask:
- “what is broken”
- “which file is responsible”
- “what is the next step”

Later, ask:
- “does this DAX overcount”
- “is this relationship one-to-many”
- “should this be a fact or dimension”

That shift means you are learning the right kind of thinking.

## What you should be able to do by yourself soon

- run the web app
- seed the database
- explain the customer flow
- explain the staff flow
- explain the ETL flow
- explain the Power BI model
- create a simple KPI dashboard

If you can do those, you are no longer “mù tịt”.
You are just still early.

## Sprint 2 note

Sprint 2 can be about:
- UI cleanup
- template improvements
- more test cases
- code refactoring
- architecture cleanup

In the current state of the project, Sprint 2 has already been used to polish the web app:
- shared responsive fragments
- customer flow cleanup
- staff flow cleanup
- manager employee management
- local payment/shipping test paths

The next step after that is to keep improving the screens that still feel old:
- product detail
- cart and checkout
- order tracking
- manager admin console

That is normal.
Sprint 1 was about getting the system working.
Sprint 2 is about making it easier to use, easier to test, and easier to explain.

## Final reminder

You do not need to become the person who knows everything.
You need to become the person who can follow the flow, find the next bug, and fix one piece at a time.

That is enough.
