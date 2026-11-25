package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneSanzioniController;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IGestioneSanzioniService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@LogMethodInvocation(logParameters = false)
public class GestioneSanzioniService extends AbstractService implements IGestioneSanzioniService {

    @Inject
    private IGestioneSanzioniController gestioneSanzioniController;

    @Override
    public Response findSanzioni(Integer pagina, Integer elementiPerPagina, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return ok(gestioneSanzioniController.findSanzioni(filtriGestioneParametriDto, pagina, elementiPerPagina));
    }

    @Override
    public Response isInserimentoSanzioniAbilitato() {
        return ok(gestioneSanzioniController.isInserimentoSanzioniAbilitato());
    }

    // il CF si legge dalla sessione qui: il controller non conosce la request HTTP
    @Override
    public Response salvaSanzioni(ModificaSanzioniDto modificaSanzioniDto) {
        gestioneSanzioniController.salvaSanzioni(modificaSanzioniDto, getCfUtente());
        return ok();
    }
}
