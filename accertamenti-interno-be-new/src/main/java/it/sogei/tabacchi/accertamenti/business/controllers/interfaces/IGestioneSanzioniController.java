package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneDto;

import java.util.List;

public interface IGestioneSanzioniController {

    RisultatoPaginatoDto<SanzioneDto> findSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // righe del gruppo (idGruppo = decorrenza yyyyMMdd) in ordine di giorni;
    // idGruppo che non è una data valida -> 400, gruppo inesistente -> BusinessException NOT_FOUND (404)
    List<SanzioneDto> findSanzioniByIdGruppo(Integer idGruppo);

    // true se non esiste un gruppo di sanzioni ancora modificabile
    boolean isInserimentoSanzioniAbilitato();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void salvaSanzioni(ModificaSanzioniDto modificaSanzioniDto, String cfUtente);
}
