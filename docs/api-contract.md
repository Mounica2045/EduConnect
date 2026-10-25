# EduConnect — REST API Specification & Contract
> **College Learning Resource Sharing Portal — Community Service Project (CSP)**  
> **Source of Truth:** Spring Boot 3.3.4 Production Controllers (`com.educonnect.controller`)  
> **Default Base URL:** `http://localhost:8080/api`

---

## 1. Authentication & Security Conventions

1. **Token Transport:** Stateless JWT Bearer tokens passed via the HTTP header:
   ```http
   Authorization: Bearer <jwt_token>
   ```
2. **Standard Error Schema (`ApiErrorResponse`):**
   ```json
   {
     "status": 400,
     "message": "Descriptive human-readable error explanation",
     "timestamp": "2026-10-25T12:00:00Z",
     "path": "/api/...",
     "errors": {
       "fieldName": "Validation constraint message"
     }
   }
   ```
3. **Role-Based Access Control:**
   - `ROLE_STUDENT`: Standard authenticated student with upload and bookmark privileges.
   - `ROLE_ADMIN`: Academic coordinator/administrator with moderation, user control, and audit privileges.

---

## 2. API Contract Endpoints Table

### Module A — Authentication (`/api/auth`) — *Member 2*

| Method | Endpoint | Access | Request Payload | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | `{ "fullName", "email", "password", "department", "semester" }` | `201 Created` + `{ "message", "user": { ... } }` | `400 Bad Request`, `409 Conflict` |
| `POST` | `/api/auth/login` | Public | `{ "email", "password" }` | `200 OK` + `{ "token", "tokenType": "Bearer", "user": { ... } }` | `400 Bad Request`, `401 Unauthorized` |
| `GET` | `/api/auth/me` | Authenticated | None | `200 OK` + `UserResponse` DTO | `401 Unauthorized` |
| `POST` | `/api/auth/logout` | Authenticated | None | `200 OK` + `{ "message": "Logged out successfully" }` | `401 Unauthorized` |

---

### Module B — User Profile Management (`/api/users`) — *Member 2*

| Method | Endpoint | Access | Request Payload | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Authenticated | None | `200 OK` + `UserResponse` DTO | `401 Unauthorized` |
| `PUT` | `/api/users/me` | Authenticated | `{ "fullName", "department", "semester" }` | `200 OK` + updated `UserResponse` DTO | `400 Bad Request`, `401 Unauthorized` |

---

### Module C — Educational Resources (`/api/resources`) — *Member 3*

| Method | Endpoint | Access | Request Params / Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/resources` | Public | `q`, `category`, `department`, `semester`, `subject`, `fileType`, `sort`, `page`, `size` | `200 OK` + `ResourcePageResponse` | None |
| `GET` | `/api/resources/recent` | Public | `limit` (default: 6) | `200 OK` + `List<ResourceResponse>` | None |
| `GET` | `/api/resources/categories` | Public | None | `200 OK` + `List<CategoryResponse>` | None |
| `GET` | `/api/resources/category/{slug}` | Public | Path: `slug`, Params: `page`, `size` | `200 OK` + `ResourcePageResponse` | None |
| `GET` | `/api/resources/subject/{subject}` | Public | Path: `subject`, Params: `page`, `size` | `200 OK` + `ResourcePageResponse` | None |
| `GET` | `/api/resources/{id}` | Public* | Path: `id` | `200 OK` + `ResourceResponse` | `404 Not Found` (if unapproved/nonexistent) |
| `GET` | `/api/resources/{id}/download`| Public* | Path: `id` | `200 OK` + File stream with `Content-Disposition` | `404 Not Found` |
| `GET` | `/api/resources/mine` | Authenticated | Params: `page`, `size` | `200 OK` + `ResourcePageResponse` | `401 Unauthorized` |
| `POST` | `/api/resources` | Authenticated | `multipart/form-data`: `file`, `title`, `description`, `subject`, `department`, `semester`, `category`, `tags` | `201 Created` + `ResourceResponse` (`status: PENDING`) | `400 Bad Request`, `401 Unauthorized` |
| `PUT` | `/api/resources/{id}` | Owner/Admin | `{ "title", "description", "subject", "department", "semester", "category", "tags" }` | `200 OK` + updated `ResourceResponse` | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `DELETE`| `/api/resources/{id}` | Owner/Admin | Path: `id` | `200 OK` + `{ "success": true, "id" }` | `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |

---

### Module D — Bookmarks (`/api/bookmarks`) — *Member 4*

| Method | Endpoint | Access | Request Params / Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/bookmarks/{resourceId}` | Authenticated | Path: `resourceId` | `201 Created` + `BookmarkResponse` | `400 Bad Request` (unapproved), `401`, `404`, `409 Conflict` (duplicate) |
| `DELETE`| `/api/bookmarks/{resourceId}`| Authenticated | Path: `resourceId` | `200 OK` + `{ "success": true, "resourceId" }` | `401 Unauthorized`, `404 Not Found` |
| `GET` | `/api/bookmarks/{resourceId}/status` | Authenticated | Path: `resourceId` | `200 OK` + `{ "resourceId", "bookmarked": boolean }` | `401 Unauthorized` |
| `GET` | `/api/bookmarks` | Authenticated | Optional Params: `page`, `size` | `200 OK` + `List<ResourceResponse>` (if no page param) OR `ResourcePageResponse` (if page provided) | `401 Unauthorized` |

---

### Module E — Administration & Moderation (`/api/admin`) — *Member 4*

| Method | Endpoint | Access | Request Params / Body | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Admin Only | None | `200 OK` + `AdminDashboardStatsResponse` | `401 Unauthorized`, `403 Forbidden` |
| `GET` | `/api/admin/resources/pending` | Admin Only | Params: `page`, `size` | `200 OK` + `ResourcePageResponse` | `401 Unauthorized`, `403 Forbidden` |
| `GET` | `/api/admin/resources` | Admin Only | Params: `status`, `page`, `size` | `200 OK` + `List<ResourceResponse>` OR `ResourcePageResponse` | `401 Unauthorized`, `403 Forbidden` |
| `PATCH`| `/api/admin/resources/{id}/approve` | Admin Only | Path: `id` | `200 OK` + `ResourceResponse` (`status: APPROVED`) | `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `PATCH`| `/api/admin/resources/{id}/reject` | Admin Only | Path: `id`, Body: `{ "reason": "..." }` | `200 OK` + `ResourceResponse` (`status: REJECTED`) | `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `DELETE`| `/api/admin/resources/{id}` | Admin Only | Path: `id` | `200 OK` + `{ "success": true, "id" }` | `401 Unauthorized`, `403 Forbidden`, `404 Not Found` |
| `GET` | `/api/admin/users` | Admin Only | Params: `page`, `size` | `200 OK` + `Page<UserResponse>` | `401 Unauthorized`, `403 Forbidden` |
| `PATCH`| `/api/admin/users/{id}/status` | Admin Only | Path: `id`, Body: `{ "enabled": boolean }` | `200 OK` + `UserResponse` | `400 Bad Request` (self-disable prevented), `401`, `403`, `404` |
| `GET` | `/api/admin/audit-logs` | Admin Only | Params: `page`, `size` | `200 OK` + `Page<AdminAuditLogResponse>` | `401 Unauthorized`, `403 Forbidden` |

---

### Module F — Community Inquiries (`/api/contact`) — *Member 5*

| Method | Endpoint | Access | Request Payload | Success Response | Error Codes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/contact` | Public | `{ "name", "email", "subject", "message" }` | `200 OK` + `{ "success": true, "message", "sender" }` | `400 Bad Request` (validation errors) |
