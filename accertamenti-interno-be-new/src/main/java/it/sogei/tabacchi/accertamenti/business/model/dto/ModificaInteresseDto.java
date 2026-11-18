package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoOperazione;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// body di POST /gestione-parametri/interessi/modifica: un solo tasso per decorrenza, niente scaglioni
// (tipo ModificaInteresse del frontend)
@Data
public class ModificaInteresseDto {
    private TipoOperazione tipoOperazione;
    private Integer idGruppo;                   // come arriva in InteresseDto (es. 20270101); null in INSERIMENTO, obbligatorio in MODIFICA
    private LocalDate dataDecorrenza;           // yyyy-MM-dd
    private Long idInteresse;                   // null in INSERIMENTO, obbligatorio in MODIFICA
    private BigDecimal tassoInteresse;
    private Boolean flagCancella;               // true (solo in MODIFICA) = tasso già salvato da cancellare
}
