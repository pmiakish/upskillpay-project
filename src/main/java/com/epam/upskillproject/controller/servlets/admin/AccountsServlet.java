package com.epam.upskillproject.controller.servlets.admin;

import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.model.dto.Account;
import com.epam.upskillproject.model.dto.StatusType;
import com.epam.upskillproject.model.service.sort.AccountSortType;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.model.dto.Page;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/accounts")
@ServletSecurity(@HttpConstraint(rolesAllowed = {"SUPERADMIN", "ADMIN"}))
public class AccountsServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AccountsServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.accounts";
    private static final String ID_PARAM = "id";
    private static final String SORT_PARAM = "sort";
    private static final String CURRENT_STATUS_PARAM = "currentStatus";
    private static final String PAGE_ATTR = "page";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/accounts.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private ParamReader paramReader;
    @Inject
    private AdminService adminService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
            String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
            req.setAttribute(PAGE_ATTR, buildAccountsPage(req));
            RequestDispatcher view = req.getRequestDispatcher(viewPath);
            view.forward(req, resp);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build accounts page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        Optional<BigInteger> id = paramReader.readBigInteger(req, ID_PARAM);
        Optional<StatusType> currentStatus = paramReader.readStatusType(req, CURRENT_STATUS_PARAM);
        try {
            if (id.isPresent() && currentStatus.isPresent()) {
                boolean updated = adminService.updateAccountStatus(
                        id.get(),
                        (currentStatus.get() == StatusType.ACTIVE) ? StatusType.BLOCKED : StatusType.ACTIVE
                );
                req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
                req.setAttribute(OPERATION_STATUS_ATTR, updated);
                resp.setStatus((updated) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } else {
                logger.log(Level.WARN, String.format("Cannot update account status (bad parameters) [id: %s, " +
                        "current status: %s]", id.orElse(null), currentStatus.orElse(null)));
                sendUpdateError(req, resp, view, HttpServletResponse.SC_BAD_REQUEST, "Bad parameters passed. ");
            }
        } catch (SQLException e) {
            logger.log(Level.ERROR, String.format("Cannot update account status (exception was thrown) [id: %s, " +
                    "current status: %s]", id.orElse(null), currentStatus.orElse(null)), e);
            sendUpdateError(req, resp, view, HttpServletResponse.SC_BAD_REQUEST, String.format("Exception thrown " +
                    "during operation (%s). ", e.getMessage()));
        }
        try {
            req.setAttribute(PAGE_ATTR, buildAccountsPage(req));
            view.forward(req, resp);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build accounts page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Page<Account> buildAccountsPage(HttpServletRequest req) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<AccountSortType> sortType = paramReader.readAccountSort(req, SORT_PARAM);
        return adminService.getAccounts(pageSize, pageNumber, sortType.orElse(null));
    }

    private void sendUpdateError(HttpServletRequest req, HttpServletResponse resp, RequestDispatcher view,
                                 int statusCode, String message) throws ServletException, IOException {
        req.setAttribute(OPERATION_NAME_ATTR, OperationType.UPDATE.name());
        req.setAttribute(OPERATION_STATUS_ATTR, false);
        resp.setStatus(statusCode);
        buildErrorMessage(req, message);
        try {
            req.setAttribute(PAGE_ATTR, buildAccountsPage(req));
        } catch (SQLException e) {
            buildErrorMessage(req, String.format("Cannot build accounts page (%s). ", e.getMessage()));
            logger.log(Level.ERROR, String.format("Cannot build accounts page (uri: %s)", req.getRequestURI()), e);
        }
        view.forward(req, resp);
    }

    private void buildErrorMessage(HttpServletRequest req, String message) {
        Optional<String> errMsg = paramReader.readString(req, ERROR_MESSAGE_ATTR);
        req.setAttribute(ERROR_MESSAGE_ATTR, errMsg.orElse("").concat((message != null) ? message : ""));
    }

}
