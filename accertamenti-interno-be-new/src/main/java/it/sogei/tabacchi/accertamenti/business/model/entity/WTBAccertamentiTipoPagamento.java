package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_TIPO_PAGAMENTO")
public class WTBAccertamentiTipoPagamento {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "DESCRIZIONE")
    private String descrizione;
}
