package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;

import java.time.LocalDate;
import java.util.List;

public interface IGestioneAliquoteDao {

    // i filtri null non filtrano; le aliquote cancellate non escono mai
    List<WTBAccertamentiAliquotaMora> findAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina);

    Long countAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto);

    // aliquote ancora modificabili: non in vigore alla data oppure inserite quel giorno
    Long countAliquoteModificabili(LocalDate data);

    // decorrenza del gruppo che precede quella indicata, null se non esiste
    LocalDate findDataInizioGruppoPrecedente(LocalDate dataDecorrenza);

    // righe non cancellate del gruppo (dataInizio = idGruppo) in ordine di giorni; lista vuota se il gruppo non esiste
    List<WTBAccertamentiAliquotaMora> findAliquoteByDataInizio(LocalDate dataInizio);

    void insertAliquota(WTBAccertamentiAliquotaMora aliquota);

    void updateAliquota(WTBAccertamentiAliquotaMora aliquota);
}
