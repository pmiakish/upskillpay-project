package com.epam.upskillproject.controller.command;

import com.epam.upskillproject.controller.command.enumeration.EndpointEnum;
import com.epam.upskillproject.controller.command.enumeration.TargetType;
import jakarta.ejb.Singleton;

@Singleton
public class CommandRouteFactory {

    public CommandRoute produce(EndpointEnum endpoint, TargetType target) {
        return new CommandRoute(endpoint, target);
    }

}
