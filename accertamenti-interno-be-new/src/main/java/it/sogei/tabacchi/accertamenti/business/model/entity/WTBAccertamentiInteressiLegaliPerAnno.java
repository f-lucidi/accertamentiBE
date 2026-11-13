package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_INTERESSI_LEGALI_PER_ANNO")
public class WTBAccertamentiInteressiLegaliPerAnno {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_POSIZIONE", nullable = false)
    private Long idPosizione;

    @Column(name = "INTERESSE_LEGALE")
    private BigDecimal interesseLegale;

    @Column(name = "ANNO")
    private Integer anno;

    // Constants.INTERESSE_LEGALE_PAGATO ("1") se pagato
    @Column(name = "STATO_PAGAMENTO_INTERESSE_LEGALE", length = 20)
    private String statoPagamentoInteresseLegale;

    @Column(name = "DATA_PAGAMENTO_INTERESSE_LEGALE")
    private LocalDate dataPagamentoInteresseLegale;

    @Column(name = "IDK_PAGAMENTO", length = 100)
    private String idkPagamento;
}
