package com.epam.upskillproject.controller.servlets;

import com.epam.upskillproject.controller.LocaleDispatcher;
import com.epam.upskillproject.controller.ParamReader;
import com.epam.upskillproject.exceptions.TransactionException;
import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.model.service.SystemService;
import com.epam.upskillproject.view.tags.OperationType;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(SignupServlet.class.getName());

    private static final String VIEW_PROP = "servlet.view.signup";
    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";
    private static final String FIRST_NAME_PARAM = "firstName";
    private static final String LAST_NAME_PARAM = "lastName";
    private static final String EMAIL_PARAM = "email";
    private static final String PASSWORD_PARAM = "pass";
    private static final String OPERATION_NAME_ATTR = "opName";
    private static final String OPERATION_STATUS_ATTR = "opStat";
    private static final String ERROR_MESSAGE_ATTR = "errMsg";
    private static final String DEFAULT_VIEW = "/WEB-INF/view/en/signup.jsp";

    @Inject
    private LocaleDispatcher localeDispatcher;
    @Inject
    private PropertiesKeeper propertiesKeeper;
    @Inject
    private ParamReader paramReader;
    @Inject
    private SystemService systemService;
    @Inject
    private Pbkdf2PasswordHash passwordHash;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String localizedView = localeDispatcher.getLocalizedView(req, VIEW_PROP);
        String viewPath = (localizedView.length() > 0) ? localizedView : DEFAULT_VIEW;
        RequestDispatcher view = req.getRequestDispatcher(viewPath);
        req.setAttribute(OPERATION_NAME_ATTR, OperationType.CREATE.name());
        try {
            boolean created = systemService.addCustomer(
                    paramReader.readString(req, EMAIL_PARAM).orElseThrow(InvalidParameterException::new),
                    passwordHash.generate(
                            paramReader.readString(req, PASSWORD_PARAM).orElseThrow(InvalidParameterException::new)
                                    .toCharArray()
                    ),
                    paramReader.readString(req, FIRST_NAME_PARAM).orElseThrow(InvalidParameterException::new),
                    paramReader.readString(req, LAST_NAME_PARAM).orElseThrow(InvalidParameterException::new)
            );
            req.setAttribute(OPERATION_STATUS_ATTR, created);
            resp.setStatus((created) ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute(OPERATION_STATUS_ATTR, false);
            req.setAttribute(ERROR_MESSAGE_ATTR, String.format("Cannot create profile (%s)", e.getMessage()));
            logger.log(Level.ERROR, "Cannot create account (exception thrown)", e);
        } catch (InvalidParameterException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute(OPERATION_STATUS_ATTR, false);
            req.setAttribute(ERROR_MESSAGE_ATTR, "Cannot create profile (incorrect parameters passed)");
            logger.log(Level.WARN, "Cannot create account (bad parameters passed)", e);
        } catch (TransactionException e) {
            logger.log(Level.WARN, "Cannot create account (transaction failed)", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute(OPERATION_STATUS_ATTR, false);
            req.setAttribute(ERROR_MESSAGE_ATTR, "Cannot create account (transaction failed)");
        }
        view.forward(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }

}
