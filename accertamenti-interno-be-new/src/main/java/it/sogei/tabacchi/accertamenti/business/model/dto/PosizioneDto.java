package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// i nomi dei campi rispecchiano il tipo PosizioneImposta del frontend
@Data
public class PosizioneDto {

    private Long idPosizione;
    private String partitaIva;                  // CF_SOG_OBBLIGATO
    private String ragioneSociale;              // da W_TB_ACCERTAMENTI_SOGGETTI_V (da valorizzare)
    private String periodoContabile;            // es. "02/2027" (mensilità) o "31/12/2026" (quindicina)
    private BigDecimal totaleImpostaDovuta;
    private String statoPosizione;
    private BigDecimal totaleImpostaVersata;    // da valorizzare
    private LocalDate dataUltimoVersamento;
    private BigDecimal indennitaMora;
    private BigDecimal interessiTardatoPagamento;
    private BigDecimal interessiLegali;
    private BigDecimal sanzione;
}
