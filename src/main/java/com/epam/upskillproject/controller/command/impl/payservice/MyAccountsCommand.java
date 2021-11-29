package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.AccountSortType;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.Page;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.util.PermissionType;
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
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class MyAccountsCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(MyAccountsCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.myAccounts";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/payservice/myAccounts.jsp";

    private static final PermissionType[] permissions = {PermissionType.CUSTOMER};

    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public MyAccountsCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                             CustomerService customerService, SecurityContext securityContext) {
        super(localeDispatcher, paramReader);
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
                    req.setAttribute(PAGE_ATTR, buildAccountsPage(req, principal));
                    return new CommandResult(view);
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

    private Page<Account> buildAccountsPage(HttpServletRequest req, Principal principal) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<AccountSortType> sortType = paramReader.readAccountSort(req, SORT_PARAM);
        return customerService.getUserAccountsPage(principal, pageSize, pageNumber, sortType.orElse(null));
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
