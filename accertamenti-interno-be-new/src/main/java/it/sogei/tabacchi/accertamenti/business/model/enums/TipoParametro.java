package it.sogei.tabacchi.accertamenti.business.model.enums;

// parametro storicizzato di cui il frontend chiede gli anni per il box di ricerca.
// Arriva come path param: i valori NON vanno rinominati
public enum TipoParametro {

    // W_TB_ACCERTAMENTI_ALIQUOTA_MORA (anno di DATA_INIZIO_VAL)
    ALIQUOTE_MORA,

    // W_TB_ACCERTAMENTI_SANZIONI (anno di DATA_INIZIO_VAL)
    SANZIONI,

    // W_TB_ACCERTAMENTI_INTERESSE_LEGALE (ANNO_VALID)
    INTERESSI_LEGALI,

    // W_TB_ACCERTAMENTI_INTERESSE_RIT_PAGAM (anno di DATA_INIZIO_VAL)
    INTERESSI_RIT_PAGAM
}
