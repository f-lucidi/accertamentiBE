package it.sogei.tabacchi.accertamenti.configurations;

import it.sogei.tabacchi.accertamenti.configurations.qualifiers.ExceptionMessages;
import it.sogei.tabacchi.accertamenti.configurations.qualifiers.ValidationMessages;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class CDIResources {

    // Il container inietta un proxy thread-safe; l’EntityManager reale è associato alla transazione JTA in corso e viene chiuso alla sua fine.
    @PersistenceContext(unitName = "primary")
    private EntityManager entityManager;         // campo vuoto: non fai mai "new" né assegnazioni

    @Produces
    @ApplicationScoped
    // "CDI, rendi disponibile quell'oggetto a chi fa @Inject"
    public EntityManager getEntityManager() {    // "CDI, rendi disponibile quell'oggetto a chi fa @Inject"
        return entityManager;
    }
    @Produces
    @ValidationMessages
    public ResourceBundle produceValidationMessages() {
        return ResourceBundle.getBundle("ValidationMessages", Locale.ITALY);
    }

    @Produces
    @ExceptionMessages
    public ResourceBundle produceExceptionMessages() {
        return ResourceBundle.getBundle("ExceptionMessages", Locale.ITALY);
    }

}
