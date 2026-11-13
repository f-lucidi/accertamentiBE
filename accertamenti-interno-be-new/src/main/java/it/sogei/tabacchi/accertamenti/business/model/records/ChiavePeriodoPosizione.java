package it.sogei.tabacchi.accertamenti.business.model.records;

// identifica il periodo di una posizione: serve a collegare posizioni e versamenti
// (W_TB_ACCERTAMENTI_VERS_MORE_INTERESSI_SANZIONI_V non ha l'ID della posizione)
public record ChiavePeriodoPosizione(String cfSogObbligato, Integer anno, Integer mese, Integer numQuindicina) {
}
