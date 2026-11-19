package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryBuilder;
import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneInteressiLegaliDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiLegaliDao implements IGestioneInteressiLegaliDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // ------------ ELENCO INTERESSI LEGALI ------------

    // anni più recenti prima
    @Override
    public List<WTBAccertamentiInteresseLegale> findInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina) {
        return applicaFiltriInteressiLegali(queryManager.createBuilder(WTBAccertamentiInteresseLegale.class), filtriGestioneParametriDto)
                .orderByDesc(WTBAccertamentiInteresseLegale_.annoValid)
                .orderByAsc(WTBAccertamentiInteresseLegale_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // selectCount prima dei filtri, come in countAliquote
    @Override
    public Long countInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return applicaFiltriInteressiLegali(queryManager.createBuilder(WTBAccertamentiInteresseLegale.class).selectCount(), filtriGestioneParametriDto)
                .getSingleResult();
    }

    // filtri comuni a find e count: con addConditionsWhenValueIsNull(false) l'anno null viene saltato.
    // Gli altri campi di FiltriGestioneParametriDto (date) non riguardano gli interessi legali e sono ignorati
    private CriteriaQueryBuilder<WTBAccertamentiInteresseLegale> applicaFiltriInteressiLegali(
            CriteriaQueryBuilder<WTBAccertamentiInteresseLegale> builder, FiltriGestioneParametriDto filtriGestioneParametriDto) {
        return builder
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .addConditionsWhenValueIsNull(false)
                .addEqualCondition(WTBAccertamentiInteresseLegale_.annoValid, filtriGestioneParametriDto.getAnno());
    }

    // ------------ SALVATAGGIO INTERESSI LEGALI ------------

    @Override
    public Long countInteressiLegaliByAnno(Integer anno) {
        return queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .selectCount()
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .addEqualCondition(WTBAccertamentiInteresseLegale_.annoValid, anno)
                .getSingleResult();
    }

    // un solo interesse non cancellato per anno: getSingleResult restituisce null se non c'è
    @Override
    public WTBAccertamentiInteresseLegale findInteresseLegaleByAnno(Integer anno) {
        return queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .addEqualCondition(WTBAccertamentiInteresseLegale_.annoValid, anno)
                .getSingleResult();
    }

    // selectAttribute prima delle condizioni, come selectCount in countAliquote
    @Override
    public List<Integer> findAnniInteressiLegaliCrescenti() {
        return queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .selectAttribute(WTBAccertamentiInteresseLegale_.annoValid)
                .distinct()
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .orderByAsc(WTBAccertamentiInteresseLegale_.annoValid)
                .getResultList();
    }

    @Override
    public WTBAccertamentiInteresseLegale findInteresseLegaleById(Long idInteresse) {
        return queryManager.findById(WTBAccertamentiInteresseLegale.class, idInteresse);
    }

    // saveImmediately fa subito il flush: un errore del DB esce qui e non al commit
    @Override
    public void insertInteresseLegale(WTBAccertamentiInteresseLegale interesseLegale) {
        queryManager.saveImmediately(interesseLegale, true);
    }

    @Override
    public void updateInteresseLegale(WTBAccertamentiInteresseLegale interesseLegale) {
        queryManager.saveImmediately(interesseLegale, false);
    }
}
