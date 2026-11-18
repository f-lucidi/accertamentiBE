package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneAliquoteController;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.UserInfo;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IGestioneAliquoteService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteService extends AbstractService implements IGestioneAliquoteService {

    @Inject
    private IGestioneAliquoteController gestioneAliquoteController;

    @Override
    public Response findAliquote(Integer pagina, Integer elementiPerPagina, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return ok(gestioneAliquoteController.findAliquote(filtriGestioneParametriDto, pagina, elementiPerPagina));
    }

    @Override
    public Response isInserimentoAliquoteAbilitato() {
        return ok(gestioneAliquoteController.isInserimentoAliquoteAbilitato());
    }

    // il CF si legge dalla sessione qui: il controller non conosce la request HTTP
    @Override
    public Response salvaAliquote(ModificaAliquoteDto modificaAliquote) {
        gestioneAliquoteController.salvaAliquote(modificaAliquote, getCfUtente());
        return ok();
    }

    // CF dell'utente loggato (username di UserInfo in sessione), null se non presente.
    // Da spostare in AbstractService quando servirà a un altro service
    private String getCfUtente() {
        UserInfo userInfo = getSessionAttribute(Constants.SessionKeys.USER, UserInfo.class);
        return null == userInfo ? null : userInfo.getUsername();
    }
}
