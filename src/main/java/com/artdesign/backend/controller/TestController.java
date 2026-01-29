package com.artdesign.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/user/list")
    public String getUserList() {
        System.out.println("Test get user list request received");
        return "User list endpoint is working!";
    }

    @GetMapping("/role/list")
    public String getRoleList() {
        System.out.println("Test get role list request received");
        return "Role list endpoint is working!";
    }

}