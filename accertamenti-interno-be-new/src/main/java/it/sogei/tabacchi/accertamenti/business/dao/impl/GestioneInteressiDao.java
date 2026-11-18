package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryBuilder;
import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneInteressiDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiDao implements IGestioneInteressiDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // ------------ ELENCO INTERESSI ------------

    // decorrenze più recenti prima; l'id come secondo criterio rende l'ordine sempre stabile,
    // così la paginazione non ripete né salta righe
    @Override
    public List<WTBAccertamentiInteresseRitPagam> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto,
                                                                int pagina, int elementiPerPagina) {
        return applicaFiltriInteressi(queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class), filtriGestioneParametriDto)
                .orderByDesc(WTBAccertamentiInteresseRitPagam_.dataInizioVal)
                .orderByAsc(WTBAccertamentiInteresseRitPagam_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // selectCount prima dei filtri, come in countAliquote
    @Override
    public Long countInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return applicaFiltriInteressi(queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class).selectCount(), filtriGestioneParametriDto)
                .getSingleResult();
    }

    // filtri comuni a find e count: con addConditionsWhenValueIsNull(false) i valori null vengono saltati
    private CriteriaQueryBuilder<WTBAccertamentiInteresseRitPagam> applicaFiltriInteressi(
            CriteriaQueryBuilder<WTBAccertamentiInteresseRitPagam> builder, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        // anno -> dal 1 gennaio al 31 dicembre; null se l'anno non è scelto
        LocalDate primoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 1, 1) : null;
        LocalDate ultimoGiornoAnno = null != filtriGestioneParametriDto.getAnno() ? LocalDate.of(filtriGestioneParametriDto.getAnno(), 12, 31) : null;

        return builder
                .addConditionsWhenValueIsNull(false)
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                // decorrenza compresa tra Da e A
                .addGreaterThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaDa())
                .addLessThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, filtriGestioneParametriDto.getDataInizioValiditaA())
                // vigenti alla data: iniziate entro quel giorno e non ancora finite (la fine è l'ultimo giorno valido)
                .addLessThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, filtriGestioneParametriDto.getDataRiferimento())
                .addGreaterThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataFineVal, filtriGestioneParametriDto.getDataRiferimento())
                // validità che tocca almeno un giorno dell'anno
                .addLessThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, ultimoGiornoAnno)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataFineVal, primoGiornoAnno);
    }

    // ------------ SALVATAGGIO INTERESSI ------------

    @Override
    public Long countInteressiModificabili(LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class)
                .selectCount()
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                // non ancora in vigore OPPURE inseriti oggi
                .orConditions(condizioniOr -> condizioniOr
                        .addGreaterThanCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, data)
                        .addEqualCondition(WTBAccertamentiInteresseRitPagam_.dtInserimento, data))
                .getSingleResult();
    }

    // selectMax prima delle condizioni, come in findDataInizioGruppoPrecedente delle aliquote
    @Override
    public LocalDate findDataInizioInteressePrecedente(LocalDate dataDecorrenza) {
        return queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class)
                .selectMax(WTBAccertamentiInteresseRitPagam_.dataInizioVal)
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                .addLessThanCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, dataDecorrenza)
                .getSingleResult();
    }

    // una sola riga non cancellata per decorrenza: getSingleResult restituisce null se non c'è
    @Override
    public WTBAccertamentiInteresseRitPagam findInteresseByDataInizio(LocalDate dataInizio) {
        return queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class)
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, dataInizio)
                .getSingleResult();
    }

    @Override
    public WTBAccertamentiInteresseRitPagam findInteresseById(Long idInteresse) {
        return queryManager.findById(WTBAccertamentiInteresseRitPagam.class, idInteresse);
    }

    // saveImmediately fa subito il flush: un errore del DB esce qui e non al commit
    @Override
    public void insertInteresse(WTBAccertamentiInteresseRitPagam interesseRitPagam) {
        queryManager.saveImmediately(interesseRitPagam, true);
    }

    @Override
    public void updateInteresse(WTBAccertamentiInteresseRitPagam interesseRitPagam) {
        queryManager.saveImmediately(interesseRitPagam, false);
    }
}
