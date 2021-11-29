package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
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
import java.math.BigInteger;
import java.sql.SQLException;

@Singleton
public class CustomerProfileCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(CustomerProfileCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.customerProfile";
    private static final String CUSTOMER_ATTR = "customer";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/customer.jsp";
    private static final String BASE_PATH = "/controller/customer/";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN, PermissionType.ADMIN};

    private final AdminService adminService;

    @Inject
    public CustomerProfileCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                  AdminService adminService) {
        super(localeDispatcher, paramReader);
        this.adminService = adminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        try {
            String path = req.getRequestURI();
            BigInteger id = new BigInteger(path.replace(BASE_PATH, ""));
            if (id.compareTo(BigInteger.ZERO) > 0) {
                Person customer = adminService.getCustomer(id);
                if (customer != null) {
                    req.setAttribute(CUSTOMER_ATTR, customer);
                    commandResult = new CommandResult(view);
                } else {
                    req.removeAttribute(CUSTOMER_ATTR);
                    logger.log(Level.INFO, String.format("Customer with id (%s) not found", id));
                    commandResult = new CommandResult(false, view, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                logger.log(Level.WARN, String.format("Incorrect customer's id passed (%s)", id));
                commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            logger.log(Level.WARN, String.format("Cannot get customer's profile (uri: %s)", req.getRequestURI()), e);
            commandResult = new CommandResult(false, view, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            logger.log(Level.WARN, String.format("Cannot get customer's profile (incorrect id parameter) [uri: %s]",
                    req.getRequestURI()), e);
            commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
        }
        return commandResult;
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
