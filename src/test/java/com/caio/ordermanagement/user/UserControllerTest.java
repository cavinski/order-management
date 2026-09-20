package com.caio.ordermanagement.user;


import com.caio.ordermanagement.user.dto.CreateUserRequest;
import com.caio.ordermanagement.user.dto.CreateUserResponse;
import com.caio.ordermanagement.user.dto.UpdateUserEmailRequest;
import com.caio.ordermanagement.user.dto.UpdateUserNameRequest;
import com.caio.ordermanagement.user.exceptions.EmailAlreadyInUseException;
import com.caio.ordermanagement.user.exceptions.UserNotFoundException;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @Autowired 
    private ObjectMapper objectMapper;

    @MockitoBean 
    private UserService userService;

    @Test 
    void shouldCreateUser() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            "password"
        );

        CreateUserResponse response = new CreateUserResponse(
            1L,
            "Caio",
            "caio@example.com",
            true,
            Instant.parse("2026-09-14T10:00:00Z")
        );

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Caio"))
            .andExpect(jsonPath("$.email").value("caio@example.com"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.createdAt").value("2026-09-14T10:00:00Z"))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldRejectInvalidUserRequest() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
            "",
            "invalid-email",
            ""
        );

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists())
            .andExpect(jsonPath("$.errors.password").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnConflictWhenEmailIsAlreadyInUse() throws Exception {

        CreateUserRequest request = new CreateUserRequest(
            "Caio",
            "caio@example.com",
            "password"
        );

        when(userService.createUser(any(CreateUserRequest.class)))
            .thenThrow(new EmailAlreadyInUseException(request.email()));

        mockMvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.detail").value("Email already in use: " + request.email()));
    }

    @Test
    void shouldGetUserById() throws Exception {

        Long userId = 1L;

        User user = new User(
            "Caio",
            "caio@example.com",
            "password"
        );

        when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/users/{id}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Caio"))
            .andExpect(jsonPath("$.email").value("caio@example.com"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        
        Long userId = 999L;

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException(userId));

        mockMvc.perform(get("/users/{id}", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("User not found"));
    }

    @Test
    void shouldUpdateUserName() throws Exception {

        Long userId = 1L;

        UpdateUserNameRequest request = new UpdateUserNameRequest("Carlos");

        mockMvc.perform(patch("/users/{id}/name", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(userService).updateUserName(userId, "Carlos");
    }

    @Test
    void shouldRejectInvalidUserName() throws Exception {

        Long userId = 1L;

        UpdateUserNameRequest request = new UpdateUserNameRequest("");

        mockMvc.perform(patch("/users/{id}/name", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNameOfNonexistentUser() throws Exception {

        Long userId = 999L;

        UpdateUserNameRequest request = new UpdateUserNameRequest("Carlos");

        doThrow(new UserNotFoundException(userId)).when(userService).updateUserName(userId, request.name());

        mockMvc.perform(patch("/users/{id}/name", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("User not found"));
    }

    @Test
    void shouldUpdateUserEmail() throws Exception {

        Long userId = 1L;

        UpdateUserEmailRequest request = new UpdateUserEmailRequest("new-email@example.com");

        mockMvc.perform(patch("/users/{id}/email", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNoContent());

        verify(userService).updateUserEmail(userId, "new-email@example.com");
    }

    @Test
    void shouldRejectInvalidUserEmail() throws Exception {

        Long userId = 1L;

        UpdateUserEmailRequest request = new UpdateUserEmailRequest("invalid-email");

        mockMvc.perform(patch("/users/{id}/email", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingEmailOfNonexistentUser() throws Exception {

        Long userId = 999L;

        UpdateUserEmailRequest request = new UpdateUserEmailRequest("new-email@example.com");

        doThrow(new UserNotFoundException(userId)).when(userService).updateUserEmail(userId, request.email());

        mockMvc.perform(patch("/users/{id}/email", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("User not found"));
    }

    @Test
    void shouldReturnConflictWhenUpdatingEmailToAnEmailAlreadyInUse() throws Exception {

        Long userId = 1L;

        UpdateUserEmailRequest request = new UpdateUserEmailRequest("existing@example.com");

        doThrow(new EmailAlreadyInUseException(request.email())).when(userService).updateUserEmail(userId, request.email());

        mockMvc.perform(patch("/users/{id}/email", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.title").value("Conflict"))
            .andExpect(jsonPath("$.detail").value("Email already in use: " + request.email()));
    }

    @Test
    void shouldDeactivateUser() throws Exception {

        Long userId = 1L;

        mockMvc.perform(patch("/users/{id}/deactivation", userId)).andExpect(status().isNoContent());

        verify(userService).deactivateUser(userId);
    }

    @Test
    void shouldReturnNotFoundWhenDeactivatingNonexistentUser() throws Exception {

        Long userId = 999L;

        doThrow(new UserNotFoundException(userId)).when(userService).deactivateUser(userId);

        mockMvc.perform(patch("/users/{id}/deactivation", userId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("User not found"));
    }
}