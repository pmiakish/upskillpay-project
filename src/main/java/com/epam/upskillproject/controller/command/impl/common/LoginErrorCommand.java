package com.epam.upskillproject.controller.command.impl.common;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.util.PermissionType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Singleton
public class LoginErrorCommand extends AbstractCommand {

    private static final String VIEW_PROP = "servlet.view.error";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/error.jsp";
    private static final PermissionType[] permissions = {};

    @Inject
    public LoginErrorCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader) {
        super(localeDispatcher, paramReader);
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
        return new CommandResult(view);
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
