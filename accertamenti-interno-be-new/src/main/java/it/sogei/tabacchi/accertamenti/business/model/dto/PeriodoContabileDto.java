package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Value;

import java.util.List;

/**
 * Filtro "periodo contabile" già interpretato (vedi {@code Utility.parsePeriodoContabile}).
 * Il periodo di una posizione si riconosce da MESE e NUM_QUINDICINA (non da SCADENZA,
 * che per le quindicine vale sempre l'ultimo giorno del mese).
 */
@Value
public class PeriodoContabileDto {

    List<Integer> mesi;       // sempre valorizzato: 3 mesi per il trimestre, 1 per mese e quindicina
    Integer numQuindicina;    // valorizzato solo per la quindicina (1 = prima, 2 = seconda)
}
