package com.epam.upskillproject.view.tags;

import com.epam.upskillproject.init.PropertiesKeeper;
import jakarta.inject.Inject;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.util.Objects;

public class OperationStatusMessageTag extends SimpleTagSupport {

    private static final String MSG_CREATE_SUCCESS = "Created successfully";
    private static final String MSG_CREATE_FAILED = "Creation failed";
    private static final String MSG_UPDATE_SUCCESS = "Updated successfully";
    private static final String MSG_UPDATE_FAILED = "Update failed";
    private static final String MSG_DELETE_SUCCESS = "Deleted successfully";
    private static final String MSG_DELETE_FAILED = "Removal failed";
    private static final String MSG_PAYMENT_SUCCESS = "Payment performed successfully";
    private static final String MSG_PAYMENT_FAILED = "Payment cannot be implemented";
    private static final String MSG_DEFAULT_SUCCESS = "Operation has been performed successfully";
    private static final String MSG_DEFAULT_FAILED = "Operation failed";
    private static final String MSG_DELIMITER = "<br />";
    private static final String ALERT_SUCCESS_PROP = "operation.tag.alert.success";
    private static final String ALERT_FAILED_PROP = "operation.tag.alert.failed";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private OperationType operation;
    private boolean result;
    private String message;

    @Override
    public void doTag() throws JspException, IOException {
        if (operation != null) {
            String contentPattern = propertiesKeeper.getString((result) ? ALERT_SUCCESS_PROP : ALERT_FAILED_PROP);
            StringBuilder tagBody = new StringBuilder();
            switch (operation) {
                case CREATE:
                    tagBody.append((result) ? MSG_CREATE_SUCCESS : MSG_CREATE_FAILED);
                    break;
                case UPDATE:
                    tagBody.append((result) ? MSG_UPDATE_SUCCESS : MSG_UPDATE_FAILED);
                    break;
                case DELETE:
                    tagBody.append((result) ? MSG_DELETE_SUCCESS : MSG_DELETE_FAILED);
                    break;
                case PAYMENT:
                    tagBody.append((result) ? MSG_PAYMENT_SUCCESS : MSG_PAYMENT_FAILED);
                    break;
                default:
                    tagBody.append((result) ? MSG_DEFAULT_SUCCESS : MSG_DEFAULT_FAILED);
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
}
