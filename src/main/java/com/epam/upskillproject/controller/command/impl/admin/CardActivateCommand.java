package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.AdminService;
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
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class CardActivateCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(CardActivateCommand.class.getName());

    private static final String ID_PARAM = "id";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final RoleType[] roles = {RoleType.SUPERADMIN, RoleType.ADMIN};

    private final AdminService adminService;

    @Inject
    public CardActivateCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader, AdminService adminService) {
        super(localeDispatcher, paramReader);
        this.adminService = adminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) {
        CommandResult commandResult;
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        if (id.isPresent()) {
            try {
                boolean updated;
                BigInteger accountId = adminService.getAccountIdByCardId(id.get());
                if (accountId != null && !adminService.getAccountStatus(accountId).equals(StatusType.BLOCKED)) {
                    updated = adminService.updateCardStatus(id.get(), StatusType.ACTIVE);
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                    req.setAttribute(OPERATION_STATUS_ATTR, updated);
                    commandResult = new CommandResult(updated, (updated) ? HttpServletResponse.SC_OK :
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    setOperationError(req, OperationType.UPDATE, "You must activate the account before the card" +
                            " activation! ");
                    commandResult = new CommandResult(false, HttpServletResponse.SC_FORBIDDEN);
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Cannot activate card (id: %s)", id.orElse(null)), e);
                setOperationError(req, OperationType.UPDATE, "Cannot activate card (internal server error). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.log(Level.WARN, "Cannot activate card (id is not passed)");
            setOperationError(req, OperationType.UPDATE, "Card id is not passed. ");
            commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
        }
        return commandResult;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
