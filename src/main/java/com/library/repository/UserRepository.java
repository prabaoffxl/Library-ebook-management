package com.library.repository;

import com.library.entity.User;
import com.library.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();

    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> countUsersByStatus();

    @Query("SELECT la.library.id, la.library.name, COUNT(DISTINCT la.user) FROM LibrarianAssignment la WHERE la.status = 'ACTIVE' GROUP BY la.library.id, la.library.name")
    List<Object[]> countUsersByLibrary();
}
