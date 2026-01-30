package com.artdesign.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String path;
    private String component;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "meta_id")
    private RouteMeta meta;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Route> children;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonBackReference
    private Route parent;

    public Route() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public RouteMeta getMeta() {
        return meta;
    }

    public void setMeta(RouteMeta meta) {
        this.meta = meta;
    }

    public List<Route> getChildren() {
        return children;
    }

    public void setChildren(List<Route> children) {
        this.children = children;
    }

    public Route getParent() {
        return parent;
    }

    public void setParent(Route parent) {
        this.parent = parent;
    }
}
