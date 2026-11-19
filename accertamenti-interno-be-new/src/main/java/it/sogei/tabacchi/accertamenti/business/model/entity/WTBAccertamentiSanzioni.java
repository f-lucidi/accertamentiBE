package it.sogei.tabacchi.accertamenti.business.model.entity;

import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// uno scaglione di sanzione: "dal giorno X al giorno Y di ritardo si applica Z" (eventualmente moltiplicato per i giorni).
// Gli scaglioni con la stessa DATA_INIZIO_VAL formano un gruppo (le sanzioni in vigore da quella data).
// A differenza delle aliquote non c'è FLAG_CANCELLA: uno scaglione tolto col cestino si cancella con una DELETE
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_SANZIONI")
public class WTBAccertamentiSanzioni {

    // l'ID lo dà la sequence Oracle al persist; allocationSize = 1 -> un numero per ogni insert
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SANZIONI")
    @SequenceGenerator(name = "SEQ_SANZIONI", sequenceName = "SEQ_W_TB_ACCERTAMENTI_SANZIONI", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "SANZIONE", nullable = false)
    private BigDecimal sanzione;

    // 1 = la sanzione va moltiplicata per i giorni di ritardo, 0 = no (Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_*)
    @Column(name = "MOLTIPLICATO_PER_GIORNI_RITARDO")
    private Integer moltiplicatoPerGiorniRitardo;

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

    @Column(name = "DT_MODIFICA")
    private LocalDate dtModifica;

    // id del gruppo (decorrenza yyyyMMdd): non è una colonna, lo calcola calcolaIdGruppo a ogni lettura dal DB
    @Transient
    private Integer idGruppo;

    // JPA lo chiama dopo ogni select che carica la riga (find, query, getResultList...)
    @PostLoad
    private void calcolaIdGruppo() {
        idGruppo = Utility.toIdGruppo(dataInizioVal);
    }
}
