package com.artdesign.backend.service;

import com.artdesign.backend.entity.User;
import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long id);

    User findByUsername(String username);

    User save(User user);

    void deleteById(Long id);

}