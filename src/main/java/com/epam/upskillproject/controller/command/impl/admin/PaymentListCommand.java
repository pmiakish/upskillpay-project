package com.epam.upskillproject.controller.command.impl.admin;

import com.epam.upskillproject.controller.servlet.util.LocaleDispatcher;
import com.epam.upskillproject.controller.servlet.util.ParamReader;
import com.epam.upskillproject.controller.command.CommandResult;
import com.epam.upskillproject.controller.command.impl.AbstractCommand;
import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.PaymentSortType;
import com.epam.upskillproject.model.dto.Page;
import com.epam.upskillproject.model.dto.Payment;
import com.epam.upskillproject.model.service.AdminService;
import com.epam.upskillproject.util.PermissionType;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Singleton
public class PaymentListCommand extends AbstractCommand {

    private static final Logger logger = LogManager.getLogger(PaymentListCommand.class.getName());

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String VIEW_PROP = "servlet.view.payments";
    private static final String SORT_PARAM = "sort";
    private static final String PAGE_ATTR = "page";
    private static final String FORMATTER_ATTR = "formatter";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/admin/payments.jsp";

    private static final PermissionType[] permissions = {PermissionType.SUPERADMIN, PermissionType.ADMIN};

    private final AdminService adminService;

    @Inject
    public PaymentListCommand(LocaleDispatcher localeDispatcher, ParamReader paramReader, AdminService adminService) {
        super(localeDispatcher, paramReader);
        this.adminService = adminService;
    }

    @Override
    public CommandResult execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RequestDispatcher view = getView(req, VIEW_PROP, DEFAULT_VIEW);
            req.setAttribute(FORMATTER_ATTR, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            req.setAttribute(PAGE_ATTR, buildPaymentsPage(req));
            return new CommandResult(view);
        } catch (SQLException e) {
            logger.log(Level.ERROR, "Cannot build payments page", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Payments page is not available");
            return null;
        }
    }

    private Page<Payment> buildPaymentsPage(HttpServletRequest req) throws SQLException {
        int pageNumber = paramReader.readPageNumber(req);
        int pageSize = paramReader.readPageSize(req);
        Optional<PaymentSortType> sortType = paramReader.readPaymentSort(req, SORT_PARAM);
        return adminService.getPayments(pageSize, pageNumber, sortType.orElse(null));
    }

    @Override
    public PermissionType[] getPermissions() {
        return permissions;
    }
}
