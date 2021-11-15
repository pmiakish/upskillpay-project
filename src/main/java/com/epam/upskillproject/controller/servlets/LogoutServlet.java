package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.init.PropertiesKeeper;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final String VIEW_PROP = "servlet.view.logout";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/logout.jsp";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private String viewPath;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        req.logout();
        session.invalidate();
        req.getRequestDispatcher(viewPath).forward(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.viewPath = propertiesKeeper.getStringOrDefault(VIEW_PROP, DEFAULT_VIEW);
    }
}



