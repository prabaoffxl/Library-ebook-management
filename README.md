# Automated Municipal Library E-Book Licensing & Digital Asset Management

A scalable, production-ready Spring Boot 3 enterprise application designed for modern municipal libraries. Features secure user authentication (JWT + BCrypt), role-based access control (ADMIN, LIBRARIAN, USER), MySQL data persistence, multi-branch municipal library management, librarian branch assignments & backend branch authorization, user profile management, user status lifecycle (`ACTIVE`, `INACTIVE`, `SUSPENDED`), activity logging, in-application notifications, complete e-book management (CRUD, search, filtering, pagination, sorting), secure file storage for e-books (PDF/EPUB), cover images, and previews, comprehensive digital asset licensing management, automated status calculation & scheduler, a full Digital Asset Management (DAM) system with SHA-256 integrity verification, versioning, duplicate detection, archival, and interactive role-specific frontend dashboards.

---

## 🛠️ Technology Stack

- **Backend**: Java 17+, Spring Boot 3.3.4, Spring Security, Spring Data JPA, Spring Web, Spring Validation, Spring Scheduling
- **Database**: MySQL 8.x (`municipal_library`)
- **Security**: JSON Web Tokens (JWT via JJWT 0.12.6), BCrypt password hashing, SHA-256 Checksum Digest
- **Build Tool**: Apache Maven 3.9+
- **Frontend**: HTML5, CSS3, JavaScript (Fetch API, LocalStorage, Modal UI, Responsive Flexbox/Grid)

---

## 📁 Package & Project Structure

```
c:/java project/
├── .env.example
├── pom.xml
├── README.md
├── database/
│   ├── schema.sql
│   └── data.sql
├── storage/
│   ├── ebooks/
│   ├── covers/
│   └── previews/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── library/
        │           ├── LibraryApplication.java
        │           ├── config/
        │           │   ├── DataInitializer.java
        │           │   ├── LicenseScheduler.java
        │           │   └── SecurityConfig.java
        │           ├── controller/
        │           │   ├── AuthController.java
        │           │   ├── BookController.java
        │           │   ├── DashboardController.java
        │           │   ├── DigitalAssetController.java
        │           │   ├── LibraryController.java
        │           │   ├── LicenseController.java
        │           │   ├── NotificationController.java
        │           │   ├── UserActivityController.java
        │           │   ├── UserController.java
        │           │   └── UserProfileController.java
        │           ├── dto/
        │           │   ├── ApiResponse.java
        │           │   ├── AssetIntegrityResponse.java
        │           │   ├── BookDTO.java
        │           │   ├── DashboardStatsDTO.java
        │           │   ├── DigitalAssetResponse.java
        │           │   ├── LibrarianAssignmentResponse.java
        │           │   ├── LibraryBookResponse.java
        │           │   ├── LibraryRequest.java
        │           │   ├── LibraryResponse.java
        │           │   ├── LicenseDTO.java
        │           │   ├── LicenseRenewRequest.java
        │           │   ├── LicenseRequest.java
        │           │   ├── LicenseResponse.java
        │           │   ├── LicenseSuspendRequest.java
        │           │   ├── LoginRequest.java
        │           │   ├── LoginResponse.java
        │           │   ├── NotificationResponse.java
        │           │   ├── PageResponse.java
        │           │   ├── RegisterRequest.java
        │           │   ├── UserActivityResponse.java
        │           │   ├── UserDTO.java
        │           │   ├── UserProfileRequest.java
        │           │   ├── UserProfileResponse.java
        │           │   ├── UserResponseDTO.java
        │           │   └── UserStatusUpdateRequest.java
        │           ├── entity/
        │           │   ├── ActivityType.java
        │           │   ├── AssetStatus.java
        │           │   ├── AssetType.java
        │           │   ├── Book.java
        │           │   ├── BookStatus.java
        │           │   ├── DigitalAsset.java
        │           │   ├── LibrarianAssignment.java
        │           │   ├── LibrarianAssignmentStatus.java
        │           │   ├── Library.java
        │           │   ├── LibraryBook.java
        │           │   ├── LibraryBookStatus.java
        │           │   ├── LibraryStatus.java
        │           │   ├── License.java
        │           │   ├── LicenseStatus.java
        │           │   ├── LicenseType.java
        │           │   ├── Notification.java
        │           │   ├── NotificationType.java
        │           │   ├── Role.java
        │           │   ├── User.java
        │           │   └── UserStatus.java
        │           ├── exception/
        │           │   ├── BadRequestException.java
        │           │   ├── DuplicateResourceException.java
        │           │   ├── FileStorageException.java
        │           │   ├── GlobalExceptionHandler.java
        │           │   ├── ResourceNotFoundException.java
        │           │   └── UnauthorizedAccessException.java
        │           ├── repository/
        │           │   ├── BookRepository.java
        │           │   ├── DigitalAssetRepository.java
        │           │   ├── LibrarianAssignmentRepository.java
        │           │   ├── LibraryBookRepository.java
        │           │   ├── LibraryRepository.java
        │           │   ├── LicenseRepository.java
        │           │   ├── NotificationRepository.java
        │           │   ├── UserActivityRepository.java
        │           │   └── UserRepository.java
        │           ├── security/
        │           │   ├── CustomUserDetailsService.java
        │           │   ├── JwtAuthenticationFilter.java
        │           │   ├── JwtService.java
        │           │   └── UserPrincipal.java
        │           └── service/
        │               ├── AssetIntegrityService.java
        │               ├── AuthService.java
        │               ├── BookService.java
        │               ├── ChecksumService.java
        │               ├── DashboardService.java
        │               ├── DigitalAssetService.java
        │               ├── FileStorageService.java
        │               ├── LibraryService.java
        │               ├── LicenseService.java
        │               ├── LicenseStatusService.java
        │               ├── NotificationService.java
        │               ├── UserActivityService.java
        │               ├── UserService.java
        │               └── impl/
        │                   ├── AssetIntegrityServiceImpl.java
        │                   ├── AuthServiceImpl.java
        │                   ├── BookServiceImpl.java
        │                   ├── ChecksumServiceImpl.java
        │                   ├── DashboardServiceImpl.java
        │                   ├── DigitalAssetServiceImpl.java
        │                   ├── FileStorageServiceImpl.java
        │                   ├── LibraryServiceImpl.java
        │                   ├── LicenseServiceImpl.java
        │                   ├── LicenseStatusServiceImpl.java
        │                   ├── NotificationServiceImpl.java
        │                   ├── UserActivityServiceImpl.java
        │                   └── UserServiceImpl.java
        └── resources/
            ├── application.properties
            └── static/
                ├── assets.html
                ├── index.html
                ├── librarian-dashboard.html
                ├── libraries.html
                ├── library-details.html
                ├── licenses.html
                ├── login.html
                ├── notifications.html
                ├── profile.html
                ├── user-dashboard.html
                ├── css/
                │   └── style.css
                └── js/
                    └── app.js
```

