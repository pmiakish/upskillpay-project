package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.model.service.SuperadminService;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
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
import java.sql.SQLException;
import java.util.*;

@WebServlet("/customer/accounts/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN", "ADMIN"}))
public class CustomerAccountsServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(CustomerAccountsServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.customerAccounts";
    private static final String ID_PARAM = "id";
    private static final String TARGET_PARAM = "target";
    private static final String CURRENT_STATUS_PARAM = "currentStatus";
    private static final String ACCOUNT_STATUS_TARGET = "accStat";
    private static final String CARD_STATUS_TARGET = "cardStat";
    private static final String ACCOUNT_DEL_TARGET = "accDelete";
    private static final String CARD_DEL_TARGET = "cardDelete";
    private static final String CUSTOMER_ATTR = "customer";
    private static final String ACCOUNTS_ATTR = "accounts";
    private static final String CARDS_ATTR = "cards";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/customerAccounts.jsp";

    @Inject
    SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SuperadminService superadminService;
    @Inject
    private AdminService adminService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        try {
            Person customer = getCustomer(req);
            if (customer != null) {
                req.setAttribute(CUSTOMER_ATTR, customer);
                req.setAttribute(ACCOUNTS_ATTR, getAccounts(req));
                req.setAttribute(CARDS_ATTR, getCards(req));
            } else {
                logger.log(Level.WARN, String.format("Customer not found (uri: %s)", req.getRequestURI()));
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot get customer's accounts (thrown exception) [uri: %s]",
                req.getRequestURI()), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Cannot get customer's accounts (thrown exception)");
        } catch (IllegalArgumentException e) {
            System.out.println("BAD FORMAT");
            logger.log(Level.WARN, String.format("Cannot get customer's accounts (%s) [uri: %s]", e.getMessage(),
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
        Optional<String> target = paramReader.readString(req, TARGET_PARAM);
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<StatusType> currentStatus = paramReader.readStatusType(req, CURRENT_STATUS_PARAM);
        try {
            // Change account status
            if (target.isPresent() && target.get().equals(ACCOUNT_STATUS_TARGET) && id.isPresent() &&
                    currentStatus.isPresent()) {
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE);
                boolean updated = adminService.updateAccountStatus(id.get(),
                        (currentStatus.get() == StatusType.ACTIVE) ? StatusType.BLOCKED : StatusType.ACTIVE);
                req.setAttribute(OPERATION_STATUS_ATTR, updated);
            // Change card status
            } else if (target.isPresent() && target.get().equals(CARD_STATUS_TARGET) && id.isPresent() &&
                    currentStatus.isPresent()) {
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE);
                boolean updated = false;
                if (currentStatus.get().equals(StatusType.BLOCKED)) {
                    BigInteger accountId = adminService.getAccountIdByCardId(id.get());
                    if (accountId != null && !adminService.getAccountStatus(accountId).equals(StatusType.BLOCKED)) {
                        updated = adminService.updateCardStatus(id.get(), StatusType.ACTIVE);
                    } else {
                        buildErrorMessage(req, "You must activate the account before the card activation! ");
                    }
                } else {
                    updated = adminService.updateCardStatus(id.get(), StatusType.BLOCKED);
                }
                req.setAttribute(OPERATION_STATUS_ATTR, updated);
            // Delete account
            } else if (target.isPresent() && target.get().equals(ACCOUNT_DEL_TARGET) && id.isPresent()) {
                if (!securityContext.isCallerInRole(PermissionType.SUPERADMIN.getType())) {
                    logger.log(Level.WARN, String.format("Forbidden: principal %s tries to delete account id %s",
                            securityContext.getCallerPrincipal().getName(), id.get()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only superadmins have delete permission");
                }
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE);
                req.setAttribute(OPERATION_STATUS_ATTR, superadminService.deleteAccount(id.get()));
            // Delete card
            } else if (target.isPresent() && target.get().equals(CARD_DEL_TARGET) && id.isPresent()) {
                if (!securityContext.isCallerInRole(PermissionType.SUPERADMIN.getType())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Only superadmins have delete permission");
                }
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE);
                req.setAttribute(OPERATION_STATUS_ATTR, superadminService.deleteCard(id.get()));
            } else {
                logger.log(Level.WARN, String.format("Cannot perform operation: incorrect parameters passed " +
                                "(target: %s, id: %s)", target.orElse(null), id.orElse(null)));
                sendOperationError(req, resp, view, OperationType.UPDATE, HttpServletResponse.SC_BAD_REQUEST,
                        "Incorrect operation parameters passed");
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Exception thrown during operation (target: %s, id: %s)",
                    target.orElse(null), id.orElse(null)), e);
            sendOperationError(req, resp, view, OperationType.DEFAULT, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown during operation (%s). ", e.getMessage()));
        }
        putViewObjectsToRequest(req, resp);
        view.forward(req, resp);
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
        resp.setStatus(statusCode);
        putViewObjectsToRequest(req, resp);
        view.forward(req, resp);
    }

    private void putViewObjectsToRequest(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Person customer = getCustomer(req);
            if (customer != null) {
                req.setAttribute(CUSTOMER_ATTR, getCustomer(req));
                req.setAttribute(ACCOUNTS_ATTR, getAccounts(req));
                req.setAttribute(CARDS_ATTR, getCards(req));
            } else {
                buildErrorMessage(req, "Customer not found. ");
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot get customer's accounts (uri: %s)", req.getRequestURI()), e);
            buildErrorMessage(req, String.format("Cannot get customer's accounts (%s). ", e.getMessage()));
        }
    }

    private Person getCustomer(HttpServletRequest req) throws SQLException, IllegalArgumentException {
        return adminService.getCustomer(getIdFromRequestUri(req));
    }

    private List<Account> getAccounts(HttpServletRequest req) throws SQLException, IllegalArgumentException {
        return adminService.getAccountsByOwner(getIdFromRequestUri(req));
    }

    private List<Card> getCards(HttpServletRequest req) throws SQLException, IllegalArgumentException {
        return adminService.getCardsByOwner(getIdFromRequestUri(req));
    }

    private BigInteger getIdFromRequestUri(HttpServletRequest req) throws IllegalArgumentException {
        String path = req.getRequestURI();
        String urlId = path.replace("/customer/accounts/", "");
        BigInteger id;
        try {
            id = new BigInteger(urlId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect customer's id");
        }
        if (id.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Customer's id may not be less than 1");
        }
        return id;
    }
}
