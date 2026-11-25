package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// aliquota di mora o sanzione vigente per i giorni di ritardo della posizione
@Data
public class PercentualeApplicataDto {

    private BigDecimal percentualeApplicata;
    private Integer giorniRitardo;
    private String dataInizioValidita;          // dd/MM/yyyy
    private String dataFineValidita;            // null se ancora in vigore
}
