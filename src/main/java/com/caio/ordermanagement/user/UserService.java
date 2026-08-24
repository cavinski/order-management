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

    @Transactional
    public void updateUserName(Long id, String name) {

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.updateName(name);
    }

    @Transactional
    public void updateUserEmail(Long id, String email) {

        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        user.updateEmail(email);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    }
}
