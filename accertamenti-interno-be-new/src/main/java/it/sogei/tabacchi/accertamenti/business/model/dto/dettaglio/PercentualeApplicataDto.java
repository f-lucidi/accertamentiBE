package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// aliquota di mora o sanzione vigente per i giorni di ritardo della posizione
@Data
public class PercentualeApplicataDto {

    private BigDecimal percentualeApplicata;
    private Integer giorniRitardo;
    private LocalDate dataInizioValidita;          // yyyy-MM-dd
    private LocalDate dataFineValidita;            // null se ancora in vigore
}
