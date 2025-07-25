package com.boot1.unit.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import com.boot1.Entities.Role;
import com.boot1.Entities.User;
import com.boot1.dto.request.UserCreationRequest;
import com.boot1.dto.request.UserUpdateRequest;
import com.boot1.dto.response.UserResponse;
import com.boot1.enums.RoleName;
import com.boot1.exception.ApiException;
import com.boot1.mapper.UserMapper;
import com.boot1.repository.RoleRepository;
import com.boot1.repository.UserRepository;
import com.boot1.service.UserService;

@ExtendWith(MockitoExtension.class)
@TestPropertySource("/test.properties")
public class UserServiceTest {
    @InjectMocks
    UserService userService;

    @Mock
    UserMapper userMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RoleRepository roleRepository;

    LocalDate dob;
    UserCreationRequest userCreationRequest;
    UserResponse userResponse;
    User user;
    UserUpdateRequest userUpdateRequest;
    Role userRole;
    Role adminRole;

    @BeforeEach
    void initData() {
        userRole = Role.builder().name(RoleName.USER.name()).build();
        adminRole = Role.builder().name(RoleName.ADMIN.name()).build();
        dob = LocalDate.of(2005, 1, 1);
        userCreationRequest = UserCreationRequest.builder()
                .username("Test")
                .firstName("test")
                .lastName("test")
                .email("user@test@gmail.com")
                .password("Testtest")
                .dob(dob)
                .build();
        userResponse = UserResponse.builder()
                .username("Test")
                .dob(dob)
                .firstName("test")
                .lastName("test")
                .roles(Set.of(adminRole))
                .id("sontaypham")
                .build();
        user = User.builder()
                .username("Test")
                .firstName("test")
                .lastName("test")
                .id("sontaypham")
                .email("user@test@gmail.com")
                .password("Testtest")
                .dob(dob)
                .build();

        userUpdateRequest = UserUpdateRequest.builder()
                .firstName("test")
                .lastName("test")
                .email("user@testupdate@gmail.com")
                .password("Testtest")
                .roles(Set.of("ADMIN"))
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() {
        // given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(userCreationRequest)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("Testtest");
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        // when
        UserResponse userResponse = userService.createUser(userCreationRequest);
        // then
        assertEquals("Test", userResponse.getUsername());
    }

    @Test
    void createUser_usernameExists_fail() {
        // given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        // when then
        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(userCreationRequest));
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void createUser_roleNotFound_fail() {
        // given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userMapper.toUser(userCreationRequest)).thenReturn(user);
        when(roleRepository.findByName(RoleName.USER.name())).thenReturn(Optional.empty());
        // when then
        ApiException exception = assertThrows(ApiException.class, () -> userService.createUser(userCreationRequest));
        assertEquals("Role not found", exception.getMessage());
    }

    @Test
    void getMyProfile_valid_success() {
        // given
        var auth = new UsernamePasswordAuthenticationToken("Test", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByUsername(eq("Test"))).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        // when
        var response = userService.getMyProfile();
        // then
        Assertions.assertThat(response.getUsername()).isEqualTo("Test");
        Assertions.assertThat(response.getId()).isEqualTo("sontaypham");
    }

    @Test
    void getMyI4_invalidRequest_fail() {
        // given
        var auth = new UsernamePasswordAuthenticationToken("Test", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByUsername(eq("Test"))).thenReturn(Optional.empty());
        // when
        ApiException exception = assertThrows(ApiException.class, () -> userService.getMyProfile());
        // then
        Assertions.assertThat(exception.getMessage()).isEqualTo("User not exists");
    }

    @Test
    void updateUser_valid_success() {
        // given
        userUpdateRequest.setPassword("newPassword");
        userUpdateRequest.setRoles(Set.of("ADMIN"));

        when(userRepository.findById("sontaypham")).thenReturn(Optional.of(user));

        doAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    UserUpdateRequest r = invocation.getArgument(1);
                    return null;
                })
                .when(userMapper)
                .updateUser(any(User.class), any(UserUpdateRequest.class));

        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPass");

        when(roleRepository.findByNameIn(Set.of("ADMIN"))).thenReturn(Set.of(adminRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("sontaypham");
            u.setRoles(Set.of(adminRole));
            return u;
        });
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);
        // when
        UserResponse updated = userService.updateUser("sontaypham", userUpdateRequest);

        // then
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertEquals("encodedPass", saved.getPassword());
        assertTrue(saved.getRoles().contains(adminRole));

        assertEquals("sontaypham", updated.getId());
        assertTrue(updated.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")));
    }

    @Test
    void updateUser_passwordNull_rolesValid_success() {
        // given
        userUpdateRequest.setPassword(null);
        userUpdateRequest.setRoles(Set.of("ADMIN"));

        when(userRepository.findById("sontaypham")).thenReturn(Optional.of(user));

        doNothing().when(userMapper).updateUser(any(User.class), any(UserUpdateRequest.class));

        when(roleRepository.findByNameIn(Set.of("ADMIN"))).thenReturn(Set.of(adminRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // when
        UserResponse updated = userService.updateUser("sontaypham", userUpdateRequest);

        // then
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
        assertEquals("sontaypham", updated.getId());
    }

    @Test
    void updateUser_passwordBlank_rolesValid_success() {
        // given
        userUpdateRequest.setPassword("");
        userUpdateRequest.setRoles(Set.of("ADMIN"));

        when(userRepository.findById("sontaypham")).thenReturn(Optional.of(user));

        doNothing().when(userMapper).updateUser(any(User.class), any(UserUpdateRequest.class));

        when(roleRepository.findByNameIn(Set.of("ADMIN"))).thenReturn(Set.of(adminRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // when
        UserResponse updated = userService.updateUser("sontaypham", userUpdateRequest);

        // then
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(any(User.class));
        assertEquals("sontaypham", updated.getId());
    }

    @Test
    void getUsers_validRequest_success() {
        // given
        user.setRoles(Set.of(adminRole));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        // when
        var response = userService.getUsers();
        // then
        assertEquals("Test", response.getFirst().getUsername());
        assertEquals(1, response.size());
    }
}
