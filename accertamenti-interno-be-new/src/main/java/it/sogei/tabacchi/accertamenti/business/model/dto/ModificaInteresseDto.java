package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// body di POST /gestione-parametri/interessi/modifica: un solo tasso per decorrenza, niente scaglioni
// (tipo ModificaInteresse del frontend).
// Le annotazioni di validazione scattano solo con @Valid sul parametro del service (IGestioneInteressiService)
@Data
public class ModificaInteresseDto {
    @NotNull(message = Constants.ErrorMessages.TIPO_OPERAZIONE_OBBLIGATORIO)
    private TipoOperazione tipoOperazione;
    private LocalDate dataDecorrenza;           // yyyy-MM-dd
    private Long idInteresse;                   // null in INSERIMENTO, obbligatorio in MODIFICA
    private BigDecimal tassoInteresse;
    private Boolean flagCancella;               // true (solo in MODIFICA) = tasso già salvato da cancellare
}
