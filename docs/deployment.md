# EduConnect — Deployment & Operations Guide
> **College Learning Resource Sharing Portal — Community Service Project (CSP)**

---

## 1. Overview

EduConnect is designed with containerization and cloud-ready infrastructure principles. The platform can be deployed in three configurations:

1. **Local Multi-Container Stack (Recommended for Evaluation):** Docker Compose spinning up MySQL 8.0, Spring Boot backend, and Nginx static frontend server with a single command.
2. **Hybrid Cloud Deployment (Free Tier Friendly):**
   - **Frontend:** Static web host (GitHub Pages, Netlify, or Vercel).
   - **Backend API & Database:** Cloud container host (Render or Railway) connected to managed MySQL.
3. **Enterprise / University Production:** AWS (EC2/ECS + RDS MySQL + S3/EBS) or self-hosted Linux server behind an Nginx reverse proxy with automated Let's Encrypt SSL/TLS certificates.

---

## 2. Option 1: Docker Compose (Local Full-Stack)

The fastest and most consistent way to run the entire EduConnect ecosystem is using Docker Compose.

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows/macOS) or Docker Engine + Docker Compose Plugin (Linux).
- Git installed.

### Step-by-Step Instructions

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Mounica2045/EduConnect.git
   cd EduConnect
   ```

2. **Configure Environment Variables:**
   Copy `.env.example` to `.env`:
   ```bash
   # Windows PowerShell
   Copy-Item .env.example .env

   # Linux / macOS
   cp .env.example .env
   ```
   *(Optional)* Edit `.env` to customize database passwords or JWT secrets. Default values work out of the box for local demonstration.

3. **Build and Launch All Services:**
   ```bash
   docker compose up -d --build
   ```

4. **Verify Running Containers:**
   ```bash
   docker compose ps
   ```
   You should see three healthy services:
   | Service | Container Name | Port Mapping | Status |
   | :--- | :--- | :--- | :--- |
   | `mysql` | `educonnect-mysql` | `127.0.0.1:3306->3306` | Up (healthy) |
   | `backend` | `educonnect-backend` | `127.0.0.1:8080->8080` | Up (healthy) |
   | `frontend` | `educonnect-frontend` | `127.0.0.1:3000->80` | Up |

5. **Access the Applications:**
   - **Frontend Web Portal:** Open [http://localhost:3000](http://localhost:3000)
   - **Backend Health Check:** Open [http://localhost:8080/api/resources](http://localhost:8080/api/resources)

6. **Shutting Down the Stack:**
   ```bash
   # Stop services while preserving database volume
   docker compose down

   # Stop services and remove database volumes (fresh start)
   docker compose down -v
   ```

---

## 3. Option 2: Manual Local Development Setup

If you prefer running services directly on your host machine without Docker:

### A. Start MySQL Database
1. Ensure MySQL 8.0 is running locally on port `3306`.
2. Connect to MySQL via MySQL Workbench or CLI:
   ```sql
   CREATE DATABASE IF NOT EXISTS educonnect_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'educonnect_user'@'localhost' IDENTIFIED BY 'educonnect_pass';
   GRANT ALL PRIVILEGES ON educonnect_db.* TO 'educonnect_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

### B. Run Spring Boot Backend
1. Open a terminal in the `backend/` directory:
   ```bash
   cd backend
   ```
2. Set environment variables (or rely on `application.properties` defaults):
   ```bash
   # Windows PowerShell
   $env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/educonnect_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:SPRING_DATASOURCE_USERNAME="educonnect_user"
   $env:SPRING_DATASOURCE_PASSWORD="educonnect_pass"
   $env:JWT_SECRET="EduConnectSecretKeyForJwtTokenGeneration2025CSPProject"

   # Run with Maven Wrapper
   .\mvnw.cmd spring-boot:run
   ```
   *(On Linux/macOS, use `./mvnw spring-boot:run`)*.
3. The server starts on port `8080`. Flyway automatically executes migrations `V1`, `V2`, and `V3`.

### C. Serve the Frontend
1. Open a terminal in the root repository directory (`EduConnect/`).
2. Run any lightweight HTTP server:
   ```bash
   # Python 3
   python -m http.server 3000

   # Or Node.js npx
   npx serve -l 3000 .
   ```
3. Open `http://localhost:3000`. The frontend will communicate seamlessly with `http://localhost:8080/api`.

---

## 4. Option 3: Cloud Deployment (Production / Demo)

### 4.1 Backend & Database Deployment on Render / Railway

