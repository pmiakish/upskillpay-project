package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.init.PropertiesKeeper;
import com.epam.upskillproject.model.service.sort.PersonSortType;
import com.epam.upskillproject.model.service.SuperadminService;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/admins")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN"}))
public class AdminsServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AdminsServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.admins";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/admin/admins.jsp";

    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SuperadminService superadminService;

    private String viewPath;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<PersonSortType> sortType = paramReader.readPersonSort(req, SORT_PARAM);
        try {
            req.setAttribute(PAGE_ATTR, superadminService.getAdmins(pageSize, pageNumber, sortType.orElse(null)));
            RequestDispatcher view = req.getRequestDispatcher(viewPath);
            view.forward(req, resp);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build admins page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.viewPath = propertiesKeeper.getStringOrDefault(VIEW_PROP, DEFAULT_VIEW);
    }
}


