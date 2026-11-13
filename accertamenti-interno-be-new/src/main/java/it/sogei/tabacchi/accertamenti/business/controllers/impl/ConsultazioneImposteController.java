package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IConsultazioneImposteController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.business.model.records.ChiavePeriodoPosizione;
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.business.validation.CommonValidator;
import it.sogei.tabacchi.accertamenti.business.validation.ConsultazioneImposteValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteController implements IConsultazioneImposteController {

    @Inject
    private IConsultazioneImposteDao consultazioneImposteDao;

    @Inject
    private CommonValidator commonValidator;

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
        commonValidator.validaObbligatorio(categoriaProdotto, Constants.ErrorMessages.CATEGORIA_PRODOTTO_OBBLIGATORIA);
        WTBAccertamentiCategorieProd categoria = consultazioneImposteDao.findCategoriaBySigla(categoriaProdotto);
        return null != categoria ? categoria.getDescrizione() : Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE;
    }

    @Override
    public ListeFiltriRicercaDto getFiltriRicerca(String categoriaProdotto, String annoContabile) {
        commonValidator.validaObbligatorio(categoriaProdotto, Constants.ErrorMessages.CATEGORIA_PRODOTTO_OBBLIGATORIA);
        String sigla = risolviSiglaCategoria(categoriaProdotto);
        // anno facoltativo: se presente la lista contiene solo i soggetti attivi in quell'anno
        Integer anno = Utility.parseInteger(annoContabile, "annoContabile");
        ListeFiltriRicercaDto filtri = new ListeFiltriRicercaDto();
        filtri.setListaRagioneSocialePIva(customMapper.toRagioneSocialePIvaDto(
                consultazioneImposteDao.findAllRagioneSocialeEPivaByCategoriaProdotto(sigla, anno)));
        filtri.setTipiPagamenti(customMapper.toTipiPagamentoDto(
                consultazioneImposteDao.findAllTipiPagamento()));
        return filtri;
    }

    @Override
    public RisultatoPaginatoDto<PosizioneDto> findAllElencoPosizioni(String categoriaProdotto, ParametriFiltriRicercaDto filtri,
                                                                     Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // categoria + filtri, senza DB: presenza e almeno un criterio
        validator.validaFiltriRicerca(categoriaProdotto, filtri);
        String sigla = risolviSiglaCategoria(categoriaProdotto);
        // stringhe del FE -> valori tipizzati, una volta sola (gli errori di formato vengono lanciati qui)
        CriteriRicercaPosizioni criteri = toCriteriRicerca(sigla, filtri);
        validator.validaCriteriRicerca(criteri);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiElencoPosizioni> posizioni = consultazioneImposteDao.findElencoPosizioni(criteri, pagina, elementiPerPagina);
        Long totaleElementi = consultazioneImposteDao.countElencoPosizioni(criteri);
        return RisultatoPaginatoDto.of(popolaPosizioni(sigla, posizioni), totaleElementi, pagina, elementiPerPagina);
    }

    // converte i filtri ricevuti come stringhe nei tipi usati dalla query: stringhe vuote -> null (filtro non applicato),
    // date in LocalDate, periodo contabile in mesi/quindicina, descrizione del tipo pagamento nel suo ID
    private CriteriRicercaPosizioni toCriteriRicerca(String sigla, ParametriFiltriRicercaDto filtri) {
        return new CriteriRicercaPosizioni(
                sigla,
                Utility.parseInteger(filtri.getAnnoContabile(), "annoContabile"),
                // il frontend invia la P.IVA nel campo "ragioneSociale"
                Utility.nullIfBlank(filtri.getRagioneSociale()),
                Utility.parsePeriodoContabile(filtri.getPeriodoContabile()),
                Utility.parseData(filtri.getInizioPeriodo(), "inizioPeriodo"),
                Utility.parseData(filtri.getFinePeriodo(), "finePeriodo"),
                Utility.parseData(filtri.getDataPagamentoDa(), "dataPagamentoDa"),
                Utility.parseData(filtri.getDataPagamentoA(), "dataPagamentoA"),
                risolviIdTipoPagamento(filtri.getTipoPagamento()));
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

    // mappa le posizioni e aggiunge i dati che stanno su altre tabelle (ragione sociale, data ultimo versamento,
    // interessi legali) con 3 query per tutta la pagina, invece di 3 query per ogni riga
    private List<PosizioneDto> popolaPosizioni(String sigla, List<WTBAccertamentiElencoPosizioni> posizioni) {
        if (posizioni.isEmpty()) {
            return List.of();
        }
        Set<String> cfSogObbligati = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getCfSogObbligato).collect(Collectors.toSet());
        Set<Integer> anni = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getAnno).collect(Collectors.toSet());
        Set<Integer> mesi = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getMese).collect(Collectors.toSet());
        Set<Long> idPosizioni = posizioni.stream().map(WTBAccertamentiElencoPosizioni::getId).collect(Collectors.toSet());

        Map<String, String> ragioniSociali = consultazioneImposteDao.findRagioniSociali(sigla, cfSogObbligati);
        Map<ChiavePeriodoPosizione, LocalDate> dateUltimoVersamento =
                consultazioneImposteDao.findDateUltimoVersamento(sigla, cfSogObbligati, anni, mesi);
        Map<Long, BigDecimal> interessiLegali = consultazioneImposteDao.findInteressiLegaliNonPagati(idPosizioni);

        return posizioni.stream().map(posizione -> {
            PosizioneDto dto = customMapper.toPosizioneDto(posizione);
            dto.setRagioneSociale(ragioniSociali.getOrDefault(posizione.getCfSogObbligato(), Constants.RAGIONE_SOCIALE_NON_DISPONIBILE));
            dto.setDataUltimoVersamento(dateUltimoVersamento.get(new ChiavePeriodoPosizione(
                    posizione.getCfSogObbligato(), posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina())));
            dto.setInteressiLegali(interessiLegali.getOrDefault(posizione.getId(), BigDecimal.ZERO));
            return dto;
        }).toList();
    }
}
