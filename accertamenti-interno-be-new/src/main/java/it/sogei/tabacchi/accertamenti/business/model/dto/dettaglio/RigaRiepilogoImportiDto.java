package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// riga dovuto / versato / restante; anno solo sulle righe "dovuti" degli interessi legali
@Data
public class RigaRiepilogoImportiDto {

    private String tipoOperazione;
    private Integer anno;
    private BigDecimal importo;
    private String metodoDiPagamento;           // solo sulla riga versato
    private String dataUltimoPagamento;         // solo sulla riga versato, dd/MM/yyyy
}
