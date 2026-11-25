package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// sanzione applicata: i campi comuni più id e percentuale (il frontend usa l'id come key)
@Data
@EqualsAndHashCode(callSuper = true)
public class SanzioneApplicataDto extends PercentualeApplicataDto {

    private Long idSanzione;
    private BigDecimal sanzione;                    // percentuale della sanzione
}
