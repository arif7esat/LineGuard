# LineGuard

> **End-to-end industrial automation simulator (MVP)** — engineered in **Java 17 / Spring Boot 3**, aligned with Siemens R&D standards.

LineGuard is a learning platform for advanced software engineering, test automation, and CI/CD practices. It implements a realistic production line state machine, a threshold-based alarm engine, a RESTful API, and a full automated test pipeline.

---

## ✨ Features

| Capability | Detail |
|---|---|
| **Production-line state machine** | `IDLE → RUNNING → PAUSED → FAULT → STOPPED`, with guarded transitions |
| **Threshold-based alarm engine** | WARNING / CRITICAL alarms raised automatically when sensor readings exceed configured limits |
| **RESTful API** | Full CRUD + lifecycle events for lines, sensor readings, thresholds, and alarms |
| **Automated test pipeline** | JUnit 5, MockMvc integration tests, GitHub Actions CI |
| **In-memory persistence** | H2 (dev/test), swap-ready for PostgreSQL / MySQL in production |

---

## 🗺 Architecture

```
com.lineguard
├── controller          # REST layer (ProductionLineController, AlarmController)
├── service             # Business logic (ProductionLineService, AlarmService)
├── statemachine        # State machine guard (ProductionLineStateMachine)
├── model               # JPA entities + enums
├── repository          # Spring Data JPA repositories
├── dto                 # Request / response DTOs
└── exception           # GlobalExceptionHandler (RFC 7807 Problem Details)
```

### State machine

```
IDLE ──start──► RUNNING ──pause──► PAUSED
                   │  ▲               │
                   │  └──reset──── FAULT ◄──fault───┘
                   │
                  stop
                   ▼
                STOPPED ──reset──► IDLE
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17** (Temurin recommended)
- **Maven 3.9+**

### Run the application

```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**.  
H2 console: **http://localhost:8080/h2-console** (`jdbc:h2:mem:lineguarddb`, user `sa`, no password).

---

## 📡 API Reference

### Production Lines

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/lines` | Create a production line |
| `GET`  | `/api/lines` | List all production lines |
| `GET`  | `/api/lines/{id}` | Get a production line |
| `POST` | `/api/lines/{id}/start` | Transition → RUNNING |
| `POST` | `/api/lines/{id}/pause` | Transition → PAUSED |
| `POST` | `/api/lines/{id}/resume` | Transition → RUNNING |
| `POST` | `/api/lines/{id}/fault` | Transition → FAULT |
| `POST` | `/api/lines/{id}/reset` | Transition → IDLE |
| `POST` | `/api/lines/{id}/stop` | Transition → STOPPED |

### Sensor Readings

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/lines/{id}/readings` | Submit a sensor reading (triggers alarm evaluation) |
| `GET`  | `/api/lines/{id}/readings` | List readings for a line |

### Alarm Thresholds

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/lines/{id}/thresholds` | Create / update a threshold |
| `GET`  | `/api/lines/{id}/thresholds` | List thresholds for a line |

### Alarms

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/api/alarms` | List all alarms |
| `GET`  | `/api/lines/{id}/alarms` | List alarms for a specific line |
| `POST` | `/api/alarms/{id}/acknowledge` | Acknowledge an alarm |
| `POST` | `/api/alarms/{id}/resolve` | Resolve an alarm |

### Example: full workflow

```bash
# 1. Create a line
curl -X POST http://localhost:8080/api/lines \
  -H 'Content-Type: application/json' \
  -d '{"name":"Assembly-1","description":"Main assembly line"}'

# 2. Configure an alarm threshold for the temperature sensor
curl -X POST http://localhost:8080/api/lines/1/thresholds \
  -H 'Content-Type: application/json' \
  -d '{"sensorName":"temperature","warningThreshold":80,"criticalThreshold":100,"unit":"°C"}'

# 3. Start the line
curl -X POST http://localhost:8080/api/lines/1/start

# 4. Submit a critical sensor reading (alarm raised automatically)
curl -X POST http://localhost:8080/api/lines/1/readings \
  -H 'Content-Type: application/json' \
  -d '{"sensorName":"temperature","value":115.0,"unit":"°C"}'

# 5. Check alarms
curl http://localhost:8080/api/lines/1/alarms

# 6. Acknowledge the alarm
curl -X POST http://localhost:8080/api/alarms/1/acknowledge
```

---

## 🧪 Testing

```bash
mvn test
```

| Suite | Tests | Description |
|-------|-------|-------------|
| `ProductionLineStateMachineTest` | 15 | Unit tests for all valid and guarded state transitions |
| `AlarmServiceTest` | 5 | Unit tests for the threshold-based alarm engine (Mockito) |
| `ProductionLineControllerTest` | 10 | MockMvc integration tests for production line lifecycle and sensor ingestion |
| `AlarmControllerTest` | 4 | MockMvc integration tests for alarm management |

---

## ⚙️ CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push and pull request:

1. Checkout code
2. Set up Java 17 (Temurin)
3. `mvn verify` — build + run all tests
4. Upload Surefire test reports as artifacts (14-day retention)

---

## 🗄 Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `spring.datasource.url` | `jdbc:h2:mem:lineguarddb` | Change to PostgreSQL / MySQL for production |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Use `validate` in production |
| `spring.h2.console.enabled` | `true` | Disable in production |
