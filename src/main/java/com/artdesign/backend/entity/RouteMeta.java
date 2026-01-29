package com.artdesign.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "route_meta")
public class RouteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String icon;
    private Boolean showBadge;
    private String showTextBadge;
    private Boolean isHide;
    private Boolean isHideTab;
    private String link;
    private Boolean isIframe;
    private Boolean keepAlive;
    private Boolean isFirstLevel;
    private Boolean fixedTab;
    private String activePath;
    private Boolean isFullPage;
    private Boolean isAuthButton;
    private String authMark;
    private String parentPath;

    @ElementCollection
    @CollectionTable(name = "route_meta_roles", joinColumns = @JoinColumn(name = "meta_id"))
    @Column(name = "role")
    private List<String> roles;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "meta_id")
    private List<AuthItem> authList;

}
