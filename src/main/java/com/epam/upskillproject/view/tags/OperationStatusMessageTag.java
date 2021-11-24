package com.epam.upskillproject.view.tags;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.EJBException;
import jakarta.inject.Inject;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Objects;

public class OperationStatusMessageTag extends SimpleTagSupport {

    private static final String MSG_CREATE_SUCCESS_PROP = "msg.create.success";
    private static final String MSG_CREATE_FAILED_PROP = "msg.create.failed";
    private static final String MSG_UPDATE_SUCCESS_PROP = "msg.update.success";
    private static final String MSG_UPDATE_FAILED_PROP = "msg.update.failed";
    private static final String MSG_DELETE_SUCCESS_PROP = "msg.delete.success";
    private static final String MSG_DELETE_FAILED_PROP = "msg.delete.failed";
    private static final String MSG_PAYMENT_SUCCESS_PROP = "msg.payment.success";
    private static final String MSG_PAYMENT_FAILED_PROP = "msg.payment.failed";
    private static final String MSG_DEFAULT_SUCCESS_PROP = "msg.default.success";
    private static final String MSG_DEFAULT_FAILED_PROP = "msg.default.failed";
    private static final String DEFAULT_LOCALE = "en";
    private static final String MSG_DELIMITER = "<br />";
    private static final String ALERT_SUCCESS_PROP = "operation.tag.alert.success";
    private static final String ALERT_FAILED_PROP = "operation.tag.alert.failed";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private OperationType operation;
    private boolean result;
    private String message;
    private String locale;

    @Override
    public void doTag() throws JspException, IOException {
        if (operation != null) {
            String contentPattern = propertiesKeeper.getString((result) ? ALERT_SUCCESS_PROP : ALERT_FAILED_PROP);
            StringBuilder tagBody = new StringBuilder();
            switch (operation) {
                case CREATE:
                    tagBody.append((result) ? localizeMessage(MSG_CREATE_SUCCESS_PROP, locale) :
                            localizeMessage(MSG_CREATE_FAILED_PROP, locale));
                    break;
                case UPDATE:
                    tagBody.append((result) ? localizeMessage(MSG_UPDATE_SUCCESS_PROP, locale) :
                            localizeMessage(MSG_UPDATE_FAILED_PROP, locale));
                    break;
                case DELETE:
                    tagBody.append((result) ? localizeMessage(MSG_DELETE_SUCCESS_PROP, locale) :
                            localizeMessage(MSG_DELETE_FAILED_PROP, locale));
                    break;
                case PAYMENT:
                    tagBody.append((result) ? localizeMessage(MSG_PAYMENT_SUCCESS_PROP, locale) :
                            localizeMessage(MSG_PAYMENT_FAILED_PROP, locale));
                    break;
                default:
                    tagBody.append((result) ? localizeMessage(MSG_DEFAULT_SUCCESS_PROP, locale) :
                            localizeMessage(MSG_DEFAULT_FAILED_PROP, locale));
                    break;
            }
            if (!result && message != null) {
                tagBody.append(MSG_DELIMITER).append(message);
            }
            JspWriter writer = getJspContext().getOut();
            writer.println(
                    String.format(contentPattern, tagBody.toString())
            );
        }
    }

    public void setOperation(String operationTypeParam) {
        try {
            this.operation = OperationType.valueOf(Objects.requireNonNull(operationTypeParam).toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            this.operation = null;
        }
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    private String localizeMessage(String property, String locale) {
        String localizedMsg = "";
        try {
            localizedMsg = (locale != null && locale.length() > 0) ?
                    propertiesKeeper.getString(String.format("%s.%s", locale.toLowerCase(), property)) :
                    propertiesKeeper.getString(String.format("%s.%s", DEFAULT_LOCALE, property));
        } catch (EJBException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                localizedMsg = propertiesKeeper.getStringOrDefault(String.format("%s.%s", DEFAULT_LOCALE, property), "");
            }
        }
        return localizedMsg;
    }
}


