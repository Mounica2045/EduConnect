# EduConnect — College Learning Resource Sharing Portal
> **Community Service Project (CSP) — Frontend Repository (Member 1)**  
> **GitHub Repository:** [https://github.com/Mounica2045/EduConnect](https://github.com/Mounica2045/EduConnect)  
> **Maintainer / GitHub:** [@Mounica2045](https://github.com/Mounica2045)  
> **Tagline:** *Learn Together. Grow Together.*

---

## 1. Project Overview

**EduConnect** is an open, student-driven academic resource sharing platform developed as a college Community Service Project (CSP). It solves the problem of academic materials (subject notes, previous examination question papers, laboratory manuals, and programming references) being scattered across fragmented chat groups, expiring links, and isolated drives.

EduConnect centralizes educational resources categorized by **Department**, **Semester**, **Subject**, and **Resource Category**, allowing students to explore, download, bookmark, and contribute study materials.

---

## 2. Technology Stack

- **Frontend:** HTML5, CSS3 Custom Properties (Design Tokens), Vanilla JavaScript (ES6+)
- **Architecture:** Multi-Page Application (MPA) with reusable layout and component modules
- **API Client:** Fetch API with asynchronous request/response processing and JWT authorization headers
- **Backend (Partner Component):** Java Spring Boot 3.x REST API
- **Database (Partner Component):** MySQL relational database accessed via Spring Data JPA
- **Zero Frontend Framework Dependencies:** Built cleanly without React, Angular, Vue, Bootstrap, or Tailwind to satisfy college academic guidelines and guarantee maximum performance and accessibility.

---

## 3. Directory Structure

```
eduConnect/
│
├── index.html                  # 1. Home Page (Hero, Search, Categories, Workflow, Recent, Benefits, CTA)
├── resources.html              # 2. Resource Library (Faceted search, multi-filters, sort, pagination)
├── resource-details.html       # 3. Resource Details (Metadata, download/preview, bookmark, related)
├── login.html                  # 4. Sign In (Authentication, validation, demo credentials helper)
├── register.html               # 5. Registration (Student signup, department/semester, validation)
├── upload.html                 # 6. Upload Resource (Drag-and-drop file upload, size/type checks, honor pledge)
├── bookmarks.html              # 7. Saved Bookmarks (Personal study list, in-page filter, remove bookmark)
├── profile.html                # 8. Student Profile (Account info, contribution history, moderation status)
├── about.html                  # 9. About Page (Problem statement, CSP mission, technical stack)
├── contact.html                # 10. Contact Page (Inquiry form, honest backend notice, FAQs)
├── admin.html                  # 11. Admin Dashboard (Pending moderation queue, approve/reject, delete, stats)
│
├── assets/
│   ├── icons/
│   │   └── favicon.svg         # High-resolution vector brand favicon
│   └── images/
│       └── hero-illustration.svg # Academic EdTech vector illustration
│
├── css/
│   ├── variables.css           # Color tokens (Navy, Teal), typography, shadows, radii
│   ├── base.css                # CSS reset, accessibility focus rings, container, typography
│   ├── layout.css              # Sticky header, desktop navbar, mobile drawer, footer
│   ├── components.css          # Reusable buttons, badges, resource cards, toasts, modals, skeletons
│   ├── forms.css               # Form fields, password visibility toggle, file dropzone, auth cards
│   ├── home.css                # Hero section, category grid, workflow cards, benefits
│   ├── resources.css           # Filter sidebar, toolbar, details layout, admin tables, profile grid
│   └── responsive.css          # Breakpoints (320px mobile to 4K desktop), touch target sizing
│
├── js/
│   ├── config.js               # Centralized configuration (API_BASE_URL, categories, sample seed data)
│   ├── api.js                  # Centralized Fetch API client (GET, POST, PUT, PATCH, DELETE, upload)
│   ├── auth.js                 # Authentication service (JWT token storage, roles, route guards)
│   ├── navigation.js           # Header auth synchronization, active page highlight, mobile menu
│   ├── components.js           # XSS-safe DOM builder, Toast notifications, confirm modals, bookmarks
│   ├── home.js                 # Home search redirection, quick topic chips, recent resource feed
│   ├── resources.js            # Resource library query params, multi-filters, sorting, pagination
│   ├── resource-details.js     # Single resource view, download streaming, related suggestions
│   ├── login.js                # Login form validation, error handling, redirect management
│   ├── register.js             # Registration validation, matching passwords, terms pledge
│   ├── upload.js               # File extension & size validation, drag-and-drop, FormData submission
│   ├── bookmarks.js            # Saved resources fetch, search within bookmarks, sync
│   ├── profile.js              # Student account details, contribution tabs, edit profile
│   ├── contact.js              # Contact inquiry validation and honest API status messaging
│   └── admin.js                # Administrator moderation queue (Approve/Reject/Delete), metrics
│
└── README.md                   # Full documentation, setup instructions, and Spring Boot contract
```

---

## 4. How to Run Locally

You can run EduConnect on any modern web browser using any local HTTP static server:

### Option A: Using Python (Recommended & Built-in)
Open a terminal in the `eduConnect` folder and run:

```bash
# Python 3
python -m http.server 3000
```
Open your browser and navigate to: **`http://localhost:3000`**

### Option B: Using VS Code Live Server
1. Open the `eduConnect` folder in Visual Studio Code.
2. Install the **Live Server** extension (by Ritwick Dey).
3. Right-click `index.html` and select **"Open with Live Server"**.

### Option C: Using Node.js `npx serve`
```bash
npx serve -l 3000 .
```

### Option D: Cloning from GitHub
```bash
git clone https://github.com/Mounica2045/EduConnect.git
cd EduConnect
python -m http.server 3000
```

---

## 5. Pushing to GitHub (`Mounica2045/EduConnect`)

To push this codebase to your GitHub repository:

```bash
# 1. Initialize git (if not already done)
git init

# 2. Add all project files
git add .

# 3. Create your initial commit
git commit -m "Initial commit: EduConnect complete frontend for CSP project"

# 4. Set default branch to main
git branch -M main

# 5. Add your GitHub remote repository
git remote add origin https://github.com/Mounica2045/EduConnect.git

# 6. Push to GitHub
git push -u origin main
```

---

## 6. Spring Boot Backend Integration Contract

The backend is being developed by our partner using **Java Spring Boot 3.x** and **MySQL**. The frontend communicates with it through REST APIs.

### A. Centralized API Base URL Configuration
Located in `js/config.js`:
```javascript
const CONFIG = {
  API_BASE_URL: 'http://localhost:8080/api',
  // ...
};
```

### B. Propose REST Endpoints Table

| Category | Method | Endpoint | Description | Request Body / Params |
| :--- | :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | Register new student | `{ fullName, email, department, semester, password }` |
| **Auth** | `POST` | `/api/auth/login` | Authenticate student/admin | `{ email, password }` &rarr; returns `{ token, user }` |
| **Auth** | `POST` | `/api/auth/logout` | Terminate session | Bearer token in header |
| **Auth** | `GET` | `/api/auth/me` | Current authenticated user | Bearer token in header |
| **Resources** | `GET` | `/api/resources` | Search and filter library | `?q=&category=&department=&semester=&fileType=&sort=&page=&size=` |
| **Resources** | `GET` | `/api/resources/{id}` | Get single resource details | Path variable `id` |
| **Resources** | `POST` | `/api/resources` | Upload document | `multipart/form-data` (`file`, `title`, `category`, `department`, `subject`, `semester`, `description`, `tags`) |
| **Resources** | `GET` | `/api/resources/recent`| Fetch latest approved items | Returns array of recent resources |
| **Bookmarks** | `GET` | `/api/bookmarks` | Fetch user's saved items | Bearer token in header |
| **Bookmarks** | `POST` | `/api/bookmarks/{id}`| Save resource to bookmarks | Path variable `id` |
| **Bookmarks** | `DELETE`| `/api/bookmarks/{id}`| Remove resource bookmark | Path variable `id` |
| **Users** | `GET` | `/api/users/me` | Fetch user profile & uploads | Bearer token in header |
| **Users** | `PUT` | `/api/users/me` | Update student profile details| `{ fullName, department, semester }` |
| **Admin** | `GET` | `/api/admin/resources` | View all resources & queue | Requires `ROLE_ADMIN` |
| **Admin** | `PATCH`| `/api/admin/resources/{id}/approve` | Approve student resource | Requires `ROLE_ADMIN` |
| **Admin** | `PATCH`| `/api/admin/resources/{id}/reject` | Reject student resource | Requires `ROLE_ADMIN` |
| **Admin** | `DELETE`| `/api/admin/resources/{id}` | Permanently delete resource| Requires `ROLE_ADMIN` |
| **Contact** | `POST` | `/api/contact` | Submit student inquiry | `{ name, email, subject, message }` |

### C. Backend CORS Configuration (For Spring Boot Team)
To prevent Cross-Origin Resource Sharing (CORS) blocks during development, add the following configuration class to your Spring Boot project:

```java
package com.educonnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 7. Demonstration & Evaluation Guide (For Reviewers)

For seamless evaluation during our CSP review and college demonstration, EduConnect includes:
1. **Clearly Separated Sample Content:** If the Spring Boot backend is offline or currently building, the application gracefully displays clearly labeled sample resources so reviewers can experience all search, filter, and detail views.
2. **One-Click Demo Credentials:** On `login.html`, click either:
   - **Student Demo:** `student@college.edu` / `Password123!`
   - **Admin Demo:** `admin@college.edu` / `AdminPassword123!`

---

## 8. Deliverables & Integration Status Checklist

- [x] **Design System & Tokens:** Navy & teal color palette, typography scale, shadows, radii in `variables.css`.
- [x] **Base & Accessibility:** WCAG 2.2 AA visible focus rings, screen reader utilities, responsive layouts in `base.css` and `responsive.css`.
- [x] **Shared Navigation & Header:** Sticky header with brand logo, desktop links, auth state synchronization, accessible mobile hamburger drawer in `layout.css` and `navigation.js`.
- [x] **Shared Footer:** Project mission, quick navigation links, copyright statement in `layout.css`.
- [x] **Reusable UI Components:** Buttons, badges, resource cards, toast notifications, confirmation dialogs, skeleton loaders, and empty states in `components.css` and `components.js`.
- [x] **Page 1 (Home Page):** Hero section with graphic illustration, interactive search with chips, 6 category cards, 3-step workflow, recent contributions, platform benefits, and community CTA in `index.html` and `home.js`.
- [x] **Page 2 (Resource Library):** Faceted search, category, department, semester, and file-type filters, sort dropdown, active filter tags, pagination, and URL query synchronization in `resources.html` and `resources.js`.
- [x] **Page 3 (Resource Details):** Breadcrumbs, metadata grid, description, virus-scanned download simulator, bookmarking, and related materials in `resource-details.html` and `resource-details.js`.
- [x] **Page 4 (Login):** Email validation, password toggle, remember-me, demo account shortcuts, and session initialization in `login.html` and `login.js`.
- [x] **Page 5 (Registration):** Full name, college email, department, semester, matching password confirmation, terms pledge, and redirect in `register.html` and `register.js`.
- [x] **Page 6 (Upload Resource):** Title, category, department, subject, semester, description, drag-and-drop file upload with 25MB constraint, and moderation notice in `upload.html` and `upload.js`.
- [x] **Page 7 (Saved Bookmarks):** Saved materials grid, search within bookmarks, remove bookmark with immediate update, and guest login prompt in `bookmarks.html` and `bookmarks.js`.
- [x] **Page 8 (Student Profile):** Profile details, upload/bookmark counters, "My Uploads" status table (Approved/Pending), and profile editing in `profile.html` and `profile.js`.
- [x] **Page 9 (About & Contact):** CSP mission, problem statement, technical stack overview in `about.html`; inquiry form with validation, FAQs, and honest developer notice in `contact.html` and `contact.js`.
- [x] **Page 10 (Admin Dashboard):** Summary metric cards, pending moderation review queue (Approve/Reject), published resources table with search and delete confirmation in `admin.html` and `admin.js`.
- [x] **Centralized API Helper:** Robust Fetch client with timeout, error handling, Bearer tokens, and multipart FormData upload in `api.js`.
- [ ] **Backend Integration:** Full end-to-end integration pending final Spring Boot REST controller deployment by Member 2.
