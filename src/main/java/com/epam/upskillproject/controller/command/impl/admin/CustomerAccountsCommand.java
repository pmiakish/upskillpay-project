package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.util.RoleType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

@Singleton
public class CustomerAccountsCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(CustomerAccountsCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.customerAccounts";
    private static final String CUSTOMER_ATTR = "customer";
    private static final String ACCOUNTS_ATTR = "accounts";
    private static final String CARDS_ATTR = "cards";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/customerAccounts.jsp";
    private static final String BASE_PATH = "/controller/customer/accounts/";

    private static final RoleType[] roles = {RoleType.SUPERADMIN, RoleType.ADMIN};

    private final AdminService adminService;

    @Inject
    public CustomerAccountsCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                   AdminService adminService) {
        super(localeDispatcher, paramReader);
        this.adminService = adminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        try {
            Person customer = getCustomer(req);
            if (customer != null) {
                req.setAttribute(CUSTOMER_ATTR, customer);
                req.setAttribute(ACCOUNTS_ATTR, getAccounts(req));
                req.setAttribute(CARDS_ATTR, getCards(req));
                commandResult = new CommandResult(view);
            } else {
                logger.log(Level.ERROR, String.format("Customer not found (uri: %s)", req.getRequestURI()));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found");
                return null;
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot get customer's accounts (uri: %s)", req.getRequestURI()), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Customer's accounts page is not available");
            return null;
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARN, String.format("Cannot get customer's accounts (uri: %s)", req.getRequestURI()), e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return null;
        }
        return commandResult;
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
        String idFromUri = path.replace(BASE_PATH, "");
        BigInteger id;
        try {
            id = new BigInteger(idFromUri);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Incorrect customer's id");
        }
        if (id.compareTo(BigInteger.ONE) < 0) {
            throw new IllegalArgumentException("Customer's id may not be less than 1");
        }
        return id;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
