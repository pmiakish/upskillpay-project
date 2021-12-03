package com.epam.upskillproject.controller.command.impl.common;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.util.RoleType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class ChangeLangCommand extends AbstractCommand {

    private static final String URI_PARAM = "uri";
    private static final String LOCALE_PARAM = "locale";
    private static final String SESSION_LOCALE_ATTR = "sessLoc";
    private static final RoleType[] roles = {};

    @Inject
    public ChangeLangCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader) {
        super(localeDispatcher, paramReader);
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Optional<String> previousUri = paramReader.readString(req, URI_PARAM);
        Optional<String> locale = paramReader.readString(req, LOCALE_PARAM);
        if (previousUri.isPresent() && locale.isPresent()) {
            HttpSession session = req.getSession();
            session.setAttribute(SESSION_LOCALE_ATTR, locale.get().toUpperCase());
            resp.sendRedirect(previousUri.get());
        } else if (previousUri.isPresent()) {
            resp.sendRedirect(previousUri.get());
        } else {
            resp.sendRedirect("/");
        }
        return null;
    }

    @Override
    public RoleType[] getRoles() {
        return roles;
    }
}
