package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import java.util.List;

// anni per il box di ricerca dei parametri storicizzati: distinti, dal più recente
public interface IGestioneParametriDao {

    // anni di decorrenza delle aliquote di mora non cancellate
    List<Integer> findAnniAliquoteMora();

    // anni di decorrenza delle sanzioni (la tabella non ha flag di cancellazione)
    List<Integer> findAnniSanzioni();

    // anni di validità degli interessi legali con FLAG_CANCELLA = 0
    List<Integer> findAnniInteressiLegali();

    // anni di decorrenza degli interessi sul tardato pagamento non cancellati
    List<Integer> findAnniInteressiRitPagam();
}
