package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;

import java.util.List;

public interface IConsultazioneImposteController {

    List<CategoriaProdottoDto> getCategorieProdotti();

    String getDescrizioneCategoria(String siglaCategoria);

    ListeFiltriRicercaDto getFiltriRicerca(String categoriaProdotto);
}
