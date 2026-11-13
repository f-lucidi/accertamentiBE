package it.sogei.tabacchi.accertamenti.business.dao;

import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.apache.commons.lang3.function.TriFunction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;


// questa è la classe base per la costruzione di query dinamiche con Criteria API.
// Fornisce metodi per aggiungere condizioni, ordinamenti e selezioni personalizzate alle query.
public class CriteriaQueryBuilder<T> {

    protected final EntityManager entityManager;
    protected final Class<T> clazz;
    protected final CriteriaBuilder criteriaBuilder;

    protected CriteriaQuery<?> criteriaQuery;
    protected Root<T> root;

    protected boolean addConditionsWhenValueIsNull;

    // Costruttore principale che inizializza l'EntityManager, la classe dell'entità e il CriteriaBuilder.
    public CriteriaQueryBuilder(EntityManager entityManager, Class<T> clazz) {
        this.entityManager = entityManager;
        this.clazz = clazz;
        criteriaBuilder = this.entityManager.getCriteriaBuilder();
        this.addConditionsWhenValueIsNull = true;
    }

    // Costruttore che copia le proprietà da un'altra istanza di CriteriaQueryBuilder.
    CriteriaQueryBuilder(CriteriaQueryBuilder<T> source) {
        this.entityManager = source.entityManager;
        this.clazz = source.clazz;
        this.criteriaBuilder = source.criteriaBuilder;
        this.criteriaQuery = source.criteriaQuery;
        this.root = source.root;
        this.addConditionsWhenValueIsNull = source.addConditionsWhenValueIsNull;
    }

    // Inizializza la query con una classe di risultato specifica.
    public <R> CriteriaQueryBuilder<T> init(Class<R> resultClass) {
        criteriaQuery = criteriaBuilder.createQuery(resultClass);
        root = criteriaQuery.from(clazz);
        this.addConditionsWhenValueIsNull = true;
        return this;
    }

    // Inizializza la query con la classe dell'entità.
    public CriteriaQueryBuilder<T> init() {
        return this.init(clazz);
    }

    // Seleziona il conteggio degli elementi nella query.
    public CriteriaQueryBuilder<T> selectCount() {
        return this.selectCustom(Long.class, criteriaBuilder::count);
    }

    // Seleziona il massimo valore di un attributo specifico nella query.
    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> selectMax(SingularAttribute<? super T, Y> attribute) {
        return this.selectCustom(attribute, root -> criteriaBuilder.greatest(root.get(attribute)));
    }

    // Seleziona il minimo valore di un attributo specifico nella query.
    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> selectMin(SingularAttribute<? super T, Y> attribute) {
        return this.selectCustom(attribute, root -> criteriaBuilder.least(root.get(attribute)));
    }

    // Seleziona la somma dei valori di un attributo specifico nella query.
    public <X> CriteriaQueryBuilder<T> selectAttribute(SingularAttribute<? super T, X> attribute) {
        return this.selectCustom(attribute, root -> root.get(attribute));
    }

    // Seleziona un attributo personalizzato nella query utilizzando una funzione di espressione.
    private <X> CriteriaQueryBuilder<T> selectCustom(SingularAttribute<? super T, X> attribute, Function<Root<T>, Expression<X>> expressionFunction) {
        return this.selectCustom(attribute.getJavaType(), expressionFunction);
    }

    // Seleziona un tipo di risultato personalizzato nella query utilizzando una funzione di espressione.
    private <X> CriteriaQueryBuilder<T> selectCustom(Class<X> queryType, Function<Root<T>, Expression<X>> expressionFunction) {
        boolean isCalledFirst = Objects.isNull(criteriaQuery.getRestriction());
        Predicate restriction = this.getRestriction();

        CriteriaQuery<X> customQuery = criteriaBuilder.createQuery(queryType);
        Root<T> newRoot = customQuery.from(clazz);
        this.criteriaQuery = customQuery.select(expressionFunction.apply(isCalledFirst ? newRoot : this.root));
        this.criteriaQuery.where(restriction);

        this.root = newRoot;

        return this;
    }

    // Imposta la query per restituire risultati distinti.
    public CriteriaQueryBuilder<T> distinct() {
        criteriaQuery.distinct(true);
        return this;
    }

    // Restituisce la restrizione corrente della query, o una congiunzione vuota se non ci sono restrizioni.
    public Predicate getRestriction() {
        return Utility.getOrDefault(criteriaQuery.getRestriction(), criteriaBuilder.conjunction());
    }

