package com.epam.upskillproject.controller.command;

import com.epam.upskillproject.util.RoleType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Command {
    CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
    RoleType[] getRoles();
}
