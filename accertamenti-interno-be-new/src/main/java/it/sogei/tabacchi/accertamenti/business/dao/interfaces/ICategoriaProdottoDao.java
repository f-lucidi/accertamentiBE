package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;

import java.util.List;

public interface ICategoriaProdottoDao {
    WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria);
    List<WTBAccertamentiCategorieProd> findAllCategorieProdotto();
}