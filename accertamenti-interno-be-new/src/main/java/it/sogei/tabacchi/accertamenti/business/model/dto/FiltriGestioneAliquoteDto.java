package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.time.LocalDate;

// body di POST .../aliquote/lista: tutti opzionali, null = filtro non applicato.
// Tutti null = tutte le aliquote; solo dataRiferimento = quelle vigenti in quella data
@Data
public class FiltriGestioneAliquoteDto {
    private LocalDate dataInizioValiditaDa;     // yyyy-MM-dd
    private LocalDate dataInizioValiditaA;      // yyyy-MM-dd
    private Integer anno;                       // validità che interseca l'anno
    private LocalDate dataRiferimento;          // yyyy-MM-dd
}
