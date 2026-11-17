package it.sogei.tabacchi.accertamenti.business.model.enums;

// operazione sul salvataggio di un gruppo di parametri (aliquote, poi sanzioni e interessi).
// Il frontend manda il nome come stringa: i valori NON vanno rinominati.
public enum TipoOperazione {

    // nuovo gruppo con una nuova decorrenza (righe senza id)
    INSERIMENTO,

    // gruppo non ancora in vigore: aggiorna, aggiunge o cancella righe
    MODIFICA
}
