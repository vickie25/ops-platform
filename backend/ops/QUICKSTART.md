# Quick Start Guide - Cymelle Ops Platform (Windows)

This guide gets your backend running in **5 minutes**.

## Prerequisites Checklist

```powershell
# 1. Verify Java 17+
java -version

# 2. Verify PostgreSQL is running
psql --version

# 3. Verify Git
git --version
```

If any are missing, see [README.md](README.md) for installation links.

---

## Step 1: Create Database (1 minute)

**Option A: Quick (pgAdmin UI)**
1. Start pgAdmin (search Start Menu)
2. Right-click **Databases** → Create → Database
3. Name: `cymelle_db`
4. Click Save

**Option B: Command Line**
```powershell
psql -U postgres

# In psql shell:
CREATE DATABASE cymelle_db;
\q
```

---

## Step 2: Build & Run (3 minutes)

```powershell
# Navigate to project
cd C:\path\to\cymelle-ops\backend\ops

# Build (first time downloads dependencies)
.\gradlew.bat clean build

# Run the application
.\gradlew.bat spring-boot:run
```

**Expected Output:**
```
2026-06-13 12:30:45.123 INFO  Started OpsApplication in 3.521 seconds
```

---

## Step 3: Test the API (1 minute)

Open your browser:

```
http://localhost:8081/api/swagger-ui.html
```

You should see the **Swagger UI** with all endpoints listed.

### Test an Endpoint

**GET /inventory** (View all inventory items)
```
http://localhost:8081/api/inventory
```

Expected response:
```json
[
  {
    "id": 1,
    "name": "Rice (5kg)",
    "price": 250.00,
    "stockQuantity": 3,
    "lowStockThreshold": 10,
    "isLowStock": true
  },
  ...
]
```

---

## Common Commands

```powershell
# Run tests
.\gradlew.bat test

# Run application
.\gradlew.bat spring-boot:run

# Build only
.\gradlew.bat build

# Clean build
.\gradlew.bat clean build

# Check specific test
.\gradlew.bat test --tests FareCalculationServiceTest
```

---

## API Endpoints Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/inventory` | All items |
| GET | `/api/inventory/low-stock` | Items below threshold |
| POST | `/api/orders` | Create order |
| GET | `/api/orders` | All orders |
| DELETE | `/api/orders/{id}` | Cancel order |
| GET | `/api/fare/calculate?distanceKm=10` | Calculate fare |

---

## Troubleshooting

### Port 8081 in Use
```powershell
# Find process
netstat -ano | findstr :8081

# Kill it
taskkill /PID <PID> /F
```

### Database Connection Error
```powershell
# Check PostgreSQL service
Get-Service postgresql-x64-*

# Start it if stopped
Start-Service postgresql-x64-14
```

### Build Fails
```powershell
.\gradlew.bat clean build --refresh-dependencies
```

---

## Next Steps

1. **Read Architecture**: Check [ARCHITECTURE.md](ARCHITECTURE.md)
2. **Explore Endpoints**: Visit [Swagger UI](http://localhost:8081/api/swagger-ui.html)
3. **Run Tests**: `.\gradlew.bat test`
4. **Read README**: Full documentation in [README.md](README.md)

---

## Need Help?

- **Database Issues**: Check [README.md#Database Setup](README.md#set-up-postgresql-database)
- **Deployment**: See [ARCHITECTURE.md#Deployment](ARCHITECTURE.md#9-deployment-architecture)
- **API Details**: Open Swagger UI at `http://localhost:8081/api/swagger-ui.html`
