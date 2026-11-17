package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// uno scaglione del gruppo (tipo AliquotaModifica del frontend)
@Data
public class AliquotaModificaDto {
    private Long idAliquota;            // null sulle aliquote nuove
    private BigDecimal valoreAliquota;
    private Integer giorniDa;
    private Integer giorniA;            // null = scaglione aperto (solo l'ultimo)
    private Boolean flagCancella;       // true = aliquota già salvata da cancellare
}
