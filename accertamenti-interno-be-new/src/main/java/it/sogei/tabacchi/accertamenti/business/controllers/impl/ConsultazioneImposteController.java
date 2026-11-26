package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IConsultazioneImposteController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.esportazione.EsportazionePosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.esportazione.EsportazioneVersamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteressiLegaliPerAnno;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiVersMoreInteressiSanzioniV;
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.business.validation.ConsultazioneImposteValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteController implements IConsultazioneImposteController {

    @Inject
    private IConsultazioneImposteDao consultazioneImposteDao;

    // ogni fase di validazione lancia lei la BusinessException se trova errori: qui non servono liste
    @Inject
    private ConsultazioneImposteValidator validator;

    @Inject
    private CustomMapper customMapper;

    @Override
    public List<CategoriaProdottoDto> getCategorieProdotti() {
        return customMapper.toCategorieProdottoDto(consultazioneImposteDao.findCategorieProdotti());
    }

    @Override
    public String getDescrizioneCategoria(String categoriaProdotto) {
        validator.validaCategoriaProdotto(categoriaProdotto);
        return Utility.getOrDefault(consultazioneImposteDao.findDescrizioneCategoria(categoriaProdotto),
                Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE);
    }

    @Override
    public ListeFiltriRicercaDto getFiltriRicerca(String categoriaProdotto, String annoContabile) {
        validator.validaCategoriaProdotto(categoriaProdotto);
        String sigla = risolviSiglaCategoria(categoriaProdotto);
        // anno facoltativo: se presente la lista contiene solo i soggetti attivi in quell'anno
        Integer anno = Utility.parseInteger(annoContabile, "annoContabile");
        ListeFiltriRicercaDto listeFiltriRicercaDto = new ListeFiltriRicercaDto();
        listeFiltriRicercaDto.setListaRagioneSocialePIva(customMapper.toRagioneSocialePIvaDto(
                consultazioneImposteDao.findAllRagioneSocialeEPivaByCategoriaProdotto(sigla, anno)));
        listeFiltriRicercaDto.setTipiPagamenti(customMapper.toTipiPagamentoDto(
                consultazioneImposteDao.findAllTipiPagamento()));
        return listeFiltriRicercaDto;
    }

    @Override
    public RisultatoPaginatoDto<PosizioneDto> findAllElencoPosizioni(String categoriaProdotto, ParametriFiltriRicercaDto parametriFiltriRicercaDto,
                                                                     Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // categoria + filtri, senza DB: presenza e almeno un criterio
        validator.validaFiltriRicerca(categoriaProdotto, parametriFiltriRicercaDto);
        String sigla = risolviSiglaCategoria(categoriaProdotto);
        // stringhe del FE -> valori tipizzati, una volta sola (gli errori di formato vengono lanciati qui)
        CriteriRicercaPosizioni criteri = toCriteriRicerca(sigla, parametriFiltriRicercaDto);
        validator.validaCriteriRicerca(criteri);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiElencoPosizioni> posizioni = consultazioneImposteDao.findElencoPosizioni(criteri, pagina, elementiPerPagina);
        Long totaleElementi = consultazioneImposteDao.countElencoPosizioni(criteri);
        // ogni posizione -> DTO completo (vedi popolaPosizione)
        List<PosizioneDto> posizioniDto = posizioni.stream().map(this::popolaPosizione).toList();
        return RisultatoPaginatoDto.of(posizioniDto, totaleElementi, pagina, elementiPerPagina);
    }

    // ------------ ESPORTAZIONE EXCEL ------------
    // 5 query fisse (vedi DAO); qui i risultati vengono agganciati alle posizioni con delle mappe

    @Override
    public List<EsportazionePosizioneDto> esportaElencoPosizioni(String categoriaProdotto, ParametriFiltriRicercaDto parametriFiltriRicercaDto) {
        // stessa validazione e stessi filtri della ricerca: l'Excel contiene le posizioni che l'utente vede
        validator.validaFiltriRicerca(categoriaProdotto, parametriFiltriRicercaDto);
        String sigla = risolviSiglaCategoria(categoriaProdotto);
        CriteriRicercaPosizioni criteri = toCriteriRicerca(sigla, parametriFiltriRicercaDto);
        validator.validaCriteriRicerca(criteri);

        List<WTBAccertamentiElencoPosizioni> posizioni = consultazioneImposteDao.findPosizioniDaEsportare(criteri);
        if (posizioni.isEmpty()) {
            return List.of();
        }
        List<Long> listaIdPosizioni = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getId).toList();
        List<String> listaCfSoggetti = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getCfSogObbligato).distinct().toList();

        // soggetto per CF: se la vista avesse due righe per lo stesso CF vince la prima
        Map<String, WTBAccertamentiSoggettiV> soggettiPerCf = consultazioneImposteDao.findSoggettiPerEsportazione(sigla, listaCfSoggetti)
                .stream()
                .collect(Collectors.toMap(WTBAccertamentiSoggettiV::getCfPiva, soggetto -> soggetto, (primo, secondo) -> primo));
        // interessi legali e imposte versate hanno già l'ID della posizione
        Map<Long, List<WTBAccertamentiInteressiLegaliPerAnno>> interessiLegaliPerPosizione =
                consultazioneImposteDao.findInteressiLegaliPerEsportazione(listaIdPosizioni)
                        .stream()
                        .collect(Collectors.groupingBy(WTBAccertamentiInteressiLegaliPerAnno::getIdPosizione));
        Map<Long, List<WTBAccertamentiDettaglioImposte>> imposteVersatePerPosizione =
                consultazioneImposteDao.findImposteVersatePerEsportazione(listaIdPosizioni)
                        .stream()
                        .collect(Collectors.groupingBy(WTBAccertamentiDettaglioImposte::getIdPosizione));
        // la vista dei versamenti non ha l'ID: si aggancia per cf + anno + mese + quindicina
        Map<String, List<WTBAccertamentiVersMoreInteressiSanzioniV>> versamentiAccessoriPerPeriodo =
                consultazioneImposteDao.findVersamentiAccessoriPerEsportazione(criteri)
                        .stream()
                        .collect(Collectors.groupingBy(versamento -> chiavePeriodo(versamento.getCfSogObbligato(),
                                versamento.getAnno(), versamento.getMese(), versamento.getNumQuindicina())));

        // l'ordine delle posizioni resta quello della query (anno, mese, quindicina, id)
        return posizioni.stream()
                .map(posizione -> componiPosizioneDaEsportare(posizione, soggettiPerCf, interessiLegaliPerPosizione,
                        imposteVersatePerPosizione, versamentiAccessoriPerPeriodo))
                .toList();
    }

    // chiave che identifica il periodo di un soggetto, es. "06741351008|2026|3|2"
    private String chiavePeriodo(String cfSogObbligato, Integer anno, Integer mese, Integer numQuindicina) {
        return cfSogObbligato + "|" + anno + "|" + mese + "|" + numQuindicina;
    }

    private EsportazionePosizioneDto componiPosizioneDaEsportare(
            WTBAccertamentiElencoPosizioni posizione,
            Map<String, WTBAccertamentiSoggettiV> soggettiPerCf,
            Map<Long, List<WTBAccertamentiInteressiLegaliPerAnno>> interessiLegaliPerPosizione,
            Map<Long, List<WTBAccertamentiDettaglioImposte>> imposteVersatePerPosizione,
            Map<String, List<WTBAccertamentiVersMoreInteressiSanzioniV>> versamentiAccessoriPerPeriodo) {
        EsportazionePosizioneDto posizioneDto = new EsportazionePosizioneDto();
        posizioneDto.setIdPosizione(posizione.getId());
        posizioneDto.setPartitaIva(posizione.getCfSogObbligato());
        posizioneDto.setAnnoContabile(posizione.getAnno());
        posizioneDto.setPeriodoContabile(Utility.formattaPeriodoContabile(posizione.getTipoPeriodo(),
                posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina()));
        posizioneDto.setImpostaDovuta(posizione.getImpostaTotaleDovuto());
        posizioneDto.setImpostaRestante(posizione.getImpostaRestanteDaPagare());
        posizioneDto.setMoraDovuta(posizione.getMoraTotaleDovuto());
        posizioneDto.setMoraRestante(posizione.getMoraRestanteDaPagare());
        posizioneDto.setInteressiTardatoPagamentoDovuti(posizione.getInteresseTardatoPagamentoTotaleDovuto());
        posizioneDto.setInteressiTardatoPagamentoRestanti(posizione.getInteresseTardatoPagamentoRestanteDaPagare());
        posizioneDto.setSanzioneDovuta(posizione.getSanzioneTotaleDovuto());
        posizioneDto.setSanzioneRestante(posizione.getSanzioneRestanteDaPagare());

        // soggetto: se manca, ragione sociale di default e date vuote
        WTBAccertamentiSoggettiV soggetto = soggettiPerCf.get(posizione.getCfSogObbligato());
        if (null != soggetto) {
            posizioneDto.setRagioneSociale(soggetto.getDenominazione());
            posizioneDto.setDataAttivazione(soggetto.getDataInizioAutorizzazione());
            // la data di fine "segnaposto" (soggetto ancora attivo) diventa null
            posizioneDto.setDataCessazione(Utility.fineValiditaVisibile(soggetto.getDataFineAutorizzazione()));
        } else {
            posizioneDto.setRagioneSociale(Constants.RAGIONE_SOCIALE_NON_DISPONIBILE);
        }

        // interessi legali: dovuti = tutti gli anni, restanti = solo quelli non pagati (le due SUM del collega)
        List<WTBAccertamentiInteressiLegaliPerAnno> interessiLegali =
                interessiLegaliPerPosizione.getOrDefault(posizione.getId(), List.of());
        List<WTBAccertamentiInteressiLegaliPerAnno> interessiLegaliNonPagati = interessiLegali.stream()
                .filter(interesse -> !Constants.INTERESSE_LEGALE_PAGATO.equals(interesse.getStatoPagamentoInteresseLegale()))
                .toList();
        posizioneDto.setInteressiLegaliDovuti(sommaInteressiLegali(interessiLegali));
        posizioneDto.setInteressiLegaliRestanti(sommaInteressiLegali(interessiLegaliNonPagati));

        posizioneDto.setVersamenti(componiVersamenti(posizione, imposteVersatePerPosizione, versamentiAccessoriPerPeriodo));
        return posizioneDto;
    }

    // imposte versate + mora/interessi/sanzioni dello stesso periodo, dal pagamento più vecchio
    private List<EsportazioneVersamentoDto> componiVersamenti(
            WTBAccertamentiElencoPosizioni posizione,
            Map<Long, List<WTBAccertamentiDettaglioImposte>> imposteVersatePerPosizione,
            Map<String, List<WTBAccertamentiVersMoreInteressiSanzioniV>> versamentiAccessoriPerPeriodo) {
        List<EsportazioneVersamentoDto> versamenti = new ArrayList<>();

        for (WTBAccertamentiDettaglioImposte imposta : imposteVersatePerPosizione.getOrDefault(posizione.getId(), List.of())) {
            EsportazioneVersamentoDto versamento = new EsportazioneVersamentoDto();
            versamento.setTipo(Constants.Dettaglio.TIPO_VERSAMENTO_IMPOSTA);
            versamento.setImporto(imposta.getImpostaImportoParziale());
            versamento.setDataPagamento(imposta.getDataPagamento());
            versamento.setGiorniRitardo(imposta.getGiorniRitardo());
            versamenti.add(versamento);
        }

        String chiave = chiavePeriodo(posizione.getCfSogObbligato(), posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina());
        for (WTBAccertamentiVersMoreInteressiSanzioniV accessorio : versamentiAccessoriPerPeriodo.getOrDefault(chiave, List.of())) {
            EsportazioneVersamentoDto versamento = new EsportazioneVersamentoDto();
            versamento.setTipo(accessorio.getTipo());
            versamento.setImporto(accessorio.getImporto());
            versamento.setDataPagamento(accessorio.getDataPagamento());
            versamenti.add(versamento);
        }

        // stesso ordine del collega (ORDER BY vers.DATA_PAGAMENTO); eventuali date null in fondo
        versamenti.sort(Comparator.comparing(EsportazioneVersamentoDto::getDataPagamento,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return versamenti;
    }

    // somma degli importi (quelli null non contano), partendo da zero
    private BigDecimal sommaInteressiLegali(List<WTBAccertamentiInteressiLegaliPerAnno> interessiLegali) {
        return interessiLegali.stream()
                .map(WTBAccertamentiInteressiLegaliPerAnno::getInteresseLegale)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // converte i filtri ricevuti come stringhe nei tipi usati dalla query: stringhe vuote -> null (filtro non applicato),
    // date in LocalDate, periodo contabile in mesi/quindicina, descrizione del tipo pagamento nel suo ID
    private CriteriRicercaPosizioni toCriteriRicerca(String sigla, ParametriFiltriRicercaDto parametriFiltriRicercaDto) {
        return new CriteriRicercaPosizioni(
                sigla,
                Utility.parseInteger(parametriFiltriRicercaDto.getAnnoContabile(), "annoContabile"),
                // il frontend invia la P.IVA nel campo "ragioneSociale"
                Utility.nullIfBlank(parametriFiltriRicercaDto.getRagioneSociale()),
                Utility.parsePeriodoContabile(parametriFiltriRicercaDto.getPeriodoContabile()),
                Utility.parseData(parametriFiltriRicercaDto.getInizioPeriodo(), "inizioPeriodo"),
                Utility.parseData(parametriFiltriRicercaDto.getFinePeriodo(), "finePeriodo"),
                Utility.parseData(parametriFiltriRicercaDto.getDataPagamentoDa(), "dataPagamentoDa"),
                Utility.parseData(parametriFiltriRicercaDto.getDataPagamentoA(), "dataPagamentoA"),
                risolviIdTipoPagamento(parametriFiltriRicercaDto.getTipoPagamento()));
    }

    // la categoria (già validata come obbligatoria) deve esistere in W_TB_ACCERTAMENTI_CATEGORIE_PROD:
    // restituisce la SIGLA presa da lì, altrimenti lancia BusinessException
    private String risolviSiglaCategoria(String categoriaProdotto) {
        WTBAccertamentiCategorieProd categoria =
                consultazioneImposteDao.findCategoriaBySigla(categoriaProdotto.trim().toUpperCase());
        if (null == categoria) {
            throw new BusinessException(String.format(Constants.ErrorMessages.CATEGORIA_PRODOTTO_NON_VALIDA, categoriaProdotto));
        }
        return categoria.getSigla();
    }

    // il form invia la DESCRIZIONE del tipo pagamento: restituisce l'ID usato dai versamenti (null se non filtrato),
    // altrimenti lancia BusinessException se la descrizione non esiste
    private Integer risolviIdTipoPagamento(String descrizione) {
        String valore = Utility.nullIfBlank(descrizione);
        if (null == valore) {
            return null;
        }
        WTBAccertamentiTipoPagamento tipoPagamento = consultazioneImposteDao.findTipoPagamentoByDescrizione(valore);
        if (null == tipoPagamento) {
            throw new BusinessException(String.format(Constants.ErrorMessages.TIPO_PAGAMENTO_NON_VALIDO, descrizione));
        }
        return tipoPagamento.getId().intValue();
    }

    // mappa una posizione nel DTO e aggiunge, con i setter, i dati che stanno su altre tabelle.
    // Ogni setter fa una query: 3 query per ogni riga della pagina.
    private PosizioneDto popolaPosizione(WTBAccertamentiElencoPosizioni posizione) {
        // campi presi direttamente dalla posizione (importi, stato, periodo...)
        PosizioneDto posizioneDto = customMapper.toPosizioneDto(posizione);
        // ragione sociale da W_TB_ACCERTAMENTI_SOGGETTI_V (testo di default se il soggetto non c'è)
        posizioneDto.setRagioneSociale(Utility.getOrDefault(
                consultazioneImposteDao.findRagioneSociale(posizione.getCategoria(), posizione.getCfSogObbligato()),
                Constants.RAGIONE_SOCIALE_NON_DISPONIBILE));
        // data dell'ultimo versamento dello stesso periodo (cf + anno + mese + quindicina)
        posizioneDto.setDataUltimoVersamento(consultazioneImposteDao.findDataUltimoVersamento(posizione.getCategoria(),
                posizione.getCfSogObbligato(), posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina()));
        // somma degli interessi legali non ancora pagati della posizione
        posizioneDto.setInteressiLegali(consultazioneImposteDao.findInteressiLegaliNonPagati(posizione.getId()));
        return posizioneDto;
    }
}
