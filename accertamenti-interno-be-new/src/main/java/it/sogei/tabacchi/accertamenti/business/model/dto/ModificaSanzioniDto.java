package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// body di POST /gestione-parametri/sanzioni/modifica: un gruppo di sanzioni con la stessa decorrenza.
// Le annotazioni di validazione scattano solo con @Valid sul parametro del service (IGestioneSanzioniService):
// qui solo i vincoli validi per ogni tipo operazione, quelli che dipendono da INSERIMENTO/MODIFICA restano nel validator
@Data
public class ModificaSanzioniDto {
    @NotNull(message = Constants.ErrorMessages.TIPO_OPERAZIONE_OBBLIGATORIO)
    private TipoOperazione tipoOperazione;
    private LocalDate dataDecorrenza;           // yyyy-MM-dd

    // righe del gruppo, ognuna è una sanzione; @NotEmpty = né null né lista vuota
    @NotEmpty(message = Constants.ErrorMessages.SANZIONI_OBBLIGATORIE)
    private List<SanzioneModificaDto> scaglioni;
}
