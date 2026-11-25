# N4 Clinic Appointment System (`appointment-cloud`)

A distributed clinic appointment booking system built with Spring Cloud Alibaba, TiDB Cloud, Redis, DeepSeek AI, and Vue 3.

- **Course:** Architecture-Based Software Development (`基于架构的软件开发`)
- **Class:** Architecture-中国石油大学-留学生-2026
- **Group N4:** SHAHBAZ AHMAD (`231091003`, Leader), RAHMAN MALIK ABDUL (`231091004`), SHAKEEL MUHAMMAD (`231091023`)

## 1. Environment & Tech Stack
- **JDK**: 17 | **Maven**: 3.9.6
- **Spring Boot**: 3.0.2 | **Spring Cloud**: 2022.0.0 | **Spring Cloud Alibaba**: 2022.0.0.0
- **Database**: TiDB Cloud (MySQL-compatible) + MyBatis-Plus 3.5.3.1 (init script: `db/init.sql`)
- **Middleware (Docker)**: Nacos 2.3.2 (`8848`), Redis 7 (`6379`), Sentinel Dashboard 1.8.6 (`8858`)
- **AI Integration**: DeepSeek Chat API (`POST /appointment/ai/recommend`) with Sentinel fallback
- **Frontends**: Two independent Vue 3 + Vite portals (`front-end` on `5173`, `admin-portal` on `5174`)

## 2. Modules & Ports
| Module | Role | Port |
| --- | --- | --- |
| `appointment-common` | Shared entities (`Patient`, `DoctorSchedule`, `Appointment`, `Result`, `BizException`) | — |
| `service-patient` | Patient login (JWT), 10-digit phone registration, Redis cache (`@Cacheable`) | `8071`, `8072` |
| `service-schedule` | Doctor schedules, slot deduction/release, Redis cache (`@Cacheable` / `@CacheEvict`), Nacos Config (`@RefreshScope`) | `8081` |
| `service-appointment` | Booking consumer (OpenFeign, LoadBalancer, Sentinel `429`/`503`, DeepSeek AI triage) | `8091` |
| `api-gateway` | Unified entry, CORS, global JWT auth filter | `7000` |
| `front-end` | Patient Portal (Login/Register, AI Triage, Doctors, My Bookings + Receipt Slip) | `5173` |
| `admin-portal` | Operations & Admin Console (All Bookings, Specialist Management, Governance Suite) | `5174` |

## 3. Startup Order
1. Start Docker infrastructure: `docker compose up -d`
2. Initialize TiDB Cloud tables using `db/init.sql`, and export environment variables:

    export TIDB_PASSWORD="your_tidb_password"
    export DEEPSEEK_API_KEY="your_deepseek_api_key"

3. In Nacos (`http://localhost:8848/nacos`), create config `service-schedule.yml` (`DEFAULT_GROUP`, YAML):

    booking:
      open: true
      notice: "Clinic booking is OPEN!"

4. Start backend services inside `appointment-backend`:
   - `service-patient` (`8071`, optionally `8072` for LoadBalancer demo)
   - `service-schedule` (`8081`)
   - `service-appointment` (`8091`)
   - `api-gateway` (`7000`)
5. Start both frontends inside `appointment-frontend`:
   - Patient Portal: `cd /workspace/front-end && npm install && npm run dev` (`http://localhost:5173`)
   - Admin Console: `cd /workspace/admin-portal && npm install && npm run dev` (`http://localhost:5174`)
6. Demo Accounts:
   - **Admin (`:5174` & `:5173`)**: `Denji` / `12345`
   - **Patients (`:5173`)**: Register a new account with a 10-digit phone number.

## 4. API Endpoints & Governance Demo Paths
- **Login / Register (Public)**: `POST /patient/login`, `POST /patient/register`
- **Schedule List / Config (Public)**: `GET /schedule/findAll`, `GET /schedule/config`
- **Add Schedule (JWT Protected)**: `POST /schedule/add`
- **Book / Cancel Appointment (JWT Protected)**: `POST /appointment/book/{patientId}/{scheduleId}`, `POST /appointment/cancel/{id}`
- **AI Doctor Recommender (JWT + Sentinel)**: `POST /appointment/ai/recommend`
- **Gateway Auth Block Demo**: `GET http://localhost:7000/appointment/findAll` (without `Authorization` header -> `401`)
- **Load Balancer Demo**: `GET http://localhost:7000/appointment/lb/port` (alternates `8071` / `8072`)
- **Sentinel Rate Limit (`429`)**: Rapidly trigger `POST /appointment/book/...` (> 2 req/s) or use **2. Sentinel Burst** in Admin Console
- **Sentinel Fallback (`503`)**: `GET http://localhost:7000/appointment/degrade/true`
- **Redis Cache Check**: `redis-cli KEYS "*"` shows `patient::1` and `schedules::all`
