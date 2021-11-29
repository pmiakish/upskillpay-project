package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import com.epam.upskillproject.model.dto.Page;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.util.PermissionType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class CustomerListCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(CustomerListCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.customers";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/customers.jsp";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN, PermissionType.ADMIN};

    private final AdminService adminService;

    @Inject
    public CustomerListCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                               AdminService adminService) {
        super(localeDispatcher, paramReader);
        this.adminService = adminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
            req.setAttribute(PAGE_ATTR, buildCustomersPage(req));
            return new CommandResult(view);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build customers page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Customers page is not available");
            return null;
        }
    }

    private Page<Person> buildCustomersPage(HttpServletRequest req) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<PersonSortType> sortType = paramReader.readPersonSort(req, SORT_PARAM);
        return adminService.getCustomers(pageSize, pageNumber, sortType.orElse(null));
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
