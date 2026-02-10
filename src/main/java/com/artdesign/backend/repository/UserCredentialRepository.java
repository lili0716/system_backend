package com.artdesign.backend.repository;

import com.artdesign.backend.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    UserCredential findByEmployeeId(String employeeId);
}
