package it.sogei.tabacchi.accertamenti.business.model.dto.esportazione;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// una posizione dell'export Excel: il file lo genera il frontend (ExcelJS) con questi dati
@Data
public class EsportazionePosizioneDto {

    private Long idPosizione;
    private String ragioneSociale;                       // da W_TB_ACCERTAMENTI_SOGGETTI_V
    private String partitaIva;                           // CF_SOG_OBBLIGATO
    private LocalDate dataAttivazione;                   // inizio autorizzazione del soggetto
    private LocalDate dataCessazione;                    // fine autorizzazione, null se ancora attivo
    private Integer annoContabile;
    private String periodoContabile;                     // es. "02/2027" (mese) o "31/12/2026" (quindicina)

    private BigDecimal impostaDovuta;
    private BigDecimal impostaRestante;
    private BigDecimal moraDovuta;
    private BigDecimal moraRestante;
    private BigDecimal interessiTardatoPagamentoDovuti;
    private BigDecimal interessiTardatoPagamentoRestanti;
    private BigDecimal interessiLegaliDovuti;            // somma di tutti gli anni
    private BigDecimal interessiLegaliRestanti;          // somma degli anni non pagati
    private BigDecimal sanzioneDovuta;
    private BigDecimal sanzioneRestante;

    // versamenti della posizione dal più vecchio; lista vuota se non ha pagato niente
    private List<EsportazioneVersamentoDto> versamenti;
}
