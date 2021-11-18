package com.epam.upskillproject.controller.servlets.payservice;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.exceptions.AccountLimitException;
import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.Page;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.ejb.EJBException;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/payservice/my_accounts")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"CUSTOMER"}))
public class MyAccountsServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(MyAccountsServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.myAccounts";
    private static final String SORT_PARAM = "sort";
    private static final String TARGET_PARAM = "target";
    private static final String ACCOUNT_ID_PARAM = "accountId";
    private static final String AMOUNT_PARAM = "amount";
    private static final String ACCOUNT_ADD_TARGET = "add";
    private static final String ACCOUNT_INCREASE_TARGET = "increase";
    private static final String PAGE_ATTR = "page";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccounts.jsp";

    @Inject
    SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private ParamReader paramReader;
    @Inject
    CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            try {
                Person user = customerService.getUserPerson(principal);
                if (user == null) {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                }
                req.setAttribute(PAGE_ATTR, buildAccountsPage(req, principal));
                view.forward(req, resp);
            } catch (SQLException e) {
                logger.log(Level.ERROR, "Cannot build accounts page", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            Optional<String> target = paramReader.readString(req, TARGET_PARAM);
            try {
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                // Add account
                if (target.isPresent() && ACCOUNT_ADD_TARGET.equals(target.get())) {
                    if (customerService.addUserAccount(principal)) {
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        req.setAttribute(OPERATION_STATUS_ATTR, true);
                        req.setAttribute(PAGE_ATTR, buildAccountsPage(req, principal));
                        view.forward(req, resp);
                    } else {
                        logger.log(Level.ERROR, String.format("Account creation failed (principal: %s)",
                                principal.getName()));
                        sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                "Account creation failed");
                    }
                // Top up account
                } else if (target.isPresent() && ACCOUNT_INCREASE_TARGET.equals(target.get())) {
                    Optional<BigInteger> accountId = paramReader.readBigInteger(req, ACCOUNT_ID_PARAM);
                    Optional<BigDecimal> amount = paramReader.readBigDecimal(req, AMOUNT_PARAM);
                    if (accountId.isPresent() && amount.isPresent() &&
                            customerService.topUpAccount(principal, accountId.get(), amount.get())) {
                        req.setAttribute(OPERATION_STATUS_ATTR, true);
                        req.setAttribute(PAGE_ATTR, buildAccountsPage(req, principal));
                        view.forward(req, resp);
                    } else {
                        logger.log(Level.ERROR, String.format("Account top up failed (principal: %s)",
                                principal.getName()));
                        sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_BAD_REQUEST,
                                "Top up account error");
                    }
                } else {
                    logger.log(Level.WARN, String.format("Cannot perform operation: incorrect parameters passed " +
                                    "(principal: %s)", principal.getName()));
                    sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_BAD_REQUEST,
                            "Incorrect operation parameters passed");
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Exception thrown during operation (principal: %s, target: %s)",
                        principal.getName(), target.orElse(null)), e);
                sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Exception thrown during operation (%s). ", e.getMessage()));
            } catch (EJBException e) {
                Throwable cause = e;
                while (cause instanceof EJBException) {
                    cause = cause.getCause();
                }
                if (cause instanceof PaymentParamException) {
                    logger.log(Level.WARN, String.format("Cannot perform payment (bad parameters) [principal: %s, " +
                                    "target: %s]", principal.getName(), target.orElse(null)), cause);
                    sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_BAD_REQUEST, cause.getMessage());
                } else if (cause instanceof TransactionException) {
                    logger.log(Level.WARN, String.format("Cannot perform payment (transaction failed) [principal: %s, " +
                                    "target: %s]", principal.getName(), target.orElse(null)), cause);
                    sendUpdateError(req, resp, view, principal, ((TransactionException) cause).getStatusCode(),
                            cause.getMessage());
                } else if (cause instanceof AccountLimitException) {
                    logger.log(Level.WARN, String.format("Limit was exceeded (principal: %s, target: %s)",
                            principal.getName(), target.orElse(null)), cause);
                    sendUpdateError(req, resp, view, principal, HttpServletResponse.SC_CONFLICT, cause.getMessage());
                } else {
                    logger.log(Level.ERROR, String.format("Unknown error (principal: %s, target: %s)",
                            principal.getName(), target.orElse(null)), cause);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error: " + cause.getMessage());
                }
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    private Page<Account> buildAccountsPage(HttpServletRequest req, Principal principal) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<AccountSortType> sortType = paramReader.readAccountSort(req, SORT_PARAM);
        return customerService.getUserAccountsPage(principal, pageSize, pageNumber, sortType.orElse(null));
    }

    private void buildErrorMessage(HttpServletRequest req, String message) {
        Optional<String> errMsg = paramReader.readString(req, ERROR_MESSAGE_ATTR);
        req.setAttribute(ERROR_MESSAGE_ATTR, errMsg.orElse("").concat((message != null) ? message : ""));
    }

    private void sendUpdateError(HttpServletRequest req, HttpServletResponse resp, RequestDispatcher view,
                                 Principal principal, int statusCode, String message)
            throws ServletException, IOException {
        req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        resp.setStatus(statusCode);
        buildErrorMessage(req, message);
        try {
            req.setAttribute(PAGE_ATTR, buildAccountsPage(req, principal));
        } catch (SQLException e) {
            buildErrorMessage(req, String.format("Cannot build accounts page (%s). ", e.getMessage()));
            logger.log(Level.ERROR, String.format("Cannot build accounts page (principal: %s)", principal.getName()), e);
        }
        view.forward(req, resp);
    }
}
