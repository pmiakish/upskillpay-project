package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PersonSortType;
import com.epam.upskillproject.model.dto.Page;
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
import java.sql.SQLException;
import java.util.Optional;

@Singleton
public class AdminListCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(AdminListCommand.class.getName());

    private static final String VIEW_PROP = "servlet.view.admins";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/admins.jsp";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN};

    private final SuperadminService superadminService;

    @Inject
    public AdminListCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader,
                            SuperadminService superadminService) {
        super(localeDispatcher, paramReader);
        this.superadminService = superadminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
            req.setAttribute(PAGE_ATTR, buildAdminsPage(req));
            return new CommandResult(view);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build admins page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Admins page is not available");
            return null;
        }
    }

    private Page<Person> buildAdminsPage(HttpServletRequest req) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<PersonSortType> sortType = paramReader.readPersonSort(req, SORT_PARAM);
        return superadminService.getAdmins(pageSize, pageNumber, sortType.orElse(null));
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
