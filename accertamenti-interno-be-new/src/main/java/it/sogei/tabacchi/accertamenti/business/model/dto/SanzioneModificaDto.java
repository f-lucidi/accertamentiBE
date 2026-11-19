package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// uno scaglione del gruppo (tipo SanzioneModifica del frontend, stessi nomi del SanzioneModificaDto del BE del collega)
@Data
public class SanzioneModificaDto implements ScaglioneGiorniRitardo {
    private Long idSanzione;                // null sulle sanzioni nuove
    private BigDecimal valoreSanzione;
    private Integer giorniDa;
    private Integer giorniA;                // null = scaglione aperto (solo l'ultimo)
    private Boolean moltiplicatoPerGiorni;  // null = no
    private Boolean flagCancella;           // true = sanzione già salvata da cancellare (DELETE)
}
