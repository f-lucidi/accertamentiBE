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
import java.util.ArrayList;
import java.util.Collections;
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

    // l'anno è già una colonna: basta riempire gli anni intermedi
    @Override
    public List<Integer> findAnniInteressiLegali() {
        List<Integer> anni = queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .selectAttribute(WTBAccertamentiInteresseLegale_.annoValid)
                .distinct()
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .getResultList();
        return riempiAnniIntermedi(anni);
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

    // date di decorrenza -> anni, poi lista completa dal più alto al più basso
    private List<Integer> toAnni(List<LocalDate> dateInizio) {
        List<Integer> anni = dateInizio.stream()
                .map(LocalDate::getYear)
                .toList();
        return riempiAnniIntermedi(anni);
    }

    // dall'anno più alto al più basso, compresi gli anni che nel DB non ci sono
    private List<Integer> riempiAnniIntermedi(List<Integer> anni) {
        if (anni.isEmpty()) {
            return List.of();
        }
        int annoPiuAlto = Collections.max(anni);
        int annoPiuBasso = Collections.min(anni);
        List<Integer> anniCompleti = new ArrayList<>();
        for (int anno = annoPiuAlto; anno >= annoPiuBasso; anno--) {
            anniCompleti.add(anno);
        }
        return anniCompleti;
    }
}
