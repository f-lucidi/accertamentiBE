package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// body di POST /gestione-parametri/aliquote/salva: un gruppo di aliquote con la stessa decorrenza
// (tipo ModificaAliquote del frontend)
@Data
public class ModificaAliquoteDto {
    private TipoOperazione tipoOperazione;
    private LocalDate dataDecorrenza;           // yyyy-MM-dd

    // righe del gruppo, ognuna è un'aliquota (stesso nome del frontend)
    private List<AliquotaModificaDto> scaglioni;
}
