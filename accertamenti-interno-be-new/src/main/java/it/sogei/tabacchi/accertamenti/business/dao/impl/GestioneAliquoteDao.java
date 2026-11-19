package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryBuilder;
import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneAliquoteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteDao implements IGestioneAliquoteDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // ------------ ELENCO ALIQUOTE ------------

    // gruppi più recenti prima, dentro il gruppo gli scaglioni in ordine di giorni
    @Override
    public List<WTBAccertamentiAliquotaMora> findAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina) {
        return applicaFiltriAliquote(queryManager.createBuilder(WTBAccertamentiAliquotaMora.class), filtriGestioneParametriDto)
                .orderByDesc(WTBAccertamentiAliquotaMora_.dataInizioVal)
                .orderByAsc(WTBAccertamentiAliquotaMora_.giorniRitardoDa)
                .orderByAsc(WTBAccertamentiAliquotaMora_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // selectCount prima dei filtri, come in countElencoPosizioni
    @Override
    public Long countAliquote(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return applicaFiltriAliquote(queryManager.createBuilder(WTBAccertamentiAliquotaMora.class).selectCount(), filtriGestioneParametriDto)
                .getSingleResult();
    }

    // filtri comuni a find e count: con addConditionsWhenValueIsNull(false) i valori null vengono saltati
    private CriteriaQueryBuilder<WTBAccertamentiAliquotaMora> applicaFiltriAliquote(
            CriteriaQueryBuilder<WTBAccertamentiAliquotaMora> builder, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        // anno -> dal 1 gennaio al 31 dicembre; null se l'anno non è scelto
        LocalDate primoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 1, 1) : null;
        LocalDate ultimoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 12, 31) : null;

        return builder
                .addConditionsWhenValueIsNull(false)
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                // decorrenza compresa tra Da e A
                .addGreaterThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaDa())
                .addLessThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaA())
                // vigenti alla data: iniziate entro quel giorno e non ancora finite (la fine è l'ultimo giorno valido)
                .addLessThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, filtriGestioneParametriDto.getDataRiferimento())
                .addGreaterThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataFineVal, filtriGestioneParametriDto.getDataRiferimento())
                // validità che tocca almeno un giorno dell'anno
                .addLessThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, ultimoGiornoAnno)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataFineVal, primoGiornoAnno);
    }

    // ------------ INSERIMENTO GRUPPO ------------

    @Override
    public Long countAliquoteModificabili(LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiAliquotaMora.class)
                .selectCount()
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                // non ancora in vigore OPPURE inserite oggi
                .orConditions(condizioniOr -> condizioniOr
                        .addGreaterThanCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, data)
                        .addEqualCondition(WTBAccertamentiAliquotaMora_.dtInserimento, data))
                .getSingleResult();
    }

    // selectMax prima delle condizioni, come in findDataUltimoVersamento
    @Override
    public LocalDate findDataInizioGruppoPrecedente(LocalDate dataDecorrenza) {
        return queryManager.createBuilder(WTBAccertamentiAliquotaMora.class)
                .selectMax(WTBAccertamentiAliquotaMora_.dataInizioVal)
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                .addLessThanCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, dataDecorrenza)
                .getSingleResult();
    }

    @Override
    public List<WTBAccertamentiAliquotaMora> findAliquoteByDataInizio(LocalDate dataInizio) {
        return queryManager.createBuilder(WTBAccertamentiAliquotaMora.class)
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                .addEqualCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, dataInizio)
                .orderByAsc(WTBAccertamentiAliquotaMora_.giorniRitardoDa)
                .getResultList();
    }

    // saveImmediately fa subito il flush: un errore del DB esce qui e non al commit
    @Override
    public void insertAliquota(WTBAccertamentiAliquotaMora aliquota) {
        queryManager.saveImmediately(aliquota, true);
    }

    @Override
    public void updateAliquota(WTBAccertamentiAliquotaMora aliquota) {
        queryManager.saveImmediately(aliquota, false);
    }
}
