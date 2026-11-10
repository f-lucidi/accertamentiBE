package it.sogei.tabacchi.accertamenti.business.dao.impl;

import it.sogei.tabacchi.accertamenti.business.dao.CriteriaQueryManager;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd_;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiDettaglioImposte_;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

@ApplicationScoped
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteDao implements IConsultazioneImposteDao {

    @Inject
    private CriteriaQueryManager queryManager;

    @Inject
    private EntityManager entityManager;

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
    public List<Integer> findAnniContabili(String categoriaProdotto) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> criteriaQuery = criteriaBuilder.createQuery(Integer.class);
        Root<WTBAccertamentiDettaglioImposte> root = criteriaQuery.from(WTBAccertamentiDettaglioImposte.class);

        criteriaQuery.select(root.get(WTBAccertamentiDettaglioImposte_.anno))
                .distinct(true)
                .where(criteriaBuilder.equal(root.get(WTBAccertamentiDettaglioImposte_.categoria), categoriaProdotto))
                .orderBy(criteriaBuilder.asc(root.get(WTBAccertamentiDettaglioImposte_.anno)));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
}
