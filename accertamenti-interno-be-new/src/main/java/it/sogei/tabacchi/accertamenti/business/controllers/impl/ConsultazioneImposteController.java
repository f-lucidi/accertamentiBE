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
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.business.validation.ConsultazioneImposteValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

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
        WTBAccertamentiCategorieProd categoria = consultazioneImposteDao.findCategoriaBySigla(categoriaProdotto);
        return null != categoria ? categoria.getDescrizione() : Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE;
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
