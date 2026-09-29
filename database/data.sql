-- Municipal Library Database Seed Data (Version 1, 2, 3 & 4)

USE municipal_library;

-- Users (Passwords are BCrypt encoded. Plain text versions: Admin@123, Librarian@123, Patron@123)
INSERT INTO users (id, name, email, password, role, created_at, updated_at) VALUES
(1, 'System Administrator', 'admin@municipallibrary.com', '$2a$10$w8.Bw5bF1YI8U5cW.5n/eu8M0X3fE/x/Jq0T9uV4z1n4e0W5H.0Oi', 'ADMIN', NOW(), NOW()),
(2, 'Senior Librarian', 'librarian@municipallibrary.com', '$2a$10$w8.Bw5bF1YI8U5cW.5n/eu8M0X3fE/x/Jq0T9uV4z1n4e0W5H.0Oi', 'LIBRARIAN', NOW(), NOW()),
(3, 'Municipal Patron', 'patron@municipallibrary.com', '$2a$10$w8.Bw5bF1YI8U5cW.5n/eu8M0X3fE/x/Jq0T9uV4z1n4e0W5H.0Oi', 'USER', NOW(), NOW())
ON DUPLICATE KEY UPDATE email=email;

-- Books
INSERT INTO books (id, title, author, isbn, category, publisher, publication_year, language, description, status, created_at, updated_at) VALUES
(1, 'Thirukkural - Translation & Commentary', 'Thiruvalluvar', '978-8123456789', 'Tamil Literature', 'Southern Classic Press', 2020, 'Tamil', 'A comprehensive Tamil ethics, governance, and philosophy classic with English annotations.', 'AVAILABLE', NOW(), NOW()),
(2, 'Clean Code Architecture in Java', 'Robert C. Martin', '978-0132350884', 'Computer Science', 'Prentice Hall', 2021, 'English', 'A handbook of agile software craftsmanship and maintainable architectural designs.', 'AVAILABLE', NOW(), NOW()),
(3, 'Municipal Digital Asset Management Guide', 'Elena Rostova', '978-1484256251', 'Technology', 'City Infrastructure Press', 2022, 'English', 'Best practices for public digital asset indexing, preservation, and e-book licensing.', 'AVAILABLE', NOW(), NOW()),
(4, 'History of Municipal Governance in South India', 'K. A. Nilakanta Sastri', '978-0195606867', 'History', 'Oxford University Press', 2018, 'English', 'In-depth historical study of local administrative institutions and public archives.', 'AVAILABLE', NOW(), NOW()),
(5, 'Modern Operating Systems Principles', 'Andrew S. Tanenbaum', '978-0133591620', 'Computer Science', 'Pearson Education', 2019, 'English', 'Fundamental concepts of operating system architecture, process scheduling, and security.', 'AVAILABLE', NOW(), NOW())
ON DUPLICATE KEY UPDATE isbn=isbn;

-- Libraries
INSERT INTO libraries (id, name, code, address, city, district, state, pincode, email, phone, status, created_at, updated_at) VALUES
(1, 'Central Municipal Library', 'CML001', '100 Heritage Square, Civil Lines', 'Chennai', 'Chennai', 'Tamil Nadu', '600001', 'central@municipallibrary.gov.in', '+91 44 2530 1100', 'ACTIVE', NOW(), NOW()),
(2, 'North Zone Branch Library', 'NZB002', '45 Anna Salai, North Sector', 'Chennai', 'Chennai', 'Tamil Nadu', '600032', 'north@municipallibrary.gov.in', '+91 44 2530 2200', 'ACTIVE', NOW(), NOW()),
(3, 'South District Reference Library', 'SDR003', '78 Knowledge Park, Guindy', 'Chennai', 'Chennai', 'Tamil Nadu', '600025', 'south@municipallibrary.gov.in', '+91 44 2530 3300', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE code=code;

-- Library Books (Assignments)
INSERT INTO library_books (id, library_id, book_id, availability_status, added_at, updated_at) VALUES
(1, 1, 1, 'AVAILABLE', NOW(), NOW()),
(2, 1, 2, 'AVAILABLE', NOW(), NOW()),
(3, 1, 3, 'AVAILABLE', NOW(), NOW()),
(4, 2, 1, 'AVAILABLE', NOW(), NOW()),
(5, 2, 4, 'AVAILABLE', NOW(), NOW()),
(6, 3, 2, 'AVAILABLE', NOW(), NOW()),
(7, 3, 5, 'AVAILABLE', NOW(), NOW())
ON DUPLICATE KEY UPDATE availability_status=availability_status;

-- Librarian Assignments
INSERT INTO librarian_assignments (id, user_id, library_id, status, assigned_at) VALUES
(1, 2, 1, 'ACTIVE', NOW()),
(2, 2, 2, 'ACTIVE', NOW())
ON DUPLICATE KEY UPDATE status=status;

-- Notifications
INSERT INTO notifications (id, user_id, title, message, type, is_read, created_at) VALUES
(1, 3, 'Welcome to Municipal Library', 'Your account has been successfully created. Explore our digital collection and e-books.', 'SYSTEM', FALSE, NOW()),
(2, 3, 'New E-Book Added', 'Thirukkural - Translation & Commentary is now available for download.', 'BOOK', FALSE, NOW())
ON DUPLICATE KEY UPDATE title=title;
