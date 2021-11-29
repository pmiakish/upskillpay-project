package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
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
import java.math.BigInteger;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class BlockUserAccountCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(BlockUserAccountCommand.class.getName());

    private static final String ACCOUNT_ID_PARAM = "accountId";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final PermissionType[] permissions = {PermissionType.CUSTOMER};

    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public BlockUserAccountCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
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
                    Optional<BigInteger> accountId = paramReader.readBigInteger(req, ACCOUNT_ID_PARAM);
                    if (accountId.isPresent()) {
                        boolean blocked = customerService.blockUserAccount(principal, accountId.get());
                        req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE);
                        req.setAttribute(OPERATION_STATUS_ATTR, blocked);
                        commandResult = new CommandResult(blocked, (blocked) ? HttpServletResponse.SC_OK :
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } else {
                        logger.log(Level.WARN, String.format("Account blocking failed (principal: %s, accountId: %s)",
                                principal.getName(), accountId.orElse(null)));
                        setOperationError(req, OperationType.UPDATE, "Cannot block an account (id not passed). ");
                        commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Cannot block user's account (principal: %s)", principal.getName()),
                        e);
                setOperationError(req, OperationType.UPDATE, "Cannot block user's account (internal server " +
                        "error). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
