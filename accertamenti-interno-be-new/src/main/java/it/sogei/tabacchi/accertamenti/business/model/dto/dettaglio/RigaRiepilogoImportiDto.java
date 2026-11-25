package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// riga dovuto / versato / restante; anno solo sulle righe "dovuti" degli interessi legali
@Data
public class RigaRiepilogoImportiDto {

    private String tipoOperazione;
    private Integer anno;
    private BigDecimal importo;
    private String metodoDiPagamento;           // solo sulla riga versato
    private LocalDate dataUltimoPagamento;         // solo sulla riga versato
}
