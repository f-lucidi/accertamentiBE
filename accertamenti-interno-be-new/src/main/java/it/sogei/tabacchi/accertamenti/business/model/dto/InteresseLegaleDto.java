package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// riga dell'elenco interessi legali (stessi nomi dei campi del BE del collega, usati dal frontend).
// Data LocalDate: nel JSON esce come yyyy-MM-dd
@Data
public class InteresseLegaleDto {
    // il frontend lo rimanda in ModificaInteresseLegaleDto per modificare o cancellare il tasso
    private Long idInteresse;
    private Integer anno;
    private BigDecimal tassoInteresseLegale;
    private LocalDate dataInserimento;
}
