package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneParametriDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class GestioneParametriDao implements IGestioneParametriDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // ------------ ANNI PER IL BOX DI RICERCA ------------

    // selectAttribute prima delle condizioni, come selectCount in countAliquote
    @Override
    public List<Integer> findAnniAliquoteMora() {
        List<LocalDate> dateInizio = queryManager.createBuilder(WTBAccertamentiAliquotaMora.class)
                .selectAttribute(WTBAccertamentiAliquotaMora_.dataInizioVal)
                .distinct()
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                .getResultList();
        return toAnni(dateInizio);
    }

    // la tabella non ha flag di cancellazione: nessuna condizione
    @Override
    public List<Integer> findAnniSanzioni() {
        List<LocalDate> dateInizio = queryManager.createBuilder(WTBAccertamentiSanzioni.class)
                .selectAttribute(WTBAccertamentiSanzioni_.dataInizioVal)
                .distinct()
                .getResultList();
        return toAnni(dateInizio);
    }

    // l'anno è già una colonna: distinct e ordinamento li fa il DB
    @Override
    public List<Integer> findAnniInteressiLegali() {
        return queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .selectAttribute(WTBAccertamentiInteresseLegale_.annoValid)
                .distinct()
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .orderByDesc(WTBAccertamentiInteresseLegale_.annoValid)
                .getResultList();
    }

    @Override
    public List<Integer> findAnniInteressiRitPagam() {
        List<LocalDate> dateInizio = queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class)
                .selectAttribute(WTBAccertamentiInteresseRitPagam_.dataInizioVal)
                .distinct()
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                .getResultList();
        return toAnni(dateInizio);
    }

    // date di decorrenza (già distinte dal DB) -> anni senza doppioni, dal più recente.
    // Il distinct qui serve perché date diverse dello stesso anno (01/01/2027 e 01/02/2027) danno lo stesso anno
    private List<Integer> toAnni(List<LocalDate> dateInizio) {
        return dateInizio.stream()
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
