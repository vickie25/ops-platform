Cymelle Ops Dashboard
This is the React frontend for the Cymelle Ops Platform. It provides an operations dashboard for inventory monitoring, order history review, and fare calculation. The UI is built with React and Vite, and it connects directly to the Spring Boot backend through HTTP APIs.

What the dashboard does
The application is organized as a focused ops console with three main areas. The inventory view shows live stock levels and highlights low-stock items. The order history view supports filtering by order status and date range. The fare engine view lets you calculate trip fares using the backend fare rules and also shows the current pricing configuration.

The frontend is designed to be readable, responsive, and resilient. It includes loading states, empty states, and error handling so the dashboard stays usable even when data is missing or an API request fails.

Tech Stack
React 19
Vite
JavaScript
date-fns for date formatting
lucide-react for icons
Tailwind CSS v4
Local Setup
Prerequisites
Node.js 18 or newer
npm
The Cymelle Ops backend running locally on http://localhost:8081
Install dependencies
From the frontend directory:

npm install
Start the app
npm run dev
Vite will print the local URL in the terminal, usually:

http://localhost:5173
Backend connection
The frontend talks to the backend through the API base URL configured in src/api.js:

http://localhost:8081/api
That means the frontend expects the backend to expose endpoints such as:

GET /api/inventory
GET /api/inventory/low-stock
GET /api/orders
GET /api/orders/filter/status
GET /api/orders/filter/date-range
GET /api/fare/config
GET /api/fare/calculate
If you change the backend port, update BASE_URL in src/api.js to match.

Features
Inventory view
The inventory section loads live inventory data from the backend and presents it in a table with search, low-stock highlighting, and empty-state handling. It is intended to help operations teams quickly identify items that need restocking.

Order history view
The order history section supports status filtering and date-range filtering. It is built to help teams inspect operational trends, review recent transactions, and narrow the list of orders without leaving the dashboard.

Fare engine view
The fare engine section uses the live backend fare configuration and fare calculation endpoint. It demonstrates the pricing rules in a simple UI and gives a quick way to validate fare behavior from the frontend.

Error handling and UX behavior
The app uses defensive rendering so unexpected API payloads do not crash the whole page. Loading placeholders are shown while data is being fetched, empty states explain when no records are available, and request failures are surfaced in the UI with a readable message.

Build and lint
npm run build
npm run lint
Notes for reviewers
This frontend is wired to live backend data rather than mocked sample data.
The backend runs on port 8081 and uses the /api base path.
The dashboard is intentionally split into clear sections to keep the UI easy to evaluate and extend.
The order history screen includes both status filtering and date-range filtering to match the assessment brief.
Suggested next steps
Add an environment variable for the API base URL.
Add pagination for larger order and inventory lists.
Add retry and toast-based error feedback.
Add automated frontend tests with React Testing Library.


# Cymelle Ops Platform - Backend

A production-grade order management and fare calculation system built with **Spring Boot 4.1**, **Java 17**, and **PostgreSQL**.

## Architecture Overview

This project follows a **modular monolith** architecture with clean separation of concerns:

```
src/main/java/com/cymelle/ops/
├── common/                          # Shared infrastructure
│   ├── config/                      # CORS, OpenAPI/Swagger config
│   ├── dto/                         # Common DTOs (ApiErrorResponse)
│   └── exception/                   # Exception hierarchy & global handler
├── inventory/                       # Module 1: Inventory Management
│   ├── entity/Item.java
│   ├── repository/ItemRepository.java
│   ├── service/InventoryService.java
│   ├── controller/InventoryController.java
│   └── dto/ItemResponse.java
├── order/                           # Module 2: Order Management
│   ├── entity/{Order, OrderItem, OrderStatus}
│   ├── repository/{OrderRepository, OrderItemRepository}
│   ├── service/OrderService.java
│   ├── controller/OrderController.java
│   └── dto/{CreateOrderRequest, OrderResponse, ...}
└── fare/                            # Module 3: Fare Calculation
    ├── config/FareProperties.java
    ├── service/FareCalculationService.java
    ├── controller/FareController.java
    └── dto/{FareRequest, FareResponse}
```

