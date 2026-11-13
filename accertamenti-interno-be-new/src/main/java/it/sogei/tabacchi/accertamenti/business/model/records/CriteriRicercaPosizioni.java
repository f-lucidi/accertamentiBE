package it.sogei.tabacchi.accertamenti.business.model.records;

import it.sogei.tabacchi.accertamenti.business.model.dto.PeriodoContabileDto;

import java.time.LocalDate;

/**
 * Filtri della ricerca posizioni già convertiti nei tipi giusti: li costruisce il controller una volta sola
 * a partire dalle stringhe ricevute dal frontend, così il DAO riceve valori pronti e contiene solo la query.
 * I campi null significano "filtro non applicato" (tranne categoria e anno, sempre presenti).
 *
 * @param categoria        sigla della categoria, già verificata su W_TB_ACCERTAMENTI_CATEGORIE_PROD
 * @param anno             anno contabile (obbligatorio)
 * @param partitaIva       P.IVA del soggetto (dal FE arriva nel campo "ragioneSociale")
 * @param periodoContabile trimestre/mese/quindicina già interpretato
 * @param inizioPeriodo    limite inferiore sulla scadenza della posizione
 * @param finePeriodo      limite superiore sulla scadenza della posizione
 * @param dataPagamentoDa  limite inferiore sulla data di almeno un versamento
 * @param dataPagamentoA   limite superiore sulla data di almeno un versamento
 * @param idTipoPagamento  ID del tipo pagamento (il FE invia la descrizione, il controller la converte)
 */
public record CriteriRicercaPosizioni(String categoria,
                                      Integer anno,
                                      String partitaIva,
                                      PeriodoContabileDto periodoContabile,
                                      LocalDate inizioPeriodo,
                                      LocalDate finePeriodo,
                                      LocalDate dataPagamentoDa,
                                      LocalDate dataPagamentoA,
                                      Integer idTipoPagamento) {
}
