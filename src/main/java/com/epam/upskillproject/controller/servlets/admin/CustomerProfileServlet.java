package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.PermissionType;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.model.service.SuperadminService;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.ejb.EJBException;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@WebServlet("/customer/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN", "ADMIN"}))
public class CustomerProfileServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(CustomerProfileServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.customerProfile";
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
    private static final String CUSTOMER_ATTR = "customer";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/admin/customer.jsp";

    @Inject
    private SecurityContext securityContext;
    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SuperadminService superadminService;
    @Inject
    private AdminService adminService;
    @Inject
    private Pbkdf2PasswordHash passwordHash;
//
    private String viewPath;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        try {
            String path = req.getRequestURI();
            BigInteger id = new BigInteger(path.replace("/customer/", ""));
            if (id.compareTo(BigInteger.ZERO) > 0) {
                Person customer = adminService.getCustomer(id);
                if (customer != null) {
                    req.setAttribute(CUSTOMER_ATTR, customer);
                } else {
                    logger.log(Level.INFO, String.format("Customer with id (%s) not found", id));
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                logger.log(Level.WARN, String.format("Incorrect customer's id passed (%s)", id));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot get customer's profile (thrown exception) [uri: %s]",
                    req.getRequestURI()), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    String.format("Cannot get customer's profile (%s)", e.getMessage()));
        } catch (NumberFormatException e) {
            logger.log(Level.WARN, String.format("Cannot get customer's profile (bad parameters) [uri: %s]",
                    req.getRequestURI()), e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } finally {
            view.forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Optional<Boolean> deleteMethod = paramReader.readBoolean(req, DELETE_PARAM);
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<Integer> customerHash = paramReader.readInteger(req, HASH_PARAM);

        // Delete customer
        if (deleteMethod.isPresent() && deleteMethod.get().equals(true)) {
            if (!securityContext.isCallerInRole(PermissionType.SUPERADMIN.getType())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only superadmins have delete permission");
            }
            if (id.isPresent() && id.get().compareTo(BigInteger.ZERO) > 0) {
                try {
                    boolean deleted = superadminService.deletePerson(id.get());
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE.name());
                    req.setAttribute(OPERATION_STATUS_ATTR, deleted);
                    resp.setStatus((deleted) ? HttpServletResponse.SC_NO_CONTENT :
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    putCustomerToRequest(req, id.get());
                    view.forward(req, resp);
                } catch (EJBException e) {
                    if (e.getCause() instanceof TransactionException) {
                        TransactionException tEx = (TransactionException) e.getCause();
                        logger.log(Level.WARN, String.format("Cannot delete customer (%s) [id: %s]", tEx.getMessage(),
                                id.orElse(null)));
                        sendOperationError(req, resp, view, OperationType.DELETE, tEx.getStatusCode(), tEx.getMessage());
                    }
                }
            } else {
                logger.log(Level.WARN, String.format("Cannot delete customer (bad id parameter) [id: %s]",
                        id.orElse(null)));
                sendOperationError(req, resp, view, OperationType.DELETE, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrect or missing admin's ID. ");
            }
        }

        // Update customer
        if (id.isEmpty() || id.get().compareTo(BigInteger.ZERO) <= 0) {
            logger.log(Level.WARN, String.format("Cannot update customer (bad id parameter) [id: %s]",
                    id.orElse(null)));
            sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_BAD_REQUEST,
                    "Incorrect or missing customer's ID. ");
        } else {
            boolean result = false;
            try {
                Person controlCustomerInstance = adminService.getCustomer(id.get());
                req.setAttribute(CUSTOMER_ATTR, controlCustomerInstance);
                if (controlCustomerInstance == null || customerHash.isEmpty() ||
                        !customerHash.get().equals(controlCustomerInstance.getHash())) {
                    logger.log(Level.WARN, String.format("Cannot update customer (person's hash was changed) [id: %s]",
                            id.get()));
                    sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_CONFLICT,
                            "Uncommitted changes were found. ");
                } else {
                    result = adminService.updateCustomer(
                            paramReader.readBigInteger(req, ID_PARAM).orElseThrow(InvalidParameterException::new),
                            (req.isUserInRole(PermissionType.SUPERADMIN.getType())) ?
                                    paramReader.readPermissionType(req, PERMISSION_PARAM).orElse(null) : null,
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
                    putCustomerToRequest(req, id.get());
                    view.forward(req, resp);
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Exception thrown during customer update operation (id: %s)", id), e);
                sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Exception thrown during operation (%s). ", e.getMessage()));
            } catch (InvalidParameterException e) {
                logger.log(Level.WARN, String.format("Cannot update customer (bad parameters passed) [id: %s]", id), e);
                sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_BAD_REQUEST,
                        "Bad parameters passed. ");
            }
        }
    }

    private void putCustomerToRequest(HttpServletRequest req, BigInteger id) {
        if (id != null && id.compareTo(BigInteger.ZERO) > 0) {
            try {
                req.setAttribute(CUSTOMER_ATTR, adminService.getCustomer(id));
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Cannot get customer's profile (id: %s)", id), e);
                buildErrorMessage(req, String.format("Cannot get customer's profile (%s). ", e.getMessage()));
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
        putCustomerToRequest(req, paramReader.readBigInteger(req, ID_PARAM).orElse(null));
        resp.setStatus(statusCode);
        view.forward(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.viewPath = propertiesKeeper.getStringOrDefault(VIEW_PROP, DEFAULT_VIEW);
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }
}
