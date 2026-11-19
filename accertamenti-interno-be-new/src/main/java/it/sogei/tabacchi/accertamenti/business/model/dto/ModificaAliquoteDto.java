package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// body di POST /gestione-parametri/aliquote/modifica: un gruppo di aliquote con la stessa decorrenza.
// Le annotazioni di validazione scattano solo con @Valid sul parametro del service (IGestioneAliquoteService):
// qui solo i vincoli validi per ogni tipo operazione, quelli che dipendono da INSERIMENTO/MODIFICA restano nel validator
@Data
public class ModificaAliquoteDto {
    @NotNull(message = Constants.ErrorMessages.TIPO_OPERAZIONE_OBBLIGATORIO)
    private TipoOperazione tipoOperazione;
    private Integer idGruppo;                   // come arriva in AliquotaDto (es. 20270101); null in INSERIMENTO, obbligatorio in MODIFICA
    private LocalDate dataDecorrenza;           // yyyy-MM-dd

    // righe del gruppo, ognuna è un'aliquota; @NotEmpty = né null né lista vuota
    @NotEmpty(message = Constants.ErrorMessages.ALIQUOTE_OBBLIGATORIE)
    private List<AliquotaModificaDto> scaglioni;
}
