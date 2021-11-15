package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.init.PropertiesKeeper;
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

@WebServlet("/income")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN"}))
public class IncomeServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(IncomeServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.income";
    private static final String INCOME_BALANCE_ATTR = "balance";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/admin/income.jsp";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private String viewPath;

    @Inject
    private SuperadminService superadminService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        try {
            req.setAttribute(INCOME_BALANCE_ATTR, superadminService.getIncomeBalance());
            view.forward(req, resp);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot get system income value", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.viewPath = propertiesKeeper.getStringOrDefault(VIEW_PROP, DEFAULT_VIEW);
    }
}
