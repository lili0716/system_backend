package com.artdesign.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "auth_items")
public class AuthItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String authMark;

    public AuthItem() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthMark() {
        return authMark;
    }

    public void setAuthMark(String authMark) {
        this.authMark = authMark;
    }
}
