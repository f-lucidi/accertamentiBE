package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IDettaglioDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class DettaglioDao implements IDettaglioDao {

    @Inject
    private CriteriaQueryManager queryManager;

    // SELECT * FROM W_TB_ACCERTAMENTI_ELENCO_POSIZIONI WHERE ID = :idPosizione   -- nessun filtro su FLAG_CANCELLA
    @Override
    public WTBAccertamentiElencoPosizioni findPosizioneById(Long idPosizione) {
        return queryManager.findById(WTBAccertamentiElencoPosizioni.class, idPosizione);
    }

    // SELECT * FROM W_TB_ACCERTAMENTI_DETTAGLIO_IMPOSTE WHERE ID_POSIZIONE = :idPosizione
    // ORDER BY STATO_IMPORTO_PARZIALE DESC, DATA_PAGAMENTO, ID   -- + paginazione
    @Override
    public List<WTBAccertamentiDettaglioImposte> findDettaglioImposte(Long idPosizione, int pagina, int elementiPerPagina) {
        return queryManager.createBuilder(WTBAccertamentiDettaglioImposte.class)
                .addEqualCondition(WTBAccertamentiDettaglioImposte_.idPosizione, idPosizione)
                .orderByDesc(WTBAccertamentiDettaglioImposte_.statoImportoParziale)
                .orderByAsc(WTBAccertamentiDettaglioImposte_.dataPagamento)
                .orderByAsc(WTBAccertamentiDettaglioImposte_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // SELECT COUNT(*) FROM W_TB_ACCERTAMENTI_DETTAGLIO_IMPOSTE WHERE ID_POSIZIONE = :idPosizione
    @Override
    public Long countDettaglioImposte(Long idPosizione) {
        return queryManager.createBuilder(WTBAccertamentiDettaglioImposte.class)
                .selectCount()
                .addEqualCondition(WTBAccertamentiDettaglioImposte_.idPosizione, idPosizione)
                .getSingleResult();
    }

    @Override
    public WTBAccertamentiTipoPagamento findTipoPagamentoById(Long idTipoPagamento) {
        return queryManager.findById(WTBAccertamentiTipoPagamento.class, idTipoPagamento);
    }

    // SELECT * FROM W_TB_ACCERTAMENTI_ALIQUOTA_MORA
    // WHERE GIORNI_RITARDO_DA <= :giorni AND (GIORNI_RITARDO_A IS NULL OR GIORNI_RITARDO_A >= :giorni)
    //   AND DATA_INIZIO_VAL <= :data AND DATA_FINE_VAL >= :data AND FLAG_CANCELLA = 0
    @Override
    public List<WTBAccertamentiAliquotaMora> findAliquoteMoraVigenti(Integer giorniRitardo, LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiAliquotaMora.class)
                .addLessThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.giorniRitardoDa, giorniRitardo)
                .orConditions(builder -> builder
                        .addIsNullCondition(WTBAccertamentiAliquotaMora_.giorniRitardoA)
                        .addGreaterThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.giorniRitardoA, giorniRitardo))
                .addLessThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataInizioVal, data)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiAliquotaMora_.dataFineVal, data)
                .addEqualCondition(WTBAccertamentiAliquotaMora_.flagCancella, false)
                .getResultList();
    }

    // SELECT * FROM W_TB_ACCERTAMENTI_INTERESSE_RIT_PAGAM
    // WHERE DATA_INIZIO_VAL <= :data AND DATA_FINE_VAL >= :data AND FLAG_CANCELLA = 0
    @Override
    public List<WTBAccertamentiInteresseRitPagam> findInteressiRitPagamVigenti(LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiInteresseRitPagam.class)
                .addLessThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataInizioVal, data)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiInteresseRitPagam_.dataFineVal, data)
                .addEqualCondition(WTBAccertamentiInteresseRitPagam_.flagCancella, false)
                .getResultList();
    }

    // SELECT * FROM W_TB_ACCERTAMENTI_INTERESSE_LEGALE
    // WHERE ANNO_VALID >= :anno AND FLAG_CANCELLA = 0 ORDER BY ANNO_VALID
    @Override
    public List<WTBAccertamentiInteresseLegale> findInteressiLegaliDaAnno(Integer anno) {
        return queryManager.createBuilder(WTBAccertamentiInteresseLegale.class)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiInteresseLegale_.annoValid, anno)
                // qui FLAG_CANCELLA è numerico: 0 = valida (esclude sia 1 sia 2)
                .addEqualCondition(WTBAccertamentiInteresseLegale_.flagCancella, Constants.GestioneParametri.FLAG_CANCELLA_NO)
                .orderByAsc(WTBAccertamentiInteresseLegale_.annoValid)
                .getResultList();
    }

    // SELECT * FROM W_TB_ACCERTAMENTI_INTERESSI_LEGALI_PER_ANNO WHERE ID_POSIZIONE = :idPosizione ORDER BY ANNO
    @Override
    public List<WTBAccertamentiInteressiLegaliPerAnno> findInteressiLegaliPerAnno(Long idPosizione) {
        return queryManager.createBuilder(WTBAccertamentiInteressiLegaliPerAnno.class)
                .addEqualCondition(WTBAccertamentiInteressiLegaliPerAnno_.idPosizione, idPosizione)
                .orderByAsc(WTBAccertamentiInteressiLegaliPerAnno_.anno)
                .getResultList();
    }

    // come le aliquote di mora, ma la tabella non ha FLAG_CANCELLA (la cancellazione è fisica)
    @Override
    public List<WTBAccertamentiSanzioni> findSanzioniVigenti(Integer giorniRitardo, LocalDate data) {
        return queryManager.createBuilder(WTBAccertamentiSanzioni.class)
                .addLessThanOrEqualToCondition(WTBAccertamentiSanzioni_.giorniRitardoDa, giorniRitardo)
                .orConditions(builder -> builder
                        .addIsNullCondition(WTBAccertamentiSanzioni_.giorniRitardoA)
                        .addGreaterThanOrEqualToCondition(WTBAccertamentiSanzioni_.giorniRitardoA, giorniRitardo))
                .addLessThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataInizioVal, data)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiSanzioni_.dataFineVal, data)
                .getResultList();
    }

    // stesso collegamento versamento -> posizione della ricerca (cf + categoria + anno + mese + quindicina):
    //   SELECT * FROM W_TB_ACCERTAMENTI_VERS_MORE_INTERESSI_SANZIONI_V
    //   WHERE CF_SOG_OBBLIGATO = :cf AND CATEGORIA = :sigla AND ANNO = :anno AND MESE = :mese
    //     AND NUM_QUINDICINA = :numQuindicina AND TIPO = :tipo
    //   ORDER BY DATA_PAGAMENTO DESC                 -- il primo è l'ultimo pagamento
    @Override
    public List<WTBAccertamentiVersMoreInteressiSanzioniV> findVersamenti(WTBAccertamentiElencoPosizioni posizione, String tipo) {
        return queryManager.createBuilder(WTBAccertamentiVersMoreInteressiSanzioniV.class)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.cfSogObbligato, posizione.getCfSogObbligato())
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.categoria, posizione.getCategoria())
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.anno, posizione.getAnno())
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.mese, posizione.getMese())
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.numQuindicina, posizione.getNumQuindicina())
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.tipo, tipo)
                .orderByDesc(WTBAccertamentiVersMoreInteressiSanzioniV_.dataPagamento)
                .getResultList();
    }
}
