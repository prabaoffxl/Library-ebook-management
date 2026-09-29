package com.library.config;

import com.library.entity.*;
import com.library.repository.BookRepository;
import com.library.repository.LicenseRepository;
import com.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final LicenseRepository licenseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, BookRepository bookRepository, LicenseRepository licenseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.licenseRepository = licenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default users if empty
        if (userRepository.count() == 0) {
            User admin = new User(null, "System Administrator", "admin@municipallibrary.com", passwordEncoder.encode("Admin@123"), Role.ADMIN);
            User librarian = new User(null, "Senior Librarian", "librarian@municipallibrary.com", passwordEncoder.encode("Librarian@123"), Role.LIBRARIAN);
            User user = new User(null, "Municipal Patron", "patron@municipallibrary.com", passwordEncoder.encode("Patron@123"), Role.USER);

            userRepository.save(admin);
            userRepository.save(librarian);
            userRepository.save(user);
        }

        // Seed default books if empty
        if (bookRepository.count() == 0) {
            Book book1 = new Book();
            book1.setTitle("Thirukkural - Translation & Commentary");
            book1.setAuthor("Thiruvalluvar");
            book1.setIsbn("978-8123456789");
            book1.setCategory("Tamil Literature");
            book1.setPublisher("Southern Classic Press");
            book1.setPublicationYear(2020);
            book1.setLanguage("Tamil");
            book1.setDescription("A comprehensive Tamil ethics, governance, and philosophy classic with English annotations.");
            book1.setStatus(BookStatus.AVAILABLE);

            Book book2 = new Book();
            book2.setTitle("Clean Code Architecture in Java");
            book2.setAuthor("Robert C. Martin");
            book2.setIsbn("978-0132350884");
            book2.setCategory("Computer Science");
            book2.setPublisher("Prentice Hall");
            book2.setPublicationYear(2021);
            book2.setLanguage("English");
            book2.setDescription("A handbook of agile software craftsmanship and maintainable architectural designs.");
            book2.setStatus(BookStatus.AVAILABLE);

            Book book3 = new Book();
            book3.setTitle("Municipal Digital Asset Management Guide");
            book3.setAuthor("Elena Rostova");
            book3.setIsbn("978-1484256251");
            book3.setCategory("Technology");
            book3.setPublisher("City Infrastructure Press");
            book3.setPublicationYear(2022);
            book3.setLanguage("English");
            book3.setDescription("Best practices for public digital asset indexing, preservation, and e-book licensing.");
            book3.setStatus(BookStatus.AVAILABLE);

            Book savedBook1 = bookRepository.save(book1);
            Book savedBook2 = bookRepository.save(book2);
            Book savedBook3 = bookRepository.save(book3);

            // Seed default licenses if empty
            if (licenseRepository.count() == 0) {
                License lic1 = new License();
                lic1.setBook(savedBook1);
                lic1.setLicenseKey("LIC-2025-0001");
                lic1.setLicenseType(LicenseType.UNLIMITED);
                lic1.setProvider("Tamil Heritage Digital Publishing");
                lic1.setPurchaseDate(LocalDate.of(2025, 1, 1));
                lic1.setStartDate(LocalDate.of(2025, 1, 1));
                lic1.setEndDate(LocalDate.of(2027, 12, 31));
                lic1.setMaxUsers(9999);
                lic1.setCurrentUsers(12);
                lic1.setPrice(new BigDecimal("12000.00"));
                lic1.setCurrency("INR");
                lic1.setTerms("Public municipal library access license");
                lic1.setStatus(LicenseStatus.ACTIVE);

                License lic2 = new License();
                lic2.setBook(savedBook2);
                lic2.setLicenseKey("LIC-2025-0002");
                lic2.setLicenseType(LicenseType.MULTI_USER);
                lic2.setProvider("Pearson Digital Library Services");
                lic2.setPurchaseDate(LocalDate.of(2025, 2, 15));
                lic2.setStartDate(LocalDate.of(2025, 3, 1));
                lic2.setEndDate(LocalDate.of(2028, 2, 28));
                lic2.setMaxUsers(50);
                lic2.setCurrentUsers(4);
                lic2.setPrice(new BigDecimal("18500.00"));
                lic2.setCurrency("INR");
                lic2.setTerms("Multi-user concurrent institutional license");
                lic2.setStatus(LicenseStatus.ACTIVE);

                License lic3 = new License();
                lic3.setBook(savedBook3);
                lic3.setLicenseKey("LIC-2025-0003");
                lic3.setLicenseType(LicenseType.SINGLE_USER);
                lic3.setProvider("Urban Infrastructure Press");
                lic3.setPurchaseDate(LocalDate.of(2025, 5, 10));
                lic3.setStartDate(LocalDate.of(2025, 6, 1));
                lic3.setEndDate(LocalDate.of(2026, 10, 15));
                lic3.setMaxUsers(1);
                lic3.setCurrentUsers(0);
                lic3.setPrice(new BigDecimal("4500.00"));
                lic3.setCurrency("INR");
                lic3.setTerms("Single seat municipal library archival license");
                lic3.setStatus(LicenseStatus.EXPIRING_SOON);

                licenseRepository.save(lic1);
                licenseRepository.save(lic2);
                licenseRepository.save(lic3);
            }
        }
    }
}
