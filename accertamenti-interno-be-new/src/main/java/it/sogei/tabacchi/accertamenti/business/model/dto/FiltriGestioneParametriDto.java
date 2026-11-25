package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.time.LocalDate;

// body di POST .../aliquote/lista e .../interessi/lista: tutti opzionali, null = filtro non applicato.
// Da/A = parametri validi almeno un giorno nel periodo (anche uno solo dei due). Tutti null = tutti i parametri; solo dataRiferimento = quelli vigenti in quella data
@Data
public class FiltriGestioneParametriDto {
    private LocalDate dataInizioValiditaDa;     // yyyy-MM-dd, inizio del periodo di validità cercato
    private LocalDate dataInizioValiditaA;      // yyyy-MM-dd, fine del periodo di validità cercato
    private Integer anno;                       // validità che interseca l'anno
    private LocalDate dataRiferimento;          // yyyy-MM-dd
}
