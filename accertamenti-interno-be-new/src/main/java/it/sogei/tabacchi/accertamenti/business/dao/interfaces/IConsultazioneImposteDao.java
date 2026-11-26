package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV;
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IConsultazioneImposteDao {

    List<WTBAccertamentiCategorieProd> findCategorieProdotti();

    WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria);

    // descrizione della categoria, null se la sigla non esiste
    String findDescrizioneCategoria(String siglaCategoria);

    // anno facoltativo: se valorizzato restituisce solo i soggetti attivi in quell'anno
    List<WTBAccertamentiSoggettiV> findAllRagioneSocialeEPivaByCategoriaProdotto(String categoriaProdotto, Integer anno);

    List<WTBAccertamentiTipoPagamento> findAllTipiPagamento();

    WTBAccertamentiTipoPagamento findTipoPagamentoByDescrizione(String descrizione);

    // i criteri null non filtrano
    List<WTBAccertamentiElencoPosizioni> findElencoPosizioni(CriteriRicercaPosizioni criteri, int pagina, int elementiPerPagina);

    Long countElencoPosizioni(CriteriRicercaPosizioni criteri);

    // denominazione del soggetto, null se non trovato
    String findRagioneSociale(String categoriaProdotto, String cfSogObbligato);

    // somma degli interessi legali non pagati della posizione (0 se non ce ne sono)
    BigDecimal findInteressiLegaliNonPagati(Long idPosizione);

    // data più recente di pagamento nel periodo della posizione, null se non ci sono versamenti
    LocalDate findDataUltimoVersamento(String categoriaProdotto, String cfSogObbligato,
                                       Integer anno, Integer mese, Integer numQuindicina);

    // ------------ esportazione excel: 5 query, il controller unisce i risultati ------------

    // tutte le posizioni della ricerca (senza paginazione), ordinate per anno, mese, quindicina, id
    List<WTBAccertamentiElencoPosizioni> findPosizioniDaEsportare(CriteriRicercaPosizioni criteri);

    List<WTBAccertamentiSoggettiV> findSoggettiPerEsportazione(String categoria, List<String> listaCfSoggetti);

    List<WTBAccertamentiInteressiLegaliPerAnno> findInteressiLegaliPerEsportazione(List<Long> listaIdPosizioni);

    // solo gli importi versati (stato diverso da "mancante"), ordinati per data di pagamento
    List<WTBAccertamentiDettaglioImposte> findImposteVersatePerEsportazione(List<Long> listaIdPosizioni);

    // mora, interessi, interessi legali e sanzioni pagati, ordinati per data di pagamento
    List<WTBAccertamentiVersMoreInteressiSanzioniV> findVersamentiAccessoriPerEsportazione(CriteriRicercaPosizioni criteri);
}
