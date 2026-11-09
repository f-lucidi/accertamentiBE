package it.sogei.tabacchi.accertamenti.server.filter;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IUserController;
import it.sogei.tabacchi.accertamenti.business.model.dto.UserInfo;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.Principal;
import java.util.Objects;

@Provider
@Slf4j
public class AuthenticationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;
    @Context
    private HttpServletRequest httpRequest;
    @Context
    private HttpServletResponse httpResponse;
    @Inject
    private IUserController userController;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (Utility.isAnnotationPresent(resourceInfo, SkipAuthFilter.class)) {
            return;
        }

        Principal principal = requestContext.getSecurityContext().getUserPrincipal();
        if (Objects.isNull(principal) || StringUtils.isBlank(principal.getName())) {
            invalidateSessionAndClearCookies();
            throw new BusinessException(ExceptionType.UNAUTHORIZED_USER);
        }

        UserInfo user = (UserInfo) httpRequest.getSession().getAttribute(Constants.SessionKeys.USER);

        if (Objects.isNull(user)) {
            user = userController.getUserInfo(this.httpRequest);
            this.httpRequest.getSession().setAttribute(Constants.SessionKeys.USER, user);
        }
    }

    public void logout() {
        this.invalidateSessionAndClearCookies();
    }

    private void invalidateSessionAndClearCookies() {
        if (httpRequest.getSession(false) != null) {
            httpRequest.getSession().invalidate();
        }
        Cookie[] cookies = httpRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("AuthFilter >>> Cookie{} - {} - {} :: {}", cookie.getName(), cookie.getDomain(), cookie.getPath(), cookie);

                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                httpResponse.addCookie(cookie);
            }
        }
    }
}
