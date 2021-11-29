package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
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
import java.sql.SQLException;

@Singleton
public class SystemIncomeCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(SystemIncomeCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.income";
    private static final String INCOME_BALANCE_ATTR = "balance";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/income.jsp";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN};

    private final SuperadminService superadminService;

    @Inject
    public SystemIncomeCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                               SuperadminService superadminService) {
        super(localeDispatcher, paramReader);
        this.superadminService = superadminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
            req.setAttribute(INCOME_BALANCE_ATTR, superadminService.getIncomeBalance());
            return new CommandResult(view);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot get system income value", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "System balance value is not available");
            return null;
        }
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
