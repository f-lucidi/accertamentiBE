package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.ICategoriaProdottoController;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.ICategorieProdottoService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;


@Path(Constants.Endpoint.CATEGORIE_PRODOTTO)
@Slf4j
public class CategorieProdottoService extends AbstractService implements ICategorieProdottoService {

    @Inject
    private ICategoriaProdottoController categoriaProdottoController;

    @Override
    public Response getDescrizioneCategoria(String siglaCategoria) {
        return ok(categoriaProdottoController.getDescrizioneCategoria(siglaCategoria));
    }

    @Override
    public Response getAllCategorieProdotto(String sigla, String descrizione) {
        return ok(categoriaProdottoController.getAllCategorieProdotto(sigla, descrizione));
    }
}
