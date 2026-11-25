package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV;

import java.time.LocalDate;
import java.util.List;

public interface IDettaglioDao {

    // per id, comprese le posizioni cancellate; null se non esiste
    WTBAccertamentiElencoPosizioni findPosizioneById(Long idPosizione);

    // righe della tabella imposta dovuta: prima i mancanti, poi i versati per data di pagamento
    List<WTBAccertamentiDettaglioImposte> findDettaglioImposte(Long idPosizione, int pagina, int elementiPerPagina);

    Long countDettaglioImposte(Long idPosizione);

    // metodo di pagamento di un versamento; null se non trovato
    WTBAccertamentiTipoPagamento findTipoPagamentoById(Long idTipoPagamento);

    // aliquote di mora non cancellate, vigenti alla data, nel cui scaglione cadono i giorni di ritardo
    List<WTBAccertamentiAliquotaMora> findAliquoteMoraVigenti(Integer giorniRitardo, LocalDate data);

    // tassi sul tardato pagamento non cancellati, vigenti alla data
    List<WTBAccertamentiInteresseRitPagam> findInteressiRitPagamVigenti(LocalDate data);

    // tassi di interesse legale non cancellati, dall'anno indicato in poi, in ordine di anno
    List<WTBAccertamentiInteresseLegale> findInteressiLegaliDaAnno(Integer anno);

    // interessi legali della posizione, uno per anno, in ordine di anno
    List<WTBAccertamentiInteressiLegaliPerAnno> findInteressiLegaliPerAnno(Long idPosizione);

    // sanzioni vigenti alla data nel cui scaglione cadono i giorni di ritardo
    List<WTBAccertamentiSanzioni> findSanzioniVigenti(Integer giorniRitardo, LocalDate data);

    // versamenti di mora / interesse / sanzione dello stesso periodo della posizione (tipo = colonna TIPO della vista)
    List<WTBAccertamentiVersMoreInteressiSanzioniV> findVersamenti(WTBAccertamentiElencoPosizioni posizione, String tipo);
}
