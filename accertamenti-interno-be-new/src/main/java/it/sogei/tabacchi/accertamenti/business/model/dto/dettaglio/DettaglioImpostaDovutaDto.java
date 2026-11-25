package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// una riga della tabella imposta dovuta: importo mancante oppure versamento
@Data
public class DettaglioImpostaDovutaDto {

    private Long idVersamento;
    private String tipoImporto;                 // "Importo mancante" / "Importo versato"
    private BigDecimal importo;
    private String metodoPagamento;             // solo sui versati
    private String dataPagamento;               // solo sui versati, dd/MM/yyyy
    private Integer giorniRitardoPagamento;     // solo sui mancanti
}
