# SCB Migration Statistics

This project is a small full-stack system for exploring migration statistics from Statistics Sweden, SCB.

The goal is simple: a user logs in, filters migration data by region, sex, age group, and year, then sees totals, an analysis chart, and the raw rows. Admins can approve new users before they are allowed to use the dashboard.

The app is split into three services:

- `backend` - Spring Boot API, database access, security, SCB import, RabbitMQ publisher
- `frontend` - React dashboard served by nginx in Docker
- `email-service` - Spring Boot worker that listens for email events from RabbitMQ

PostgreSQL stores users and migration rows. RabbitMQ is used only for email notifications.

## Quick Start

From the repository root:

```bash
docker compose up --build
```

Open:

- Frontend: http://localhost:5173
- Backend docs endpoint: http://localhost:8080/api/docs
- RabbitMQ UI: http://localhost:15672

RabbitMQ login:

```text
guest / guest
```

Default admin login:

```text
admin@scb.se / Admin123!
```

New registered users are disabled by default. Log in as admin and enable them from the admin page.

## What The App Does

The app shows Swedish migration statistics from SCB for 1997 to 2024.

The dashboard lets a logged-in user filter by:

- region
- sex
- age group
- year

After a search, the frontend shows:

- total number of matching rows
- total immigrations
- total emigrations
- total net migration
- an analysis diagram
- the full result table

The analysis diagram uses the current search result. If the result contains several years, it shows the trend per year. If the result is focused on one year, it shows the regions with the largest net migration.

## How The System Fits Together

```text
browser
  |
  | HTTP
  v
frontend nginx / React
  |
  | /api/*
  v
backend Spring Boot
  |
  | JPA
  v
PostgreSQL

backend Spring Boot
  |
  | startup fetch
  v
SCB API

backend Spring Boot
  |
  | email events
  v
RabbitMQ
  |
  v
email-service Spring Boot
```

The frontend does not call SCB directly. Only the backend talks to SCB.

## Backend

Main package:

```text
backend/src/main/java/se/scb
```

Important folders:

```text
config/       security, JWT filter, RabbitMQ config, rate limiting
controller/   REST endpoints
dto/          request and response objects
exception/    API error handling
model/        JPA entities
repository/   Spring Data repositories and filtering specs
service/      business logic, SCB import, email publishing
util/         startup data loader and JWT utility
```

### Startup Logic

The backend starts from:

```text
backend/src/main/java/se/scb/ScbMigrationApplication.java
```

On startup, `DataLoader` runs:

```text
backend/src/main/java/se/scb/util/DataLoader.java
```

It does two things:

1. Creates the default admin user if `admin@scb.se` does not exist.
2. Loads migration data from SCB if the `migrations` table is empty.

That second condition matters. If data already exists, the backend does not fetch SCB again on every restart.

### SCB Data Import

The import flow is:

```text
DataLoader
  -> ScbDataFetcher
  -> ScbApiClient
  -> SCB API
  -> ScbResponseParser
  -> MigrationRepository
  -> PostgreSQL
```

Files:

```text
backend/src/main/java/se/scb/service/ScbDataFetcher.java
backend/src/main/java/se/scb/service/ScbApiClient.java
backend/src/main/java/se/scb/service/ScbResponseParser.java
```

`ScbApiClient` sends POST requests to SCB's PxWeb API.

The current SCB table returns immigrations and emigrations as two value columns. The parser turns each SCB row into a `Migration` entity with:

```text
region
regionCode
gender
ageGroup
year
immigrations
emigrations
```

Age groups are fetched in chunks, for example:

```text
0-9
10-19
20-29
...
100+
```

This keeps the SCB requests at a size the API accepts.

### Migration Search

The dashboard calls:

```text
GET /api/migrations
GET /api/migrations/filters
```

Controller:

```text
backend/src/main/java/se/scb/controller/MigrationController.java
```

Service:

```text
backend/src/main/java/se/scb/service/MigrationService.java
```

Filtering is built in:

```text
backend/src/main/java/se/scb/repository/MigrationSpecification.java
```

The filters are optional. A request can include any mix of:

```text
region
gender
ageGroup
year
```

Example:

