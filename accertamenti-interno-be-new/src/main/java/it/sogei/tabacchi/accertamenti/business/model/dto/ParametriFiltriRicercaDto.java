package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

// il frontend invia tutti i campi come stringa ("" se non valorizzati):
// la conversione in numeri/date (e "" -> nessun filtro) avviene in Utility
@Data
public class ParametriFiltriRicercaDto {
    private String annoContabile;
    private String periodoContabile;    // uno dei Constants.Periodo.CODICI_VALIDI (T1..T4, M1..M12, Q1..Q24), oppure vuoto/TUTTE
    private String ragioneSociale;      // contiene la P.IVA
    private String dataPagamentoDa;     // yyyy-MM-dd
    private String dataPagamentoA;      // yyyy-MM-dd
    private String tipoPagamento;
    private String inizioPeriodo;       // yyyy-MM-dd, filtra scadenza >= inizioPeriodo
    private String finePeriodo;         // yyyy-MM-dd, filtra scadenza <= finePeriodo
}
