package com.epam.upskillproject.controller.command;

import com.epam.upskillproject.controller.command.enums.EndpointEnum;
import com.epam.upskillproject.controller.command.enums.TargetType;
import java.util.Objects;

public class CommandRoute {
    private final EndpointEnum endpoint;
    private final TargetType target;

    public CommandRoute(EndpointEnum endpoint, TargetType target) {
        this.endpoint = endpoint;
        this.target = target;
    }

    public EndpointEnum getEndpoint() {
        return endpoint;
    }

    public TargetType getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandRoute that = (CommandRoute) o;
        return endpoint == that.endpoint && target == that.target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, target);
    }

    @Override
    public String toString() {
        return "CommandRoute{" +
                "endpoint=" + endpoint +
                ", target=" + target.name() +
                '}';
    }
}
