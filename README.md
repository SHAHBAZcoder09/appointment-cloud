# N4 Clinic Appointment System (`appointment-cloud`)

A distributed clinic appointment booking system built with Spring Cloud Alibaba, TiDB Cloud, Redis, and Vue 3.

## 1. Environment & Tech Stack
- **JDK**: 17 | **Maven**: 3.9.6
- **Spring Boot**: 3.0.2 | **Spring Cloud**: 2022.0.0 | **Spring Cloud Alibaba**: 2022.0.0.0
- **Database**: TiDB Cloud (MySQL-compatible) + MyBatis-Plus 3.5.3.1 (init script: `db/init.sql`)
- **Middleware (Docker)**: Nacos 2.3.2 (`8848`), Redis 7 (`6379`), Sentinel Dashboard 1.8.6 (`8858`)
- **Frontend**: Vue 3 + Vite (`5173`)

## 2. Modules & Ports
| Module | Role | Port |
| --- | --- | --- |
| `appointment-common` | Shared entities (`Patient`, `DoctorSchedule`, `Appointment`, `Result`, `BizException`) | — |
| `service-patient` | Patient login (JWT), registration, Redis cache | `8071`, `8072` |
| `service-schedule` | Doctor schedules, slot deduction/release, Nacos Config (`@RefreshScope`) | `8081` |
| `service-appointment` | Booking consumer (OpenFeign, LoadBalancer, Sentinel) | `8091` |
| `api-gateway` | Unified entry, CORS, global JWT auth filter | `7000` |
| `front-end` | Vue 3 UI (Login/Sign-Up, Doctors, Appointments + Receipt, Admin Demo, Light/Dark Mode) | `5173` |

## 3. Startup Order
1. Start Docker infrastructure: `docker compose up -d`
2. Initialize TiDB Cloud tables using `db/init.sql` and set `export TIDB_PASSWORD="your_password"`
3. In Nacos (`http://localhost:8848/nacos`), create config `service-schedule.yml` (`DEFAULT_GROUP`, YAML):

    booking:
      open: true
      notice: "Clinic booking is OPEN!"

4. Start services inside `appointment-backend`:
   - `service-patient` (8071, optionally 8072 for load balancing)
   - `service-schedule` (8081)
   - `service-appointment` (8091)
   - `api-gateway` (7000)
5. Start frontend inside `appointment-frontend`: `cd /workspace/front-end && npm install && npm run dev`
6. Open `http://localhost:5173`
   - **Admin account**: `Denji` / `12345` (can add doctor schedules and access the Governance Demo tab)
   - **Patient accounts**: Register a new account (10-digit phone validation) to search doctors, book/cancel appointments, and view receipts.

## 4. API Endpoints & Governance Demo Paths
- **Login / Register (Public)**: `POST /patient/login`, `POST /patient/register`
- **Schedule List / Config (Public)**: `GET /schedule/findAll`, `GET /schedule/config`
- **Add Schedule (JWT Protected)**: `POST /schedule/add`
- **Book / Cancel Appointment (JWT Protected)**: `POST /appointment/book/{patientId}/{scheduleId}`, `POST /appointment/cancel/{id}`
- **Gateway Auth Block Demo**: `GET http://localhost:7000/appointment/findAll` (without `Authorization` header -> `401`)
- **Load Balancer Demo**: `GET http://localhost:7000/appointment/lb/port` (alternates `8071` / `8072`)
- **Sentinel Fallback Demo**: `GET http://localhost:7000/appointment/degrade/true` (`503` fallback)
- **Redis Cache Check**: `GET http://localhost:8071/patient/find/1` -> `redis-cli KEYS "*"` shows `patient::1`
