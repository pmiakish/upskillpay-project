package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.exception.AccountLimitException;
import com.epam.upskillproject.exception.PaymentParamException;
import com.epam.upskillproject.exception.TransactionException;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.CustomerService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.view.tag.OperationType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class TopupUserAccountCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(TopupUserAccountCommand.class.getName());

    private static final String ACCOUNT_ID_PARAM = "accountId";
    private static final String AMOUNT_PARAM = "amount";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final RoleType[] roles = {RoleType.CUSTOMER};

    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public TopupUserAccountCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
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
                    Optional<BigDecimal> amount = paramReader.readBigDecimal(req, AMOUNT_PARAM);
                    if (accountId.isPresent() && amount.isPresent() &&
                            customerService.topUpAccount(principal, accountId.get(), amount.get())) {
                        req.setAttribute(OPERATION_NAME_ATTR, OperationType.PAYMENT);
                        req.setAttribute(OPERATION_STATUS_ATTR, true);
                        commandResult = new CommandResult(true, HttpServletResponse.SC_OK);
                    } else {
                        logger.log(Level.WARN, String.format("Account top up failed (principal: %s, accountId: %s, " +
                                "amount: %s)", principal.getName(), accountId.orElse(null), amount.orElse(null)));
                        setOperationError(req, OperationType.CREATE, "Cannot top up account (check parameters). ");
                        commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
                    }
                } else {
                    logger.log(Level.ERROR, String.format("Cannot get principal person from database (principal: %s)",
                            principal.getName()));
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get principal person from database");
                    return null;
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Cannot top up user's account (principal: %s)", principal.getName()),
                        e);
                setOperationError(req, OperationType.PAYMENT, "Cannot top up user's account (internal server " +
                        "error). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (AccountLimitException e) {
                logger.log(Level.WARN, String.format("Cannot top up an account: limit was exceeded (principal: %s)",
                        principal.getName()), e);
                setOperationError(req, OperationType.PAYMENT, e.getMessage());
                commandResult = new CommandResult(false, HttpServletResponse.SC_CONFLICT);
            } catch (PaymentParamException e) {
                logger.log(Level.WARN, String.format("Cannot perform payment (bad parameters) [principal: %s]",
                        principal.getName()), e);
                setOperationError(req, OperationType.PAYMENT, e.getMessage());
                commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
            } catch (TransactionException e) {
                logger.log(Level.WARN, String.format("Cannot perform payment (transaction failed) [principal: %s]",
                        principal.getName()), e);
                setOperationError(req, OperationType.PAYMENT, "Cannot perform payment. " + e.getType().getMessage());
                commandResult = new CommandResult(false, e.getStatusCode());
            }
        } else {
            logger.log(Level.ERROR, "Caller principal is null");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot get caller principal");
            return null;
        }
        return commandResult;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
