package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.records.ChiavePeriodoPosizione;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IConsultazioneImposteDao {

    List<WTBAccertamentiCategorieProd> findCategorieProdotti();

    WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria);

    // anno facoltativo: se valorizzato restituisce solo i soggetti attivi in quell'anno
    List<WTBAccertamentiSoggettiV> findAllRagioneSocialeEPivaByCategoriaProdotto(String categoriaProdotto, Integer anno);

    List<WTBAccertamentiTipoPagamento> findAllTipiPagamento();

    WTBAccertamentiTipoPagamento findTipoPagamentoByDescrizione(String descrizione);

    // i criteri null non filtrano
    List<WTBAccertamentiElencoPosizioni> findElencoPosizioni(CriteriRicercaPosizioni criteri, int pagina, int elementiPerPagina);

    Long countElencoPosizioni(CriteriRicercaPosizioni criteri);

    // cf -> denominazione
    Map<String, String> findRagioniSociali(String categoriaProdotto, Collection<String> cfSogObbligati);

    // id posizione -> somma degli interessi legali non pagati
    Map<Long, BigDecimal> findInteressiLegaliNonPagati(Collection<Long> idPosizioni);

    // periodo della posizione -> data dell'ultimo versamento
    Map<ChiavePeriodoPosizione, LocalDate> findDateUltimoVersamento(String categoriaProdotto, Collection<String> cfSogObbligati,
                                                                   Collection<Integer> anni, Collection<Integer> mesi);
}
