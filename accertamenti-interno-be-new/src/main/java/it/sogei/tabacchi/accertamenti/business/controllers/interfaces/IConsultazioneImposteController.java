package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;

import java.util.List;

public interface IConsultazioneImposteController {

    List<CategoriaProdottoDto> getCategorieProdotti();

    String getDescrizioneCategoria(String categoriaProdotto);

    ListeFiltriRicercaDto getFiltriRicerca(String categoriaProdotto, String annoContabile);

    RisultatoPaginatoDto<PosizioneDto> findAllElencoPosizioni(String categoriaProdotto, ParametriFiltriRicercaDto filtri,
                                                              Integer pagina, Integer elementiPerPagina);
}
