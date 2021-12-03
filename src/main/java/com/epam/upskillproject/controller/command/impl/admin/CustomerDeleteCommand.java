package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.exception.TransactionException;
import com.epam.upskillproject.model.service.SuperadminService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.view.tag.OperationType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.util.Optional;

@Singleton
public class CustomerDeleteCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(CustomerDeleteCommand.class.getName());

    private static final String ID_PARAM = "id";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final RoleType[] roles = {RoleType.SUPERADMIN};

    private final SuperadminService superadminService;

    @Inject
    public CustomerDeleteCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                                 SuperadminService superadminService) {
        super(localeDispatcher, paramReader);
        this.superadminService = superadminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) {
        CommandResult commandResult;
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        try {
            if (id.isEmpty() || id.get().compareTo(BigInteger.ZERO) <= 0) {
                logger.log(Level.WARN, String.format("Cannot delete customer (bad id parameter) [id: %s]",
                        id.orElse(null)));
                setOperationError(req, OperationType.DELETE, "Incorrect or missing customer's ID. ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                boolean deleted = superadminService.deletePerson(id.get());
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.DELETE.name());
                req.setAttribute(OPERATION_STATUS_ATTR, deleted);
                commandResult = new CommandResult(deleted, (deleted) ? HttpServletResponse.SC_OK :
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }  catch (TransactionException e) {
            logger.log(Level.WARN, String.format("Cannot delete customer (id: %s) - transaction failed", id.orElse(null)),
                    e);
            setOperationError(req, OperationType.DELETE, e.getType().getMessage());
            commandResult = new CommandResult(false, e.getStatusCode());
        }
        return commandResult;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
