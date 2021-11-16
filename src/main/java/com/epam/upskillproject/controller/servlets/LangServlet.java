package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.ParamReader;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/lang")
public class LangServlet extends HttpServlet {

    private static final String URI_PARAM = "uri";
    private static final String LOCALE_PARAM = "locale";
    private static final String SESSION_LOCALE_ATTR = "sessLoc";

    @Inject
    private ParamReader paramReader;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Optional<String> previousUri = paramReader.readString(req, URI_PARAM);
        Optional<String> locale = paramReader.readString(req, LOCALE_PARAM);
        if (previousUri.isPresent() && locale.isPresent()) {
            HttpSession session = req.getSession();
            session.setAttribute(SESSION_LOCALE_ATTR, locale.get());
            resp.sendRedirect(previousUri.get());
        } else if (previousUri.isPresent()) {
            resp.sendRedirect(previousUri.get());
        } else {
            resp.sendRedirect("/");
        }
    }
}
