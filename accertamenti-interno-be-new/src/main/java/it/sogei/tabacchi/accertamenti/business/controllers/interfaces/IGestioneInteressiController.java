package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;

public interface IGestioneInteressiController {

    RisultatoPaginatoDto<InteresseDto> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // interesse non cancellato con quell'id; se non esiste (o è cancellato) BusinessException NOT_FOUND (404)
    InteresseDto findInteresseById(Long idInteresse);

    // true se non esiste un interesse ancora modificabile
    boolean isInserimentoInteressiAbilitato();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void scriviInteresse(ModificaInteresseDto modificaInteresseDto, String cfUtente);
}
