package com.library.repository;

import com.library.entity.LibrarianAssignment;
import com.library.entity.LibrarianAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibrarianAssignmentRepository extends JpaRepository<LibrarianAssignment, Long> {

    List<LibrarianAssignment> findByUserId(Long userId);

    List<LibrarianAssignment> findByUserIdAndStatus(Long userId, LibrarianAssignmentStatus status);

    List<LibrarianAssignment> findByLibraryId(Long libraryId);

    List<LibrarianAssignment> findByLibraryIdAndStatus(Long libraryId, LibrarianAssignmentStatus status);

    Optional<LibrarianAssignment> findByUserIdAndLibraryId(Long userId, Long libraryId);

    boolean existsByUserIdAndLibraryIdAndStatus(Long userId, Long libraryId, LibrarianAssignmentStatus status);
}
