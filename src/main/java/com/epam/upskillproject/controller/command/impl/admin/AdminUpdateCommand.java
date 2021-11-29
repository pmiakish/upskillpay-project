package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.SuperadminService;
import com.epam.upskillproject.util.PermissionType;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class AdminUpdateCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(AdminUpdateCommand.class.getName());

    private static final String STATUS_ACTIVE_PARAM = "active";
    private static final String STATUS_ACTIVE_VALUE = "on";
    private static final String ID_PARAM = "id";
    private static final String HASH_PARAM = "hash";
    private static final String PERMISSION_PARAM = "permission";
    private static final String EMAIL_PARAM = "email";
    private static final String PASSWORD_PARAM = "pass";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String REG_DATE_PARAM = "regDate";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN};

    private final PropertiesKeeper propertiesKeeper;
    private final SuperadminService superadminService;
    private final Pbkdf2PasswordHash passwordHash;

    @Inject
    public AdminUpdateCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                              PropertiesKeeper propertiesKeeper, SuperadminService superadminService,
                              Pbkdf2PasswordHash passwordHash) {
        super(localeDispatcher, paramReader);
        this.propertiesKeeper = propertiesKeeper;
        this.superadminService = superadminService;
        this.passwordHash = passwordHash;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) {
        CommandResult commandResult;
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<Integer> adminHash = paramReader.readInteger(req, HASH_PARAM);
        try {
            if (id.isEmpty() || id.get().compareTo(BigInteger.ZERO) <= 0) {
                logger.log(Level.WARN, String.format("Cannot update admin (bad id parameter) [id: %s]",
                        id.orElse(null)));
                setOperationError(req, OperationType.UPDATE, "Incorrect or missing admin's ID. ");
                commandResult = new CommandResult(false, HttpServletResponse.SC_BAD_REQUEST);
            } else {
                boolean updated;
                Person controlAdminInstance = superadminService.getAdmin(id.get());
                if (controlAdminInstance == null || adminHash.isEmpty() ||
                        !adminHash.get().equals(controlAdminInstance.getHash())) {
                    logger.log(Level.WARN, String.format("Cannot update admin (person's hash was changed) [id: %s]",
                            id.get()));
                    setOperationError(req, OperationType.UPDATE, "Uncommitted changes were found. ");
                    commandResult = new CommandResult(false, HttpServletResponse.SC_CONFLICT);
                } else {
                    updated = superadminService.updateAdmin(
                            paramReader.readBigInteger(req, ID_PARAM).orElseThrow(InvalidParameterException::new),
                            paramReader.readPermissionType(req, PERMISSION_PARAM).orElse(null),
                            paramReader.readString(req, EMAIL_PARAM).orElseThrow(InvalidParameterException::new),
                            paramReader.readString(req, PASSWORD_PARAM).isPresent() ?
                                    passwordHash.generate(paramReader.readString(req, PASSWORD_PARAM).get().toCharArray()) : null,
                            paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                            paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                            (STATUS_ACTIVE_VALUE.equals(paramReader.readString(req, STATUS_ACTIVE_PARAM).orElse(""))) ?
                                    StatusType.ACTIVE : StatusType.BLOCKED,
                            paramReader.readLocalDate(req, REG_DATE_PARAM).orElseThrow(InvalidParameterException::new)
                    );
                    req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                    req.setAttribute(OPERATION_STATUS_ATTR, updated);
                    commandResult = new CommandResult(updated, (updated) ? HttpServletResponse.SC_OK :
                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARN, String.format("Cannot update admin (id: %s)", id.get()), e);
            setOperationError(req, OperationType.UPDATE, "Cannot update admin (internal server error). ");
            commandResult = new CommandResult(false, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return commandResult;
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
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
