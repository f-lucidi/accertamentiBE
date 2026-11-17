package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;

import java.time.LocalDate;
import java.util.List;

public interface IGestioneAliquoteDao {

    // i filtri null non filtrano; le aliquote cancellate non escono mai
    List<WTBAccertamentiAliquotaMora> findAliquote(FiltriGestioneAliquoteDto filtri, int pagina, int elementiPerPagina);

    Long countAliquote(FiltriGestioneAliquoteDto filtri);

    // aliquote ancora modificabili: non in vigore alla data oppure inserite quel giorno
    Long countAliquoteModificabili(LocalDate data);

    // decorrenza del gruppo che precede quella indicata, null se non esiste
    LocalDate findDataInizioGruppoPrecedente(LocalDate dataDecorrenza);

    List<WTBAccertamentiAliquotaMora> findAliquoteByDataInizio(LocalDate dataInizio);

    // null se non esiste
    WTBAccertamentiAliquotaMora findAliquotaById(Long idAliquota);

    void insertAliquota(WTBAccertamentiAliquotaMora aliquota);

    void updateAliquota(WTBAccertamentiAliquotaMora aliquota);
}
