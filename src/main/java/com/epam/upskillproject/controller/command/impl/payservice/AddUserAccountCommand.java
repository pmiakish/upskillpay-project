package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.exceptions.AccountLimitException;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.util.PermissionType;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.Principal;
import java.sql.SQLException;

@Singleton
public class AddUserAccountCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(AddUserAccountCommand.class.getName());

    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final PermissionType[] permissions = {PermissionType.CUSTOMER};

    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public AddUserAccountCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                 CustomerService customerService, SecurityContext securityContext) {
        super(localeDispatcher, paramReader);
        this.customerService = customerService;
        this.securityContext = securityContext;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        Principal principal = securityContext.getCallerPrincipal();
        if (principal != null) {
            try {
                Person user = customerService.getUserPerson(principal);
                if (user != null) {
                    boolean created = customerService.addUserAccount(principal);
                    if (created) {
                        req.setAttribute(OPERATION_NAME_ATTR, OperationType.CREATE);
                        req.setAttribute(OPERATION_STATUS_ATTR, true);
                        commandResult = new CommandResult(true, HttpServletResponse.SC_CREATED);
                    } else {
                        logger.log(Level.WARN, String.format("Account creation failed (principal: %s)",
                                principal.getName()));
                        setOperationError(req, OperationType.CREATE, "Cannot create an account. ");
                        commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Cannot add user's account (principal: %s)", principal.getName()),
                        e);
                setOperationError(req, OperationType.CREATE, "Cannot add user's account (internal server error). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (AccountLimitException e) {
                logger.log(Level.WARN, String.format("Cannot add an account: limit was exceeded (principal: %s)",
                        principal.getName()), e);
                setOperationError(req, OperationType.CREATE, e.getMessage());
                commandResult = new CommandResult(false, HttpServletResponse.SC_CONFLICT);
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
            return null;
        }
        return commandResult;
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