---

## 🔐 User Roles & Security Authorization Matrix

| Feature / Endpoint | `ADMIN` | `LIBRARIAN` | `USER` |
| :--- | :---: | :---: | :---: |
| Login & Account Registration | ✅ | ✅ | ✅ |
| View Active Libraries & Search Branches | ✅ | ✅ | ✅ |
| View Books / Search & Filter Catalog | ✅ | ✅ | ✅ |
| Download E-Book File (Requires Active User & License) | ✅ | ✅ | ✅ |
| User Profile GET/PUT (`/api/users/me`) | ✅ | ✅ | ✅ |
| User Activity Log History (`/api/users/me/activity`) | ✅ | ✅ | ✅ |
| Internal Notifications (`/api/notifications`) | ✅ | ✅ | ✅ |
| Create/Edit Library Branch | ✅ | ❌ | ❌ |
| Assign Book to Branch / Remove Book from Branch | ✅ | ✅ (Assigned Branch Only) | ❌ |
| Assign Librarian to Branch / Remove Assignment | ✅ | ❌ | ❌ |
| View Branch Librarians | ✅ | ✅ | ❌ |
| Digital Asset Upload / Replace / Cover / Preview | ✅ | ✅ | ❌ |
| Digital Asset File Integrity Verification | ✅ | ✅ | ❌ |
| Digital Asset Archive / Restore / Delete | ✅ | ✅ | ❌ |
| Change User Status (`ACTIVE`, `SUSPENDED`) | ✅ | ❌ | ❌ |

---

## 🏛️ Municipal Library Management & Portal (Version 6)

1. **Multi-Branch Municipal Libraries**:
   - Entities: `Library`, `LibraryBook`, `LibrarianAssignment`.
   - Branch status lifecycle: `ACTIVE`, `INACTIVE`.
   - Unique code constraint (`CML001`, `NZB002`).
2. **Strict Backend Branch Authorization**:
   - Service layer verifies `canLibrarianManageLibrary(userEmail, libraryId)`.
   - Librarians can only manage books, assets, and licenses for branches they are assigned to.
3. **User Profile & Status Enforcement**:
   - Profile fields: `phone`, `address`, `city`, `state`, `pincode`, `profileImage`.
   - Account statuses: `ACTIVE`, `INACTIVE`, `SUSPENDED`.
   - Inactive/suspended users are blocked from authentication.
