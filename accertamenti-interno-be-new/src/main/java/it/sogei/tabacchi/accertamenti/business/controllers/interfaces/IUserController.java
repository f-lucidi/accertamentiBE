package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.UserInfo;
import jakarta.servlet.http.HttpServletRequest;

public interface IUserController {

    UserInfo getUserInfo(HttpServletRequest session);
}
