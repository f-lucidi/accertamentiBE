package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// vista dei versamenti (imposta, mora, interessi, sanzioni): si collega alla posizione
// tramite cf + categoria + anno + mese + num_quindicina
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_VERS_MORE_INTERESSI_SANZIONI_V")
public class WTBAccertamentiVersMoreInteressiSanzioniV {

    @Id
    @Column(name = "IDK_VERSAMENTO")
    private Long idkVersamento;

    @Column(name = "CF_SOG_OBBLIGATO")
    private String cfSogObbligato;

    @Column(name = "CATEGORIA")
    private String categoria;

    @Column(name = "ANNO")
    private Integer anno;

    @Column(name = "MESE")
    private Integer mese;

    @Column(name = "NUM_QUINDICINA")
    private Integer numQuindicina;

    @Column(name = "DATA_PAGAMENTO")
    private LocalDate dataPagamento;

    // ID di W_TB_ACCERTAMENTI_TIPO_PAGAMENTO
    @Column(name = "TIPO_PAGAMENTO")
    private Integer tipoPagamento;

    @Column(name = "TIPO")
    private String tipo;

    @Column(name = "IMPORTO")
    private BigDecimal importo;
}
