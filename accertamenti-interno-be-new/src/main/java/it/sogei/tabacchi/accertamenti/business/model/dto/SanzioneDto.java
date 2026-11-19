package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// riga dell'elenco sanzioni (tipo Sanzione del frontend, stessi nomi del SanzioneDto del BE del collega).
// Date già formattate dd/MM/yyyy: il frontend le mostra così come arrivano
@Data
public class SanzioneDto {
    // in modifica il frontend rimanda gli idSanzione: da lì si ricava il gruppo (niente idGruppo, come nel BE del collega)
    private Long idSanzione;
    private BigDecimal sanzione;
    private Boolean moltiplicatoPerGiorni;
    private Integer giorniRitardoDa;
    private Integer giorniRitardoA;         // null = scaglione aperto
    private String dataInizioValidita;
    private String dataFineValidita;        // null = gruppo ancora aperto (2999-12-31)
    private String dataInserimento;
}
