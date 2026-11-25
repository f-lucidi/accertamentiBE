package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IDettaglioController;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IDettaglioService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path(Constants.Endpoint.DETTAGLIO)
@LogMethodInvocation(logParameters = false)
public class DettaglioService extends AbstractService implements IDettaglioService {

    @Inject
    private IDettaglioController dettaglioController;

    @Override
    public Response getDettaglio(Long idPosizione, Integer pagina, Integer elementiPerPagina) {
        // solo posizioni restituite dall'ultima ricerca, altrimenti OPERATION_NOT_ALLOWED
        validateAgainstSessionObjList(Constants.SessionKeys.LISTA_POSIZIONI,
                (List<Long> idPosizioni) -> idPosizioni.contains(idPosizione));
        return ok(dettaglioController.getDettaglio(idPosizione, pagina, elementiPerPagina));
    }
}
