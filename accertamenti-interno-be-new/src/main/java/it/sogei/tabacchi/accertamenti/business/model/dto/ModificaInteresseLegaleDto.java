package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// body di POST /gestione-parametri/interessi-legali/modifica: un tasso per anno, niente decorrenza
// (stessi nomi dei campi del ModificaTassoInteresseLegaleDto del BE del collega, usati dal frontend).
// Le annotazioni di validazione scattano solo con @Valid sul parametro del service (IGestioneInteressiLegaliService)
@Data
public class ModificaInteresseLegaleDto {
    @NotNull(message = Constants.ErrorMessages.TIPO_OPERAZIONE_OBBLIGATORIO)
    private TipoOperazione tipoOperazione;
    private Long idInteresse;                   // null in INSERIMENTO, obbligatorio in MODIFICA
    private Integer anno;                       // obbligatorio solo in INSERIMENTO (lo controlla il validator)
    private BigDecimal tassoInteresseLegale;
    private Boolean flagCancella;               // true (solo in MODIFICA) = tasso già salvato da cancellare
}
