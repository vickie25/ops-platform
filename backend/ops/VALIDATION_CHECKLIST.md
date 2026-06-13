# Backend Validation Checklist

Use this checklist to verify the backend is working correctly.

## Pre-Flight (Before Running)

- [ ] PostgreSQL installed and running
- [ ] Database `cymelle_db` created
- [ ] Java 17+ installed
- [ ] Gradle wrapper present (`gradlew.bat`)
- [ ] All source files in `src/main/java`
- [ ] All test files in `src/test/java`
- [ ] `build.gradle` updated with correct dependencies
- [ ] `application.properties` configured with DB credentials

---

## Build & Startup

```powershell
# 1. Clean build (first time or after changes)
.\gradlew.bat clean build

# Expected: BUILD SUCCESSFUL
```

- [ ] Build succeeds without errors
- [ ] No compilation errors
- [ ] JAR file created in `build/libs/`

```powershell
# 2. Run application
.\gradlew.bat spring-boot:run

# Expected: Started OpsApplication in ~3.5 seconds
```

- [ ] Application starts successfully
- [ ] No connection errors to PostgreSQL
- [ ] No exception stack traces in console
- [ ] Port 8080 is accessible

---

## Endpoints Validation

### Access Swagger UI
```
http://localhost:8080/api/swagger-ui.html
```

- [ ] Swagger UI loads without errors
- [ ] All 3 modules visible (Inventory, Order, Fare)
- [ ] All endpoints listed

### Inventory Module

**1. Get all items:**
```
GET http://localhost:8080/api/inventory
```
- [ ] Returns HTTP 200
- [ ] Response contains array of items
- [ ] Each item has: id, name, price, stockQuantity, lowStockThreshold, isLowStock
- [ ] Sample data loaded (should see 10 items from V2__Seed_Sample_Data.sql)

**2. Get low-stock items:**
```
GET http://localhost:8080/api/inventory/low-stock
```
- [ ] Returns HTTP 200
- [ ] Response contains items where stockQuantity < lowStockThreshold
- [ ] Should see at least "Rice (5kg)" with quantity 3 < threshold 10
- [ ] Should see "Beans (1kg)" with quantity 2 < threshold 8

**3. Get item by ID:**
```
GET http://localhost:8080/api/inventory/1
```
- [ ] Returns HTTP 200
- [ ] Returns specific item with ID 1
- [ ] Contains all item fields

**4. Get items below custom threshold:**
```
GET http://localhost:8080/api/inventory/threshold?threshold=15
```
- [ ] Returns HTTP 200
- [ ] Returns all items with stockQuantity < 15
- [ ] Should include more items than low-stock default

### Order Module

**1. Create order (Critical Flow):**
```
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 3
    }
  ]
}
```
- [ ] Returns HTTP 201 Created
- [ ] Response contains orderId, status (CONFIRMED), totalAmount
- [ ] totalAmount = (100*2) + (80*3) = 440
- [ ] Order items include price snapshot: unitPriceAtOrder

**Verify Stock Deduction:**
```
GET http://localhost:8080/api/inventory/1
```
- [ ] Rice quantity should decrease by 2 (from 3 to 1)

**2. Get all orders:**
```
GET http://localhost:8080/api/orders
```
- [ ] Returns HTTP 200
- [ ] Response contains array of orders
- [ ] Newest orders first (sorted by createdAt DESC)
- [ ] Should see the order you just created

**3. Get order by ID:**
```
GET http://localhost:8080/api/orders/{orderId}
```
(Replace {orderId} with order ID from creation)
- [ ] Returns HTTP 200
- [ ] Returns specific order with all details
- [ ] Shows order items with price snapshot

**4. Get orders by status:**
```
GET http://localhost:8080/api/orders/filter/status?status=CONFIRMED
```
- [ ] Returns HTTP 200
- [ ] Returns all orders with status CONFIRMED
- [ ] Should include order created in step 1

**5. Get orders by date range:**
```
GET http://localhost:8080/api/orders/filter/date-range?from=2024-01-01T00:00:00&to=2026-12-31T23:59:59
```
- [ ] Returns HTTP 200
- [ ] Returns orders within date range
- [ ] Should include recently created order

