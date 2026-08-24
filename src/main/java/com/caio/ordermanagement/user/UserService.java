package com.caio.ordermanagement.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String name, String email, String password) {

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        User user = new User(name, email, password);

        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        user.deactivate();
    }
}
