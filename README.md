# MediSync

Pharmacy management system for patient care, consultations, prescriptions, inventory, orders, and payments.

**Stack:** Spring Boot 3 (Java 21) · MySQL · React 19 + Vite

---

## Features by role

| Role | What they can do |
|------|------------------|
| **Patient** | Register and log in, book and cancel appointments, track prescriptions, view and pay for orders, download receipts, manage profile |
| **Doctor** | View today's appointments, start consultations, write prescriptions, view their patients and issued prescriptions |
| **Pharmacist / Admin** | Manage inventory and medications, process the prescription queue (Pending → Received → Preparing → Ready for Collection → Collected), create orders, take payments, view reports, manage users, doctors, and patients, view audit log |

Key workflows:
- **Appointments** — patients book with a doctor; doctors see today's list and start a consultation.
- **Prescriptions** — doctors issue prescriptions; pharmacists advance processing status; patients track progress.
- **Orders & payments** — an order is created when a prescription moves to Preparing. Payment is required before collection. Card payments use a simulated gateway (demo mode). Only the last four digits of the card are stored.
- **Reports** — dashboard statistics, revenue report with date range, and CSV export of payments.
- **Audit log** — important actions are recorded.

---

## Prerequisites

- Java 21
- Maven (or the included `mvnw` wrapper)
- MySQL 8 running on `localhost:3306`
- Node.js 18+ and npm

---

## Quick start

### 1. Database

Create the database (or let the app create it with `createDatabaseIfNotExist=true`):

```sql
CREATE DATABASE IF NOT EXISTS pharmacy_db;
```

Default connection in `src/main/resources/application.properties` points at:

```
jdbc:mysql://localhost:3306/pharmacy_db
username: root
```

### 2. Backend

Generate a JWT secret and start the app:

**PowerShell**
```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
./mvnw spring-boot:run
```

**bash**
```bash
export JWT_SECRET=$(openssl rand -base64 32)
./mvnw spring-boot:run
```

The API listens on **http://localhost:8090**.

Optional environment variables:

| Variable | Purpose | Default |
|----------|---------|---------|
| `JWT_SECRET` | Signing key for tokens (required) | — |
| `ADMIN_USERNAME` | Bootstrap admin username | `admin` |
| `ADMIN_EMAIL` | Bootstrap admin email | `admin@medisync.example` |
| `ADMIN_PASSWORD` | Bootstrap admin password | `Admin@12345` |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM` | Password-reset email | localhost / 25 |
| `FRONTEND_URL` | Base URL used in reset links | `http://localhost:5173` |

On first run the app seeds:
- An admin account (`admin` / `Admin@12345` unless overridden)
- Sample doctors and doctor logins (password: `password`)
- A sample South African medicine catalogue and stock

### 3. Frontend

```bash
cd pharmacy-frontend
cp .env.example .env
npm ci
npm run dev
```

Open **http://localhost:5173**. The app talks to the backend at the URL in `VITE_API_URL` (default `http://localhost:8090`).

---

## Demo accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `Admin@12345` |
| Doctor | (seeded doctor logins) | `password` |
| Patient | register a new account | — |

---

## Demo card numbers (simulated gateway)

| Number | Result |
|--------|--------|
| `4242 4242 4242 4242` | Visa — approved |
| `5555 5555 5555 4444` | Mastercard — approved |
| `4000 0000 0000 0002` | Declined |
| `4000 0000 0000 9995` | Insufficient funds |
| `4000 0000 0000 0069` | Expired card |

Use any future expiry and a 3- or 4-digit CVV. The backend never stores the full card number or CVV.

---

## Running tests

```bash
./mvnw test
```

Most tests run without a database. The full Spring context integration test is opt-in:

```bash
RUN_DB_INTEGRATION_TESTS=true ./mvnw test
```

---

## Project structure

```
pharmacy-system-medisnyc/
├── src/main/java/za/ac/cput/medisnyc/
│   ├── controller/     REST endpoints
│   ├── service/        business logic
│   ├── domain/         JPA entities
│   ├── repository/     data access
│   ├── security/       JWT + RBAC
│   ├── dto/            request/response objects
│   └── exception/      global error handling
├── pharmacy-frontend/  React + Vite app
└── pom.xml
```

---

## One-time database note

If you have an older database, widen the audit log column (Hibernate `ddl-auto=update` does not enlarge existing columns):

```sql
ALTER TABLE audit_logs MODIFY COLUMN details TEXT;
```

---

## Notes

- Legacy orders created before payment tracking have a null payment status and are treated as unpaid. They are excluded from revenue until paid.
- Password reset requires SMTP configuration. Without it, registration and login still work.
- This is a demonstration system. Do not use real card data.
