package it.sogei.tabacchi.accertamenti.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "W_TB_ACCERTAMENTI_SOGGETTI_V")
// chiave composta CF_PIVA + DENOMINAZIONE + CATEGORIA: vedi WTBAccertamentiSoggettiVId
@IdClass(WTBAccertamentiSoggettiVId.class)
public class WTBAccertamentiSoggettiV {

    @Id
    @Column(name = "CF_PIVA")
    private String cfPiva;

    @Id
    @Column(name = "DENOMINAZIONE")
    private String denominazione;

    @Column(name = "COD_FISC")
    private String codFisc;

    @Column(name = "DATA_INIZIO_AUTORIZZAZIONE")
    private LocalDate dataInizioAutorizzazione;

    @Column(name = "DATA_FINE_AUTORIZZAZIONE")
    private LocalDate dataFineAutorizzazione;

    @Id
    @Column(name = "CATEGORIA")
    private String categoria;
}
