package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

// aliquota di mora applicata: i campi comuni più id e percentuale (il frontend usa l'id come key, insieme a giorniRitardo)
@Data
@EqualsAndHashCode(callSuper = true)
public class AliquotaApplicataDto extends PercentualeApplicataDto {

    private Long idAliquota;
    private BigDecimal aliquota;                    // percentuale dell'aliquota di mora
}
