package com.epam.upskillproject.controller.command.impl;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.Command;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.util.RoleType;
import com.epam.upskillproject.view.tag.OperationType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public abstract class AbstractCommand implements Command {

    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";

    protected final LocaleDispatcher localeDispatcher;
    protected final ParamReader paramReader;

    public AbstractCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader) {
        this.localeDispatcher = localeDispatcher;
        this.paramReader = paramReader;
    }

    public abstract CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    public abstract RoleType[] getRoles();

    protected void setOperationError(HttpServletRequest req, OperationType operationType, String message) {
        req.setAttribute(OPERATION_NAME_ATTR, operationType.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        buildErrorMessage(req, message);
    }

    protected void buildErrorMessage(HttpServletRequest req, String message) {
        Optional<String> errMsg = paramReader.readString(req, ERROR_MESSAGE_ATTR);
        req.setAttribute(ERROR_MESSAGE_ATTR, errMsg.orElse("").concat((message != null) ? message : ""));
    }

    protected RequestDispatcher getView(HttpServletRequest req, String viewProp, String defaultViewPath) {
        String localizedView = localeDispatcher.getLocalizedView(req, viewProp);
        String viewPath = (localizedView.length() > 0) ? localizedView : defaultViewPath;
        return req.getRequestDispatcher(viewPath);
    }

}
