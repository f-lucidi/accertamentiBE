package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_STATI_PAGAMENTO")
public class WTBAccertamentiStatiPagamento {

    @Id
    @Column(name = "ID_STATO")
    private Long id;

    @Column(name = "DESCRIZIONE_STATO")
    private String descrizione;
}
