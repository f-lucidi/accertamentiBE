package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;

public interface IGestioneAliquoteController {

    RisultatoPaginatoDto<AliquotaDto> findAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // true se non esiste un gruppo di aliquote ancora modificabile
    boolean isInserimentoAliquoteAbilitato();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void salvaAliquote(ModificaAliquoteDto modificaAliquote, String cfUtente);
}
