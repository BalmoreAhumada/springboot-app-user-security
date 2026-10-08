package com.springboot.backend.usersapp.users_backend.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.backend.usersapp.users_backend.entities.Role;
import com.springboot.backend.usersapp.users_backend.entities.User;
import com.springboot.backend.usersapp.users_backend.models.IUser;
import com.springboot.backend.usersapp.users_backend.models.UserRequest;
import com.springboot.backend.usersapp.users_backend.repositories.RoleRepository;
import com.springboot.backend.usersapp.users_backend.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> findAll() {
        return ((List<User>) this.userRepository.findAll()).stream().map(user -> {
            boolean admin = user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
            user.setAdmin(admin);
            return user;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<User> findAll(Pageable pageable) {
        return this.userRepository.findAll(pageable).map(user -> {
            boolean admin = user.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
            user.setAdmin(admin);
            return user;
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findById(Long id) {
        return this.userRepository.findById(id);
    }

    @Transactional
    @Override
    public User save(User user) {
        user.setRoles(getRoleOptional(user));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.userRepository.save(user);
    }

    @Override
    public Optional<User> update(UserRequest user, Long id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isPresent()) {
            User userDb = userOptional.orElseThrow();
            userDb.setUsername(user.getUsername());
            userDb.setName(user.getName());
            userDb.setLastname(user.getLastname());
            userDb.setEmail(user.getEmail());

            userDb.setRoles(getRoleOptional(user));
            return Optional.of(userRepository.save(userDb));
        }
        return Optional.empty();
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        this.userRepository.deleteById(id);
    }

    private List<Role> getRoleOptional(IUser user) {
        List<Role> roles = new ArrayList<>();
        Optional<Role> roleOptional = this.roleRepository.findByName("ROLE_USER");
        roleOptional.ifPresent(roles::add);
        if (user.isAdmin()) {
            Optional<Role> roleAdmin = this.roleRepository.findByName("ROLE_ADMIN");
            roleAdmin.ifPresent(roles::add);
        }
        return roles;
    }
}
