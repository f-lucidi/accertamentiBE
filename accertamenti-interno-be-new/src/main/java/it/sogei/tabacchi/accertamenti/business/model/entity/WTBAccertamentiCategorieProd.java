package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_CATEGORIE_PROD")

public class WTBAccertamentiCategorieProd {

    @Id
    @Column(name = "SIGLA", nullable = false)
    private String sigla;
    @Column(name = "DESCRIZIONE")
    private String descrizione;
}