    /**
     * Quando si passa true a questo metodo, le successive condizioni vengono aggiunte solamente se
     * il valore specifico della condizione è diverso da <b>null</b>.
     * <br>
     * Questo metodo <b>non ha effetto</b> con {@link CriteriaQueryBuilder#orConditions} e {@link CriteriaQueryBuilder#andConditions}
     * @param value
     * @return
     */
    public CriteriaQueryBuilder<T> addConditionsWhenValueIsNull(boolean value) {
        this.addConditionsWhenValueIsNull = value;
        return this;
    }

    protected CriteriaQueryBuilder<T> applyPredicate(Predicate predicate) {
        Predicate previousConditions = this.getRestriction();
        criteriaQuery.where(criteriaBuilder.and(previousConditions, predicate));
        return this;
    }

    /* [START] Gestione di condizioni * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    public CriteriaQueryBuilder<T> orConditions(Function<CriteriaQueryBuilder<T>, CriteriaQueryBuilder<T>> conditions) {
        return this.subConditions(conditions, true);
    }

    public CriteriaQueryBuilder<T> andConditions(Function<CriteriaQueryBuilder<T>, CriteriaQueryBuilder<T>> conditions) {
        return this.subConditions(conditions, false);
    }

    private CriteriaQueryBuilder<T> subConditions(Function<CriteriaQueryBuilder<T>, CriteriaQueryBuilder<T>> subCondition, boolean applyAsOr) {
        PredicateCollector<T> collector = new PredicateCollector<>(this);
        subCondition.apply(collector);
        return this.applyPredicate(applyAsOr ? collector.buildOr() : collector.buildAnd());
    }

    public <X> CriteriaQueryBuilder<T> addCondition(
            Path<X> attributePath,
            X value,
            TriFunction<CriteriaBuilder, Path<X>, X, Predicate> conditionFunction) {
        /* Controllo per verificare se aggiungere le condizioni quando il parametro "value" è null */
        if(addConditionsWhenValueIsNull || Objects.nonNull(value)) {
            Predicate actualCondition = conditionFunction.apply(criteriaBuilder, attributePath, value);
            this.applyPredicate(actualCondition);
        }
        return this;
    }

    public <X> CriteriaQueryBuilder<T> addCondition(
            Path<X> attributePath,
            BiFunction<CriteriaBuilder, Path<X>, Predicate> conditionFunction) {

        boolean previous = this.addConditionsWhenValueIsNull;

        return this.addConditionsWhenValueIsNull(true)
                .addCondition(attributePath, null, (cb, path, value) -> conditionFunction.apply(cb, path))
                .addConditionsWhenValueIsNull(previous);
    }

    public <X> CriteriaQueryBuilder<T> addCondition(
            SingularAttribute<? super T, X> attribute,
            X value,
            TriFunction<CriteriaBuilder, Path<X>, X, Predicate> conditionFunction) {
        return this.addCondition(root.get(attribute), value, conditionFunction);
    }

    public <X, Y> CriteriaQueryBuilder<T> addCondition(
            SingularAttribute<? super T, X> id,
            SingularAttribute<X, Y> attribute,
            Y value,
            TriFunction<CriteriaBuilder, Path<Y>, Y, Predicate> conditionFunction) {
        return this.addCondition(root.get(id).get(attribute), value, conditionFunction);
    }

    public <X> CriteriaQueryBuilder<T> addCondition(
            SingularAttribute<? super T, X> attribute,
            BiFunction<CriteriaBuilder, Path<X>, Predicate> conditionFunction) {
        return this.addCondition(root.get(attribute), conditionFunction);
    }

    public <X, Y> CriteriaQueryBuilder<T> addCondition(
            SingularAttribute<? super T, X> id,
            SingularAttribute<X, Y> attribute,
            BiFunction<CriteriaBuilder, Path<Y>, Predicate> conditionFunction) {
        return this.addCondition(root.get(id).get(attribute), conditionFunction);
    }

    public <X> CriteriaQueryBuilder<T> addEqualCondition(SingularAttribute<? super T, X> attribute, X value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::equal);
    }

    public <X, Y> CriteriaQueryBuilder<T> addEqualCondition(SingularAttribute<? super T, X> id, SingularAttribute<? super X, Y> attribute, Y value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::equal);
    }

    public CriteriaQueryBuilder<T> addLikeCondition(SingularAttribute<? super T, String> attribute, String value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::like);
    }

    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> addGreaterThanCondition(SingularAttribute<? super T, Y> attribute, Y value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::greaterThan);
    }

    public <Y, X extends Comparable<? super X>> CriteriaQueryBuilder<T> addGreaterThanCondition(SingularAttribute<? super T, Y> id, SingularAttribute<Y, X> attribute, X value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::greaterThan);
    }

    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> addLessThanCondition(SingularAttribute<? super T, Y> attribute, Y value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::lessThan);
    }

    public <Y, X extends Comparable<? super X>> CriteriaQueryBuilder<T> addLessThanCondition(SingularAttribute<? super T, Y> id, SingularAttribute<Y, X> attribute, X value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::lessThan);
    }

    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> addGreaterThanOrEqualToCondition(SingularAttribute<? super T, Y> attribute, Y value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::greaterThanOrEqualTo);
    }

    public <Y, X extends Comparable<? super X>> CriteriaQueryBuilder<T> addGreaterThanOrEqualToCondition(SingularAttribute<? super T, Y> id, SingularAttribute<Y, X> attribute, X value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::greaterThanOrEqualTo);
    }

    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> addLessThanOrEqualToCondition(SingularAttribute<? super T, Y> attribute, Y value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::lessThanOrEqualTo);
    }

    public <Y, X extends Comparable<? super X>> CriteriaQueryBuilder<T> addLessThanOrEqualToCondition(SingularAttribute<? super T, Y> id, SingularAttribute<Y, X> attribute, X value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::lessThanOrEqualTo);
    }

    public <Y extends Comparable<? super Y>> CriteriaQueryBuilder<T> addBetweenCondition(SingularAttribute<? super T, Y> attribute, Y lowerValue, Y upperValue) {
        return this.addCondition(root.get(attribute), (cb, path) -> cb.between(path, lowerValue, upperValue));
    }

    public <Y> CriteriaQueryBuilder<T> addIsNullCondition(SingularAttribute<? super T, Y> attribute) {
        return this.addCondition(root.get(attribute), CriteriaBuilder::isNull);
    }

    public <Y> CriteriaQueryBuilder<T> addIsNotNullCondition(SingularAttribute<? super T, Y> attribute) {
        return this.addCondition(root.get(attribute), CriteriaBuilder::isNotNull);
    }

    // come le altre condizioni: con addConditionsWhenValueIsNull(false) una lista null viene saltata
    public <Y> CriteriaQueryBuilder<T> addInCondition(SingularAttribute<? super T, Y> attribute, List<Y> values) {
        if (addConditionsWhenValueIsNull || Objects.nonNull(values)) {
            this.applyPredicate(root.get(attribute).in(values));
        }
        return this;
    }

    public <Y> CriteriaQueryBuilder<T> addNotEqualCondition(SingularAttribute<? super T, Y> attribute, Y value) {
        return this.addCondition(root.get(attribute), value, CriteriaBuilder::notEqual);
    }

    public <X, Y> CriteriaQueryBuilder<T> addNotEqualCondition(SingularAttribute<? super T, X> id, SingularAttribute<? super X, Y> attribute, Y value) {
        return this.addCondition(root.get(id).get(attribute), value, CriteriaBuilder::notEqual);
    }

    /**
     * Questo metodo aggiunge una condizione tra una colonna (DATE) ed un valore, sottoponendo la colonna alla funzione TO_CHAR per formattarla
     * ricavando da essa per esempio solo l'anno o il mese. <br>
     * Un esempio di utilizzo: <br>
     * <code>addCompareDateCondition(Model_.dataCol, 2025, "YYYY", CriteriaBuilder::equal)</code> <br>
     * Il codice riportato genererebbe una condizione simile a: <br>
     *<code>TO_CHAR(DATA_COL, 'YYYY') = 2025</code> <br>
     * @param localDateAttribute colonna della tabella da confrontare
     * @param value valore da confrontare
     * @param pattern formato della data (es. "YYYY", "MM", ecc.)
     * @param criteriaCondition funzione che ritorna la condizione da applicare (es. CriteriaBuilder::equal, CriteriaBuilder::greaterThan, ecc.)
     * @return CriteriaQueryBuilder<T> con la condizione applicata
     */
    public CriteriaQueryBuilder<T> addCompareDateCondition(SingularAttribute<? super T, LocalDate> localDateAttribute, Object value, String pattern, TriFunction<CriteriaBuilder, Expression<String>, String, Predicate> criteriaCondition) {
        /* La variabile "placeholder" serve solo a poter passare il valore al metodo addCondition
         * se il placeholder è null la condizione non viene aggiunta */
        LocalDate placeholder = Objects.isNull(value) ? null : LocalDate.now();
        return this.addCondition(localDateAttribute, placeholder, (cb, path, val) -> {
            Expression<String> dateFieldExtractor = cb.function("TO_CHAR", String.class, path, cb.literal(pattern));
            return criteriaCondition.apply(cb, dateFieldExtractor, String.valueOf(value));
        });
    }
    /**
     * Aggiunge una condizione EXISTS con una subquery su un'altra entity, collegata alla query principale. <br>
     * Un esempio di utilizzo: <br>
     * <code>addExistsCondition(Figlio.class, (cb, padre, figlio) -> List.of(cb.equal(figlio.get(Figlio_.idPadre), padre.get(Padre_.id))))</code> <br>
     * Il codice riportato genererebbe una condizione simile a: <br>
     * <code>EXISTS (SELECT 1 FROM FIGLIO f WHERE f.ID_PADRE = p.ID)</code> <br>
     * Va chiamato <b>dopo</b> gli eventuali select (es. {@link CriteriaQueryBuilder#selectCount()}), così la subquery viene creata sulla query corretta.
     * @param subqueryClass entity su cui fare la subquery
     * @param conditions funzione che riceve il CriteriaBuilder, il root principale e il root della subquery e ritorna le condizioni della subquery
     * @return CriteriaQueryBuilder<T> con la condizione applicata
     */
    public <S> CriteriaQueryBuilder<T> addExistsCondition(Class<S> subqueryClass, TriFunction<CriteriaBuilder, Root<T>, Root<S>, List<Predicate>> conditions) {
        Subquery<Integer> subquery = criteriaQuery.subquery(Integer.class);
        Root<S> subqueryRoot = subquery.from(subqueryClass);

        List<Predicate> predicates = conditions.apply(criteriaBuilder, root, subqueryRoot);
        subquery.select(criteriaBuilder.literal(1)).where(predicates.toArray(Predicate[]::new));

        return this.applyPredicate(criteriaBuilder.exists(subquery));
    }
    /* [END] Gestione di condizioni * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /* [START] Getione ordinamento * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
    public <Y> CriteriaQueryBuilder<T> orderBy(Path<Y> attributePath, BiFunction<CriteriaBuilder, Path<Y>, Order> orderFunction) {
        Order order = orderFunction.apply(criteriaBuilder, attributePath);

        List<Order> orders = Utility.getOrDefault(criteriaQuery.getOrderList(), new ArrayList<>());
        orders.add(order);

        criteriaQuery.orderBy(orders);

        return this;
    }

    public <Y> CriteriaQueryBuilder<T> orderByAsc(SingularAttribute<? super T, Y> attribute) {
        return this.orderBy(root.get(attribute), CriteriaBuilder::asc);
    }

    public <X, Y> CriteriaQueryBuilder<T> orderByAsc(SingularAttribute<? super T, X> id, SingularAttribute<? super X, Y> attribute) {
        return this.orderBy(root.get(id).get(attribute), CriteriaBuilder::asc);
    }

    public <Y> CriteriaQueryBuilder<T> orderByDesc(SingularAttribute<? super T, Y> attribute) {
        return this.orderBy(root.get(attribute), CriteriaBuilder::desc);
    }

    public <X, Y> CriteriaQueryBuilder<T> orderByDesc(SingularAttribute<? super T, X> id, SingularAttribute<? super X, Y> attribute) {
        return this.orderBy(root.get(id).get(attribute), CriteriaBuilder::desc);
    }

    /* [END] Getione ordinamento * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    @SuppressWarnings("unchecked")
    public <R> List<R> getResultList() {
        List<R> resultList = (List<R>) entityManager.createQuery(criteriaQuery).getResultList();
        this.init();
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public <R> List<R> getPaginatedResults(int page, int numElePerPag) {
        List<R> resultList = (List<R>) entityManager.createQuery(criteriaQuery)
                .setFirstResult((page - 1) * numElePerPag)
                .setMaxResults(numElePerPag)
                .getResultList();

        this.init();
        return resultList;
    }

    public <R> List<R> getPaginatedResults(int page) {
        return this.getPaginatedResults(page, Constants.DEFAULT_ELEMENTS_PER_PAGE);
    }

    @SuppressWarnings("unchecked")
    public <R> R getSingleResult() {
        try {
            R result = (R) entityManager.createQuery(criteriaQuery).getSingleResult();
            this.init();
            return result;
        } catch (NoResultException e) {
            return null;
        }
    }
}
