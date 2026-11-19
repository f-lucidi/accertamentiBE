package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryBuilder;
import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneSanzioniDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

// stesse query delle aliquote (GestioneAliquoteDao) senza il filtro su FLAG_CANCELLA, che questa tabella non ha
@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class GestioneSanzioniDao implements IGestioneSanzioniDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // ------------ ELENCO SANZIONI ------------

    // gruppi più recenti prima, dentro il gruppo gli scaglioni in ordine di giorni
    @Override
    public List<WTBAccertamentiSanzioni> findSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina) {
        return applicaFiltriSanzioni(queryManager.createBuilder(WTBAccertamentiSanzioni.class), filtriGestioneParametriDto)
                .orderByDesc(WTBAccertamentiSanzioni_.dataInizioVal)
                .orderByAsc(WTBAccertamentiSanzioni_.giorniRitardoDa)
                .orderByAsc(WTBAccertamentiSanzioni_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // selectCount prima dei filtri, come in countAliquote
    @Override
    public Long countSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return applicaFiltriSanzioni(queryManager.createBuilder(WTBAccertamentiSanzioni.class).selectCount(), filtriGestioneParametriDto)
                .getSingleResult();
    }

    // filtri comuni a find e count: con addConditionsWhenValueIsNull(false) i valori null vengono saltati
    private CriteriaQueryBuilder<WTBAccertamentiSanzioni> applicaFiltriSanzioni(
            CriteriaQueryBuilder<WTBAccertamentiSanzioni> builder, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        // anno -> dal 1 gennaio al 31 dicembre; null se l'anno non è scelto
        LocalDate primoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 1, 1) : null;
        LocalDate ultimoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 12, 31) : null;

        return builder
                .addConditionsWhenValueIsNull(false)
                // decorrenza compresa tra Da e A
                .addGreaterThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaDa())
                .addLessThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaA())
                // vigenti alla data: iniziate entro quel giorno e non ancora finite (la fine è l'ultimo giorno valido)
                .addLessThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataInizioVal, filtriGestioneParametriDto.getDataRiferimento())
                .addGreaterThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataFineVal, filtriGestioneParametriDto.getDataRiferimento())
                // validità che tocca almeno un giorno dell'anno
                .addLessThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataInizioVal, ultimoGiornoAnno)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataFineVal, primoGiornoAnno);
    }

    // ------------ SALVATAGGIO GRUPPO ------------

    @Override
    public Long countSanzioniModificabili(LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiSanzioni.class)
                .selectCount()
                // non ancora in vigore OPPURE inserite oggi
                .orConditions(condizioniOr -> condizioniOr
                        .addGreaterThanCondition(WTBAccertamentiSanzioni_.dataInizioVal, data)
                        .addEqualCondition(WTBAccertamentiSanzioni_.dtInserimento, data))
                .getSingleResult();
    }

    // selectMax prima delle condizioni, come in findDataInizioGruppoPrecedente delle aliquote
    @Override
    public LocalDate findDataInizioGruppoPrecedente(LocalDate dataDecorrenza) {
        return queryManager.createBuilder(WTBAccertamentiSanzioni.class)
                .selectMax(WTBAccertamentiSanzioni_.dataInizioVal)
                .addLessThanCondition(WTBAccertamentiSanzioni_.dataInizioVal, dataDecorrenza)
                .getSingleResult();
    }

    @Override
    public WTBAccertamentiSanzioni findSanzioneById(Long id) {
        return queryManager.findById(WTBAccertamentiSanzioni.class, id);
    }

    @Override
    public List<WTBAccertamentiSanzioni> findSanzioniByDataInizio(LocalDate dataInizio) {
        return queryManager.createBuilder(WTBAccertamentiSanzioni.class)
                .addEqualCondition(WTBAccertamentiSanzioni_.dataInizioVal, dataInizio)
                .getResultList();
    }

    // saveImmediately/deleteImmediately fanno subito il flush: un errore del DB esce qui e non al commit
    @Override
    public void insertSanzione(WTBAccertamentiSanzioni sanzione) {
        queryManager.saveImmediately(sanzione, true);
    }

    @Override
    public void updateSanzione(WTBAccertamentiSanzioni sanzione) {
        queryManager.saveImmediately(sanzione, false);
    }

    // la sanzione arriva da findSanzioniByDataInizio, quindi è gestita dall'EntityManager: remove la accetta
    @Override
    public void deleteSanzione(WTBAccertamentiSanzioni sanzione) {
        queryManager.deleteImmediately(sanzione);
    }
}
