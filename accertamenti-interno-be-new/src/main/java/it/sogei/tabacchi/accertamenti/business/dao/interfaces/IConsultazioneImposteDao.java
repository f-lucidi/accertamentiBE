package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiTipoPagamento;

import java.util.List;

public interface IConsultazioneImposteDao {

    List<WTBAccertamentiCategorieProd> findCategorieProdotti();

    WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria);

    List<WTbAccertamentiSoggettiV> findAllRagioneSocialeEPivaByCategoriaProdotto(String categoriaProdotto);

    List<WTbAccertamentiTipoPagamento> findAllTipiPagamento();
}
