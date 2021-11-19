package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.SuperadminService;
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
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/admin/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN"}))
public class AdminProfileServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AdminProfileServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.adminProfile";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";
    private static final String STATUS_ACTIVE_PARAM = "active";
    private static final String STATUS_ACTIVE_VALUE = "on";
    private static final String DELETE_PARAM = "delete";
    private static final String ID_PARAM = "id";
    private static final String HASH_PARAM = "hash";
    private static final String PERMISSION_PARAM = "permission";
    private static final String EMAIL_PARAM = "email";
    private static final String PASSWORD_PARAM = "pass";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String REG_DATE_PARAM = "regDate";
    private static final String ADMIN_ATTR = "admin";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/admin.jsp";

    @Inject
    LocaleDispatcher localeDispatcher;
    @Inject
    private SecurityContext securityContext;
    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SuperadminService superadminService;
    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        try {
            String path = req.getRequestURI();
            BigInteger id = new BigInteger(path.replace("/admin/", ""));
            if (id.compareTo(BigInteger.ZERO) > 0) {
                Person admin = superadminService.getAdmin(id);
                if (admin != null) {
                    req.setAttribute(ADMIN_ATTR, admin);
                } else {
                    logger.log(Level.INFO, String.format("Admin with id (%s) not found", id));
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                logger.log(Level.WARN, String.format("Incorrect admin's id passed (%s)", id));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot get admin's profile (thrown exception) [uri: %s]",
                    req.getRequestURI()), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    String.format("Cannot get admin's profile (%s)", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.log(Level.WARN, String.format("Cannot get admin's profile (bad parameters) [uri: %s]",
                    req.getRequestURI()), e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            view.forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Optional<Boolean> deleteMethod = paramReader.readBoolean(req, DELETE_PARAM);
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<Integer> adminHash = paramReader.readInteger(req, HASH_PARAM);

        // Delete admin
        if (deleteMethod.isPresent() && deleteMethod.get().equals(true)) {
            try {
                Principal principal = securityContext.getCallerPrincipal();
                if (id.isEmpty() || id.get().compareTo(BigInteger.ZERO) <= 0) {
                    logger.log(Level.WARN, String.format("Cannot delete admin (bad id parameter) [id: %s]",
                            id.orElse(null)));
                    sendOperationError(req, resp, view, OperationType.DELETE, HttpServletResponse.SC_BAD_REQUEST,
                            "Incorrect or missing admin's ID. ");
                } else if (principal == null) {
                    logger.log(Level.WARN, "Cannot delete admin (caller principal is not identified)");
                    sendOperationError(req, resp, view, OperationType.DELETE, HttpServletResponse.SC_FORBIDDEN,
                            "The caller principal is not identified. ");
                } else if (principal.getName().equals(superadminService.getAdmin(id.get()).getEmail())) {
                    logger.log(Level.WARN, String.format("Admin cannot delete himself, principal: %s",
                            principal.getName()));
                    sendOperationError(req, resp, view, OperationType.DELETE, HttpServletResponse.SC_FORBIDDEN,
                            "You cannot delete your own profile. ");
                } else {
                    boolean deleted = superadminService.deletePerson(id.get());
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE.name());
                    req.setAttribute(OPERATION_STATUS_ATTR, deleted);
                    resp.setStatus((deleted) ? HttpServletResponse.SC_OK :
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    putAdminToRequest(req, id.get());
                    view.forward(req, resp);
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Exception thrown during admin update operation (id: %s)",
                        id.get()), e);
                sendOperationError(req, resp, view, OperationType.DELETE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Exception thrown during operation (%s). ", e.getMessage()));
            }
        } else {
            // Update admin
            if (id.isEmpty() || id.get().compareTo(BigInteger.ZERO) <= 0) {
                logger.log(Level.WARN, String.format("Cannot update admin (bad id parameter) [id: %s]",
                        id.orElse(null)));
                sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrect or missing admin's ID. ");
            } else {
                boolean result = false;
                try {
                    Person controlAdminInstance = superadminService.getAdmin(id.get());
                    req.setAttribute(ADMIN_ATTR, controlAdminInstance);
                    if (controlAdminInstance == null || adminHash.isEmpty() ||
                            !adminHash.get().equals(controlAdminInstance.getHash())) {
                        logger.log(Level.WARN, String.format("Cannot update admin (person's hash was changed) [id: %s]",
                                id.get()));
                        sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_CONFLICT,
                                "Uncommitted changes were found. ");
                    } else {
                        result = superadminService.updateAdmin(
                                paramReader.readBigInteger(req, ID_PARAM).orElseThrow(InvalidParameterException::new),
                                paramReader.readPermissionType(req, PERMISSION_PARAM).orElse(null),
                                paramReader.readString(req, EMAIL_PARAM).orElseThrow(InvalidParameterException::new),
                                paramReader.readString(req, PASSWORD_PARAM).isPresent() ?
                                        passwordHash.generate(paramReader.readString(req, PASSWORD_PARAM).get().toCharArray()) : null,
                                paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                                paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                                (STATUS_ACTIVE_VALUE.equals(paramReader.readString(req, STATUS_ACTIVE_PARAM).orElse(""))) ?
                                        StatusType.ACTIVE : StatusType.BLOCKED,
                                paramReader.readLocalDate(req, REG_DATE_PARAM).orElseThrow(InvalidParameterException::new)
                        );
                        req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                        req.setAttribute(OPERATION_STATUS_ATTR, result);
                        resp.setStatus((result) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        putAdminToRequest(req, id.get());
                        view.forward(req, resp);
                    }
                } catch (SQLException e) {
                    logger.log(Level.ERROR, String.format("Exception thrown during admin update operation (id: %s)", id), e);
                    sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            String.format("Exception thrown during operation (%s). ", e.getMessage()));
                } catch (InvalidParameterException e) {
                    logger.log(Level.WARN, String.format("Cannot update admin (bad parameters passed) [id: %s]", id), e);
                    sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_BAD_REQUEST,
                            "Bad parameters passed. ");
                }
            }
        }



    }

    private void putAdminToRequest(HttpServletRequest req, BigInteger id) {
        if (id != null && id.compareTo(BigInteger.ZERO) > 0) {
            try {
                req.setAttribute(ADMIN_ATTR, superadminService.getAdmin(id));
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Cannot get admin's profile (id: %s)", id), e);
                buildErrorMessage(req, String.format("Cannot get admin's profile (%s). ", e.getMessage()));
            }
        }
    }

    private void buildErrorMessage(HttpServletRequest req, String message) {
        Optional<String> errMsg = paramReader.readString(req, ERROR_MESSAGE_ATTR);
        req.setAttribute(ERROR_MESSAGE_ATTR, errMsg.orElse("").concat((message != null) ? message : ""));
    }

    private void sendOperationError(HttpServletRequest req, HttpServletResponse resp, RequestDispatcher view,
                                    OperationType operationType, int statusCode, String message)
            throws ServletException, IOException {
        req.setAttribute(OPERATION_NAME_ATTR, operationType.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        buildErrorMessage(req, message);
        putAdminToRequest(req, paramReader.readBigInteger(req, ID_PARAM).orElse(null));
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
