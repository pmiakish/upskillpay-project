package com.epam.upskillproject.util.init;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:global/customProjectDB",
        callerQuery = "SELECT PASSWORD FROM PERSON WHERE EMAIL=?",
        groupsQuery = "SELECT prm.NAME FROM PERSON p INNER JOIN PERM prm ON p.PERM=prm.ID WHERE p.EMAIL=?",
        hashAlgorithmParameters = {
                "Pbkdf2PasswordHash.Iterations=3072",
                "Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA512",
                "Pbkdf2PasswordHash.SaltSizeBytes=32",
                "Pbkdf2PasswordHash.KeySizeBytes=32"
        }
)
@FormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/WEB-INF/view/login.jsp",
                errorPage = "/error")
)
@DeclareRoles({"SUPERADMIN", "ADMIN", "CUSTOMER"})
@ApplicationScoped
public class AppConfig{}