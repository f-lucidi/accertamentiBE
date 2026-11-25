package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// tasso di interesse legale di un anno
@Data
public class TassoInteresseLegaleDto {

    private Integer anno;
    private BigDecimal tassoInteresseLegale;
}
