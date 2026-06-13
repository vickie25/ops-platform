# Cymelle Ops Dashboard

This is the React frontend for the Cymelle Ops Platform. It provides an operations dashboard for inventory monitoring, order history review, and fare calculation. The UI is built with React and Vite, and it connects directly to the Spring Boot backend through HTTP APIs.

## What the dashboard does

The application is organized as a focused ops console with three main areas. The inventory view shows live stock levels and highlights low-stock items. The order history view supports filtering by order status and date range. The fare engine view lets you calculate trip fares using the backend fare rules and also shows the current pricing configuration.

The frontend is designed to be readable, responsive, and resilient. It includes loading states, empty states, and error handling so the dashboard stays usable even when data is missing or an API request fails.

## Tech Stack

- React 19
- Vite
- JavaScript
- `date-fns` for date formatting
- `lucide-react` for icons
- Tailwind CSS v4

## Local Setup

### Prerequisites

- Node.js 18 or newer
- npm
- The Cymelle Ops backend running locally on `http://localhost:8081`

### Install dependencies

From the frontend directory:

```powershell
npm install
```

### Start the app

```powershell
npm run dev
```

Vite will print the local URL in the terminal, usually:

```text
http://localhost:5173
```

## Backend connection

The frontend talks to the backend through the API base URL configured in [`src/api.js`](src/api.js):

```text
http://localhost:8081/api
```

That means the frontend expects the backend to expose endpoints such as:

- `GET /api/inventory`
- `GET /api/inventory/low-stock`
- `GET /api/orders`
- `GET /api/orders/filter/status`
- `GET /api/orders/filter/date-range`
- `GET /api/fare/config`
- `GET /api/fare/calculate`

If you change the backend port, update `BASE_URL` in `src/api.js` to match.

## Features

### Inventory view

The inventory section loads live inventory data from the backend and presents it in a table with search, low-stock highlighting, and empty-state handling. It is intended to help operations teams quickly identify items that need restocking.

### Order history view

The order history section supports status filtering and date-range filtering. It is built to help teams inspect operational trends, review recent transactions, and narrow the list of orders without leaving the dashboard.

### Fare engine view

The fare engine section uses the live backend fare configuration and fare calculation endpoint. It demonstrates the pricing rules in a simple UI and gives a quick way to validate fare behavior from the frontend.

## Error handling and UX behavior

The app uses defensive rendering so unexpected API payloads do not crash the whole page. Loading placeholders are shown while data is being fetched, empty states explain when no records are available, and request failures are surfaced in the UI with a readable message.

## Build and lint

```powershell
npm run build
npm run lint
```

## Notes for reviewers

- This frontend is wired to live backend data rather than mocked sample data.
- The backend runs on port `8081` and uses the `/api` base path.
- The dashboard is intentionally split into clear sections to keep the UI easy to evaluate and extend.
- The order history screen includes both status filtering and date-range filtering to match the assessment brief.

## Suggested next steps

- Add an environment variable for the API base URL.
- Add pagination for larger order and inventory lists.
- Add retry and toast-based error feedback.
- Add automated frontend tests with React Testing Library.

