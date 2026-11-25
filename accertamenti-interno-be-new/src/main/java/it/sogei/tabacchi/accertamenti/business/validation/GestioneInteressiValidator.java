package it.sogei.tabacchi.accertamenti.business.validation;


import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// validazioni degli interessi sul tardato pagamento.
// Ogni metodo pubblico è una fase: void, raccoglie gli errori in una lista propria e se non è vuota lancia lui
// la BusinessException con i messaggi concatenati (commonValidator.checkAndThrowIfErrors); il controller li chiama in fila.
// I metodi privati sono regole riusate da più fasi: aggiungono alla lista ricevuta e non lanciano.
// Non legge il DB: i dati che servono (es. interesse precedente) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiValidator {

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco interessi (date di decorrenza da/a coerenti)
    public void validaFiltri(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // Body e tipo operazione li controlla Bean Validation prima del controller:
    // annotazioni su ModificaInteresseDto e @Valid @NotNull sul parametro di IGestioneInteressiService.scriviInteresse

    // 2° passo, ramo INSERIMENTO: niente id, niente cancellazione, decorrenza, tasso.
    // Che non esista già un interesse modificabile lo controlla prima il controller (errore 403, non sui dati).
    // Il confronto col precedente è a parte (validaTassoDiversoDaPrecedente): il precedente si cerca con la decorrenza,
    // che deve essere già valida per usarla in una query
    public void validaInserimentoInteresse(ModificaInteresseDto modificaInteresseDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaNuovoInteresse(modificaInteresseDto.getIdInteresse(), modificaInteresseDto.getFlagCancella(), errori);
        commonValidator.validaDataDecorrenza(modificaInteresseDto.getDataDecorrenza(), errori);
        commonValidator.validaTasso(modificaInteresseDto.getTassoInteresse(), errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo INSERIMENTO: il nuovo tasso (già validato, non null) non può ripetere quello precedente;
    // interessePrecedente letto dal controller, null = nessun vincolo
    public void validaTassoDiversoDaPrecedente(BigDecimal tassoInteresse, WTBAccertamentiInteresseRitPagam interessePrecedente) {
        List<String> errori = new ArrayList<>();
        aggiungiErroreTassoUgualePrecedente(tassoInteresse, interessePrecedente, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 2° passo, ramo MODIFICA, prima di leggere il DB: l'id dell'interesse da modificare.
    // Ogni decorrenza ha una sola riga: l'id basta a trovarla
    public void validaRichiestaModifica(ModificaInteresseDto modificaInteresseDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaObbligatorio(modificaInteresseDto.getIdInteresse(), Constants.ErrorMessages.ID_INTERESSE_OBBLIGATORIO, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo MODIFICA: l'interesse letto per id esiste, non è cancellato ed è ancora modificabile.
    // findById restituisce anche le righe cancellate: per il frontend una riga cancellata non esiste.
    // Stessa regola di countInteressiModificabili: decorrenza futura oppure inserito oggi
    public void validaInteresseModificabile(WTBAccertamentiInteresseRitPagam interesseSalvato) {
        List<String> errori = new ArrayList<>();
        LocalDate oggi = LocalDate.now();
        if (null == interesseSalvato || Boolean.TRUE.equals(interesseSalvato.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.INTERESSE_NON_TROVATO);
        } else if (!interesseSalvato.getDataInizioVal().isAfter(oggi) && !oggi.equals(interesseSalvato.getDtInserimento())) {
            errori.add(String.format(Constants.ErrorMessages.INTERESSE_NON_MODIFICABILE,
                    Utility.formattaData(interesseSalvato.getDataInizioVal())));
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 4° passo, ramo MODIFICA quando non è una cancellazione: nuova decorrenza (valida e successiva al precedente)
    // e nuovo tasso (valido e diverso dal precedente), tutti gli errori insieme.
    // interessePrecedente letto dal controller, null = nessun vincolo
    public void validaModificaInteresse(ModificaInteresseDto modificaInteresseDto, WTBAccertamentiInteresseRitPagam interessePrecedente) {
        List<String> errori = new ArrayList<>();
        LocalDate dataDecorrenza = modificaInteresseDto.getDataDecorrenza();
        // il confronto col precedente ha senso solo su una decorrenza valida
        if (commonValidator.validaDataDecorrenza(dataDecorrenza, errori)) {
            commonValidator.validaDecorrenzaSuccessivaPrecedente(dataDecorrenza,
                    null != interessePrecedente ? interessePrecedente.getDataInizioVal() : null,
                    Constants.ErrorMessages.DECORRENZA_INTERESSE_NON_SUCCESSIVA_PRECEDENTE, errori);
        }
        // e sul tasso solo se il tasso c'è ed è positivo
        if (commonValidator.validaTasso(modificaInteresseDto.getTassoInteresse(), errori)) {
            aggiungiErroreTassoUgualePrecedente(modificaInteresseDto.getTassoInteresse(), interessePrecedente, errori);
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // regola usata da inserimento e modifica: aggiunge alla lista ricevuta, non lancia
    // (in modifica gli errori vanno uniti a quelli sulla decorrenza).
    // compareTo e non equals: per BigDecimal equals considera diversi 2.0 e 2.00
    private void aggiungiErroreTassoUgualePrecedente(BigDecimal tassoInteresse, WTBAccertamentiInteresseRitPagam interessePrecedente,
                                                     List<String> errori) {
        if (null != interessePrecedente && 0 == tassoInteresse.compareTo(interessePrecedente.getTassoInteresse())) {
            errori.add(String.format(Constants.ErrorMessages.TASSO_INTERESSE_UGUALE_VIGENTE,
                    interessePrecedente.getTassoInteresse().toPlainString()));
        }
    }
}
