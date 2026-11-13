package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.Dependent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Arrays;

// validazioni generiche riutilizzabili: i validator delle singole funzionalità (bean @Dependent) lo iniettano con @Inject.
// Tutti i metodi lanciano BusinessException se il dato non è valido
@Dependent
@Slf4j
@LogMethodInvocation(logParameters = false)
public class CommonValidator {

    // stringa null, vuota o di soli spazi
    public void validaObbligatorio(String valore, String messaggio) {
        if (null == valore || valore.isBlank()) {
            throw new BusinessException(messaggio);
        }
    }

    // valore null (numeri, date, oggetti)
    public void validaObbligatorio(Object valore, String messaggio) {
        if (null == valore) {
            throw new BusinessException(messaggio);
        }
    }

    // entrambe le date valorizzate e "da" successiva ad "a"
    public void validaIntervalloDate(LocalDate da, LocalDate a, String messaggio) {
        if (null != da && null != a && da.isAfter(a)) {
            throw new BusinessException(messaggio);
        }
    }

    // nessuno dei valori è valorizzato (null, vuoto o di soli spazi)
    public void validaAlmenoUnoValorizzato(String messaggio, String... valori) {
        boolean almenoUnoValorizzato = Arrays.stream(valori).anyMatch(valore -> null != valore && !valore.isBlank());
        if (!almenoUnoValorizzato) {
            throw new BusinessException(messaggio);
        }
    }
}
