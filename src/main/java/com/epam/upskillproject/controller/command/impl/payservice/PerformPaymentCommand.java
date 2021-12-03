package com.epam.upskillproject.controller.command.impl.payservice;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
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
public class PerformPaymentCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(PerformPaymentCommand.class.getName());

    private static final String CARD_ID_PARAM = "cardId";
    private static final String CVC_PARAM = "cvc";
    private static final String RECEIVER_PARAM = "receiver";
    private static final String AMOUNT_PARAM = "amount";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final RoleType[] roles = {RoleType.CUSTOMER};

    private final CustomerService customerService;
    private final SecurityContext securityContext;

    @Inject
    public PerformPaymentCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
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
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.PAYMENT);
                    Optional<BigInteger> cardId = paramReader.readBigInteger(req, CARD_ID_PARAM);
                    Optional<String> cvc = paramReader.readString(req, CVC_PARAM);
                    Optional<BigInteger> receiver = paramReader.readBigInteger(req, RECEIVER_PARAM);
                    Optional<BigDecimal> amount = paramReader.readBigDecimal(req, AMOUNT_PARAM);
                    if (cardId.isPresent() && cvc.isPresent() && receiver.isPresent() && amount.isPresent()) {
                        try {
                            customerService.performPayment(principal, cardId.get(), cvc.get(), receiver.get(), amount.get());
                            req.setAttribute(OPERATION_STATUS_ATTR, true);
                            commandResult = new CommandResult();
                        } catch (SQLException e) {
                            logger.log(Level.ERROR, String.format("Cannot perform payment (payer card: %s, receiver " +
                                    "account: %s, amount: %s)", cardId.get(), receiver.get(), amount.get()), e);
                            setOperationError(req, OperationType.PAYMENT, "Cannot perform payment. ");
                            commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        } catch (PaymentParamException e) {
                            logger.log(Level.WARN, String.format("Cannot perform payment - bad parameters (payer " +
                                    "card: %s, receiver account: %s, amount: %s)", cardId.get(), receiver.get(),
                                    amount.get()), e);
                            setOperationError(req, OperationType.PAYMENT, "Cannot perform payment: incorrect " +
                                    "parameters passed. ");
                            commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
                        } catch (TransactionException e) {
                            logger.log(Level.WARN, String.format("Cannot perform payment - transaction failed (payer " +
                                    "card: %s, receiver account: %s, amount: %s)", cardId.get(), receiver.get(),
                                    amount.get()), e);
                            setOperationError(req, OperationType.PAYMENT, String.format("Cannot perform " +
                                    "payment. %s", e.getType().getMessage()));
                            commandResult = new CommandResult(false, e.getStatusCode());
                        }
                    } else {
                        logger.log(Level.WARN, String.format("Cannot perform payment (incorrect parameters passed) " +
                                "[payer card: %s, cvc is present: %s, receiver: %s, amount: %s]", cardId.orElse(null),
                                cvc.isPresent(), receiver.orElse(null), amount.orElse(null)));
                        setOperationError(req, OperationType.PAYMENT, "Cannot perform payment: incorrect " +
                                "parameters passed. ");
                        commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
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
        return commandResult;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
