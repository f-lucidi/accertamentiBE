package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneParametriController;
import it.sogei.tabacchi.accertamenti.business.model.enums.TipoParametro;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IGestioneParametriService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@LogMethodInvocation(logParameters = false)
public class GestioneParametriService extends AbstractService implements IGestioneParametriService {

    @Inject
    private IGestioneParametriController gestioneParametriController;

    @Override
    public Response findAnni(TipoParametro tipoParametro) {
        return ok(gestioneParametriController.findAnni(tipoParametro));
    }
}
