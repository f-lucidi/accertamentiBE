package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// riga dell'elenco interessi sul tardato pagamento (tipo Interesse del frontend).
// Date LocalDate: nel JSON escono come yyyy-MM-dd
@Data
public class InteresseDto {
    private Long idInteresse;
    private BigDecimal tassoInteresse;
    private LocalDate dataInizioValidita;
    private LocalDate dataFineValidita;        // null = tasso ancora aperto (2999-12-31)
    private LocalDate dataInserimento;
}
