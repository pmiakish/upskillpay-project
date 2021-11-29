package com.epam.upskillproject.controller.command;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;

public class CommandResult {
    private final boolean success;
    private final RequestDispatcher view;
    private final Integer statusCode;

    public CommandResult() {
        this.success = true;
        this.view = null;
        this.statusCode = HttpServletResponse.SC_OK;
    }

    public CommandResult(RequestDispatcher view) {
        this.success = true;
        this.view = view;
        this.statusCode = HttpServletResponse.SC_OK;
    }

    public CommandResult(boolean success, Integer statusCode) {
        this.success = success;
        this.view = null;
        this.statusCode = statusCode;
    }

    public CommandResult(boolean success, RequestDispatcher view, Integer statusCode) {
        this.success = success;
        this.view = view;
        this.statusCode = statusCode;
    }

    public boolean isSuccessful() {
        return success;
    }

    public RequestDispatcher getView() {
        return view;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandResult that = (CommandResult) o;
        return success == that.success && Objects.equals(view, that.view) && Objects.equals(statusCode, that.statusCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, view, statusCode);
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "success=" + success +
                ", view is present : " + (view != null) +
                ", statusCode=" + statusCode +
                '}';
    }
}
