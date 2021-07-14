package de.intension.keycloak;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Details;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.util.CookieHelper;

/**
 * Makes sure to call ftl in correct order and complete login process
 */
public class TwoStepLoginWithCodeAuthenticator extends AuthenticationManager
    implements Authenticator
{

    private static final String FTL_ENTER_EMAIL      = "enter-email.ftl";
    private static final String FTL_ENTER_PASSWORD   = "enter-password.ftl";
    private static final String FTL_STAY_LOGGED_IN   = "stay-logged-in.ftl";
    private static final String AUTH_NOTE_USER_EMAIL = "userEmail";

    @Context
    private ClientConnection    clientConnection;

    @Override
    public void action(AuthenticationFlowContext context)
    {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String identificator = formData.getFirst("identificatorInput");
        String passwordInput = formData.getFirst("passwordInput");
        String rememberMeYes = formData.getFirst("rememberMeYes");
        String rememberMeNo = formData.getFirst("rememberMeNo");

        if (identificator != null && !identificator.isEmpty()) {
            redirectBasedOnIdentificatorInput(context, identificator);
        }
        else if (passwordInput != null) {
            redirectBasedOnPasswordInput(context, passwordInput);
        }
        else if (rememberMeYes != null || rememberMeNo != null) {
            saveRememberMeDecision(context, rememberMeYes);
            loginUser(context);
        }
        else {
            context.challenge(context.form().createForm(FTL_ENTER_EMAIL));
        }
    }

    @Override
    public void authenticate(AuthenticationFlowContext context)
    {
        if (!CookieHelper.getCookieValue(KEYCLOAK_REMEMBER_ME).isEmpty()) {
            setUserByUsername(context, CookieHelper.getCookieValue(KEYCLOAK_REMEMBER_ME).toString().substring(10).replace("]", ""));
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            redirectBasedOnIdentificatorInput(context, getUserEmail(context, null));
            saveRememberMeDecision(context, null);
        }
        else {
            context.challenge(context.form().createForm(FTL_ENTER_EMAIL));
        }
    }

    private String getUserEmail(AuthenticationFlowContext context, String identificator)
    {
        UserModel user = context.getUser();

        if (user == null) {
            user = context.getSession().users().getUserByEmail(identificator, context.getRealm());
            context.setUser(user);
        }
        return user == null ? null : user.getEmail();
    }

    private void redirectBasedOnIdentificatorInput(AuthenticationFlowContext context, String identificator)
    {
        context.getAuthenticationSession().setAuthNote(AUTH_NOTE_USER_EMAIL, getUserEmail(context, identificator));
        context.challenge(context.form().setAttribute("email", context.getAuthenticationSession().getAuthNote(AUTH_NOTE_USER_EMAIL))
            .createForm(FTL_ENTER_PASSWORD));
    }

    private void redirectBasedOnPasswordInput(AuthenticationFlowContext context, String passwordInput)
    {
        context.setUser(getUser(context));

        if (context.getSession().userCredentialManager().isValid(context.getRealm(), context.getUser(), UserCredentialModel.password(passwordInput))) {

            if (context.getRealm().isRememberMe()) {

                try {
                    if (context.getAuthenticationSession().getAuthNote(Details.REMEMBER_ME).equals("true")) {
                        saveRememberMeDecision(context, "refreshing token");
                        loginUser(context);
                    }
                    else {
                        context.challenge(context.form().createForm(FTL_STAY_LOGGED_IN));
                    }
                } catch (NullPointerException e) {
                    context.challenge(context.form().createForm(FTL_STAY_LOGGED_IN));
                }
            }
            else {
                if (context.getAuthenticationSession().getAuthNote(Details.REMEMBER_ME).equals("true")) {
                    saveRememberMeDecision(context, "refreshing token");
                }
                loginUser(context);
            }
        }
        else {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, context.form().createForm(FTL_ENTER_PASSWORD));
        }
    }

    private void saveRememberMeDecision(AuthenticationFlowContext context, String rememberMeYes)
    {
        if (rememberMeYes != null) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");

            String path = getIdentityCookiePath(context.getRealm(), context.getSession().getContext().getUri());
            CookieHelper.addCookie(KEYCLOAK_REMEMBER_ME, "username:" + context.getUser().getUsername(), path, null, null, 31536000, true, true);
        }
        else {
            String path = getIdentityCookiePath(context.getRealm(), context.getSession().getContext().getUri());
            String cookieName = KEYCLOAK_REMEMBER_ME;
            logger.debugf("Expiring cookie: %s path: %s", cookieName, path);
            CookieHelper.addCookie(cookieName, "", path, null, "Expiring cookie", 0, true, true);
        }
    }

    private void loginUser(AuthenticationFlowContext context)
    {
        context.setUser(getUser(context));
        context.success();

    }

    private void setUserByUsername(AuthenticationFlowContext context, String username)
    {
        context.setUser(context.getSession().users().getUserByUsername(username, context.getRealm()));
    }

    private UserModel getUser(AuthenticationFlowContext context)
    {
        return context.getSession().users()
            .getUserByEmail(context.getAuthenticationSession().getAuthNote(AUTH_NOTE_USER_EMAIL), context.getRealm());
    }

    @Override
    public boolean requiresUser()
    {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user)
    {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user)
    {
        /*
         * not needed for current version
         */
    }

    @Override
    public void close()
    {
        /*
         * not used for current version
         */
    }

}