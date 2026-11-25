package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// tasso sul tardato pagamento vigente alla scadenza della posizione
@Data
public class InteresseVigenteDto {

    private BigDecimal interessiVigenti;
    private String inizioValiditaInteresse;     // dd/MM/yyyy
    private String fineValiditaInteresse;       // null se ancora in vigore
}
