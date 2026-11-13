package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// una riga per posizione (soggetto + categoria + anno + mese + quindicina) con i totali già calcolati
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_ELENCO_POSIZIONI")
public class WTBAccertamentiElencoPosizioni {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "CF_SOG_OBBLIGATO", nullable = false, length = 20)
    private String cfSogObbligato;

    @Column(name = "CATEGORIA", nullable = false)
    private String categoria;

    @Column(name = "ANNO", nullable = false)
    private Integer anno;

    @Column(name = "MESE", nullable = false)
    private Integer mese;

    // 1 o 2 per le quindicine, 0 per le mensilità
    @Column(name = "NUM_QUINDICINA", nullable = false)
    private Integer numQuindicina;

    // "Q" quindicina, "M" mensilità
    @Column(name = "TIPO_PERIODO", length = 1)
    private String tipoPeriodo;

    @ManyToOne
    @JoinColumn(name = "STATO_POSIZIONE", referencedColumnName = "ID_STATO")
    private WTBAccertamentiStatiPagamento statoPagamento;

    @Column(name = "SCADENZA", nullable = false)
    private LocalDate scadenza;

    @Column(name = "IMPOSTA_TOTALE_DOVUTO", nullable = false)
    private BigDecimal impostaTotaleDovuto;

    @Column(name = "IMPOSTA_TOTALE_PAGATO")
    private BigDecimal impostaTotalePagato;

    @Column(name = "IMPOSTA_RESTANTE_DA_PAGARE")
    private BigDecimal impostaRestanteDaPagare;

    @Column(name = "MORA_TOTALE_DOVUTO")
    private BigDecimal moraTotaleDovuto;

    @Column(name = "MORA_TOTALE_PAGATO")
    private BigDecimal moraTotalePagato;

    @Column(name = "MORA_RESTANTE_DA_PAGARE")
    private BigDecimal moraRestanteDaPagare;

    @Column(name = "INTERESSE_TARDATO_PAGAMENTO_TOTALE_DOVUTO")
    private BigDecimal interesseTardatoPagamentoTotaleDovuto;

    @Column(name = "INTERESSE_TARDATO_PAGAMENTO_TOTALE_PAGATO")
    private BigDecimal interesseTardatoPagamentoTotalePagato;

    @Column(name = "INTERESSE_TARDATO_PAGAMENTO_RESTANTE_DA_PAGARE")
    private BigDecimal interesseTardatoPagamentoRestanteDaPagare;

    @Column(name = "SANZIONE_TOTALE_DOVUTO")
    private BigDecimal sanzioneTotaleDovuto;

    @Column(name = "SANZIONE_TOTALE_PAGATO")
    private BigDecimal sanzioneTotalePagato;

    @Column(name = "SANZIONE_RESTANTE_DA_PAGARE")
    private BigDecimal sanzioneRestanteDaPagare;

    @Column(name = "DATA_MODIFICA")
    private LocalDate dataModifica;

    @Column(name = "FLAG_CANCELLA")
    private Boolean flagCancella;
}
