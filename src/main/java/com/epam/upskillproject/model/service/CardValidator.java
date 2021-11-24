package com.epam.upskillproject.model.service;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.Card;
import com.epam.upskillproject.model.dto.StatusType;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class CardValidator {

    private static final String PASSWORD_HASH_ITERATIONS_PROP = "Pbkdf2PasswordHash.Iterations";
    private static final String PASSWORD_HASH_ALGORITHM_PROP = "Pbkdf2PasswordHash.Algorithm";
    private static final String PASSWORD_HASH_KEY_SIZE_PROP = "Pbkdf2PasswordHash.KeySizeBytes";
    private static final String PASSWORD_HASH_SALT_SIZE_PROP = "Pbkdf2PasswordHash.SaltSizeBytes";

    private final PropertiesKeeper propertiesKeeper;
    private final Pbkdf2PasswordHash passwordHash;

    @Inject
    public CardValidator(PropertiesKeeper propertiesKeeper, Pbkdf2PasswordHash passwordHash) {
        this.propertiesKeeper = propertiesKeeper;
        this.passwordHash = passwordHash;
    }

    public boolean validate(Card card, String cvc) {
        if (card != null) {
            return (
                    card.getStatus().equals(StatusType.ACTIVE) &&
                    card.getExpDate().isAfter(LocalDate.now()) &&
                    passwordHash.verify(cvc.toCharArray(), card.getCvc())
            );
        } else {
            return false;
        }
    }

    @PostConstruct
    private void init() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PASSWORD_HASH_ITERATIONS_PROP, propertiesKeeper.getString(PASSWORD_HASH_ITERATIONS_PROP));
        parameters.put(PASSWORD_HASH_ALGORITHM_PROP, propertiesKeeper.getString(PASSWORD_HASH_ALGORITHM_PROP));
        parameters.put(PASSWORD_HASH_KEY_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_KEY_SIZE_PROP));
        parameters.put(PASSWORD_HASH_SALT_SIZE_PROP, propertiesKeeper.getString(PASSWORD_HASH_SALT_SIZE_PROP));
        passwordHash.initialize(parameters);
    }
}
