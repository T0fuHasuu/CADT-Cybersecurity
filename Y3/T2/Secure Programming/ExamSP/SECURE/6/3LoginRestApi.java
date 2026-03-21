Selected solution 
package com.lib.api;

import com.lib.exception.ApplicationException;
import com.lib.persistence.model.entity.LoginAttemptEntity;
import com.lib.persistence.model.entity.UserEntity;
import com.lib.service.*;
import com.lib.util.HtmlSanitizer;
import com.lib.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Date;

import static com.lib.util.ApplicationConstants.*;
import static com.lib.util.URLConstants.LOGIN_URL;
import static javax.security.enterprise.identitystore
        .CredentialValidationResult.Status.INVALID;
import static javax.security.enterprise.identitystore
        .CredentialValidationResult.Status.NOT_VALIDATED;

@Path(LOGIN_URL)
public class LoginRestApi {

    private static final Logger log =
            Logger.getLogger(LoginRestApi.class);

    private static final Date DATE = new Date();

    @Inject
    private UserService userService;

    @Inject
    private TokenService tokenService;

    @Inject
    private LoginAttemptService loginAttemptService;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @Inject
    private IdentityStore identityStore;

    @POST
    @PermitAll
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticateByLogin(@FormParam("username") String username,
                                        @FormParam("password") String password
    ) {
        try {
            login(
                    HtmlSanitizer.sanitize(username),
                    HtmlSanitizer.sanitize(password)
            );
            return Response.ok().build();
        } catch (ApplicationException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    private void login(String username, String password)
            throws ApplicationException {
        UsernamePasswordCredential credential = null;
        if(StringUtils.isNotBlank(username)
                && StringUtils.isNotBlank(password)) {
            credential = new UsernamePasswordCredential(username, password);
        }
        CredentialValidationResult validationResult =
                identityStore.validate(credential);
        if (validationResult.getStatus().equals(NOT_VALIDATED)) {
            log.error("Login or password is incorrect");
            throw new ApplicationException();
        }
        LoginAttemptEntity loginAttemptEntity =
                new LoginAttemptEntity(
                        null,
                        false,
                        DATE,
                        UserUtil.getIpAddress(request)
                );
        if (validationResult.getStatus().equals(INVALID)) {
            loginAttemptService.save(loginAttemptEntity);
            log.error("Login or password is incorrect");
            throw new ApplicationException();
        }

        updateSession(request.getSession(true), credential.getCaller());

        loginAttemptEntity.setSuccess(true);
        loginAttemptService.save(loginAttemptEntity);

        tokenService.save(username, response);
    }

    private void updateSession(HttpSession session, String username) {
        if (StringUtils.isBlank(username)) {
            return;
        }
        UserEntity userEntity = userService.getUserByUsername(username);
        if (userEntity == null) {
            return;
        }
        session.setAttribute("login", userEntity.getUsername());
        session.setAttribute("userId", userEntity.getId());
    }

}