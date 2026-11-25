package it.sogei.tabacchi.accertamenti.business.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

// stato di rendicontazione mostrato nel dettaglio: deriva da W_TB_ACCERTAMENTI_ELENCO_POSIZIONI.FLAG_CANCELLA
@Getter
@AllArgsConstructor
public enum StatoRendicontazione {

    // FLAG_CANCELLA = true
    CANCELLATA("Cancellata"),

    // FLAG_CANCELLA = false o null
    TRASMESSA("Trasmessa");

    // testo inviato al frontend
    private final String descrizione;
}
