package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;

import java.time.LocalDate;
import java.util.List;

public interface IGestioneSanzioniDao {

    // i filtri null non filtrano
    List<WTBAccertamentiSanzioni> findSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina);

    Long countSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto);

    // sanzioni ancora modificabili: non in vigore alla data oppure inserite quel giorno
    Long countSanzioniModificabili(LocalDate data);

    // decorrenza del gruppo che precede quella indicata, null se non esiste
    LocalDate findDataInizioGruppoPrecedente(LocalDate dataDecorrenza);

    // sanzione con quell'id; null se non esiste
    WTBAccertamentiSanzioni findSanzioneById(Long id);

    // righe del gruppo (stessa data di inizio); lista vuota se il gruppo non esiste
    List<WTBAccertamentiSanzioni> findSanzioniByDataInizio(LocalDate dataInizio);

    void insertSanzione(WTBAccertamentiSanzioni sanzione);

    void updateSanzione(WTBAccertamentiSanzioni sanzione);

    // cancellazione fisica: la tabella non ha FLAG_CANCELLA
    void deleteSanzione(WTBAccertamentiSanzioni sanzione);
}
