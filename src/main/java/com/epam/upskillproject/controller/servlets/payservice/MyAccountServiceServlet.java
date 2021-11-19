package com.epam.upskillproject.controller.servlets.payservice;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.exceptions.AccountLimitException;
import com.epam.upskillproject.exceptions.PaymentParamException;
import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.CardNetworkType;
import com.epam.upskillproject.model.service.CustomerService;
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
import java.math.RoundingMode;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet("/payservice/my_account_service/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"CUSTOMER"}))
public class MyAccountServiceServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(MyAccountServiceServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.myAccountService";
    private static final String COMMISSION_RATE_PROP = "system.payments.commissionRate";
    private static final String TARGET_PARAM = "target";
    private static final String ID_PARAM = "id";
    private static final String CARD_NETWORK_PARAM = "cardNet";
    private static final String CVC_PARAM = "cvc";
    private static final String RECEIVER_PARAM = "receiver";
    private static final String AMOUNT_PARAM = "amount";
    private static final String ACCOUNT_BLOCK_TARGET = "accBlock";
    private static final String CARD_BLOCK_TARGET = "cardBlock";
    private static final String PAYMENT_TARGET = "payment";
    private static final String ADD_CARD_TARGET = "addCard";
    private static final String DEL_ACCOUNT_TARGET = "accDelete";
    private static final String DEL_CARD_TARGET = "cardDelete";
    private static final String ACCOUNT_ATTR = "account";
    private static final String CARDS_ATTR = "cards";
    private static final String CARD_NETWORKS_ATTR = "cardNetworks";
    private static final String COMMISSION_RATE_ATTR = "commissionRate";
    private static final String CREATED_CVC_ATTR = "createdCvc";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccountService.jsp";

    @Inject
    private SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private CustomerService customerService;

    private BigDecimal commissionRate;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            putViewObjectsToRequest(req, resp, principal);
            view.forward(req, resp);
        } else {
            logger.log(Level.ERROR, "Cannot get caller principal, uri: " + req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        Optional<String> target = paramReader.readString(req, TARGET_PARAM);
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<CardNetworkType> cardNet = paramReader.readCardNetworkType(req, CARD_NETWORK_PARAM);
        if (principal != null) {
            try {
                // Block account
                if (target.isPresent() && id.isPresent() && target.get().equals(ACCOUNT_BLOCK_TARGET)) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE);
                    req.setAttribute(OPERATION_STATUS_ATTR, customerService.blockUserAccount(principal, id.get()));
                // Block card
                } else if (target.isPresent() && id.isPresent() && target.get().equals(CARD_BLOCK_TARGET)) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE);
                    req.setAttribute(OPERATION_STATUS_ATTR, customerService.blockUserCard(principal, id.get()));
                // Add card
                } else if (target.isPresent() && target.get().equals(ADD_CARD_TARGET) && cardNet.isPresent()) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.CREATE);
                    String cvc = customerService.addUserCard(principal, getIdFromRequestUri(req), cardNet.get());
                    if (cvc.length() > 0) {
                        req.setAttribute(OPERATION_STATUS_ATTR, true);
                        req.setAttribute(CREATED_CVC_ATTR, cvc);
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        sendOperationError(req, resp, view, OperationType.CREATE, principal,
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Card issue failed");
                    }
                // Delete account
                } else if (target.isPresent() && id.isPresent() && target.get().equals(DEL_ACCOUNT_TARGET)) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE);
                    try {
                        req.setAttribute(OPERATION_STATUS_ATTR, customerService.deleteUserAccount(principal, id.get()));
                    } catch (EJBException e) {
                        if (e.getCause() instanceof TransactionException) {
                            TransactionException tEx = (TransactionException) e.getCause();
                            logger.log(Level.WARN, String.format("Cannot delete account (%s) [id: %s]", tEx.getMessage(),
                                    id.orElse(null)));
                            sendOperationError(req, resp, view, OperationType.DELETE, principal, tEx.getStatusCode(),
                                    tEx.getMessage());
                        } else {
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error");
                        }
                    }
                // Delete card
                } else if (target.isPresent() && id.isPresent() && target.get().equals(DEL_CARD_TARGET)) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE);
                    req.setAttribute(OPERATION_STATUS_ATTR, customerService.deleteUserCard(principal, id.get()));
                // Perform payment
                } else if (target.isPresent() && id.isPresent() && target.get().equals(PAYMENT_TARGET)) {
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.PAYMENT);
                    Optional<String> cvc = paramReader.readString(req, CVC_PARAM);
                    Optional<BigInteger> receiver = paramReader.readBigInteger(req, RECEIVER_PARAM);
                    Optional<BigDecimal> amount = paramReader.readBigDecimal(req, AMOUNT_PARAM);
                    if (cvc.isPresent() && receiver.isPresent() && amount.isPresent()) {
                        try {
                            customerService.performPayment(principal, id.get(), cvc.get(), receiver.get(), amount.get());
                            req.setAttribute(OPERATION_STATUS_ATTR, true);
                        } catch (SQLException e) {
                            logger.log(Level.ERROR, String.format("Cannot perform payment (exception was thrown) " +
                                    "[receiver: %s, amount: %s]", receiver.get(), amount.get()), e);
                            sendOperationError(req, resp, view, OperationType.PAYMENT, principal,
                                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                    String.format("Cannot perform payment (%s)", e.getMessage()));
                        }
                    } else {
                        logger.log(Level.WARN, String.format("Cannot perform payment (incorrect parameters passed) " +
                                "[cvc is present: %s, receiver: %s, amount: %s]",
                                cvc.isPresent(), receiver.orElse(null), amount.orElse(null)));
                        sendOperationError(req, resp, view, OperationType.PAYMENT, principal,
                                HttpServletResponse.SC_BAD_REQUEST,"Incorrect payment parameters passed");
                    }
                } else {
                    logger.log(Level.WARN, String.format("Cannot perform operation: incorrect parameters passed " +
                            "(principal: %s, target: %s, id: %s)", principal.getName(), target.orElse(null),
                            id.orElse(null)));
                    sendOperationError(req, resp, view, OperationType.DEFAULT, principal,
                            HttpServletResponse.SC_BAD_REQUEST, "Incorrect operation parameters passed");
                }
                putViewObjectsToRequest(req, resp, principal);
                view.forward(req, resp);
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Exception thrown during operation (principal: %s, target: %s, " +
                                "id: %s)", principal.getName(), target.orElse(null), id.orElse(null)), e);
                sendOperationError(req, resp, view, OperationType.DEFAULT, principal,
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        String.format("Exception thrown during operation (%s). ", e.getMessage()));
            } catch (EJBException e) {
                Throwable cause = e;
                while (cause instanceof EJBException) {
                    cause = cause.getCause();
                }
                if (cause instanceof PaymentParamException) {
                    logger.log(Level.WARN, String.format("Cannot perform payment (bad parameters) [principal: %s, " +
                            "target: %s, id: %s]", principal.getName(), target.orElse(null), id.orElse(null)),
                            cause);
                    sendOperationError(req, resp, view, OperationType.PAYMENT, principal,
                            HttpServletResponse.SC_BAD_REQUEST, cause.getMessage());
                } else if (cause instanceof TransactionException) {
                    logger.log(Level.WARN, String.format("Cannot perform payment (transaction failed) [principal: %s, " +
                            "target: %s, id: %s]", principal.getName(), target.orElse(null), id.orElse(null)),
                            cause);
                    sendOperationError(req, resp, view, OperationType.PAYMENT, principal,
                            ((TransactionException) cause).getStatusCode(), cause.getMessage());
                } else if (cause instanceof AccountLimitException) {
                    logger.log(Level.WARN, String.format("Limit was exceeded (principal: %s, target: %s, id: %s)",
                            principal.getName(), target.orElse(null), id.orElse(null)), cause);
                    sendOperationError(req, resp, view, OperationType.PAYMENT, principal,
                            HttpServletResponse.SC_CONFLICT, cause.getMessage());
                } else {
                    logger.log(Level.ERROR, String.format("Unknown error (principal: %s, target: %s, id: %s)",
                            principal.getName(), target.orElse(null), id.orElse(null)), cause);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown error: " + cause.getMessage());
                }
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    private void buildErrorMessage(HttpServletRequest req, String message) {
        Optional<String> errMsg = paramReader.readString(req, ERROR_MESSAGE_ATTR);
        req.setAttribute(ERROR_MESSAGE_ATTR, errMsg.orElse("").concat((message != null) ? message : ""));
    }

    private void sendOperationError(HttpServletRequest req, HttpServletResponse resp, RequestDispatcher view,
                                    OperationType operationType, Principal principal, int statusCode, String message)
            throws ServletException, IOException {
        req.setAttribute(OPERATION_NAME_ATTR, operationType.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        buildErrorMessage(req, message);
        resp.setStatus(statusCode);
        putViewObjectsToRequest(req, resp, principal);
        view.forward(req, resp);
    }

    private void putViewObjectsToRequest(HttpServletRequest req, HttpServletResponse resp, Principal principal) {
        try {
            Account account = customerService.getUserAccountById(principal, getIdFromRequestUri(req));
            if (account != null) {
                List<Card> cards = customerService.getUserCardsByAccount(principal, account.getId());
                req.setAttribute(ACCOUNT_ATTR, account);
                req.setAttribute(CARDS_ATTR, cards);
                req.setAttribute(COMMISSION_RATE_ATTR, commissionRate);
                req.setAttribute(CARD_NETWORKS_ATTR, CardNetworkType.values());
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                req.setAttribute(ERROR_MESSAGE_ATTR, "Account not found");
            }
        } catch (SQLException | IllegalArgumentException e) {
            logger.log(Level.ERROR, String.format("Cannot get customer's accounts (principal: %s)",
                    principal.getName()), e);
            buildErrorMessage(req, String.format("Cannot get customer's accounts (%s). ", e.getMessage()));
        }
    }

    private BigInteger getIdFromRequestUri(HttpServletRequest req) throws IllegalArgumentException {
        String path = req.getRequestURI();
        String urlId = path.replace("/payservice/my_account_service/", "");
        BigInteger id;
        try {
            id = new BigInteger(urlId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect account id");
        }
        if (id.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Account id may not be less than 1");
        }
        return id;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        commissionRate = propertiesKeeper.getBigDecimal(COMMISSION_RATE_PROP).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
