package it.sogei.tabacchi.accertamenti.external.service;


import it.finanze.security.AuthRestJee7;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import jakarta.servlet.http.HttpServletRequest;

public interface ICauController {

    String getUfficio(AuthRestJee7 authRestJee7, HttpServletRequest request) throws BusinessException;

//    String[] getUffici(AuthRestJee7 authRestJee7, HttpServletRequest request) throws BusinessException;
}
