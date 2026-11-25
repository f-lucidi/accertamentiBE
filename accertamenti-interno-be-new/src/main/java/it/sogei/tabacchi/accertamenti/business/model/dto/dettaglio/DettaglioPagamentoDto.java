package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.math.BigDecimal;

// blocco in testa al dettaglio: i nomi rispecchiano il tipo DettaglioPagamento del frontend
@Data
public class DettaglioPagamentoDto {

    private Long idPosizione;
    private String piva;                        // CF_SOG_OBBLIGATO
    private String ragioneSociale;              // da W_TB_ACCERTAMENTI_SOGGETTI_V
    private String categoriaProdotto;           // descrizione della categoria
    private String statoRendicontazione;        // "cancellata" / "trasmessa"
    private String periodoContabile;            // es. "02/2027" o "15/12/2026"
    private BigDecimal importoTotaleDaPagare;
    private String statoPosizione;
    private String dataScadenza;                // dd/MM/yyyy
}