**6. Cancel order (Critical Flow):**
```
DELETE http://localhost:8080/api/orders/{orderId}
```
(Use orderId from step 1 or 3)
- [ ] Returns HTTP 200
- [ ] Order status changed to CANCELLED
- [ ] Response shows status: "CANCELLED"

**Verify Stock Restoration:**
```
GET http://localhost:8080/api/inventory/1
```
- [ ] Rice quantity should increase back (from 1 to 3)
- [ ] Stock successfully restored

**7. Validate order state transitions:**

Try cancelling a completed order:
```
DELETE http://localhost:8080/api/orders/{orderId}
```
(If you modify the order status to COMPLETED first)
- [ ] Returns HTTP 400 Bad Request
- [ ] Error message: "Cannot cancel completed order"

Try cancelling an already cancelled order:
```
DELETE http://localhost:8080/api/orders/{orderId}
```
(Use the order you cancelled in step 6)
- [ ] Returns HTTP 400 Bad Request
- [ ] Error message: "already cancelled"

### Fare Module

**1. Calculate fare (normal):**
```
GET http://localhost:8080/api/fare/calculate?distanceKm=10
```
- [ ] Returns HTTP 200
- [ ] fare = 50 + (10 * 10) * 1.0 = 150
- [ ] finalFare: 150.00
- [ ] Response shows breakdown: baseFare, distanceCharge, surgeMultiplier, minimumFare

**2. Calculate fare with surge:**
```
GET http://localhost:8080/api/fare/calculate?distanceKm=10&surgeMultiplier=1.5
```
- [ ] Returns HTTP 200
- [ ] fare = (50 + 100) * 1.5 = 225
- [ ] finalFare: 225.00

**3. Calculate fare (minimum applied):**
```
GET http://localhost:8080/api/fare/calculate?distanceKm=1
```
- [ ] Returns HTTP 200
- [ ] Base calculation = 50 + (1 * 10) = 60
- [ ] But minimumFare = 100
- [ ] finalFare: 100.00

**4. Calculate fare (POST variant):**
```
POST http://localhost:8080/api/fare/calculate
Content-Type: application/json

{
  "distanceKm": 5.5,
  "surgeMultiplier": "1.2"
}
```
- [ ] Returns HTTP 200
- [ ] fare = (50 + 55) * 1.2 = 126
- [ ] finalFare: 126.00

**5. Get fare configuration:**
```
GET http://localhost:8080/api/fare/config
```
- [ ] Returns HTTP 200
- [ ] Shows: baseFare, ratePerKm, minimumFare, defaultSurgeMultiplier
- [ ] baseFare: 50.00
- [ ] ratePerKm: 10.00
- [ ] minimumFare: 100.00

---

## Error Handling Validation

### Insufficient Stock

Try creating order with quantity exceeding stock:
```
POST http://localhost:8080/api/orders

{
  "items": [{
    "productId": 1,
    "quantity": 1000
  }]
}
```
- [ ] Returns HTTP 409 Conflict
- [ ] Error message contains: "INSUFFICIENT_STOCK"
- [ ] Message shows available vs requested quantity

### Resource Not Found

Try getting non-existent item:
```
GET http://localhost:8080/api/inventory/99999
```
- [ ] Returns HTTP 404 Not Found
- [ ] Error message: "Item not found with ID: 99999"

### Validation Error

Try creating order with invalid data:
```
POST http://localhost:8080/api/orders

{
  "items": [{
    "productId": 1,
    "quantity": -5
  }]
}
```
- [ ] Returns HTTP 400 Bad Request
- [ ] Error message contains validation error about negative quantity

### Invalid Order State

Try cancelling a completed order:
```
DELETE http://localhost:8080/api/orders/{completedOrderId}
```
- [ ] Returns HTTP 400 Bad Request
- [ ] Error message: "Cannot cancel completed order"

---

## Unit Tests

```powershell
.\gradlew.bat test
```

- [ ] All tests pass
- [ ] Output shows: "24 tests passed" (12 fare + 12 order)
- [ ] No test failures
- [ ] No warnings about deprecated code

### Run Specific Tests

```powershell
.\gradlew.bat test --tests FareCalculationServiceTest
```
- [ ] All 12 fare tests pass

