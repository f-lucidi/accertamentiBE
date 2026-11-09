package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.ICategoriaProdottoDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd_;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class CategoriaProdottoDao implements ICategoriaProdottoDao {

    @Inject
    private CriteriaQueryManager queryManager;

    @Override
    public WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria) {
        return queryManager.findById(WTBAccertamentiCategorieProd.class, siglaCategoria);
    }

    @Override
    public List<WTBAccertamentiCategorieProd> findAllCategorieProdotto(String sigla, String descrizione) {
        return queryManager.createBuilder(WTBAccertamentiCategorieProd.class)
                .orderByAsc(WTBAccertamentiCategorieProd_.sigla)
                .getResultList();
    }
}



