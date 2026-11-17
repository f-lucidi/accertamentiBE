package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// uno scaglione di aliquota di mora: "dal giorno X al giorno Y di ritardo si applica Z%".
// Gli scaglioni con la stessa DATA_INIZIO_VAL formano un gruppo (le aliquote in vigore da quella data).
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_ALIQUOTA_MORA")
public class WTBAccertamentiAliquotaMora {

    // prima entity che scrive: l'ID non lo sceglie il codice, lo dà la sequence Oracle al momento del persist.
    // allocationSize = 1 -> un numero per ogni insert (senza, JPA ne "prenoterebbe" 50 alla volta)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_ALIQUOTA_MORA")
    @SequenceGenerator(name = "SEQ_ALIQUOTA_MORA", sequenceName = "SEQ_W_TB_ACCERTAMENTI_ALIQUOTA_MORA", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    // percentuale dello scaglione (es. 2 = 2%)
    @Column(name = "ALIQUOTA", nullable = false)
    private BigDecimal aliquota;

    // intervallo di giorni di ritardo a cui si applica lo scaglione
    @Column(name = "GIORNI_RITARDO_DA", nullable = false)
    private Integer giorniRitardoDa;

    // null = scaglione aperto ("da N giorni in poi"): può esserlo solo l'ultimo del gruppo
    @Column(name = "GIORNI_RITARDO_A")
    private Integer giorniRitardoA;

    // data di decorrenza del gruppo
    @Column(name = "DATA_INIZIO_VAL", nullable = false)
    private LocalDate dataInizioVal;

    // ultimo giorno di validità; 2999-12-31 = gruppo ancora aperto (quello vigente o quello futuro)
    @Column(name = "DATA_FINE_VAL")
    private LocalDate dataFineVal;

    // chi ha inserito lo scaglione: CF dell'utente oppure "SYSTEM"
    @Column(name = "CF_INSERIMENTO", length = 16)
    private String cfInserimento;

    @Column(name = "DT_INSERIMENTO")
    private LocalDate dtInserimento;

    // cancellazione logica: true = scaglione eliminato (la riga resta sul DB)
    @Column(name = "FLAG_CANCELLA", nullable = false)
    private Boolean flagCancella;

    @Column(name = "DATA_MODIFICA")
    private LocalDate dataModifica;
}
