package com.epam.upskillproject.controller.servlet;

import com.epam.upskillproject.controller.command.ActionFactory;
import com.epam.upskillproject.controller.command.Command;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.exception.CommandNotFoundException;
import com.epam.upskillproject.util.RoleType;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/controller/*")
public class ControllerServlet extends HttpServlet {

    private static final String TARGET_ATTR = "command";
    private static final String GET_TARGET_VALUE = "GET";

    @Inject
    private ActionFactory actionFactory;
    @Inject
    private SecurityContext securityContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute(TARGET_ATTR, GET_TARGET_VALUE);
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Command command = actionFactory.produce(req, resp);
            if (command.getRoles().length > 0) {
                req.authenticate(resp);
                if (checkCallerRoles(command)) {
                    applyCommand(req, resp, command);
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have sufficient privileges to " +
                            "view this page");
                }
            } else {
                applyCommand(req, resp, command);
            }
        } catch (CommandNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Page not found");
        }
    }

    private boolean checkCallerRoles(Command command) {
        RoleType[] allowedRoles = command.getRoles();
        for (RoleType role : allowedRoles) {
            if (securityContext.isCallerInRole(role.getType())) {
                return true;
            }
        }
        return false;
    }

    private void applyCommand(HttpServletRequest req, HttpServletResponse resp, Command command)
            throws ServletException, IOException {
        CommandResult commandResult = command.execute(req, resp);
        if (commandResult != null && !resp.isCommitted()) {
            resp.setStatus(commandResult.getStatusCode());
            RequestDispatcher view = commandResult.getView();
            view.forward(req, resp);
        }
    }
}
