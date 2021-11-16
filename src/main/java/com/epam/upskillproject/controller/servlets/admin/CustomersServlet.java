package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.model.service.sort.PersonSortType;
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

@WebServlet("/customers")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN", "ADMIN"}))
public class CustomersServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(CustomersServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.customers";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/customers.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private ParamReader paramReader;
    @Inject
    private AdminService adminService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<PersonSortType> sortType = paramReader.readPersonSort(req, SORT_PARAM);
        try {
            req.setAttribute(PAGE_ATTR, adminService.getCustomers(pageSize, pageNumber, sortType.orElse(null)));
            view.forward(req, resp);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build customers page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}