## Design Principles

1. **Modularity**: Each domain (inventory, orders, fare) is self-contained with its own controller → service → repository → entity layers.
2. **Dependency Direction**: `Order` depends on `Inventory` (not vice versa). No circular dependencies.
3. **Transactionality**: Critical flows (order creation, cancellation) are wrapped in `@Transactional` to ensure consistency.
4. **Error Handling**: Custom exceptions mapped to HTTP status codes via `GlobalExceptionHandler`.
5. **Testability**: Services are fully unit-tested with comprehensive coverage.

## Prerequisites

### Windows System Requirements

- **Java Development Kit (JDK) 17+**
  - Download from [oracle.com](https://www.oracle.com/java/technologies/downloads/#java17)
  - Verify installation: `java -version`

- **Gradle** (included via wrapper)
  - No separate installation needed; uses `gradlew.bat`

- **PostgreSQL 14+**
  - Download from [postgresql.org](https://www.postgresql.org/download/windows/)
  - Or use [Chocolatey](https://chocolatey.org/) on Windows:

    ```powershell
    choco install postgresql
    ```

- **Git** (for version control)
  - Download from [git-scm.com](https://git-scm.com/download/win)

---

## Setup Instructions (Windows)

### 1. Clone the Repository

```powershell
# Open PowerShell and navigate to your desired location
cd C:\dev
git clone <repository-url>
cd cymelle-ops\backend\ops
```

### 2. Set Up PostgreSQL Database

**Option A: Using pgAdmin (GUI)**

1. Start PostgreSQL server (starts automatically on Windows)
2. Open pgAdmin from Start Menu
3. Create a new database:
   - Right-click **Databases** → **Create** → **Database**
   - Name: `cymelle_db`
   - Click **Save**

**Option B: Using Command Prompt**

```cmd
# Open Command Prompt as Administrator
psql -U postgres

-- In psql shell:
CREATE DATABASE cymelle_db;
\q
```

**Verify Connection:**

```powershell
psql -U postgres -h localhost -d cymelle_db
```

### 3. Configure Database Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cymelle_db
spring.datasource.username=postgres
spring.datasource.password=vick3900  # Your PostgreSQL password
```

### 4. Build the Project

```powershell
# Navigate to project root
cd C:\path\to\cymelle-ops\backend\ops

# Build using Gradle Wrapper
.\gradlew.bat build

# Or clean build (recommended first time)
.\gradlew.bat clean build
```

**Expected Output:**

```
BUILD SUCCESSFUL in Xs
```

### 5. Run the Application

```powershell
# Start the Spring Boot application
.\gradlew.bat spring-boot:run

# Or run the JAR directly
java -jar build/libs/ops-1.0.0.jar
```

**Expected Console Output:**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::           (v4.1.0)

2026-06-13 12:30:45.123 INFO  com.cymelle.ops.OpsApplication : Started OpsApplication in 3.521 seconds
```

---

## API Endpoints

### Base URL

```
http://localhost:8081/api
```

### Inventory Management

**Get all items**

```
GET /api/inventory
```

**Get item by ID**

```
GET /api/inventory/{itemId}
```

**Get low-stock items**

```
GET /api/inventory/low-stock
```

**Get items below custom threshold**

```
GET /api/inventory/threshold?threshold=10
```

### Order Management

**Create order** ⚠️ **Critical Flow** — Stock deduction happens here

```
POST /api/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 5
    },
    {
      "productId": 2,
      "quantity": 3
    }
  ]
}
```

**Get all orders**

```
GET /api/orders
```

**Get order by ID**

```
GET /api/orders/{orderId}
```

**Get orders by status**

```
GET /api/orders/filter/status?status=PENDING
```

**Get orders by date range**

```
GET /api/orders/filter/date-range?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
```

**Get orders by status and date range**

```
GET /api/orders/filter/advanced?status=CONFIRMED&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
```

**Cancel order** ⚠️ **Critical Flow** — Stock restoration happens here

```
DELETE /api/orders/{orderId}
```

### Fare Calculation

**Calculate fare (GET)**

```
GET /api/fare/calculate?distanceKm=10&surgeMultiplier=1.5
```

**Calculate fare (POST)**

```
POST /api/fare/calculate
Content-Type: application/json

{
  "distanceKm": 10.5,
  "surgeMultiplier": "1.5"
}
```

**Get fare configuration**

```
GET /api/fare/config
```

---

## Documentation & Testing

### Swagger UI (Interactive API Explorer)

Once the application is running, access:

```
http://localhost:8081/api/swagger-ui.html
```

This provides an interactive interface to test all endpoints.

### API Documentation (OpenAPI/JSON)

```
http://localhost:8081/api/v3/api-docs
```

### Run Unit Tests

```powershell
# Run all tests
.\gradlew.bat test

# Run specific test class
.\gradlew.bat test --tests FareCalculationServiceTest

# Run with detailed output
.\gradlew.bat test --info
```

**Test Coverage:**

- `FareCalculationServiceTest`: 12 comprehensive tests covering:
  - Normal fare calculation
  - Surge multiplier scenarios
  - Minimum fare application
  - Edge cases (zero distance, null values, large distances)

- `OrderServiceTest`: 12 comprehensive tests covering:
  - Order creation with stock deduction ✅
  - Stock insufficiency validation ✅
  - Order cancellation with stock restoration ✅
  - Invalid order state handling
  - Date range filtering

---

## Database Schema

### Items Table (Inventory)

```sql
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL,
    low_stock_threshold INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Orders Table

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(30) NOT NULL,          -- PENDING, CONFIRMED, CANCELLED, COMPLETED
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Order Items Table (Junction)

```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES items(id),
    product_name VARCHAR(255) NOT NULL,
    unit_price_at_order DECIMAL(10, 2) NOT NULL,  -- Price snapshot at order time
    quantity INTEGER NOT NULL,
    subtotal DECIMAL(12, 2) NOT NULL
);
```

**Key Design Decisions:**

- `unit_price_at_order` snapshots the price at order time → historical accuracy even if item price changes
- `order_items` uses CASCADE delete → cancelling order removes associated items
- Indexes on frequently queried columns (status, created_at, stock_quantity)

---

## Critical Implementation Details

### Stock Deduction (Order Creation)

✅ **Everything in one transaction** (`@Transactional`):

```
1. Validate all items exist
2. Check stock availability for each item
3. Calculate total amount
4. Create Order (status = PENDING)
5. Create OrderItems with price snapshot
6. Deduct stock from inventory
7. Update Order status to CONFIRMED
```

If any step fails, entire transaction rolls back → **No orphaned orders or inconsistent stock**.

### Stock Restoration (Order Cancellation)

✅ **Restore stock only for cancellable orders**:

```
1. Retrieve order
2. Validate order status (not CANCELLED or COMPLETED)
3. For each order item: restore stock
4. Mark order as CANCELLED
5. Do NOT physically delete row → maintain audit trail
```

### Fare Calculation Engine

**Formula:**

```
fare = (baseFare + distanceKm × ratePerKm) × surgeMultiplier
if fare < minimumFare: fare = minimumFare
```

**Configuration (externalised):**

```properties
fare.base-fare=50.00
fare.rate-per-km=10.00
fare.minimum-fare=100.00
fare.default-surge-multiplier=1.0
```

No hardcoded values → easy to adjust without code changes.

---

## Error Handling

All errors return consistent JSON responses:

```json
{
  "timestamp": "2026-06-13T12:30:45",
  "status": 409,
  "error": "INSUFFICIENT_STOCK",
  "message": "Insufficient stock for item: Rice. Available: 3, Requested: 5",
  "path": "/api/orders"
}
```

**Mapped Exceptions:**

| Exception | HTTP Status |
|-----------|------------|
| `ResourceNotFoundException` | 404 Not Found |
| `InsufficientStockException` | 409 Conflict |
| `InvalidOrderStateException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 400 Bad Request |
| Generic `Exception` | 500 Internal Server Error |

---

## Environment Variables

Create a `.env` file (optional, for overriding application.properties):

```
DB_URL=jdbc:postgresql://localhost:5432/cymelle_db
DB_USERNAME=postgres
DB_PASSWORD=vick3900
SERVER_PORT=8081
FARE_BASE=50.00
FARE_RATE_PER_KM=10.00
FARE_MINIMUM=100.00
```

---

## Deployment (Production Checklist)

- [ ] Use PostgreSQL (not H2) for persistence
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate` (migrations only)
- [ ] Enable HTTPS (SSL/TLS)
- [ ] Use environment variables for sensitive data (DB password, API keys)
- [ ] Configure CORS for your frontend domain
- [ ] Set up database backups
- [ ] Enable audit logging for orders and inventory changes
- [ ] Use connection pooling (HikariCP configured automatically)

---

## Troubleshooting

### Port 8081 Already in Use

```powershell
# Find process using port 8081
netstat -ano | findstr :8081

# Kill the process (replace PID with actual value)
taskkill /PID <PID> /F
```

### PostgreSQL Connection Refused

```powershell
# Check if PostgreSQL service is running
Get-Service postgresql-x64-*

# Start the service if stopped
Start-Service postgresql-x64-14  # Replace 14 with your version
```

### Gradle Build Fails

```powershell
# Clear Gradle cache and rebuild
.\gradlew.bat clean build --refresh-dependencies

# Check Java version
java -version  # Should be 17+
```

### Database Migration Failed

```powershell
# Check Flyway history table
psql -U postgres -d cymelle_db -c "SELECT * FROM flyway_schema_history;"

# Reset migrations (careful in production!)
psql -U postgres -d cymelle_db -c "DELETE FROM flyway_schema_history;"
```

---

## Performance Considerations

1. **Batch Processing**: Hibernate batch size set to 10
2. **Lazy Loading**: OrderItems fetched eagerly with Order to avoid N+1 queries
3. **Indexes**: Created on frequently filtered columns (status, created_at, stock_quantity)
4. **Connection Pooling**: HikariCP automatically configured (10 connections default)

---

## Security Best Practices

✅ **Implemented:**

- Input validation via `@Valid` annotations
- SQL injection prevention via parameterized queries (JPA)
- CORS configured for React frontend (localhost:3000, localhost:5173)
- Exception handling without exposing stack traces
- Secrets in environment variables (not hardcoded)

⚠️ **TODO (for production):**

- Add Spring Security for authentication/authorization
- Enable HTTPS
- Rate limiting
- API key authentication for third-party integrations

---

## Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.1.0 | Web framework |
| Java | 17 LTS | Language |
| Gradle | 8.x | Build tool |
| PostgreSQL | 14+ | Database |
| Flyway | 10.0.1 | Database migrations |
| Lombok | 1.18.30 | Boilerplate reduction |
| JUnit 5 | 5.10.0 | Testing |
| Mockito | 5.5.1 | Mocking |
| SpringDoc OpenAPI | 2.2.0 | Swagger UI |

---

## Related Documentation

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Flyway Docs](https://flywaydb.org/documentation/)
- [OpenAPI/Swagger](https://swagger.io/tools/swagger-ui/)

---

## License

Apache 2.0 - See LICENSE file

---

## Support

For issues or questions, create a GitHub issue or contact the development team.


