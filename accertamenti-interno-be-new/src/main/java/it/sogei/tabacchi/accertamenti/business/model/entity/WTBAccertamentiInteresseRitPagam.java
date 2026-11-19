package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// tasso di interesse sul tardato pagamento in vigore da una data di decorrenza.
// A differenza delle aliquote di mora, ogni decorrenza ha una sola riga (nessuno scaglione).
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_INTERESSE_RIT_PAGAM")
public class WTBAccertamentiInteresseRitPagam {

    // l'ID lo dà la sequence Oracle al momento del persist; allocationSize = 1 -> un numero per ogni insert.
    // Nota: la sequence ha INTERESSI al plurale, a differenza della tabella (verificato sul DB)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INTERESSE_RIT_PAGAM")
    @SequenceGenerator(name = "SEQ_INTERESSE_RIT_PAGAM", sequenceName = "SEQ_W_TB_ACCERTAMENTI_INTERESSI_RIT_PAGAM", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    // percentuale del tasso (es. 2 = 2%)
    @Column(name = "TASSO_INTERESSE")
    private BigDecimal tassoInteresse;

    // data di decorrenza del tasso
    @Column(name = "DATA_INIZIO_VAL", nullable = false)
    private LocalDate dataInizioVal;

    // ultimo giorno di validità; 2999-12-31 = tasso ancora aperto (quello vigente o quello futuro)
    @Column(name = "DATA_FINE_VAL")
    private LocalDate dataFineVal;

    // chi ha inserito il tasso: CF dell'utente oppure "SYSTEM"
    @Column(name = "CF_INSERIMENTO", length = 16)
    private String cfInserimento;

    @Column(name = "DT_INSERIMENTO")
    private LocalDate dtInserimento;

    // cancellazione logica: true = tasso eliminato (la riga resta sul DB)
    @Column(name = "FLAG_CANCELLA", nullable = false)
    private Boolean flagCancella;

    @Column(name = "DATA_MODIFICA")
    private LocalDate dataModifica;

    // id del gruppo (decorrenza yyyyMMdd): non è una colonna, lo calcola calcolaIdGruppo a ogni lettura dal DB
    @Transient
    private Integer idGruppo;

    // JPA lo chiama dopo ogni select che carica la riga (find, query, getResultList...)
    @PostLoad
    private void calcolaIdGruppo() {
        idGruppo = Utility.toIdGruppo(dataInizioVal);
    }
}
