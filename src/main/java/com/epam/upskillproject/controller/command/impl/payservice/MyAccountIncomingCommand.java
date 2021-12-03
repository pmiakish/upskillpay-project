package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dao.queryhandler.sqlorder.sort.PaymentSortType;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.util.init.PropertiesKeeper;
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
import java.math.BigInteger;
import java.security.Principal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Singleton
public class MyAccountIncomingCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(MyAccountIncomingCommand.class.getName());

    private static final String DATE_TIME_PATTERN_PROP = "system.datetime.pattern";
    private static final String VIEW_PROP = "servlet.view.myAccountIncoming";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String ACCOUNT_ATTR = "account";
    private static final String FORMATTER_ATTR = "formatter";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccountIncoming.jsp";
    private static final String BASE_PATH = "/controller/payservice/my_account_incoming/";

    private static final RoleType[] roles = {RoleType.CUSTOMER};

    private final PropertiesKeeper propertiesKeeper;
    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public MyAccountIncomingCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                    PropertiesKeeper propertiesKeeper, CustomerService customerService,
                                    SecurityContext securityContext) {
        super(localeDispatcher, paramReader);
        this.propertiesKeeper = propertiesKeeper;
        this.customerService = customerService;
        this.securityContext = securityContext;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            try {
                Person user = customerService.getUserPerson(principal);
                if (user != null) {
                    int pageNumber = paramReader.readPageNumber(req);
                    int pageSize = paramReader.readPageSize(req);
                    Optional<PaymentSortType> sortType = paramReader.readPaymentSort(req, SORT_PARAM);
                    try {
                        BigInteger accountId = getIdFromRequestUri(req);
                        Account account = customerService.getUserAccountById(principal, accountId);
                        req.setAttribute(FORMATTER_ATTR,
                                DateTimeFormatter.ofPattern(propertiesKeeper.getString(DATE_TIME_PATTERN_PROP)));
                        if (account != null) {
                            req.setAttribute(ACCOUNT_ATTR, account);
                            req.setAttribute(PAGE_ATTR, customerService.getUserIncomingPaymentsByAccount(principal,
                                    account.getId(), pageSize, pageNumber, sortType.orElse(null)));
                        }
                        return new CommandResult(view);
                    } catch (SQLException e) {
                        logger.log(Level.ERROR, String.format("Cannot build payments page, uri: %s",
                                req.getRequestURI()), e);
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payments page is not available");
                        return null;
                    } catch (IllegalArgumentException e) {
                        logger.log(Level.WARN, "Cannot build payments page (incorrect account id)", e);
                        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                        return null;
                    }
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.ERROR, "Cannot build accounts page", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Accounts page is not available");
                return null;
            }
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
}
