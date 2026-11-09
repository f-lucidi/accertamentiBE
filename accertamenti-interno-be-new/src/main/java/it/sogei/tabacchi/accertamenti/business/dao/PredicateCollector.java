package it.sogei.tabacchi.accertamenti.business.dao;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class PredicateCollector<T> extends CriteriaQueryBuilder<T> {

    private final List<Predicate> collectedPredicates;

    PredicateCollector(CriteriaQueryBuilder<T> source) {
        super(source);
        this.collectedPredicates = new ArrayList<>();
    }

    @Override
    protected CriteriaQueryBuilder<T> applyPredicate(Predicate predicate) {
        collectedPredicates.add(predicate);
        return this;
    }

    Predicate buildOr() {
        return criteriaBuilder.or(collectedPredicates.toArray(Predicate[]::new));
    }

    Predicate buildAnd() {
        return criteriaBuilder.and(collectedPredicates.toArray(Predicate[]::new));
    }
}
