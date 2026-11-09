package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IUserController;
import it.sogei.tabacchi.accertamenti.business.model.dto.UfficioDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.UserInfo;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.PropertyUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@RequestScoped
@Slf4j
@LogMethodInvocation(logParameters = false)
public class UserController implements IUserController {

    private String username;

//    @Inject
//    private ICauController cauController;

    @Override
    public UserInfo getUserInfo(HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute(Constants.SessionKeys.USER);

        if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getUfficio())) {
            userInfo = this.initUserInfo(request);
        }

        return userInfo;
    }

    private UserInfo initUserInfo(HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();

        log.info("[USER MANAGER] Inizializzazione utente");

        userInfo.setSessionTimeout(PropertyUtils.getAsInteger(Constants.Properties.SESSION_TIMEOUT));
        userInfo.setSessionExpiring(PropertyUtils.getAsInteger(Constants.Properties.SESSION_EXPIRING));


        String username = this.getUsername(request);
        userInfo.setUsername(username);
//        AuthRestJee7 authRestJee7 = new AuthRestJee7(ServiziTypes.DEFAULT.getNomeServizio());

//        String ufficio = cauController.getUfficio(authRestJee7, request);
        userInfo.setUfficio(new UfficioDto() {{
            setCodiceUfficio("100000");
            setDescrizione("Ufficio mock");
        }});

        return userInfo;
    }

    private String getUsername(HttpServletRequest request) {
        if (username == null) {
            log.info("[USER MANAGER] Recupero utenza");
            String codiceFiscale = request.getUserPrincipal().getName();
            if (codiceFiscale.startsWith("cn=")) {
                codiceFiscale = codiceFiscale.trim().substring(3, 19);
            }
            if (codiceFiscale.indexOf("-") > 0) {
                codiceFiscale = codiceFiscale.trim().substring(0, codiceFiscale.indexOf("-"));
            }

            codiceFiscale = codiceFiscale.toUpperCase();
            String remoteUser = request.getRemoteUser();
            log.debug("[USER MANAGER] RemoteUser: {}, Tipo Autenticazione: {} ", remoteUser, request.getAuthType());
            log.debug("[USER MANAGER] CF: {}, Tipo Autenticazione: {} ", codiceFiscale, request.getAuthType());
            username = codiceFiscale;
        }
        return username;
    }
}
