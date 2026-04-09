# Metabase Setup

This guide is the recommended BI path for Sprint 4.

## Why Metabase

- Easy to self-host locally.
- Easy to embed in the web app with `iframe`.
- Better fit for the "web app + BI page" flow in this project.
- No dependency on Power BI trial or Power BI Service licensing.

## Quick Start

### Option 1: Docker

If you have Docker installed:

```powershell
docker pull metabase/metabase:latest
docker run -d -p 3000:3000 --name metabase metabase/metabase
```

Then open:

```text
http://localhost:3000
```

### Option 2: JAR

If you prefer Java:

1. Download the Metabase OSS JAR from the official site.
2. Put it in a folder.
3. Run it with Java:

```powershell
java -jar metabase.jar
```

## Connect to the Warehouse

In Metabase:

1. Add a new database.
2. Choose MySQL.
3. Point it to `phoneshop_dw`.
4. Save the connection.

## Build the Dashboard

1. Create questions from `phoneshop_dw`.
2. Add cards to a dashboard.
3. Add filters if needed.
4. Test the dashboard in desktop and mobile layouts.

## Embed in the Web App

Use the embed URL from Metabase and place it in:

```properties
bi.dashboard-url=YOUR_METABASE_EMBED_URL
```

The web app already renders that URL inside the `/admin/reports` page with an `iframe`.

## Recommended Sprint 4 Flow

1. Run Metabase locally.
2. Connect it to `phoneshop_dw`.
3. Build the dashboard.
4. Copy the embed URL.
5. Paste it into `application.properties`.
6. Open `/admin/reports` in the web app.

