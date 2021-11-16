package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.LocaleDispatcher;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/error")
public class LoginErrorServlet extends HttpServlet {

    private static final String VIEW_PROP = "servlet.view.error";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/error.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        view.forward(req, resp);
    }
}



