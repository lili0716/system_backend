package com.artdesign.backend.service;

import com.artdesign.backend.entity.Route;

import java.util.List;

public interface RouteService {

    List<Route> getRoutes();

    void initDefaultRoutes();

}
