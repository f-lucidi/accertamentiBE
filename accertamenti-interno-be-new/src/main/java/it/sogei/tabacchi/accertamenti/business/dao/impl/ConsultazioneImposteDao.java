package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryBuilder;
import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.dto.PeriodoContabileDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// applicationScoped: indica che l'istanza di questa classe sarà condivisa tra tutte
// le richieste e sessioni dell'applicazione
@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteDao implements IConsultazioneImposteDao {

    @Inject
    private CriteriaQueryManager queryManager;

    @Override
    public List<WTBAccertamentiCategorieProd> findCategorieProdotti() {
        return queryManager.createBuilder(WTBAccertamentiCategorieProd.class)
                .orderByAsc(WTBAccertamentiCategorieProd_.sigla)
                .getResultList();
    }

    @Override
    public WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria) {
        return queryManager.findById(WTBAccertamentiCategorieProd.class, siglaCategoria);
    }

    // Menu "ragione sociale - P.IVA" dei filtri: è il punto di partenza del flusso. Il FE mostra la coppia
    // DENOMINAZIONE / CF_PIVA e poi rimanda alla ricerca solo CF_PIVA, che si confronta con CF_SOG_OBBLIGATO
    // delle posizioni.
    //   SELECT * FROM W_TB_ACCERTAMENTI_SOGGETTI_V
    //   WHERE CATEGORIA = :sigla
    //   [ AND (DATA_INIZIO_AUTORIZZAZIONE IS NULL OR DATA_INIZIO_AUTORIZZAZIONE <= :31dicembreAnno)
    //     AND (DATA_FINE_AUTORIZZAZIONE IS NULL OR DATA_FINE_AUTORIZZAZIONE >= :1gennaioAnno) ]   -- solo con l'anno
    //   ORDER BY DENOMINAZIONE
    @Override
    public List<WTBAccertamentiSoggettiV> findAllRagioneSocialeEPivaByCategoriaProdotto(String categoriaProdotto, Integer anno) {
        CriteriaQueryBuilder<WTBAccertamentiSoggettiV> builder = queryManager.createBuilder(WTBAccertamentiSoggettiV.class)
                .addEqualCondition(WTBAccertamentiSoggettiV_.categoria, categoriaProdotto.toUpperCase());
        if (null != anno) {
            soloAttiviNellAnno(builder, anno);
        }
        return builder.orderByAsc(WTBAccertamentiSoggettiV_.denominazione).getResultList();
    }

    // attivo nell'anno = l'autorizzazione si sovrappone all'anno:
    // inizio (se presente) <= 31/12/anno  E  fine assente oppure >= 01/01/anno
    private CriteriaQueryBuilder<WTBAccertamentiSoggettiV> soloAttiviNellAnno(
            CriteriaQueryBuilder<WTBAccertamentiSoggettiV> builder, int anno) {
        LocalDate inizioAnno = LocalDate.of(anno, 1, 1);
        LocalDate fineAnno = LocalDate.of(anno, 12, 31);
        return builder
                .orConditions(b -> b
                        .addIsNullCondition(WTBAccertamentiSoggettiV_.dataInizioAutorizzazione)
                        .addLessThanOrEqualToCondition(WTBAccertamentiSoggettiV_.dataInizioAutorizzazione, fineAnno))
                .orConditions(b -> b
                        .addIsNullCondition(WTBAccertamentiSoggettiV_.dataFineAutorizzazione)
                        .addGreaterThanOrEqualToCondition(WTBAccertamentiSoggettiV_.dataFineAutorizzazione, inizioAnno));
    }

    @Override
    public List<WTBAccertamentiTipoPagamento> findAllTipiPagamento() {
        return queryManager.findAll(WTBAccertamentiTipoPagamento.class);
    }

    @Override
    public WTBAccertamentiTipoPagamento findTipoPagamentoByDescrizione(String descrizione) {
        List<WTBAccertamentiTipoPagamento> tipi = queryManager.createBuilder(WTBAccertamentiTipoPagamento.class)
                .addEqualCondition(WTBAccertamentiTipoPagamento_.descrizione, descrizione)
                .getResultList();
        return tipi.isEmpty() ? null : tipi.get(0);
    }

    // ------------ ELENCO POSIZIONI ------------
    //
    // FLUSSO: il controller chiama in sequenza
    //   1) findElencoPosizioni  -> le righe della pagina richiesta
    //   2) countElencoPosizioni -> il totale delle righe x paginazione
    //
    //   SELECT p.*                                                   -- tutte le colonne di WTBAccertamentiElencoPosizioni
    //   FROM W_TB_ACCERTAMENTI_ELENCO_POSIZIONI p
    //   WHERE p.FLAG_CANCELLA = 0                                    -- escludo le posizioni soft-cancellate
    //     AND p.CATEGORIA = :sigla                                   -- categoria dal path (sigla già verificata)
    //     AND p.ANNO = :annoContabile                                -- anno contabile (obbligatorio, validato prima)
    //   [ AND p.CF_SOG_OBBLIGATO = :partitaIva ]                     -- P.IVA scelta nel menu "ragione sociale - P.IVA"
    //   [ AND p.SCADENZA >= :inizioPeriodo ]                         -- inizio periodo
    //   [ AND p.SCADENZA <= :finePeriodo ]                           -- fine periodo
    //   [ AND p.MESE IN (:mesi) ]                                    -- periodo contabile: 3 mesi (T1..T4) o 1 mese (M1..M12, Q1..Q24)
    //   [ AND p.NUM_QUINDICINA = :numQuindicina ]                    -- solo per Q1..Q24: 1 = prima quindicina, 2 = seconda
    //   [ AND EXISTS (                                               -- solo se c'è almeno un filtro sui pagamenti
    //         SELECT 1
    //         FROM W_TB_ACCERTAMENTI_VERS_MORE_INTERESSI_SANZIONI_V v
    //         WHERE v.CF_SOG_OBBLIGATO = p.CF_SOG_OBBLIGATO          -- collegamento versamento -> posizione:
    //           AND v.CATEGORIA        = p.CATEGORIA                 --   stesso soggetto, stessa categoria
    //           AND v.ANNO             = p.ANNO                      --   e stesso periodo
    //           AND v.MESE             = p.MESE                      --   (anno + mese + quindicina)
    //           AND v.NUM_QUINDICINA   = p.NUM_QUINDICINA
    //         [ AND v.DATA_PAGAMENTO >= :dataPagamentoDa ]
    //         [ AND v.DATA_PAGAMENTO <= :dataPagamentoA ]
    //         [ AND v.TIPO_PAGAMENTO  = :idTipoPagamento ]           -- ID ricavato dal controller dalla descrizione
    //     ) ]
    //   ORDER BY p.SCADENZA DESC, p.ID ASC                           -- più recenti prima; ID per un ordine stabile
    //   OFFSET (:pagina - 1) * :elementiPerPagina ROWS               -- paginazione (setFirstResult)
    //   FETCH NEXT :elementiPerPagina ROWS ONLY                      -- paginazione (setMaxResults)
    //
    // La descrizione dello stato (@ManyToOne statoPagamento) la carica Hibernate a parte:
    //   SELECT * FROM W_TB_ACCERTAMENTI_STATI_PAGAMENTO WHERE ID_STATO = :statoPosizione
    // ---------------------------------------------------------------------------------------------------------
    @Override
    public List<WTBAccertamentiElencoPosizioni> findElencoPosizioni(CriteriRicercaPosizioni criteri, int pagina, int elementiPerPagina) {
        // 1. createBuilder: nuova query "SELECT p FROM WTBAccertamentiElencoPosizioni p" (ancora senza WHERE)
        // 2. applicaFiltriElencoPosizioni: aggiunge tutte le condizioni della WHERE
        // 3. getPaginatedResults: esegue la query leggendo solo le righe della pagina (offset = (pagina - 1) * elementi)
        return applicaFiltriElencoPosizioni(queryManager.createBuilder(WTBAccertamentiElencoPosizioni.class), criteri)
                .orderByDesc(WTBAccertamentiElencoPosizioni_.scadenza)
                .orderByAsc(WTBAccertamentiElencoPosizioni_.id)
                .getPaginatedResults(pagina, elementiPerPagina);
    }

    // Stessa WHERE di findElencoPosizioni, ma "SELECT COUNT(p)" e senza ORDER BY né paginazione:
    //   SELECT COUNT(*) FROM W_TB_ACCERTAMENTI_ELENCO_POSIZIONI p WHERE <stesse condizioni>
    @Override
    public Long countElencoPosizioni(CriteriRicercaPosizioni criteri) {
        // selectCount va chiamato PRIMA dei filtri (anche dell'EXISTS), così la subquery è creata sulla query di count
        return applicaFiltriElencoPosizioni(queryManager.createBuilder(WTBAccertamentiElencoPosizioni.class).selectCount(), criteri)
                .getSingleResult();
    }

    // filtri comuni a ricerca e count: i criteri arrivano già convertiti dal controller (null = filtro non applicato).
    // Ogni chiamata add...Condition aggiunge un pezzo alla WHERE, unito ai precedenti con AND.
    private CriteriaQueryBuilder<WTBAccertamentiElencoPosizioni> applicaFiltriElencoPosizioni(
            CriteriaQueryBuilder<WTBAccertamentiElencoPosizioni> builder, CriteriRicercaPosizioni criteri) {
        builder
                // Serve a rendere facoltativi i filtri: dopo quella chiamata, una condizione
                // con valore null viene saltata invece di finire nella query.
                // (senza, un valore null produrrebbe "COLONNA IS NULL" e non troverebbe nulla)
                .addConditionsWhenValueIsNull(false)
                // posso tirare fuori solo posizioni che non sono state soft-cancellate
                // -> AND p.FLAG_CANCELLA = 0
                .addEqualCondition(WTBAccertamentiElencoPosizioni_.flagCancella, false)
                // -> AND p.CATEGORIA = :sigla
                .addEqualCondition(WTBAccertamentiElencoPosizioni_.categoria, criteri.categoria())
                // -> AND p.ANNO = :annoContabile (il validator ne ha già garantito la presenza)
                .addEqualCondition(WTBAccertamentiElencoPosizioni_.anno, criteri.anno())
                // -> AND p.CF_SOG_OBBLIGATO = :partitaIva (saltata se non scelta)
                .addEqualCondition(WTBAccertamentiElencoPosizioni_.cfSogObbligato, criteri.partitaIva())
                // inizio/fine periodo: intervallo sulla scadenza
                // -> AND p.SCADENZA >= :inizioPeriodo   /   AND p.SCADENZA <= :finePeriodo (ognuna saltata se null)
                .addGreaterThanOrEqualToCondition(WTBAccertamentiElencoPosizioni_.scadenza, criteri.inizioPeriodo())
                .addLessThanOrEqualToCondition(WTBAccertamentiElencoPosizioni_.scadenza, criteri.finePeriodo());

        // periodo contabile (T1..T4, M1..M12, Q1..Q24 già tradotti in mesi + quindicina)
        applicaFiltroPeriodoContabile(builder, criteri.periodoContabile());
        // filtri che non stanno su ELENCO_POSIZIONI ma sui versamenti: diventano una subquery EXISTS
        applicaFiltroPagamenti(builder, criteri);
        return builder;
    }

    // il periodo di una posizione è identificato da MESE + NUM_QUINDICINA (non da SCADENZA, che per le
    // quindicine vale sempre l'ultimo giorno del mese, anche per la prima quindicina)
    // periodo null = nessun filtro: i valori null vengono saltati grazie a addConditionsWhenValueIsNull(false)
    // Esempi:  T2  -> mesi [4,5,6], quindicina null -> AND p.MESE IN (4, 5, 6)
    //          M3  -> mesi [3],     quindicina null -> AND p.MESE IN (3)
    //          Q4  -> mesi [2],     quindicina 2    -> AND p.MESE IN (2) AND p.NUM_QUINDICINA = 2
    //          "Tutte" o vuoto -> periodo null       -> nessuna condizione
    private void applicaFiltroPeriodoContabile(CriteriaQueryBuilder<WTBAccertamentiElencoPosizioni> builder, PeriodoContabileDto periodoContabileDto) {
        builder
                .addInCondition(WTBAccertamentiElencoPosizioni_.mese, null != periodoContabileDto ? periodoContabileDto.getMesi() : null)
                .addEqualCondition(WTBAccertamentiElencoPosizioni_.numQuindicina, null != periodoContabileDto ? periodoContabileDto.getNumQuindicina() : null);
    }

    // la posizione viene presa solo se esiste almeno un versamento dello stesso periodo che rispetta i filtri sui pagamenti.
    // La subquery NON restituisce dati sui versamenti: per ogni posizione risponde solo sì/no.
    private void applicaFiltroPagamenti(CriteriaQueryBuilder<WTBAccertamentiElencoPosizioni> builder, CriteriRicercaPosizioni criteri) {
        // senza filtri sui pagamenti la EXISTS non va aggiunta: con le sole condizioni di collegamento
        // escluderebbe le posizioni senza versamenti (proprio quelle non pagate)
        if (null != criteri.idTipoPagamento() || null != criteri.dataPagamentoDa() || null != criteri.dataPagamentoA()) {
            // addExistsCondition crea "EXISTS (SELECT 1 FROM VERS_MORE_INTERESSI_SANZIONI_V v WHERE ...)":
            // la lambda riceve il CriteriaBuilder (cb), il root della query esterna (posizione = p) e il root
            // della subquery (versamento = v) e restituisce le condizioni da mettere nella WHERE della subquery
            builder.addExistsCondition(WTBAccertamentiVersMoreInteressiSanzioniV.class,
                    (cb, posizione, versamento)
                            -> condizioniVersamento(cb, posizione, versamento, criteri.idTipoPagamento(),
                            criteri.dataPagamentoDa(), criteri.dataPagamentoA()));
        }
    }

    // condizioni della subquery: collegamento versamento-posizione + filtri sui pagamenti valorizzati
    private List<Predicate> condizioniVersamento(CriteriaBuilder cb, Root<WTBAccertamentiElencoPosizioni> posizione,
                                                 Root<WTBAccertamentiVersMoreInteressiSanzioniV> versamento,
                                                 Integer idTipoPagamento, LocalDate dataPagamentoDa, LocalDate dataPagamentoA) {
        // Ogni Predicate è una condizione della subquery: collegamento versamento-posizione o filtro pagamento.
        // La lista viene applicata alla WHERE dell'EXISTS e le condizioni sono combinate in AND.
        // Tutte le condizioni valgono per lo STESSO versamento: "esiste un versamento F24 pagato dopo il 01/05",
        // non "esiste un F24 ed esiste (magari un altro) pagamento dopo il 01/05".
        List<Predicate> condizioni = new ArrayList<>();
        // collegamento (sempre presente): tra le due entity non c'è una relazione JPA, quindi si confrontano a mano
        // le 5 colonne che identificano il periodo della posizione. È questo che rende la subquery "correlata":
        // viene valutata per ogni riga p della query esterna.
        condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.cfSogObbligato), posizione.get(WTBAccertamentiElencoPosizioni_.cfSogObbligato)));
        condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.categoria), posizione.get(WTBAccertamentiElencoPosizioni_.categoria)));
        condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.anno), posizione.get(WTBAccertamentiElencoPosizioni_.anno)));
        condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.mese), posizione.get(WTBAccertamentiElencoPosizioni_.mese)));
        condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.numQuindicina), posizione.get(WTBAccertamentiElencoPosizioni_.numQuindicina)));

        // filtri sui pagamenti: solo quelli valorizzati. Qui servono gli if perché i Predicate sono costruiti
        // a mano con cb e non passano dal salto automatico dei null del builder.
        // Nota: con un filtro sulle date i versamenti con DATA_PAGAMENTO null non contano (NULL >= x non è vero).
        if (null != dataPagamentoDa) {
            condizioni.add(cb.greaterThanOrEqualTo(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.dataPagamento), dataPagamentoDa));
        }
        if (null != dataPagamentoA) {
            condizioni.add(cb.lessThanOrEqualTo(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.dataPagamento), dataPagamentoA));
        }
        if (null != idTipoPagamento) {
            condizioni.add(cb.equal(versamento.get(WTBAccertamentiVersMoreInteressiSanzioniV_.tipoPagamento), idTipoPagamento));
        }
        return condizioni;
    }

    // ------------ DATI AGGIUNTIVI DI UNA POSIZIONE (una query per ogni dato, per ogni riga della pagina) ------------
    //
    // FLUSSO: dopo findElencoPosizioni il controller, per ogni posizione della pagina, chiama questi tre metodi
    // e mette il risultato nel DTO con il setter corrispondente. Codice semplice, ma 3 query per riga
    // (con 10 righe per pagina sono 30 query in più).

    // Ragione sociale del soggetto: è il collegamento del menu filtri fatto al contrario
    // (da CF_SOG_OBBLIGATO della posizione a DENOMINAZIONE del soggetto).
    //   SELECT * FROM W_TB_ACCERTAMENTI_SOGGETTI_V WHERE CATEGORIA = :sigla AND CF_PIVA = :cf
    // null se il soggetto non c'è; se la vista avesse più righe vince la prima (niente getSingleResult,
    // che con più righe lancerebbe eccezione e farebbe fallire tutta la ricerca)
    @Override
    public String findRagioneSociale(String categoriaProdotto, String cfSogObbligato) {
        List<WTBAccertamentiSoggettiV> soggetti = queryManager.createBuilder(WTBAccertamentiSoggettiV.class)
                .addEqualCondition(WTBAccertamentiSoggettiV_.categoria, categoriaProdotto)
                .addEqualCondition(WTBAccertamentiSoggettiV_.cfPiva, cfSogObbligato)
                .getResultList();
        return soggetti.isEmpty() ? null : soggetti.get(0).getDenominazione();
    }

    // Somma degli interessi legali ancora da pagare della posizione:
    //   SELECT * FROM W_TB_ACCERTAMENTI_INTERESSI_LEGALI_PER_ANNO
    //   WHERE ID_POSIZIONE = :idPosizione
    //     AND (STATO_PAGAMENTO_INTERESSE_LEGALE IS NULL OR STATO_PAGAMENTO_INTERESSE_LEGALE <> :pagato)
    // la somma (equivalente a SUM(INTERESSE_LEGALE)) si fa in Java; 0 se non ce ne sono
    @Override
    public BigDecimal findInteressiLegaliNonPagati(Long idPosizione) {
        List<WTBAccertamentiInteressiLegaliPerAnno> interessiNonPagati =
                queryManager.createBuilder(WTBAccertamentiInteressiLegaliPerAnno.class)
                        .addEqualCondition(WTBAccertamentiInteressiLegaliPerAnno_.idPosizione, idPosizione)
                        // non pagato = stato vuoto oppure diverso da "pagato"
                        .orConditions(b -> b
                                .addIsNullCondition(WTBAccertamentiInteressiLegaliPerAnno_.statoPagamentoInteresseLegale)
                                .addNotEqualCondition(WTBAccertamentiInteressiLegaliPerAnno_.statoPagamentoInteresseLegale, Constants.INTERESSE_LEGALE_PAGATO))
                        .getResultList();
        // somma degli importi (quelli null non contano), partendo da zero
        return interessiNonPagati.stream()
                .map(WTBAccertamentiInteressiLegaliPerAnno::getInteresseLegale)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Data dell'ultimo versamento del periodo della posizione (senza i filtri sui pagamenti della ricerca).
    // Il massimo lo calcola direttamente il DB:
    //   SELECT MAX(DATA_PAGAMENTO) FROM W_TB_ACCERTAMENTI_VERS_MORE_INTERESSI_SANZIONI_V
    //   WHERE CATEGORIA = :sigla AND CF_SOG_OBBLIGATO = :cf
    //     AND ANNO = :anno AND MESE = :mese AND NUM_QUINDICINA = :numQuindicina
    // null se nel periodo non ci sono versamenti (MAX ignora le date null)
    @Override
    public LocalDate findDataUltimoVersamento(String categoriaProdotto, String cfSogObbligato,
                                              Integer anno, Integer mese, Integer numQuindicina) {
        // selectMax va chiamato PRIMA delle condizioni (come selectCount), così la WHERE resta sulla query del MAX
        return queryManager.createBuilder(WTBAccertamentiVersMoreInteressiSanzioniV.class)
                .selectMax(WTBAccertamentiVersMoreInteressiSanzioniV_.dataPagamento)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.categoria, categoriaProdotto)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.cfSogObbligato, cfSogObbligato)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.anno, anno)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.mese, mese)
                .addEqualCondition(WTBAccertamentiVersMoreInteressiSanzioniV_.numQuindicina, numQuindicina)
                .getSingleResult();
    }
}
