package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.model.dto.Person;
import com.epam.upskillproject.model.service.SuperadminService;
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
public class AdminProfileCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(AdminProfileCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.adminProfile";
    private static final String ADMIN_ATTR = "admin";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/admin.jsp";
    private static final String BASE_PATH = "/controller/admin/";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN};

    private final SuperadminService superadminService;

    @Inject
    public AdminProfileCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                               SuperadminService superadminService) {
        super(localeDispatcher, paramReader);
        this.superadminService = superadminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CommandResult commandResult;
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        try {
            String path = req.getRequestURI();
            BigInteger id = new BigInteger(path.replace(BASE_PATH, ""));
            if (id.compareTo(BigInteger.ZERO) > 0) {
                Person admin = superadminService.getAdmin(id);
                if (admin != null) {
                    req.setAttribute(ADMIN_ATTR, admin);
                    commandResult = new CommandResult(view);
                } else {
                    req.removeAttribute(ADMIN_ATTR);
                    logger.log(Level.INFO, String.format("Admin with id (%s) not found", id));
                    commandResult = new CommandResult(false, view, HttpServletResponse.SC_NOT_FOUND);
                }
            } else {
                logger.log(Level.WARN, String.format("Incorrect admin's id passed (%s)", id));
                commandResult = new CommandResult(false, view, HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (SQLException e) {
            logger.log(Level.WARN, String.format("Cannot get admin's profile (uri: %s)", req.getRequestURI()), e);
            commandResult = new CommandResult(false, view, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            logger.log(Level.WARN, String.format("Cannot get admin's profile (incorrect id parameter) [uri: %s]",
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
