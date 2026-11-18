package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;

import java.time.LocalDate;
import java.util.List;

public interface IGestioneInteressiDao {

    // i filtri null non filtrano; gli interessi cancellati non escono mai
    List<WTBAccertamentiInteresseRitPagam> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina);

    Long countInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto);

    // interessi ancora modificabili: non in vigore alla data oppure inseriti quel giorno
    Long countInteressiModificabili(LocalDate data);

    // decorrenza dell'interesse che precede quella indicata, null se non esiste
    LocalDate findDataInizioInteressePrecedente(LocalDate dataDecorrenza);

    // null se non esiste
    WTBAccertamentiInteresseRitPagam findInteresseByDataInizio(LocalDate dataInizio);

    // null se non esiste
    WTBAccertamentiInteresseRitPagam findInteresseById(Long idInteresse);

    void insertInteresse(WTBAccertamentiInteresseRitPagam interesseRitPagam);

    void updateInteresse(WTBAccertamentiInteresseRitPagam interesseRitPagam);
}
