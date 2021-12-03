package com.epam.upskillproject.controller.command.impl;

import com.epam.upskillproject.controller.command.Command;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.util.RoleType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class CommandWrapper implements Command {

    private final Command baseCommand;
    private final Command wrapperCommand;

    public CommandWrapper(Command baseCommand, Command wrapperCommand) {
        this.baseCommand = baseCommand;
        this.wrapperCommand = wrapperCommand;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CommandResult baseCommandResult = baseCommand.execute(req, resp);
        if (baseCommandResult != null) {
            // external command execution
            CommandResult wrapperCommandResult = wrapperCommand.execute(req, resp);
            return new CommandResult(
                    (baseCommandResult.isSuccessful() && wrapperCommandResult.isSuccessful()),
                    wrapperCommandResult.getView(),
                    (!baseCommandResult.isSuccessful()) ? baseCommandResult.getStatusCode() :
                            wrapperCommandResult.getStatusCode()
            );
        // case when inner command was not executed correctly and error sending is assumed
        } else {
            return null;
        }
    }

    @Override
    // Returns only those Roles that are contained in both commands
    public RoleType[] getRoles() {
        RoleType[] baseCommandRoles = baseCommand.getRoles();
        RoleType[] wrapperCommandRoles = wrapperCommand.getRoles();
        return Arrays.stream(baseCommandRoles)
                .filter(bp -> Arrays.asList(wrapperCommandRoles).contains(bp))
                .toArray(RoleType[]::new);
    }
}
