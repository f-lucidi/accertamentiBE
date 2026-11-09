package it.sogei.tabacchi.accertamenti.business.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class CriteriaQueryManager {

    @Inject
    private EntityManager entityManager;

    public EntityManager entityManager() {
        return this.entityManager;
    }

    public <T> CriteriaQueryBuilder<T> createBuilder(Class<T> entityClass) {
        CriteriaQueryBuilder<T> criteriaQueryBuilder = new CriteriaQueryBuilder<>(entityManager, entityClass);

        return criteriaQueryBuilder.init();
    }

    public <T> List<T> findAll(Class<T> entityClass) {
        return this.createBuilder(entityClass)
                .getResultList();
    }

    public <T> T findById(Class<T> entityClass, Object id) {
        return this.entityManager.find(entityClass, id);
    }

    public <T> T retrieveById(Class<T> entityClass, Object id) {
        return this.entityManager.getReference(entityClass, id);
    }

    public void saveImmediately(Object entity, boolean isNew) {
        if(isNew) {
            this.entityManager.persist(entity);
        } else {
            this.entityManager.merge(entity);
        }
        this.entityManager.flush();
    }

    public void deleteImmediately(Object entity) {
        this.entityManager.remove(entity);
        this.entityManager.flush();
    }

}
