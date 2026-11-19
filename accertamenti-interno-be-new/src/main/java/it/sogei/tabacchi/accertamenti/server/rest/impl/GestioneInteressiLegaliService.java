package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneInteressiLegaliController;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IGestioneInteressiLegaliService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiLegaliService extends AbstractService implements IGestioneInteressiLegaliService {

    @Inject
    private IGestioneInteressiLegaliController gestioneInteressiLegaliController;

    @Override
    public Response findInteressiLegali(Integer pagina, Integer elementiPerPagina, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return ok(gestioneInteressiLegaliController.findInteressiLegali(filtriGestioneParametriDto, pagina, elementiPerPagina));
    }

    @Override
    public Response findInteresseLegaleById(Long idInteresse) {
        return ok(gestioneInteressiLegaliController.findInteresseLegaleById(idInteresse));
    }

    @Override
    public Response findPrimoAnnoMancante() {
        return ok(gestioneInteressiLegaliController.findPrimoAnnoMancante());
    }

    // il CF si legge dalla sessione qui (getCfUtente di AbstractService): il controller non conosce la request HTTP
    @Override
    public Response scriviInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        gestioneInteressiLegaliController.scriviInteresseLegale(modificaInteresseLegaleDto, getCfUtente());
        return ok();
    }
}