```powershell
.\gradlew.bat test --tests OrderServiceTest
```
- [ ] All 12 order tests pass

---

## Database Verification

### Check Flyway Migrations

```powershell
psql -U postgres -d cymelle_db

# In psql:
SELECT * FROM flyway_schema_history;
```

- [ ] Shows 2 migrations: V1__Initial_Schema, V2__Seed_Sample_Data
- [ ] Both with status "Success"

### Check Tables

```sql
\dt
```

- [ ] Shows 3 tables: items, orders, order_items
- [ ] Tables have correct indexes

### Check Sample Data

```sql
SELECT COUNT(*) FROM items;
SELECT COUNT(*) FROM orders;
```

- [ ] items table has 10 rows (seeded)
- [ ] orders table has 0 rows initially (will increase as you create orders)

---

## Concurrent Order Creation (Stress Test)

Create 3 orders simultaneously to test transaction isolation:

**Order 1:**
```
POST /api/orders
{
  "items": [{"productId": 1, "quantity": 1}]
}
```

**Order 2:**
```
POST /api/orders
{
  "items": [{"productId": 2, "quantity": 1}]
}
```

**Order 3:**
```
POST /api/orders
{
  "items": [{"productId": 3, "quantity": 1}]
}
```

- [ ] All 3 orders created successfully (no race conditions)
- [ ] Stock for items 1, 2, 3 all decremented correctly
- [ ] No duplicate orders
- [ ] No inconsistent stock levels

---

## Performance Checks

### Response Time: GET /inventory
- [ ] Response time < 100ms (typically ~50ms)

### Response Time: POST /orders
- [ ] Response time < 500ms (typically ~200ms)

### Response Time: GET /fare/calculate
- [ ] Response time < 50ms (CPU-bound, fast)

---

## Security Checks

### No Stack Traces

Try an error scenario:
```
GET http://localhost:8080/api/inventory/invalid_id
```
- [ ] Error response does NOT contain Java stack trace
- [ ] Error response is JSON with friendly message

### CORS Headers

Make a request from `http://localhost:3000` (React frontend):
- [ ] Response includes CORS headers
- [ ] Access-Control-Allow-Origin header present

---

## Documentation Verification

- [ ] README.md exists and is readable
- [ ] ARCHITECTURE.md explains design decisions
- [ ] QUICKSTART.md provides 5-minute setup
- [ ] IMPLEMENTATION_SUMMARY.md documents what was built
- [ ] Swagger UI accessible at http://localhost:8080/api/swagger-ui.html

---

## Final Checklist

- [ ] Backend builds successfully
- [ ] All endpoints working
- [ ] All 24 unit tests passing
- [ ] Stock deduction works (CREATE order)
- [ ] Stock restoration works (CANCEL order)
- [ ] Fare calculation correct (all scenarios)
- [ ] Error handling returns proper HTTP status codes
- [ ] Database migrations applied
- [ ] No stack traces in error responses
- [ ] Documentation complete
- [ ] CORS configured
- [ ] Response times acceptable
- [ ] Concurrent operations work correctly

---

## What to Do If Tests Fail

### Build Fails
```powershell
.\gradlew.bat clean build --refresh-dependencies
```

### Database Connection Error
```powershell
# Verify PostgreSQL is running
psql -U postgres -h localhost

# Check database exists
\l

# Reconnect and quit
\q
```

### Tests Fail
```powershell
# Run with verbose output
.\gradlew.bat test --info

# Check specific test
.\gradlew.bat test --tests OrderServiceTest::shouldDeductStockWhenOrderCreated
```

### Port Already in Use
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

---

## Next Steps

After validation:

1. **Connect React Frontend**
   - Point React app to `http://localhost:8080/api`
   - CORS already configured

2. **Deploy to Production**
   - See ARCHITECTURE.md#Deployment
   - Use Docker + PostgreSQL RDS

3. **Add Authentication**
   - Integrate Spring Security
   - Add JWT tokens

4. **Monitor & Scale**
   - Set up logging (ELK stack)
   - Add metrics (Prometheus)
   - Configure auto-scaling

---

## Sign-Off

✅ **Backend Ready for Development**

Date Validated: _______________
Validated By: _______________
Notes: _______________________
