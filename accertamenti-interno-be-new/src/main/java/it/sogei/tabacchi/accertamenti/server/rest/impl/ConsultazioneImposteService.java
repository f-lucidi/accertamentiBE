package it.sogei.tabacchi.accertamenti.server.rest.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IConsultazioneImposteController;
import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.server.rest.AbstractService;
import it.sogei.tabacchi.accertamenti.server.rest.interfaces.IConsultazioneImpostaService;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path(Constants.Endpoint.CONSULTAZIONE_IMPOSTE)
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteService extends AbstractService implements IConsultazioneImpostaService {

    @Inject
    private IConsultazioneImposteController consultazioneImposteController;

    @Override
    public Response getCategorieProdotti() {
        return ok(consultazioneImposteController.getCategorieProdotti());
    }

    @Override
    public Response getDescrizioneCategoria(String categoriaProdotto) {
        return ok(consultazioneImposteController.getDescrizioneCategoria(categoriaProdotto));
    }

    @Override
    public Response getFiltriRicerca(String categoriaProdotto, String anno) {
        return ok(consultazioneImposteController.getFiltriRicerca(categoriaProdotto, anno));
    }

    @Override
    public Response findAllElencoPosizioni(String categoriaProdotto, Integer pagina, Integer elementiPerPagina,
                                           ParametriFiltriRicercaDto parametriFiltriRicercaDto) {
        RisultatoPaginatoDto<PosizioneDto> posizioni = consultazioneImposteController.findAllElencoPosizioni(
                categoriaProdotto, parametriFiltriRicercaDto, pagina, elementiPerPagina);
        // il dettaglio si potrà aprire solo per le posizioni appena mostrate
        setSessionAttribute(Constants.SessionKeys.LISTA_POSIZIONI,
                posizioni.getData().stream().map(PosizioneDto::getIdPosizione).toList());
        return ok(posizioni);
    }
}
