package com.epam.upskillproject.controller.command.impl.common;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.service.SystemService;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.view.tag.OperationType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class UserUpdateCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(UserUpdateCommand.class.getName());

    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";
    private static final String HASH_PARAM = "hash";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String PASSWORD_PARAM = "pass";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";

    private static final RoleType[] roles = {RoleType.SUPERADMIN, RoleType.ADMIN,
            RoleType.CUSTOMER};

    private final PropertiesKeeper propertiesKeeper;
    private final SystemService systemService;
    private final SecurityContext securityContext;
    private final Pbkdf2PasswordHash passwordHash;

    @Inject
    public UserUpdateCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                             PropertiesKeeper propertiesKeeper, SystemService systemService,
                             SecurityContext securityContext, Pbkdf2PasswordHash passwordHash) {
        super(localeDispatcher, paramReader);
        this.propertiesKeeper = propertiesKeeper;
        this.systemService = systemService;
        this.securityContext = securityContext;
        this.passwordHash = passwordHash;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        Principal principal = securityContext.getCallerPrincipal();
        Optional<Integer> hash = paramReader.readInteger(req, HASH_PARAM);
        if (principal != null) {
            try {
                if (hash.isEmpty() || !systemService.checkPersonHash(principal.getName(), hash.get())) {
                    logger.log(Level.WARN, String.format("Cannot update user's profile: uncommitted changes were " +
                            "found (principal: %s)", principal.getName()));
                    setOperationError(req, OperationType.UPDATE, "Uncommitted changes were found");
                    commandResult = new CommandResult(false, HttpServletResponse.SC_CONFLICT);
                } else {
                    boolean updated = systemService.updateUser(
                            principal.getName(),
                            paramReader.readString(req, PASSWORD_PARAM).isPresent() ?
                                    passwordHash.generate(paramReader.readString(req, PASSWORD_PARAM).get().toCharArray()) :
                                    null,
                            paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                            paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new)
                    );
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                    req.setAttribute(OPERATION_STATUS_ATTR, updated);
                    commandResult = new CommandResult(updated, (updated) ? HttpServletResponse.SC_OK :
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } catch (SQLException e) {
                logger.log(Level.WARN, String.format("Exception thrown during profile update operation " +
                        "(principal: %s)", principal.getName()), e);
                setOperationError(req, OperationType.UPDATE, "Cannot update user's profile (internal server error). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (InvalidParameterException e) {
                logger.log(Level.WARN, String.format("Cannot update profile (bad parameters passed) [principal: %s]",
                        principal.getName()), e);
                setOperationError(req, OperationType.UPDATE, "Cannot update user's profile (incorrect parameters " +
                        "passed). ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
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
