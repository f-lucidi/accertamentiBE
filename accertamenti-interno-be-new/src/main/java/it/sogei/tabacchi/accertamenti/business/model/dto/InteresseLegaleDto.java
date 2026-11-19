package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// riga dell'elenco interessi legali (stessi nomi dei campi del BE del collega, usati dal frontend).
// Data già formattata dd/MM/yyyy: il frontend la mostra così come arriva
@Data
public class InteresseLegaleDto {
    // il frontend lo rimanda in ModificaInteresseLegaleDto per modificare o cancellare il tasso
    private Long idInteresse;
    private Integer anno;
    private BigDecimal tassoInteresseLegale;
    private String dataInserimento;
}
