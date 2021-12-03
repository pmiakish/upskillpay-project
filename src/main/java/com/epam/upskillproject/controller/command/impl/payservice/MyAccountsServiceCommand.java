package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dto.*;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.RequestDispatcher;
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

@Singleton
public class MyAccountsServiceCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(MyAccountsServiceCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.myAccountService";
    private static final String COMMISSION_RATE_PROP = "system.payments.commissionRate";
    private static final String ACCOUNT_ATTR = "account";
    private static final String CARDS_ATTR = "cards";
    private static final String CARD_NETWORKS_ATTR = "cardNetworks";
    private static final String COMMISSION_RATE_ATTR = "commissionRate";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccountService.jsp";
    private static final String BASE_PATH = "/controller/payservice/my_account_service/";
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    private static final RoleType[] roles = {RoleType.CUSTOMER};

    private final PropertiesKeeper propertiesKeeper;
    private final CustomerService customerService;
    private final SecurityContext securityContext;
    private BigDecimal commissionRate;

    @Inject
    public MyAccountsServiceCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                    PropertiesKeeper propertiesKeeper, CustomerService customerService,
                                    SecurityContext securityContext) {
        super(localeDispatcher, paramReader);
        this.propertiesKeeper = propertiesKeeper;
        this.customerService = customerService;
        this.securityContext = securityContext;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            try {
                Person user = customerService.getUserPerson(principal);
                if (user != null) {
                    Account account = customerService.getUserAccountById(principal, getIdFromRequestUri(req));
                    req.setAttribute(ACCOUNT_ATTR, account);
                    if (account != null) {
                        List<Card> cards = customerService.getUserCardsByAccount(principal, account.getId());
                        req.setAttribute(CARDS_ATTR, cards);
                        req.setAttribute(COMMISSION_RATE_ATTR, commissionRate);
                        req.setAttribute(CARD_NETWORKS_ATTR, CardNetworkType.values());
                    }
                    return new CommandResult(view);
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, String.format("Cannot get customer's data (uri: %s)", req.getRequestURI()), e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Customer's account page is not " +
                        "available");
                return null;
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARN, String.format("Cannot get customer's data - bad id (uri: %s)",
                        req.getRequestURI()), e);
                commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
            }
            return commandResult;
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
            return null;
        }
    }

    private BigInteger getIdFromRequestUri(HttpServletRequest req) throws IllegalArgumentException {
        String path = req.getRequestURI();
        String idFromUri = path.replace(BASE_PATH, "");
        BigInteger id;
        try {
            id = new BigInteger(idFromUri);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect account id");
        }
        if (id.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Account id may not be less than 1");
        }
        return id;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }

    @PostConstruct
    public void init() {
        commissionRate = propertiesKeeper.getBigDecimal(COMMISSION_RATE_PROP).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
}
