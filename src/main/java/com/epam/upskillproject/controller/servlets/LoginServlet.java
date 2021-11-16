package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.LocaleDispatcher;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VIEW_PROP = "servlet.view.login";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/login.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        req.logout();
        session.invalidate();
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        view.forward(req, resp);
    }
}



