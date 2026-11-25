package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// riga dell'elenco sanzioni (tipo Sanzione del frontend, stessi nomi del SanzioneDto del BE del collega).
// Date LocalDate: nel JSON escono come yyyy-MM-dd
@Data
public class SanzioneDto {
    private Long idSanzione;
    private Integer idGruppo;               // decorrenza yyyyMMdd (es. 20270101): il frontend raggruppa le righe, non lo rimanda
    private BigDecimal sanzione;
    private Boolean moltiplicatoPerGiorni;
    private Integer giorniRitardoDa;
    private Integer giorniRitardoA;         // null = scaglione aperto
    private LocalDate dataInizioValidita;
    private LocalDate dataFineValidita;        // null = gruppo ancora aperto (2999-12-31)
    private LocalDate dataInserimento;
}
