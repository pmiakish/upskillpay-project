package com.epam.upskillproject.controller.command.impl.common;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.SystemService;
import com.epam.upskillproject.util.PermissionType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

@Singleton
public class UserProfileCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(UserProfileCommand.class.getName());

    private static final String USER_ATTR = "user";
    private static final String VIEW_PROP = "servlet.view.profile";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/profile.jsp";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN, PermissionType.ADMIN,
            PermissionType.CUSTOMER};

    private final SystemService systemService;
    private final SecurityContext securityContext;

    @Inject
    public UserProfileCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader, SystemService systemService,
                              SecurityContext securityContext) {
        super(localeDispatcher, paramReader);
        this.securityContext = securityContext;
        this.systemService = systemService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            try {
                Person user = systemService.getPerson(principal.getName());
                if (user != null) {
                    HttpSession session = req.getSession();
                    session.setAttribute(USER_ATTR, user);
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Cannot get user's profile (principal: %s)", principal.getName()),
                        e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot get user's profile (internal " +
                        "server error). ");
                return null;
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
            return null;
        }
        return new CommandResult(view);
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
