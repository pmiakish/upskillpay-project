package com.epam.upskillproject.controller.servlet.util;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public class LocaleDispatcher {

    private static final Logger logger = LogManager.getLogger(LocaleDispatcher.class.getName());

    private static final String SESSION_LOCALE_ATTR = "sessLoc";
    private static final LocaleType DEFAULT_LOCALE = LocaleType.EN;

    private LocaleType locale = DEFAULT_LOCALE;

    @Inject
    private PropertiesKeeper propertiesKeeper;

    public String getLocalizedView(HttpServletRequest req, String VIEW_PROP) {
        HttpSession session = req.getSession();
        Object sessionLocale = session.getAttribute(SESSION_LOCALE_ATTR);
        if (sessionLocale instanceof String && !((String) sessionLocale).equalsIgnoreCase(locale.name())) {
            setLocale((String) sessionLocale);
        } else if (sessionLocale != null && !(sessionLocale instanceof String) ) {
            session.removeAttribute(SESSION_LOCALE_ATTR);
        }
        String localizedView;
        try {
            localizedView = propertiesKeeper.getString(String.format("%s.%s", locale.name().toLowerCase(), VIEW_PROP));
        } catch (IllegalArgumentException e) {
            localizedView = propertiesKeeper.getStringOrDefault(String.format("%s.%s",
                    DEFAULT_LOCALE.name().toLowerCase(), VIEW_PROP), "");
        }
        return localizedView;
    }

    public void setLocale(String localeParam) {
        if (localeParam != null && localeParam.length() > 0) {
            try {
                this.locale = LocaleType.valueOf(localeParam.toUpperCase());
                logger.log(Level.TRACE, "Set locale: " + locale.name());
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARN, "Unknown locale type: " + localeParam);
            }
        }
    }

    public LocaleType getLocale() {
        return locale;
    }
}
