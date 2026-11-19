package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;

import java.util.List;

public interface IGestioneAliquoteController {

    RisultatoPaginatoDto<AliquotaDto> findAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);

    // righe non cancellate del gruppo (idGruppo = decorrenza yyyyMMdd) in ordine di giorni;
    // idGruppo che non è una data valida -> 400, gruppo inesistente -> BusinessException NOT_FOUND (404)
    List<AliquotaDto> findAliquoteByIdGruppo(Integer idGruppo);

    // true se non esiste un gruppo di aliquote ancora modificabile
    boolean isInserimentoAliquoteAbilitato();

    // cfUtente: chi salva, null se sconosciuto (viene registrato "SYSTEM")
    void salvaAliquote(ModificaAliquoteDto modificaAliquote, String cfUtente);
}
