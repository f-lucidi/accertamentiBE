package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PrimoAnnoMancanteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;

public interface IGestioneInteressiLegaliController {

    RisultatoPaginatoDto<InteresseLegaleDto> findInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // valori per precompilare il form di inserimento: primo anno senza tasso e tasso proposto
    PrimoAnnoMancanteDto findPrimoAnnoMancante();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void scriviInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, String cfUtente);
}
