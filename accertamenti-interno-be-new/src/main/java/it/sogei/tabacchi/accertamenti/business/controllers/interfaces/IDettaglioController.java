package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio.DettaglioDto;

public interface IDettaglioController {

    // dettaglio completo della posizione; pagina ed elementiPerPagina valgono solo per la tabella imposta dovuta
    DettaglioDto getDettaglio(Long idPosizione, Integer pagina, Integer elementiPerPagina);
}
