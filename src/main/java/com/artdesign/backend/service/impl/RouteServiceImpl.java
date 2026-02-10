package com.artdesign.backend.service.impl;

import com.artdesign.backend.entity.AuthItem;
import com.artdesign.backend.entity.Route;
import com.artdesign.backend.entity.RouteMeta;
import com.artdesign.backend.repository.RouteRepository;
import com.artdesign.backend.service.RouteService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;

    public RouteServiceImpl(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Route> getRoutes() {
        List<Route> routes = routeRepository.findByParentIsNull();
        if (routes.isEmpty()) {
            initDefaultRoutes();
            routes = routeRepository.findByParentIsNull();
        }
        sortRoutes(routes);
        return routes;
    }

    private void sortRoutes(List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            return;
        }
        routes.sort((r1, r2) -> {
            int sort1 = (r1.getMeta() != null && r1.getMeta().getSort() != null) ? r1.getMeta().getSort() : 0;
            int sort2 = (r2.getMeta() != null && r2.getMeta().getSort() != null) ? r2.getMeta().getSort() : 0;
            return Integer.compare(sort1, sort2);
        });
        for (Route route : routes) {
            sortRoutes(route.getChildren());
        }
    }

    @Override
    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public Route getRouteById(Long id) {
        return routeRepository.findById(id).orElse(null);
    }

    @Override
    public void initDefaultRoutes() {
        // 仪表盘路由
        Route dashboardRoute = createRoute("Dashboard", "/dashboard", "/index/index", null);
        RouteMeta dashboardMeta = createRouteMeta("menus.dashboard.title", "ri:pie-chart-line", null);
        dashboardMeta.setRoles(List.of("R_SUPER", "R_ADMIN"));
        dashboardRoute.setMeta(dashboardMeta);

        Route consoleRoute = createRoute("Console", "console", "/dashboard/console", dashboardRoute);
        RouteMeta consoleMeta = createRouteMeta("menus.dashboard.console", null, null);
        consoleMeta.setKeepAlive(false);
        consoleMeta.setFixedTab(true);
        consoleRoute.setMeta(consoleMeta);
        dashboardRoute.setChildren(List.of(consoleRoute));

        // 系统管理路由
        Route systemRoute = createRoute("System", "/system", "/index/index", null);
        RouteMeta systemMeta = createRouteMeta("menus.system.title", "ri:settings-5-line", null);
        systemMeta.setRoles(List.of("R_SUPER", "R_ADMIN"));
        systemRoute.setMeta(systemMeta);

        Route menuRoute = createRoute("Menu", "menu", "/system/menu", systemRoute);
        RouteMeta menuMeta = createRouteMeta("menus.system.menu", null, null);
        menuRoute.setMeta(menuMeta);

        Route roleRoute = createRoute("Role", "role", "/system/role", systemRoute);
        RouteMeta roleMeta = createRouteMeta("menus.system.role", null, null);
        roleRoute.setMeta(roleMeta);

        Route userRoute = createRoute("User", "user", "/system/user", systemRoute);
        RouteMeta userMeta = createRouteMeta("menus.system.user", null, null);
        userRoute.setMeta(userMeta);

        systemRoute.setChildren(List.of(menuRoute, roleRoute, userRoute));

        // 结果页路由
        Route resultRoute = createRoute("Result", "/result", "/index/index", null);
        RouteMeta resultMeta = createRouteMeta("menus.result.title", "ri:check-double-line", null);
        resultRoute.setMeta(resultMeta);

        Route successRoute = createRoute("Success", "success", "/result/success", resultRoute);
        RouteMeta successMeta = createRouteMeta("menus.result.success", null, null);
        successRoute.setMeta(successMeta);

        Route failRoute = createRoute("Fail", "fail", "/result/fail", resultRoute);
        RouteMeta failMeta = createRouteMeta("menus.result.fail", null, null);
        failRoute.setMeta(failMeta);

        resultRoute.setChildren(List.of(successRoute, failRoute));

        // 异常页路由
        Route exceptionRoute = createRoute("Exception", "/exception", "/index/index", null);
        RouteMeta exceptionMeta = createRouteMeta("menus.exception.title", "ri:alert-line", null);
        exceptionRoute.setMeta(exceptionMeta);

        Route error403Route = createRoute("Exception403", "403", "/exception/403", exceptionRoute);
        RouteMeta error403Meta = createRouteMeta("menus.exception.403", null, null);
        error403Route.setMeta(error403Meta);

        Route error404Route = createRoute("Exception404", "404", "/exception/404", exceptionRoute);
        RouteMeta error404Meta = createRouteMeta("menus.exception.404", null, null);
        error404Route.setMeta(error404Meta);

        Route error500Route = createRoute("Exception500", "500", "/exception/500", exceptionRoute);
        RouteMeta error500Meta = createRouteMeta("menus.exception.500", null, null);
        error500Route.setMeta(error500Meta);

        exceptionRoute.setChildren(List.of(error403Route, error404Route, error500Route));

        // 考勤管理路由
        Route attendanceRoute = createRoute("Attendance", "/attendance", "/index/index", null);
        RouteMeta attendanceMeta = createRouteMeta("menus.attendance.title", "ri:calendar-2-line", null);
        attendanceRoute.setMeta(attendanceMeta);

        Route approvalRoute = createRoute("Approval", "approval", "/attendance/approval", attendanceRoute);
        RouteMeta approvalMeta = createRouteMeta("menus.attendance.approval", null, null);
        approvalRoute.setMeta(approvalMeta);

        Route applicationRoute = createRoute("Application", "form_application", "/attendance/form_application",
                attendanceRoute);
        RouteMeta applicationMeta = createRouteMeta("menus.attendance.application", null, null);
        applicationRoute.setMeta(applicationMeta);

        attendanceRoute.setChildren(List.of(approvalRoute, applicationRoute));

        // 保存所有路由
        routeRepository.saveAll(List.of(dashboardRoute, systemRoute, resultRoute, exceptionRoute, attendanceRoute));
    }

    private Route createRoute(String name, String path, String component, Route parent) {
        Route route = new Route();
        route.setName(name);
        route.setPath(path);
        route.setComponent(component);
        route.setParent(parent);
        route.setChildren(new ArrayList<>());
        return route;
    }

    private RouteMeta createRouteMeta(String title, String icon, List<AuthItem> authList) {
        RouteMeta meta = new RouteMeta();
        meta.setTitle(title);
        meta.setIcon(icon);
        meta.setAuthList(authList);
        return meta;
    }

}
