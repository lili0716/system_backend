package com.artdesign.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

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
    private Boolean alwaysShow;

    @ElementCollection
    @CollectionTable(name = "route_meta_roles", joinColumns = @JoinColumn(name = "meta_id"))
    @Column(name = "role")
    private List<String> roles;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "meta_id")
    private List<AuthItem> authList;

    public RouteMeta() {
    }

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getShowBadge() {
        return showBadge;
    }

    public void setShowBadge(Boolean showBadge) {
        this.showBadge = showBadge;
    }

    public String getShowTextBadge() {
        return showTextBadge;
    }

    public void setShowTextBadge(String showTextBadge) {
        this.showTextBadge = showTextBadge;
    }

    public Boolean getIsHide() {
        return isHide;
    }

    public void setIsHide(Boolean isHide) {
        this.isHide = isHide;
    }

    public Boolean getIsHideTab() {
        return isHideTab;
    }

    public void setIsHideTab(Boolean isHideTab) {
        this.isHideTab = isHideTab;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Boolean getIsIframe() {
        return isIframe;
    }

    public void setIsIframe(Boolean isIframe) {
        this.isIframe = isIframe;
    }

    public Boolean getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public Boolean getIsFirstLevel() {
        return isFirstLevel;
    }

    public void setIsFirstLevel(Boolean isFirstLevel) {
        this.isFirstLevel = isFirstLevel;
    }

    public Boolean getFixedTab() {
        return fixedTab;
    }

    public void setFixedTab(Boolean fixedTab) {
        this.fixedTab = fixedTab;
    }

    public String getActivePath() {
        return activePath;
    }

    public void setActivePath(String activePath) {
        this.activePath = activePath;
    }

    public Boolean getIsFullPage() {
        return isFullPage;
    }

    public void setIsFullPage(Boolean isFullPage) {
        this.isFullPage = isFullPage;
    }

    public Boolean getIsAuthButton() {
        return isAuthButton;
    }

    public void setIsAuthButton(Boolean isAuthButton) {
        this.isAuthButton = isAuthButton;
    }

    public String getAuthMark() {
        return authMark;
    }

    public void setAuthMark(String authMark) {
        this.authMark = authMark;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public Boolean getAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(Boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<AuthItem> getAuthList() {
        return authList;
    }

    public void setAuthList(List<AuthItem> authList) {
        this.authList = authList;
    }
}
