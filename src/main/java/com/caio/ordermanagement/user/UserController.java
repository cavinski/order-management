package com.caio.ordermanagement.user;

import com.caio.ordermanagement.user.dto.CreateUserRequest;
import com.caio.ordermanagement.user.dto.CreateUserResponse;
import com.caio.ordermanagement.user.dto.GetUserResponse;
import com.caio.ordermanagement.user.dto.UpdateUserEmailRequest;
import com.caio.ordermanagement.user.dto.UpdateUserNameRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController 
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping 
    public ResponseEntity<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        CreateUserResponse response = userService.createUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetUserResponse> getUserById(@PathVariable Long id) {

        User user = userService.getUserById(id);

        GetUserResponse response = new GetUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isActive(),
            user.getCreatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/name") 
    @ResponseStatus(HttpStatus.NO_CONTENT) 
    public void updateUserName(@PathVariable Long id, @Valid @RequestBody UpdateUserNameRequest request) {

        userService.updateUserName(id, request.name());
    }

    @PatchMapping("/{id}/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserEmail(@PathVariable Long id, @Valid @RequestBody UpdateUserEmailRequest request) {

        userService.updateUserEmail(id, request.email());
    }

    @PatchMapping("/{id}/deactivation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable Long id) {

        userService.deactivateUser(id);
    }

}