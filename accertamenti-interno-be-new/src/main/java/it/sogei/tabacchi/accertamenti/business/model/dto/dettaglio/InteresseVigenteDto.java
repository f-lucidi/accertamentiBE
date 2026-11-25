package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// tasso sul tardato pagamento vigente alla scadenza della posizione
@Data
public class InteresseVigenteDto {

    private BigDecimal interessiVigenti;
    private LocalDate inizioValiditaInteresse;     // yyyy-MM-dd
    private LocalDate fineValiditaInteresse;       // null se ancora in vigore
}
