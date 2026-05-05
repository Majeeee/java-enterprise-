# SCB Flyttningsstatistik API

Ett REST API byggt med Spring Boot som exponerar SCB:s flyttningsstatistik 1997–2024 med filtrering på region, kön och ålder. Frontend byggd med React + Vite.

## Teknologier

- **Backend**: Spring Boot 3, Spring Security, JWT, JPA, PostgreSQL
- **Frontend**: React 18, Vite, React Router v6, Axios
- **Meddelandekö**: RabbitMQ
- **Email**: Spring Mail via microservice
- **Container**: Docker + Docker Compose

## Kom igång

### Krav
- Java 21
- Node.js 20+
- Docker + Docker Compose

### Kör lokalt (utan Docker)

```bash
# Backend
cd backend
./mvnw spring-boot:run

# Frontend (nytt terminalfönster)
cd frontend
npm install
npm run dev
```

### Kör med Docker

```bash
docker-compose up --build
```

Frontend: http://localhost:5173  
Backend API: http://localhost:8080  
RabbitMQ UI: http://localhost:15672 (guest/guest)

## API-endpoints

| Metod | URL | Åtkomst | Beskrivning |
|-------|-----|---------|-------------|
| POST | /api/auth/register | Public | Registrera ny användare |
| POST | /api/auth/login | Public | Logga in |
| POST | /api/auth/logout | Autentiserad | Logga ut |
| GET | /api/migrations | Autentiserad | Hämta flyttningar med filter |
| GET | /api/migrations/regions | Autentiserad | Lista alla regioner |
| GET | /api/admin/users | ADMIN | Lista alla användare |
| PUT | /api/admin/users/{id}/enable | ADMIN | Aktivera användarkonto |
| DELETE | /api/admin/users/{id} | ADMIN | Ta bort användare |

### Filter-parametrar för /api/migrations

```
GET /api/migrations?region=01&gender=men&age=20-24&year=2020
```

Alla parametrar är valfria.

## Säkerhet

- JWT lagras i HttpOnly-cookie (ej localStorage)
- Lösenord hashas med BCrypt
- Nya konton får `isEnabled=false` tills admin aktiverar
- CSRF-skydd aktiverat
- CORS konfigurerat för frontend-origin

## Projektstruktur

```
java-enterprise-/
├── backend/          Spring Boot API
├── frontend/         React SPA
├── email-service/    RabbitMQ email-microservice
├── docker-compose.yml
└── README.md
```
