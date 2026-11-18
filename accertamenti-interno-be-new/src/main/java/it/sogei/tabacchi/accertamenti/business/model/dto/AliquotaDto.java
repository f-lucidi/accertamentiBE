package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// riga dell'elenco aliquote di mora (tipo Aliquota del frontend).
// Date già formattate dd/MM/yyyy: il frontend le mostra così come arrivano
@Data
public class AliquotaDto {
    // id del gruppo (decorrenza yyyyMMdd, es. 20270101), uguale su tutte le righe del gruppo:
    // il frontend lo rimanda in ModificaAliquoteDto per operare sul gruppo
    private Integer idGruppo;
    private Long idAliquota;
    private BigDecimal aliquota;
    private Integer giorniRitardoDa;
    private Integer giorniRitardoA;         // null = scaglione aperto
    private String dataInizioValidita;
    private String dataFineValidita;        // null = gruppo ancora aperto (2999-12-31)
    private String dataInserimento;
}
