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

// tasso di interesse legale valido per un anno solare: un solo tasso non cancellato per anno, nessuna decorrenza
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_INTERESSE_LEGALE")
public class WTBAccertamentiInteresseLegale {

    // l'ID lo dà la sequence Oracle al momento del persist; allocationSize = 1 -> un numero per ogni insert
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INTERESSE_LEGALE")
    @SequenceGenerator(name = "SEQ_INTERESSE_LEGALE", sequenceName = "SEQ_W_TB_ACCERTAMENTI_INTERESSI_LEGALI", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "TASSO_INTERESSE")
    private BigDecimal tassoInteresse;

    // anno di validità del tasso (già intero, non una data)
    @Column(name = "ANNO_VALID")
    private Integer annoValid;

    @Column(name = "CF_INSERIMENTO", length = 16)
    private String cfInserimento;

    @Column(name = "DT_INSERIMENTO")
    private LocalDate dtInserimento;

    // Integer e non Boolean: a DB vale 0, 1 o 2. 0 = riga valida; 1 e 2 = cancellata (stesso significato),
    // quindi ogni valore diverso da 0 è una cancellazione. In cancellazione si scrive 1 (FLAG_CANCELLA_SI)
    @Column(name = "FLAG_CANCELLA")
    private Integer flagCancella;

    @Column(name = "DATA_MODIFICA")
    private LocalDate dataModifica;
}
