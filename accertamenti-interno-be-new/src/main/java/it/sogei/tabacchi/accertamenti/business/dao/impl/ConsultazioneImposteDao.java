package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiSoggettiV_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Locale;

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

    @Override
    public List<WTbAccertamentiSoggettiV> findAllRagioneSocialeEPivaByCategoriaProdotto(String categoriaProdotto) {
        return queryManager.createBuilder(WTbAccertamentiSoggettiV.class)
                .addEqualCondition(WTbAccertamentiSoggettiV_.categoria, categoriaProdotto.toUpperCase())
                .orderByAsc(WTbAccertamentiSoggettiV_.denominazione)
                .getResultList();
    }

    @Override
    public List<WTbAccertamentiTipoPagamento> findAllTipiPagamento() {
        return queryManager.findAll(WTbAccertamentiTipoPagamento.class);
    }

}
