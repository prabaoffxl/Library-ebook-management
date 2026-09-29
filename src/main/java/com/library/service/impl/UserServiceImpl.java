package com.library.service.impl;

import com.library.dto.*;
import com.library.entity.ActivityType;
import com.library.entity.NotificationType;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.entity.UserStatus;
import com.library.exception.BadRequestException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.UserRepository;
import com.library.service.NotificationService;
import com.library.service.UserActivityService;
import com.library.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserActivityService userActivityService;
    private final NotificationService notificationService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserActivityService userActivityService,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userActivityService = userActivityService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER); // Default role is always USER
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        notificationService.createNotification(
                savedUser.getId(),
                "Account Created Successfully",
                "Welcome to Municipal Library E-Book Services! Explore our digital collections and library branches.",
                NotificationType.ACCOUNT
        );

        return mapToResponseDTO(savedUser);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PageResponse<UserProfileResponse> getUsersPaginated(String query, String roleStr, String statusStr, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<User> spec = Specification.where(null);

        if (query != null && !query.trim().isEmpty()) {
            String q = "%" + query.trim().toLowerCase() + "%";
            spec = spec.and((root, qry, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), q),
                    cb.like(cb.lower(root.get("email")), q),
                    cb.like(cb.lower(root.get("phone")), q),
                    cb.like(cb.lower(root.get("city")), q)
            ));
        }

        if (roleStr != null && !roleStr.trim().isEmpty()) {
            try {
                Role role = Role.valueOf(roleStr.trim().toUpperCase());
                spec = spec.and((root, qry, cb) -> cb.equal(root.get("role"), role));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid user role: " + roleStr);
            }
        }

        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                UserStatus status = UserStatus.valueOf(statusStr.trim().toUpperCase());
                spec = spec.and((root, qry, cb) -> cb.equal(root.get("status"), status));
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid user status: " + statusStr);
            }
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<UserProfileResponse> content = userPage.getContent().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(UserDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password cannot be empty for user creation");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole() != null ? dto.getRole() : Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        return mapToResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + dto.getEmail());
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToResponseDTO(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserProfileResponse getUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));
        return mapToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String userEmail, UserProfileRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPincode(request.getPincode());
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        User saved = userRepository.save(user);

        userActivityService.logActivity(userEmail, ActivityType.PROFILE_UPDATE, null, null, "Updated personal profile information");

        return mapToProfileResponse(saved);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserStatus(Long id, UserStatusUpdateRequest request, String currentUserEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setStatus(request.getStatus());
        User saved = userRepository.save(user);

        notificationService.createNotification(
                user.getId(),
                "Account Status Updated",
                "Your account status has been updated to: " + request.getStatus().name(),
                NotificationType.ACCOUNT
        );

        userActivityService.logActivity(currentUserEmail, ActivityType.PROFILE_UPDATE, null, null, "Updated user #" + id + " status to " + request.getStatus().name());

        return mapToProfileResponse(saved);
    }

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse res = new UserProfileResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setRole(user.getRole());
        res.setPhone(user.getPhone());
        res.setAddress(user.getAddress());
        res.setCity(user.getCity());
        res.setState(user.getState());
        res.setPincode(user.getPincode());
        res.setProfileImage(user.getProfileImage());
        res.setStatus(user.getStatus());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }
}
