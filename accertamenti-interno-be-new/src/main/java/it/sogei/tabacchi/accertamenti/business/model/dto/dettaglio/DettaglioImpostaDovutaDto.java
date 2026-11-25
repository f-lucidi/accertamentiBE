package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// una riga della tabella imposta dovuta: importo mancante oppure versamento
@Data
public class DettaglioImpostaDovutaDto {

    private Long idVersamento;
    private String tipoImporto;                 // "Importo mancante" / "Importo versato"
    private BigDecimal importo;
    private String metodoPagamento;             // solo sui versati
    private LocalDate dataPagamento;               // solo sui versati
    private Integer giorniRitardoPagamento;     // solo sui mancanti
}
