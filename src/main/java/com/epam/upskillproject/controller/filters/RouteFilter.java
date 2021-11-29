package com.epam.upskillproject.controller.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RouteFilter implements Filter {

    private static final String VIEW_PATTERN = "/WEB-INF/view/.*";
    private static final String RESOURCES_PATTERN = "/img/.*";
    private static final String CONTROLLER_PREFIX = "/controller";

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        String uri = httpReq.getRequestURI();
        if (uri.matches(VIEW_PATTERN) || uri.matches(RESOURCES_PATTERN) || uri.contains(CONTROLLER_PREFIX)) {
            chain.doFilter(req, resp);
        } else if (!resp.isCommitted()) {
            httpReq.getRequestDispatcher(CONTROLLER_PREFIX.concat(uri)).forward(req, resp);
        }
    }
}
