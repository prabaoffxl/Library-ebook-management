package com.library.service;

import com.library.dto.*;

import java.util.List;

public interface UserService {
    UserResponseDTO registerUser(RegisterRequest request);
    List<UserResponseDTO> getAllUsers();
    PageResponse<UserProfileResponse> getUsersPaginated(String query, String role, String status, int page, int size, String sortBy, String sortDir);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO createUser(UserDTO dto);
    UserResponseDTO updateUser(Long id, UserDTO dto);
    void deleteUser(Long id);

    UserProfileResponse getUserProfile(String userEmail);
    UserProfileResponse updateUserProfile(String userEmail, UserProfileRequest request);
    UserProfileResponse updateUserStatus(Long id, UserStatusUpdateRequest request, String currentUserEmail);
}