4. **Activity Logging & In-Application Notifications**:
   - Logs `LOGIN`, `BOOK_VIEW`, `BOOK_DOWNLOAD`, `SEARCH`, `PROFILE_UPDATE` actions.
   - Internal notification system for account status changes and system alerts.

---

## 🔑 Demo Test Credentials

- **Admin Account**: `admin@municipallibrary.com` / `Admin@123`
- **Librarian Account**: `librarian@municipallibrary.com` / `Librarian@123`
- **User Account (Patron)**: `patron@municipallibrary.com` / `Patron@123`

---

## 🗄️ Database Setup (MySQL)

Ensure MySQL server is running on port `3306` with database `municipal_library`:
```sql
CREATE DATABASE IF NOT EXISTS municipal_library;
```
Connection settings in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/municipal_library?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

---

## 🚀 How to Run the Spring Boot Application

Set `JWT_SECRET` to a private Base64-encoded 256-bit key before starting the application. Generate one with `openssl rand -base64 32`; do not commit the generated value.

### 1. Build Project
```bash
mvn clean compile
```

### 2. Run Application
```bash
mvn spring-boot:run
```

Access the UI at: `http://localhost:8080/login.html`

---

## 📡 REST API Reference Table (Version 6 additions)

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/libraries/active` | Authenticated | List active library branches |
| `GET` | `/api/libraries/search` | Authenticated | Search library branches by `query` |
| `GET` | `/api/libraries/{id}` | Authenticated | Get library branch details |
| `POST` | `/api/libraries` | `ADMIN` | Create new library branch |
| `PUT` | `/api/libraries/{id}` | `ADMIN`, `LIBRARIAN` | Update library branch |
| `DELETE` | `/api/libraries/{id}` | `ADMIN` | Delete library branch |
| `POST` | `/api/libraries/{libraryId}/books/{bookId}` | `ADMIN`, `LIBRARIAN` | Assign book to library branch |
| `DELETE` | `/api/libraries/{libraryId}/books/{bookId}` | `ADMIN`, `LIBRARIAN` | Remove book assignment from branch |
| `GET` | `/api/libraries/{libraryId}/books` | Authenticated | Get books assigned to a branch |
| `POST` | `/api/libraries/{libraryId}/librarians/{userId}` | `ADMIN` | Assign librarian to branch |
| `DELETE` | `/api/libraries/{libraryId}/librarians/{userId}` | `ADMIN` | Remove librarian assignment |
| `GET` | `/api/users/me` | Authenticated | Get current user profile |
| `PUT` | `/api/users/me` | Authenticated | Update current user profile |
| `PUT` | `/api/users/{id}/status` | `ADMIN` | Change user account status (`ACTIVE`, `SUSPENDED`) |
| `GET` | `/api/users/me/activity` | Authenticated | View own activity history log |
| `GET` | `/api/activities/recent` | `ADMIN` | View system-wide recent activity log |
| `GET` | `/api/notifications` | Authenticated | Get user notifications |
| `PUT` | `/api/notifications/{id}/read` | Authenticated | Mark notification as read |
| `PUT` | `/api/notifications/read-all` | Authenticated | Mark all notifications as read |

---

## 📖 Version 10 — E-Book Preview, Online Reading & Secure Access

### Key Components:
- **Online E-Book Reader (`reader.html`)**: Web PDF reader displaying book title, author, cover, page counter, prev/next, zoom, fit-to-screen, and fullscreen.
- **Secure File Streaming & Range Requests**: `GET /api/books/{id}/read` and `GET /api/books/{id}/preview` support `HTTP 206 Partial Content` and HTTP Range headers (`Range: bytes=start-end`) without exposing storage paths.
- **Strict License Enforcement**: Validates user account status, book status, active asset (`EBOOK` & `ACTIVE`), active license (`ACTIVE`/`EXPIRING_SOON`), and concurrency limits (`maxUsers`). Returns clean JSON error responses for expired/suspended/cancelled licenses.
- **Reading Progress (`reading_progress`)**: Tracks user's reading session, current page, total pages, progress percentage, and last read timestamp with unique constraint on `(user_id, book_id)`.
- **"My Reading" Portal (`my-reading.html`)**: Personal portal for users to view active e-books, progress bars, and "Continue Reading" shortcuts.

### REST API Reference (Version 10):

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/books/{bookId}/read` | Authenticated | Stream licensed PDF e-book with HTTP Range support |
| `GET` | `/api/books/{bookId}/preview` | Authenticated | Stream e-book preview PDF content |
| `GET` | `/api/books/{bookId}/progress` | Authenticated | Get user's reading progress for a book |
| `PUT` | `/api/books/{bookId}/progress` | Authenticated | Update reading progress (`currentPage`, `totalPages`) |
| `GET` | `/api/users/me/reading-progress` | Authenticated | Get logged-in user's active reading list |



