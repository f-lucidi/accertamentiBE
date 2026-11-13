package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.records.CriteriRicercaPosizioni;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Validazioni della funzionalità "Consultazione imposte".
 * Tutti i metodi lanciano BusinessException se il dato non è valido.
 *
 * <p><b>Perché è un bean {@code @Dependent}</b> (e non una classe creata con {@code new}):</p>
 * <ul>
 *   <li>è CDI a crearla, quindi {@code @Inject} funziona: le dipendenze (es. {@link CommonValidator})
 *       si dichiarano qui sotto e sono visibili, invece di recuperarle a mano con {@code CDI.current()};
 *       con {@code new}, invece, {@code @Inject} verrebbe ignorato e i campi resterebbero null;</li>
 *   <li>nei test si può creare con {@code @InjectMocks} passando un mock di CommonValidator,
 *       senza bisogno di un container CDI attivo;</li>
 *   <li>con {@code @LogMethodInvocation} l'interceptor registra anche le chiamate ai suoi metodi pubblici
 *       ([START]/[END] nei log); l'interceptor scatta solo sulle chiamate fatte da un'altra classe,
 *       non su quelle tra metodi della stessa classe;</li>
 *   <li>{@code @Dependent} significa che non ha un ciclo di vita proprio: ogni punto di iniezione riceve
 *       un'istanza nuova, che nasce e muore insieme al bean che la inietta (qui il controller,
 *       {@code @RequestScoped}, quindi una per richiesta). Va bene perché la classe non ha stato.</li>
 * </ul>
 * <p>Uso nel controller: {@code @Inject private ConsultazioneImposteValidator validator;}</p>
 */
@Dependent
@LogMethodInvocation(logParameters = false)
public class ConsultazioneImposteValidator {

    @Inject
    private CommonValidator commonValidator;

    // 1° passo, sui valori grezzi ricevuti (categoria dal path + filtri dal body), prima di ogni accesso al DB:
    // presenza della categoria e dei filtri, almeno un criterio di ricerca
    public void validaFiltriRicerca(String categoriaProdotto, ParametriFiltriRicercaDto filtri) {
        commonValidator.validaObbligatorio(categoriaProdotto, Constants.ErrorMessages.CATEGORIA_PRODOTTO_OBBLIGATORIA);
        commonValidator.validaObbligatorio(filtri, Constants.ErrorMessages.FILTRI_RICERCA_OBBLIGATORI);
        // almeno un criterio tra P.IVA, periodo contabile (anche "Tutte") e data pagamento da/a:
        // va controllato sulle stringhe, perché dopo la conversione "Tutte" diventa null
        commonValidator.validaAlmenoUnoValorizzato(
                Constants.ErrorMessages.ALMENO_UN_CRITERIO_DI_RICERCA,
                filtri.getRagioneSociale(), filtri.getPeriodoContabile(),
                filtri.getDataPagamentoDa(), filtri.getDataPagamentoA());
    }

    // 2° passo, sui filtri già convertiti (gli errori di formato li ha già lanciati la conversione):
    // anno obbligatorio e intervalli di date coerenti
    public void validaCriteriRicerca(CriteriRicercaPosizioni criteri) {
        commonValidator.validaObbligatorio(criteri.anno(), Constants.ErrorMessages.ANNO_CONTABILE_OBBLIGATORIO);
        commonValidator.validaIntervalloDate(criteri.dataPagamentoDa(), criteri.dataPagamentoA(),
                Constants.ErrorMessages.INTERVALLO_DATE_PAGAMENTO_NON_VALIDO);
        // inizio/fine periodo: intervallo sulla scadenza
        commonValidator.validaIntervalloDate(criteri.inizioPeriodo(), criteri.finePeriodo(),
                Constants.ErrorMessages.INTERVALLO_PERIODO_NON_VALIDO);
    }
}
