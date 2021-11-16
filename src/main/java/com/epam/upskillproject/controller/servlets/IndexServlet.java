package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.LocaleDispatcher;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("")
public class IndexServlet extends HttpServlet {

    private static final String VIEW_PROP = "servlet.view.index";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/index.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        req.getRequestDispatcher(viewPath).forward(req, resp);
    }
}
