package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneInteressiController;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseDto;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IGestioneInteressiService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiService extends AbstractService implements IGestioneInteressiService {

    @Inject
    private IGestioneInteressiController gestioneInteressiController;

    @Override
    public Response findInteressi(Integer pagina, Integer elementiPerPagina, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return ok(gestioneInteressiController.findInteressi(filtriGestioneParametriDto, pagina, elementiPerPagina));
    }

    @Override
    public Response isInserimentoInteressiAbilitato() {
        return ok(gestioneInteressiController.isInserimentoInteressiAbilitato());
    }

    // il CF si legge dalla sessione qui (getCfUtente di AbstractService): il controller non conosce la request HTTP
    @Override
    public Response scriviInteresse(ModificaInteresseDto modificaInteresseDto) {
        gestioneInteressiController.scriviInteresse(modificaInteresseDto, getCfUtente());
        return ok();
    }
}
