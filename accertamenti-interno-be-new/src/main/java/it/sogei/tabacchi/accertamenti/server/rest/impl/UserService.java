package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IUserController;
import it.sogei.tabacchi.accertamenti.business.model.dto.UserInfo;
import it.sogei.tabacchi.accertamenti.server.filter.AuthenticationFilter;
import it.sogei.tabacchi.accertamenti.server.filter.SkipAuthFilter;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IUserService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path(Constants.Endpoint.EMPTY)
@RolesAllowed(Constants.Application.ROLES_ALLOWED)
@Slf4j
@LogMethodInvocation(logParameters = false)
public class UserService extends AbstractService implements IUserService {

    @Inject
    private IUserController userController;

    @Inject
    private AuthenticationFilter authenticationFilter;

    @Override
    public Response getUserInfo() {
        UserInfo userInfo = userController.getUserInfo(this.httpRequest);
//        log.info("VersamentiTestSuite.getStaamsEndpoint(): {}", VersamentiServiceProperties.getServiceEndpoint());
//        log.info("VersamentiTestSuite.getStaamsUsername(): {}", VersamentiServiceProperties.getServiceUsername());
//        log.info("VersamentiTestSuite.getStaamsPassword(): {}", VersamentiServiceProperties.getServicePassword());
        this.setSessionAttribute(Constants.SessionKeys.USER, userInfo);
        return ok(userInfo);
    }

    @Override
    @SkipAuthFilter
    public Response logout() {
        authenticationFilter.logout();
        return ok("Logout effettuato");
    }

    @Override
    public Response reactivateSession() {
        return ok("Sessione riattivata");
    }
}
