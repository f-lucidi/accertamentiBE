package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_SOGGETTI_V")
public class WTbAccertamentiSoggettiV {

    @Id
    @Column(name = "CF_PIVA")
    private String cfPiva;

    @Column(name = "DENOMINAZIONE")
    private String denominazione;

    @Column(name = "COD_FISC")
    private String codFisc;

    @Column(name = "DATA_INIZIO_AUTORIZZAZIONE")
    private LocalDate dataInizioAutorizzazione;

    @Column(name = "DATA_FINE_AUTORIZZAZIONE")
    private LocalDate dataFineAutorizzazione;

    @Column(name = "CATEGORIA")
    private String categoria;
}