```text
/api/migrations?region=Skane&gender=men&ageGroup=20-29&year=2024
```

The response uses `MigrationResponse`, which also calculates:

```text
netMigration = immigrations - emigrations
```

### Users And Login

User accounts are stored in the `users` table.

Important files:

```text
backend/src/main/java/se/scb/model/User.java
backend/src/main/java/se/scb/model/Role.java
backend/src/main/java/se/scb/controller/AuthController.java
backend/src/main/java/se/scb/service/AuthService.java
backend/src/main/java/se/scb/util/JwtUtil.java
backend/src/main/java/se/scb/config/JwtAuthFilter.java
backend/src/main/java/se/scb/config/SecurityConfig.java
```

The login flow is:

```text
frontend login form
  -> POST /api/auth/login
  -> AuthService checks email/password
  -> JwtUtil creates a token
  -> backend sets jwt as an HttpOnly cookie
  -> frontend stores basic user info in sessionStorage
```

The JWT cookie is what authenticates API calls. It is HttpOnly, so React cannot read it directly.

The frontend keeps user display info in `sessionStorage`, such as first name and role. That is used for navigation and page guards. The backend still decides if protected API calls are allowed.

### Roles

There are two roles:

```text
ROLE_USER
ROLE_ADMIN
```

Users can access:

```text
/api/migrations
/api/migrations/filters
```

Admins can also access:

```text
/api/admin/users
/api/admin/users/{id}/enable
/api/admin/users/{id}
```

New users are created with:

```text
enabled = false
```

An admin must enable the account before the user can log in.

### Rate Limiting

Login attempts are rate limited in:

```text
backend/src/main/java/se/scb/config/RateLimitFilter.java
```

The current rule is:

```text
5 login attempts per IP per minute
```

If the limit is reached, the backend returns HTTP `429`.

### Email Events

The backend does not send email directly. It publishes an event to RabbitMQ.

Publisher:

```text
backend/src/main/java/se/scb/service/EmailPublisher.java
```

RabbitMQ setup:

```text
backend/src/main/java/se/scb/config/RabbitMQConfig.java
```

Events are published when:

- a user registers
- a user logs in

If RabbitMQ is unavailable, the backend logs the error and keeps running.

## Email Service

Main package:

```text
email-service/src/main/java/se/scb/email
```

Important files:

```text
consumer/EmailConsumer.java
consumer/EmailEvent.java
service/MailSenderService.java
config/RabbitMQConfig.java
```

The worker listens to the email queue. When it receives an event, it chooses which email to send:

```text
REGISTER -> welcome email
LOGIN    -> login notification
```

Mail settings are in:

```text
email-service/src/main/resources/application.properties
```

For real email, set these before starting Docker:

```bash
MAIL_USERNAME=your@email.com MAIL_PASSWORD=your-password docker compose up --build
```

Without real credentials, the service can start, but actual email sending will fail when it tries to use SMTP.

## Frontend

Main folder:

```text
frontend/src
```

Important folders:

```text
components/auth/       login and register forms
components/layout/     navbar, protected route, spinner
components/migration/  filters, stats, analysis chart, data table
context/               auth state
pages/                 page-level views
services/              API wrappers
styles/                CSS
```

### Frontend Routing

Routes are defined in:

```text
frontend/src/App.jsx
```

Main routes:

```text
/login
/register
/dashboard
/admin
/docs
```

`ProtectedRoute` blocks pages when the frontend does not have a user in auth state.

### API Calls

Axios is configured in:

```text
frontend/src/services/api.js
```

It uses:

```text
baseURL: /api
withCredentials: true
```

`withCredentials` is important because the browser must send the JWT cookie with API requests.

Service wrappers:

```text
authService.js       login, register, logout
migrationService.js  migration data and filters
adminService.js      admin user management
```

### Dashboard Flow

Dashboard page:

```text
frontend/src/pages/DashboardPage.jsx
```

The flow is:

```text
DashboardPage
  -> FilterPanel loads filter choices
  -> user clicks Search
  -> migrationService.getMigrations(...)
  -> StatsSummary displays totals
  -> AnalysisChart displays a diagram
  -> DataTable displays rows
```

The chart is in:

