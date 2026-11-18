package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.Dependent;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// validazioni generiche riutilizzabili: i validator delle singole funzionalità (bean @Dependent) lo iniettano con @Inject.
// Nessun metodo lancia eccezioni: ogni regola violata aggiunge il suo messaggio alla lista errori creata dal controller,
// che se la lista restituita ha size() > 0 lancia una sola BusinessException con tutti i messaggi.
// I metodi che restituiscono boolean dicono se il dato è valido: serve a non controllare un dato che dipende da uno già sbagliato
@Dependent
@Slf4j
@LogMethodInvocation(logParameters = false)
public class CommonValidator {

    // stringa null, vuota o di soli spazi; true se valorizzata
    public boolean validaObbligatorio(String valore, String messaggio, List<String> errori) {
        boolean isValorizzato = null != valore && !valore.isBlank();
        if (!isValorizzato) {
            errori.add(messaggio);
        }
        return isValorizzato;
    }

    // valore null (numeri, date, oggetti); true se valorizzato
    public boolean validaObbligatorio(Object valore, String messaggio, List<String> errori) {
        boolean isValorizzato = null != valore;
        if (!isValorizzato) {
            errori.add(messaggio);
        }
        return isValorizzato;
    }

    // entrambe le date valorizzate e "da" successiva ad "a"
    public void validaIntervalloDate(LocalDate da, LocalDate a, String messaggio, List<String> errori) {
        if (null != da && null != a && da.isAfter(a)) {
            errori.add(messaggio);
        }
    }

    // filtri degli elenchi di gestione parametri (aliquote, interessi), già normalizzati dal controller (null = filtri vuoti)
    public List<String> validaFiltriGestioneParametri(FiltriGestioneParametriDto filtriGestioneParametriDto, List<String> errori) {
        validaIntervalloDate(filtriGestioneParametriDto.getDataInizioValiditaDa(), filtriGestioneParametriDto.getDataInizioValiditaA(),
                Constants.ErrorMessages.INTERVALLO_DECORRENZA_NON_VALIDO, errori);
        return errori;
    }

    // decorrenza di un parametro storicizzato (aliquote, interessi): obbligatoria e non nel passato (oggi è ammesso).
    // true se valida: solo allora si può usare (confronto col precedente, query)
    public boolean validaDataDecorrenza(LocalDate dataDecorrenza, List<String> errori) {
        boolean isPresente = validaObbligatorio(dataDecorrenza, Constants.ErrorMessages.DATA_DECORRENZA_OBBLIGATORIA, errori);
        boolean isNonPassata = !isPresente || !dataDecorrenza.isBefore(LocalDate.now());
        if (!isNonPassata) {
            errori.add(Constants.ErrorMessages.DATA_DECORRENZA_PASSATA);
        }
        return isPresente && isNonPassata;
    }

    // la nuova decorrenza (già validata, non null) deve venire dopo l'inizio del parametro precedente,
    // altrimenti il precedente finirebbe prima di iniziare.
    // dataInizioPrecedente null = nessun precedente, nessun vincolo. messaggio con %s = data del precedente (dd/MM/yyyy)
    public void validaDecorrenzaSuccessivaPrecedente(LocalDate dataDecorrenza, LocalDate dataInizioPrecedente, String messaggio,
                                                     List<String> errori) {
        if (null != dataInizioPrecedente && !dataDecorrenza.isAfter(dataInizioPrecedente)) {
            errori.add(String.format(messaggio, Utility.formattaData(dataInizioPrecedente)));
        }
    }

    // nessuno dei valori è valorizzato (null, vuoto o di soli spazi)
    public void validaAlmenoUnoValorizzato(String messaggio, List<String> errori, String... valori) {
        boolean almenoUnoValorizzato = Arrays.stream(valori).anyMatch(valore -> null != valore && !valore.isBlank());
        if (!almenoUnoValorizzato) {
            errori.add(messaggio);
        }
    }
}
