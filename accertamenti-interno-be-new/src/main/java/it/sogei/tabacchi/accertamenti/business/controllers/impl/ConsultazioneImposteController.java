package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IConsultazioneImposteController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.AnniContabiliDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.PropertyUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteController implements IConsultazioneImposteController {

    @Inject
    private IConsultazioneImposteDao consultazioneImposteDao;

    @Override
    public List<CategoriaProdottoDto> getCategorieProdotti() {
        return consultazioneImposteDao.findCategorieProdotti().stream()
                .map(this::toCategoriaProdottoDto)
                .toList();
    }

    @Override
    public String getDescrizioneCategoria(String siglaCategoria) {
        if (siglaCategoria == null || siglaCategoria.isBlank()) {
            throw new BusinessException("La sigla della categoria non può essere nulla o vuota.");
        }

        WTBAccertamentiCategorieProd categoria = consultazioneImposteDao.findCategoriaBySigla(siglaCategoria);
        return categoria != null ? categoria.getDescrizione() : Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE;
    }

    @Override
    public ListeFiltriRicercaDto getFiltriRicerca(String categoriaProdotto) {
        if (categoriaProdotto == null || categoriaProdotto.isBlank()) {
            throw new BusinessException("La categoria prodotto è obbligatoria.");
        }

        ListeFiltriRicercaDto filtri = new ListeFiltriRicercaDto();
        filtri.setListaAnniContabili(consultazioneImposteDao.findAnniContabili(categoriaProdotto).stream()
                .map(this::toAnnoContabileDto)
                .toList());
        filtri.setListaRagioneSocialePIva(List.of());
        filtri.setTipiPagamenti(PropertyUtils.getAsStringList(Constants.Properties.LIST_TIPO_PAGAMENTO).stream()
                .filter(tipo -> !tipo.isBlank())
                .map(this::toTipoPagamentoDto)
                .toList());
        return filtri;
    }

    private CategoriaProdottoDto toCategoriaProdottoDto(WTBAccertamentiCategorieProd categoria) {
        CategoriaProdottoDto dto = new CategoriaProdottoDto();
        dto.setSigla(categoria.getSigla());
        dto.setDescrizione(categoria.getDescrizione());
        return dto;
    }

    private AnniContabiliDto toAnnoContabileDto(Integer anno) {
        AnniContabiliDto dto = new AnniContabiliDto();
        dto.setAnnoContabile(anno);
        return dto;
    }

    private TipoPagamentoDto toTipoPagamentoDto(String tipoPagamento) {
        TipoPagamentoDto dto = new TipoPagamentoDto();
        dto.setDescrizione(tipoPagamento);
        return dto;
    }
}
