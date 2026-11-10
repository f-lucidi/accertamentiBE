package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IConsultazioneImposteController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteController implements IConsultazioneImposteController {

    @Inject
    private IConsultazioneImposteDao consultazioneImposteDao;

    @Inject
    private CustomMapper customMapper;

    @Override
    public List<CategoriaProdottoDto> getCategorieProdotti() {
        return customMapper.toCategorieProdottoDto(consultazioneImposteDao.findCategorieProdotti());
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
        } else {
            ListeFiltriRicercaDto filtri = new ListeFiltriRicercaDto();
            filtri.setListaRagioneSocialePIva(customMapper.toRagioneSocialePIvaDto(
                    consultazioneImposteDao.findAllRagioneSocialeEPivaByCategoriaProdotto(categoriaProdotto)));
            filtri.setTipiPagamenti(customMapper.toTipiPagamentoDto(
                    consultazioneImposteDao.findAllTipiPagamento()));
            return filtri;
        }
    }
}
