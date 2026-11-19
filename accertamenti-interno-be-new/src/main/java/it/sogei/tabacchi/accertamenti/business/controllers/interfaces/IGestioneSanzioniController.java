package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneDto;

public interface IGestioneSanzioniController {

    RisultatoPaginatoDto<SanzioneDto> findSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // true se non esiste un gruppo di sanzioni ancora modificabile
    boolean isInserimentoSanzioniAbilitato();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void salvaSanzioni(ModificaSanzioniDto modificaSanzioniDto, String cfUtente);
}
