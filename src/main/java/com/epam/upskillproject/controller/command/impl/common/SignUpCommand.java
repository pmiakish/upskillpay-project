package com.epam.upskillproject.controller.command.impl.common;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.exception.CustomSQLCode;
import com.epam.upskillproject.exception.TransactionException;
import com.epam.upskillproject.model.service.SystemService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.view.tag.OperationType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SignUpCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(SignUpCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.signup";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String EMAIL_PARAM = "email";
    private static final String PASSWORD_PARAM = "pass";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/signup.jsp";

    private static final RoleType[] roles = {};

    private final PropertiesKeeper propertiesKeeper;
    private final SystemService systemService;
    private final Pbkdf2PasswordHash passwordHash;

    @Inject
    public SignUpCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                         PropertiesKeeper propertiesKeeper, SystemService systemService,
                         Pbkdf2PasswordHash passwordHash) {
        super(localeDispatcher, paramReader);
        this.propertiesKeeper = propertiesKeeper;
        this.systemService = systemService;
        this.passwordHash = passwordHash;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        req.setAttribute(OPERATION_NAME_ATTR, OperationType.CREATE.name());
        try {
            boolean created = systemService.addCustomer(
                    paramReader.readString(req, EMAIL_PARAM).orElseThrow(InvalidParameterException::new),
                    passwordHash.generate(
                            paramReader.readString(req, PASSWORD_PARAM).orElseThrow(InvalidParameterException::new)
                                    .toCharArray()
                    ),
                    paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                    paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new)
            );
            req.setAttribute(OPERATION_STATUS_ATTR, created);
            commandResult =  new CommandResult(created, view, (created) ? HttpServletResponse.SC_CREATED :
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) {
                logger.log(Level.WARN, "Cannot create profile: such email already exists", e);
                setOperationError(req, OperationType.CREATE, "Cannot create profile (such email already exists). ");
                commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
            } else if (e.getErrorCode() == CustomSQLCode.INVALID_STATEMENT_PARAMETER.getCode()) {
                logger.log(Level.WARN, "Cannot create profile: bad parameters", e);
                setOperationError(req, OperationType.CREATE, "Cannot create profile (Check up correctness of " +
                        "all input data). ");
                commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                logger.log(Level.ERROR, "Cannot create profile", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot create profile (internal server error). ");
                return null;
            }
        } catch (InvalidParameterException e) {
            logger.log(Level.WARN, "Cannot create profile (incorrect parameters passed)", e);
            setOperationError(req, OperationType.CREATE, "Cannot create profile (incorrect parameters passed). ");
            commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
        } catch (TransactionException e) {
            logger.log(Level.WARN, "Cannot create profile (transaction failed)", e);
            setOperationError(req, OperationType.CREATE, "Cannot create profile. " + e.getType().getMessage());
            commandResult = new CommandResult(false, view, e.getStatusCode());
        }
        return commandResult;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }

    @PostConstruct
    public void init() {
        // Pbkdf2PasswordHash parameters
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }
}
