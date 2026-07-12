# SHEMACH — Purchase & Quota Management System

A desktop application for managing the purchase of rationed/subsidized household goods (like sugar and cooking oil) at the local community level, built to enforce fair-distribution rules automatically.

The project is modeled on a real-world scenario: essential goods that are periodically scarce and government-subsidized need controlled, auditable distribution — restricted to registered households, capped by monthly quotas, and tracked by the staff who process each sale.

## What it does

- Lets staff record purchases for two buyer types — **registered homeowners** and **non-homeowners**
- Automatically restricts sensitive items (Sugar, Oil) to homeowners only
- Enforces a **monthly purchase quota per item**, checked before every sale
- Lets staff preview a household's remaining quota before completing a sale
- Provides a full, sortable **purchase history** lookup per household
- Adapts its unit of measurement (kg vs. liter) depending on the item selected

## Why it's structured this way

The database schema mirrors a real administrative hierarchy, from the top down:

```
government → woreda → kebele → shemachoche → home
```

Each `employee` is tied to a `shemachoche` (the local distribution point the project is named after), and every `purchase` links to an `item` and — for homeowners — a `home`. This lets any purchase be traced back to exactly who bought it and which local office processed it.

The core business rules (which items are restricted, and how much of each item a household or non-homeowner can buy per month) live in a **SQL Server stored procedure**, `Insert_Purchase_WithQuotaCheck`, rather than in the application code. That keeps the rule enforced consistently no matter what client talks to the database.

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Java (Swing) |
| Connectivity | JDBC (Microsoft SQL Server driver) |
| Database | Microsoft SQL Server |
| Business logic | T-SQL stored procedure |
| Build tooling | Apache Ant / NetBeans project |

## Project Structure

```
SHEMACH/
├── PurchaseGUI/
│   ├── src/purchasegui/PurchaseGUI.java   # Main application (Swing GUI)
│   ├── build.xml                          # Ant build script
│   ├── manifest.mf
│   └── nbproject/                         # NetBeans project config
└── shemachadvdb.sql                       # Full database schema, seed data, and stored procedure
```

## Database Schema (core tables)

```sql
item(item_id, item_name, is_restricted, category, max_per_month)
home(home_id, home_owner_name, number_of_family, shemachoche_id)
employee(emp_id, shemachoche_id, emp_name, emp_position, ...)
purchase(purchase_id, home_id NULL, item_id, quantity, purchase_date, buyer_type)
```

`home_id` is nullable by design — non-homeowners can purchase unrestricted items without being tied to a registered household.

## Getting Started

### Prerequisites
- Java JDK 17+
- Microsoft SQL Server (local or remote instance)
- NetBeans (recommended) or Ant on the command line

### Setup
1. Run `shemachadvdb.sql` against your SQL Server instance to create the database, tables, seed data, and stored procedure.
2. Open `PurchaseGUI/` in NetBeans, or build it directly with Ant:
   ```bash
   cd PurchaseGUI
   ant jar
   ```
3. Update the database connection details (server, database name, credentials) in `PurchaseGUI.java`.
4. Run the built jar:
   ```bash
   java -jar dist/PurchaseGUI.jar
   ```

## Known Limitations / Next Steps

- Database credentials are currently set directly in the source — should be externalized to environment variables or a config file.
- The quota check and purchase insert aren't wrapped in an explicit transaction, which could allow a race condition under concurrent access.
- The stored procedure has a hardcoded quota fallback value rather than always reading `max_per_month` from the `item` table.
- A future version could move this to a web-based interface (e.g. Spring Boot) with employee login and role-based access.

## About the Name

**Shemach** comes from `shemachoche` — the smallest administrative unit modeled in the database, representing a local distribution point within a neighborhood (`kebele`).

