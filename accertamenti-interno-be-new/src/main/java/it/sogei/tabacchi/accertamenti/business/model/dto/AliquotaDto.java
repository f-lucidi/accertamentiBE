package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// riga dell'elenco aliquote di mora (tipo Aliquota del frontend).
// Date LocalDate: nel JSON escono come yyyy-MM-dd
@Data
public class AliquotaDto {
    private Long idAliquota;
    private Integer idGruppo;               // decorrenza yyyyMMdd (es. 20270101): il frontend raggruppa le righe, non lo rimanda
    private BigDecimal aliquota;
    private Integer giorniRitardoDa;
    private Integer giorniRitardoA;         // null = scaglione aperto
    private LocalDate dataInizioValidita;
    private LocalDate dataFineValidita;        // null = gruppo ancora aperto (2999-12-31)
    private LocalDate dataInserimento;
}
