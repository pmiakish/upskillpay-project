package com.epam.upskillproject.controller.servlets.payservice;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PaymentSortType;
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
import java.security.Principal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@WebServlet("/payservice/my_account_incoming/*")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"CUSTOMER"}))
public class MyAccountIncomingServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(MyAccountIncomingServlet.class.getName());

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String VIEW_PROP = "servlet.view.myAccountIncoming";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String ACCOUNT_ATTR = "account";
    private static final String FORMATTER_ATTR = "formatter";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccountIncoming.jsp";

    @Inject
    private SecurityContext securityContext;
    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private ParamReader paramReader;
    @Inject
    private CustomerService customerService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            int pageNumber = paramReader.readPageNumber(req);
            int pageSize = paramReader.readPageSize(req);
            Optional<PaymentSortType> sortType = paramReader.readPaymentSort(req, SORT_PARAM);
            try {
                BigInteger accountId = getIdFromRequestUri(req);
                Account account = customerService.getUserAccountById(principal, accountId);
                req.setAttribute(FORMATTER_ATTR, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
                if (account != null) {
                    req.setAttribute(ACCOUNT_ATTR, account);
                    req.setAttribute(PAGE_ATTR, customerService.getUserIncomingPaymentsByAccount(principal,
                            account.getId(), pageSize, pageNumber, sortType.orElse(null)));
                }
                view.forward(req, resp);
            } catch (SQLException e) {
                logger.log(Level.ERROR, "Cannot build payments page", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.log(Level.ERROR, "Cannot build payments page (incorrect account id)", e);
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        } else {
            logger.log(Level.ERROR, "Cannot get caller principal, uri: " + req.getRequestURI());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
        }
    }

    private BigInteger getIdFromRequestUri(HttpServletRequest req) throws IllegalArgumentException {
        String path = req.getRequestURI();
        String urlId = path.replace("/payservice/my_account_incoming/", "");
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
}
