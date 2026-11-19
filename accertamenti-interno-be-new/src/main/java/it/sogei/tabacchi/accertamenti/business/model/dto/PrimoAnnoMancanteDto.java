package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// risposta di GET /gestione-parametri/interessi-legali/primo-anno-mancante: valori con cui il frontend
// precompila il form di inserimento (stessi nomi dei campi del DettaglioValoriInteressiDto del BE del collega)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrimoAnnoMancanteDto {
    // primo anno senza tasso, oppure l'ultimo anno + 1 se non ci sono buchi
    private Integer anno;
    // tasso dell'anno corrente se c'è, altrimenti Constants.GestioneParametri.TASSO_INTERESSE_LEGALE_DEFAULT
    private BigDecimal tassoInteresseLegale;
}
