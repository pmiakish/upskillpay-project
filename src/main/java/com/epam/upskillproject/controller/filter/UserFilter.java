package com.epam.upskillproject.controller.filter;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.SystemService;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

@WebFilter("*")
public class UserFilter implements Filter {

    private static final Logger logger = LogManager.getLogger(UserFilter.class.getName());

    private static final String VIEW_PROP = "servlet.view.blocked";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/blocked.jsp";
    private static final String HOME_ENDPOINT = "/";
    private static final String LOGOUT_ENDPOINT = "/logout";
    private static final String LANG_ENDPOINT = "/lang";
    private static final String IMG_ENDPOINT_PATTERN = "/img/.*";
    private static final String USER_ATTR = "user";
    private static final String BLOCKED_ATTR = "blocked";

    @Inject
    private SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private SystemService systemService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;
        HttpSession session = httpReq.getSession();
        Principal principal = securityContext.getCallerPrincipal();
        Object sessionUser = session.getAttribute(USER_ATTR);
        Person user = (sessionUser instanceof Person) ? (Person) sessionUser : null;
        // In case when session does not contain user object or user object has been changed
        try {
            if (principal != null &&
                    (user == null || !systemService.checkPersonHash(principal.getName(), user.getHash()))) {
                user = systemService.getPerson(principal.getName());
                if (user != null) {
                    session.setAttribute(USER_ATTR, user);
                    logger.log(Level.TRACE, String.format("Put principal user object in session: %s (%s) ",
                            user.getEmail(), user.getrole()));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARN, "Cannot get principal user object", e);
        }

        // In case when blocked user tries to access project resources
        boolean isActive = true;
        if (principal != null) {
            try {
                isActive = systemService.checkActiveStatus(principal.getName());
            } catch (SQLException e) {
                logger.log(Level.WARN, "Cannot get principal user status", e);
                isActive = false;
            }
        }
        if (!isActive) {
            session.setAttribute(BLOCKED_ATTR, true);
            String uri = httpReq.getRequestURI();
            if (!HOME_ENDPOINT.equals(uri) && !LOGOUT_ENDPOINT.equals(uri) && !LANG_ENDPOINT.equals(uri) &&
                    !uri.matches(IMG_ENDPOINT_PATTERN)) {
                String localizedView = localeDispatcher.getLocalizedView(httpReq, VIEW_PROP);
                String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
                httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpReq.getRequestDispatcher(viewPath).forward(httpReq, httpResp);
                logger.log(Level.INFO, String.format("Attempt to access the resource %s of blocked user has been " +
                        "prevented (principal: %s)", httpReq.getRequestURI(), principal.getName()));
            }
        } else if (principal != null) {
            session.setAttribute(BLOCKED_ATTR, false);
        }
        chain.doFilter(httpReq, httpResp);
    }
}
