package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// tasso di interesse legale valido per un anno.
// Per ora solo lettura: niente sequence (da aggiungere quando si inseriranno tassi)
@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_INTERESSE_LEGALE")
public class WTBAccertamentiInteresseLegale {

    @Id
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

    // Integer e non Boolean: a DB vale 0, 1 o 2. Solo 0 = riga valida (Constants.GestioneParametri.FLAG_CANCELLA_NO)
    @Column(name = "FLAG_CANCELLA")
    private Integer flagCancella;

    @Column(name = "DATA_MODIFICA")
    private LocalDate dataModifica;
}