```text
frontend/src/components/migration/AnalysisChart.jsx
```

It does not use a chart library. It aggregates the rows in JavaScript and renders CSS bars.

## Docker Setup

The root `docker-compose.yml` starts:

```text
postgres
rabbitmq
backend
email-service
frontend
```

The frontend container serves static files with nginx. Its nginx config proxies `/api/` to the backend container:

```text
frontend/nginx.conf
```

The backend runs with the `prod` Spring profile in Docker and uses PostgreSQL.

Local development uses the `dev` profile and H2 by default.

## Running Locally Without Docker

Backend:

```bash
cd backend
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to:

```text
http://localhost:8080
```

Note: the Maven wrapper must be correctly configured for `./mvnw` to work. If the wrapper jar/config is missing, use a local Maven installation or Docker.

## Tests

Backend tests:

```bash
cd backend
./mvnw test
```

Frontend tests:

```bash
cd frontend
npm test
```

Existing test coverage includes:

- auth service behavior
- migration service mapping
- admin service actions
- auth controller responses
- login form behavior
- protected frontend routes

## Common Problems

### The dashboard filters are empty

Check if migration data exists:

```bash
docker exec scb-postgres psql -U scb -d scb_migrations -c "select count(*) from migrations;"
```

If the count is `0`, check backend logs:

```bash
docker logs scb-backend
```

The backend imports SCB data only when the table is empty. If SCB rejects the request or the network is unavailable during startup, the app still starts, but the dashboard has no data.

### Login works, but API calls fail

Check that requests include credentials. In the frontend this is handled by:

```text
frontend/src/services/api.js
```

The setting must stay enabled:

```text
withCredentials: true
```

### A registered user cannot log in

That is expected until an admin enables the account.

Log in as:

```text
admin@scb.se / Admin123!
```

Then go to the admin page and activate the user.

### Frontend changes do not show in Docker

The Docker frontend is a production nginx build. Rebuild it:

```bash
docker compose up -d --build frontend
```

For hot reload, run Vite locally instead:

```bash
cd frontend
npm run dev
```

## Useful Commands

Start everything:

```bash
docker compose up --build
```

Start in background:

```bash
docker compose up -d --build
```

See running containers:

```bash
docker ps
```

Backend logs:

```bash
docker logs -f scb-backend
```

Frontend logs:

```bash
docker logs -f scb-frontend
```

Database row count:

```bash
docker exec scb-postgres psql -U scb -d scb_migrations -c "select count(*) from migrations;"
```

Stop containers:

```bash
docker compose down
```

Stop containers and remove the database volume:

```bash
docker compose down -v
```

Use `down -v` carefully. It deletes the PostgreSQL data, including registered users and imported migration rows.

## Where To Start When Changing Things

For login or roles:

```text
backend/src/main/java/se/scb/service/AuthService.java
backend/src/main/java/se/scb/config/SecurityConfig.java
frontend/src/context/AuthContext.jsx
```

For migration filters:

```text
backend/src/main/java/se/scb/repository/MigrationSpecification.java
frontend/src/components/migration/FilterPanel.jsx
```

For dashboard display:

```text
frontend/src/pages/DashboardPage.jsx
frontend/src/components/migration/
frontend/src/styles/dashboard.css
```

For SCB import:

```text
backend/src/main/java/se/scb/service/ScbApiClient.java
backend/src/main/java/se/scb/service/ScbResponseParser.java
backend/src/main/java/se/scb/service/ScbDataFetcher.java
```

For admin user management:

```text
backend/src/main/java/se/scb/controller/AdminController.java
backend/src/main/java/se/scb/service/AdminService.java
frontend/src/pages/AdminPage.jsx
```

For email:

```text
backend/src/main/java/se/scb/service/EmailPublisher.java
email-service/src/main/java/se/scb/email/consumer/EmailConsumer.java
email-service/src/main/java/se/scb/email/service/MailSenderService.java
```

## Short Version

This is an admin-approved, login-protected statistics dashboard.

The backend imports migration data from SCB into PostgreSQL, exposes filtered API endpoints, and secures them with JWT cookies. The frontend lets users filter and analyze the data. RabbitMQ connects the backend to a separate email worker for registration and login notifications.