Both [Render](https://render.com) and [Railway](https://railway.app) support Docker-based deployments with managed MySQL databases.

#### Deploying on Render:
1. **Create Managed MySQL Database:**
   - Create a new PostgreSQL or MySQL instance (via Render Marketplace or external provider like Aiven/PlanetScale).
   - Note down the `Host`, `Port`, `Database`, `Username`, and `Password`.
2. **Deploy Backend as a Web Service:**
   - Connect your GitHub repository `https://github.com/Mounica2045/EduConnect`.
   - Specify the **Root Directory** as `backend`.
   - Select **Dockerfile** as the runtime environment.
   - Add the following Environment Variables in the Render dashboard:
     ```env
     SPRING_PROFILES_ACTIVE=prod
     SPRING_DATASOURCE_URL=jdbc:mysql://<cloud-db-host>:<port>/<db-name>?useSSL=true
     SPRING_DATASOURCE_USERNAME=<db-user>
     SPRING_DATASOURCE_PASSWORD=<db-password>
     JWT_SECRET=ProduceAStrong64CharRandomSecretKeyForProductionSecurityUse!
     JWT_EXPIRATION_MS=86400000
     CORS_ALLOWED_ORIGINS=https://your-frontend-domain.netlify.app,https://your-custom-domain.edu
     FILE_UPLOAD_DIR=/app/uploads
     ```
   - Render automatically builds the multi-stage Dockerfile and exposes port `8080`.

#### Deploying on Railway:
1. Create a new Railway project and choose **Deploy from GitHub repo**.
2. Add a **MySQL** plugin service in Railway.
3. Railway injects `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, and `MYSQLDATABASE`.
4. In the backend service variables, configure:
   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false
   SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   JWT_SECRET=SuperSecureKeyGeneratedForProduction2025RailwayDeployment!
   CORS_ALLOWED_ORIGINS=https://your-frontend.netlify.app
   ```

---

### 4.2 Frontend Static Deployment (Netlify / Vercel / GitHub Pages)

Because EduConnect's frontend is standard Vanilla HTML5, CSS3, and ES6 JavaScript with zero compile/bundling steps, it deploys instantly to any static CDN host.

#### Deploying on Netlify:
1. Log in to [Netlify](https://www.netlify.com).
2. Choose **Add new site** > **Import an existing project**.
3. Select GitHub repo: `Mounica2045/EduConnect`.
4. Configure Build Settings:
   - **Base directory:** Leave blank or `/`
   - **Build command:** Leave blank (no build step needed)
   - **Publish directory:** `.` (root)
5. **Configuring Backend API Endpoint:**
   Before publishing, update `js/config.js` or inject `window.EDUCONNECT_API_BASE_URL` in an inline script or snippet:
   ```html
   <script>
     window.EDUCONNECT_API_BASE_URL = 'https://your-backend-api.onrender.com/api';
   </script>
   ```
   Or edit `js/config.js`:
   ```javascript
   API_BASE_URL: (window.EDUCONNECT_API_BASE_URL || 'https://your-backend-api.onrender.com/api').replace(/\/+$/, '')
   ```

---

## 5. Security & Production Checklist

Before exposing EduConnect to campus networks or the public internet:

1. **Rotate Secrets:**
   - Change `JWT_SECRET` to a cryptographically secure 256-bit or 512-bit pseudo-random string.
   - Change the default `admin@college.edu` password immediately after initial login.
2. **Restrict CORS:**
   - Set `CORS_ALLOWED_ORIGINS` to only your trusted frontend domains (e.g., `https://educonnect.college.edu`).
   - Disallow wildcards (`*`) with credentials enabled in production.
3. **Enable HTTPS / SSL:**
   - Never transmit JWT tokens or passwords over plain HTTP.
   - Use Cloudflare, Let's Encrypt (Certbot), or platform-provided TLS certificates.
4. **Persistent File Storage:**
   - Ensure `/app/uploads` is mapped to a persistent Docker volume, AWS EBS volume, or configure AWS S3 / Cloudflare R2 for durable multi-instance object storage.
5. **Database Backups:**
   - Schedule periodic database dumps using `mysqldump`:
     ```bash
     mysqldump -u educonnect_user -p educonnect_db | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
     ```

---

## 6. Health Checks & Troubleshooting

| Symptom | Probable Cause | Resolution |
| :--- | :--- | :--- |
| **`CORS error` in browser console** | Frontend origin not matched in backend CORS allowed origins | Update `CORS_ALLOWED_ORIGINS` in backend environment or `CorsConfig.java`. |
| **`Connection refused` on :8080** | Backend container still starting or MySQL unhealthy | Check logs: `docker compose logs -f backend`. Verify MySQL is responding. |
| **File uploads fail with HTTP 500** | Upload directory does not exist or lacks write permissions | Ensure upload path exists and container user has write permission. |
| **Uploaded files lost after restart** | Ephemeral container filesystem without volume mount | Verify `educonnect-uploads` named volume is mounted to `/app/uploads` in `docker-compose.yml`. |
| **Flyway checksum validation failure** | Migration files modified after being applied | Run `mvn flyway:repair` or drop test database schema during development. |
