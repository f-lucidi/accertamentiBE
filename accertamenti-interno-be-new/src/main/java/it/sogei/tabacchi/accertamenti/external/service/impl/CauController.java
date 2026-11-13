package it.sogei.tabacchi.accertamenti.external.service.impl;


import it.finanze.security.AuthRestJee7;
import it.sogei.tabacchi.accertamenti.external.service.ICauController;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Dependent
@Slf4j
@LogMethodInvocation(logParameters = false)
public class CauController implements ICauController {

    @Override
    public String getUfficio(AuthRestJee7 authRestJee7, HttpServletRequest request) throws BusinessException {
        try {
            String ufficio = authRestJee7.getCodiceUfficio(request);
            if (StringUtils.isBlank(ufficio)) {
                throw new BusinessException(Constants.ErrorMessages.UFFICIO_UTENTE_NON_TROVATO);
            }
            return ufficio;
        } catch (Exception e) {
            log.error("Errore durante la chiamata getCodiceUfficio", e);
            throw new BusinessException(Constants.ErrorMessages.UFFICIO_UTENTE_NON_TROVATO);
        }
    }

//    @Override
//    public String[] getUffici(AuthRestJee7 authRestJee7, HttpServletRequest request) throws BusinessException {
//        try {
//            String[] uffici = authRestJee7.getUffici(request);
//            if (null == uffici || uffici.length == 0) {
//                throw new BusinessException("Uffici correlati all'utente non trovati");
//            }
//            return uffici;
//        } catch (Exception e) {
//            log.error("Errore durante la chiamata getUffici", e);
//            throw new BusinessException("Uffici utente non trovati");
//        }
//    }
}
