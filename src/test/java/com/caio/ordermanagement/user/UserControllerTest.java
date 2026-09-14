package com.caio.ordermanagement.user;


import com.caio.ordermanagement.user.dto.CreateUserRequest;
import com.caio.ordermanagement.user.dto.CreateUserResponse;
import com.caio.ordermanagement.user.exceptions.EmailAlreadyInUseException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
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
}