package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.service.SystemService;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/profile")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN", "ADMIN", "CUSTOMER"}))
public class UserProfileServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(UserProfileServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.profile";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";
    private static final String HASH_PARAM = "hash";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String PASSWORD_PARAM = "pass";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String USER_ATTR = "user";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/profile.jsp";

    @Inject
    private SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SystemService systemService;
    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        view.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        Optional<Integer> hash = paramReader.readInteger(req, HASH_PARAM);
        if (principal != null) {
            try {
                if (hash.isEmpty() || !systemService.checkPersonHash(principal.getName(), hash.get())) {
                    logger.log(Level.WARN, String.format("Cannot update user's profile: uncommitted changes were " +
                            "found (principal: %s)", principal.getName()));
                    sendUpdateError(req, resp, view, HttpServletResponse.SC_CONFLICT, "Uncommitted changes were found");
                }
                boolean updated = systemService.updateUser(
                        principal.getName(),
                        paramReader.readString(req, PASSWORD_PARAM).isPresent() ?
                                passwordHash.generate(paramReader.readString(req, PASSWORD_PARAM).get().toCharArray()) :
                                null,
                        paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                        paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new)
                );
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                req.setAttribute(OPERATION_STATUS_ATTR, updated);
                req.setAttribute(USER_ATTR, systemService.getPerson(principal.getName()));
                resp.setStatus((updated) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                view.forward(req, resp);
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Exception thrown during profile update operation " +
                        "(principal: %s)", principal.getName()), e);
                sendUpdateError(req, resp, view, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Exception thrown during operation (%s).", e.getMessage()));
            } catch (InvalidParameterException e) {
                logger.log(Level.WARN, String.format("Cannot update profile (bad parameters passed) [principal: %s]",
                        principal.getName()), e);
                sendUpdateError(req, resp, view, HttpServletResponse.SC_BAD_REQUEST, "Bad parameters passed.");
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    private void sendUpdateError(HttpServletRequest req, HttpServletResponse resp, RequestDispatcher view,
                                 int statusCode, String message) throws ServletException, IOException {
        req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        req.setAttribute(ERROR_MESSAGE_ATTR, message);
        resp.setStatus(statusCode);
        view.forward(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }
}
