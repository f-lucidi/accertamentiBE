package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// scaglione di sanzione in vigore da una data di decorrenza.
// Per ora solo lettura: niente sequence (da aggiungere quando si inseriranno sanzioni)
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_SANZIONI")
public class WTBAccertamentiSanzioni {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "SANZIONE", nullable = false)
    private BigDecimal sanzione;

    @Column(name = "MOLTIPLICATO_PER_GIORNI_RITARDO")
    private Integer moltiplicatoPerGiorniRitardo;

    // intervallo di giorni di ritardo a cui si applica lo scaglione; giorniRitardoA null = scaglione aperto
    @Column(name = "GIORNI_RITARDO_DA", nullable = false)
    private Integer giorniRitardoDa;

    @Column(name = "GIORNI_RITARDO_A")
    private Integer giorniRitardoA;

    // data di decorrenza
    @Column(name = "DATA_INIZIO_VAL", nullable = false)
    private LocalDate dataInizioVal;

    @Column(name = "DATA_FINE_VAL")
    private LocalDate dataFineVal;

    @Column(name = "CF_INSERIMENTO", length = 16)
    private String cfInserimento;

    @Column(name = "DT_INSERIMENTO")
    private LocalDate dtInserimento;

    @Column(name = "DT_MODIFICA")
    private LocalDate dtModifica;
}
