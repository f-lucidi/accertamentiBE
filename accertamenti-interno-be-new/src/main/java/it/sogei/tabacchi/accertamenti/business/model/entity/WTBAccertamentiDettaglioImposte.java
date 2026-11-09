package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_DETTAGLIO_IMPOSTE")

public class WTBAccertamentiDettaglioImposte {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "ID_POSIZIONE", nullable = false)
    private Long idPosizione;

    @Column(name = "CF_SOG_OBBLIGATO", nullable = false, length = 20)
    private String cfSogObbligato;

    @Column(name = "ANNO", nullable = false)
    private Integer anno;

    @Column(name = "MESE", nullable = false)
    private Integer mese;

    @Column(name = "NUM_QUINDICINA", nullable = false)
    private Integer numQuindicina;

    @Column(name = "SCADENZA", nullable = false)
    private LocalDate scadenza;

    @Column(name = "IMPOSTA_IMPORTO_PARZIALE", nullable = false)
    private BigDecimal impostaImportoParziale;

    @Column(name = "DATA_PAGAMENTO")
    private LocalDate dataPagamento;

    @Column(name = "TIPO_PAGAMENTO")
    private Integer tipoPagamento;

    @Column(name = "STATO_IMPORTO_PARZIALE", length = 20)
    private String statoImportoParziale;

    @Column(name = "GIORNI_RITARDO")
    private Integer giorniRitardo;

    @Column(name = "MORA_PARZIALE_DOVUTO")
    private BigDecimal moraParzialeDovuto;

    @Column(name = "INTERESSE_TARDATO_PAGAMENTO_PARZIALE_DOVUTO")
    private BigDecimal interesseTardatoPagamentoParzialeDovuto;

    @Column(name = "INTERESSE_LEG_PARZIALE_DOVUTO")
    private BigDecimal interesseLegParzialeDovuto;

    @Column(name = "SANZIONE_PARZIALE_DOVUTO")
    private BigDecimal sanzioneParzialeDovuto;

    @Column(name = "DATA_MODIFICA")
    private LocalDateTime dataModifica;

    @Column(name = "CATEGORIA", length = 20)
    private String categoria;

    @Column(name = "ID_PAGAMENTO", length = 30)
    private String idPagamento;

    @Column(name = "RECORD_DA_ALLINEARE")
    private Boolean recordDaAllineare;
}

