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
import java.util.List;

// validazioni degli interessi sul tardato pagamento. Nessun metodo lancia eccezioni: ogni regola violata aggiunge
// il suo messaggio alla lista errori creata dal controller, che la riceve indietro e lancia una sola BusinessException.
// Non legge il DB: i dati che servono (es. interesse precedente) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiValidator {

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco interessi (date di decorrenza da/a coerenti)
    public List<String> validaFiltri(FiltriGestioneParametriDto filtriGestioneParametriDto, List<String> errori) {
        return commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto, errori);
    }

    // Body e tipo operazione li controlla Bean Validation prima del controller:
    // annotazioni su ModificaInteresseDto e @Valid @NotNull sul parametro di IGestioneInteressiService.scriviInteresse

    // 2° passo, ramo INSERIMENTO: niente id, niente cancellazione, decorrenza, tasso.
    // Che non esista già un interesse modificabile lo controlla prima il controller (errore 403, non sui dati).
    // Il confronto col precedente è a parte (validaTassoDiversoDaPrecedente): il precedente si cerca con la decorrenza,
    // che deve essere già valida per usarla in una query
    public List<String> validaInserimentoInteresse(ModificaInteresseDto modificaInteresseDto, List<String> errori) {
        if (null != modificaInteresseDto.getIdGruppo()) {
            errori.add(Constants.ErrorMessages.ID_GRUPPO_NON_AMMESSO);
        }
        if (null != modificaInteresseDto.getIdInteresse()) {
            errori.add(Constants.ErrorMessages.ID_INTERESSE_NON_AMMESSO);
        }
        if (Boolean.TRUE.equals(modificaInteresseDto.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.CANCELLAZIONE_INTERESSE_NON_AMMESSA);
        }
        commonValidator.validaDataDecorrenza(modificaInteresseDto.getDataDecorrenza(), errori);
        commonValidator.validaTasso(modificaInteresseDto.getTassoInteresse(), errori);
        return errori;
    }

    // 3° passo: il nuovo tasso (già validato, non null) non può ripetere quello precedente;
    // interessePrecedente letto dal controller, null = nessun vincolo.
    // compareTo e non equals: per BigDecimal equals considera diversi 2.0 e 2.00
    public List<String> validaTassoDiversoDaPrecedente(BigDecimal tassoInteresse, WTBAccertamentiInteresseRitPagam interessePrecedente,
                                                       List<String> errori) {
        if (null != interessePrecedente && 0 == tassoInteresse.compareTo(interessePrecedente.getTassoInteresse())) {
            errori.add(String.format(Constants.ErrorMessages.TASSO_INTERESSE_UGUALE_VIGENTE,
                    interessePrecedente.getTassoInteresse().toPlainString()));
        }
        return errori;
    }

    // 2° passo, ramo MODIFICA, prima di leggere il DB: l'id dell'interesse da modificare.
    // L'idGruppo non serve: a differenza delle aliquote ogni decorrenza ha una sola riga, l'id basta a trovarla
    public List<String> validaRichiestaModifica(ModificaInteresseDto modificaInteresseDto, List<String> errori) {
        commonValidator.validaObbligatorio(modificaInteresseDto.getIdInteresse(), Constants.ErrorMessages.ID_INTERESSE_OBBLIGATORIO, errori);
        return errori;
    }

    // 3° passo, ramo MODIFICA: l'interesse letto per id esiste, non è cancellato ed è ancora modificabile.
    // findById restituisce anche le righe cancellate: per il frontend una riga cancellata non esiste.
    // Stessa regola di countInteressiModificabili: decorrenza futura oppure inserito oggi
    public List<String> validaInteresseModificabile(WTBAccertamentiInteresseRitPagam interesseSalvato, List<String> errori) {
        LocalDate oggi = LocalDate.now();
        if (null == interesseSalvato || Boolean.TRUE.equals(interesseSalvato.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.INTERESSE_NON_TROVATO);
        } else if (!interesseSalvato.getDataInizioVal().isAfter(oggi) && !oggi.equals(interesseSalvato.getDtInserimento())) {
            errori.add(String.format(Constants.ErrorMessages.INTERESSE_NON_MODIFICABILE,
                    Utility.formattaData(interesseSalvato.getDataInizioVal())));
        }
        return errori;
    }

    // 4° passo, ramo MODIFICA quando non è una cancellazione: nuova decorrenza (valida e successiva al precedente)
    // e nuovo tasso (valido e diverso dal precedente). interessePrecedente letto dal controller, null = nessun vincolo
    public List<String> validaModificaInteresse(ModificaInteresseDto modificaInteresseDto, WTBAccertamentiInteresseRitPagam interessePrecedente,
                                                List<String> errori) {
        LocalDate dataDecorrenza = modificaInteresseDto.getDataDecorrenza();
        // il confronto col precedente ha senso solo su una decorrenza valida
        if (commonValidator.validaDataDecorrenza(dataDecorrenza, errori)) {
            commonValidator.validaDecorrenzaSuccessivaPrecedente(dataDecorrenza,
                    null != interessePrecedente ? interessePrecedente.getDataInizioVal() : null,
                    Constants.ErrorMessages.DECORRENZA_INTERESSE_NON_SUCCESSIVA_PRECEDENTE, errori);
        }
        // e sul tasso solo se il tasso c'è ed è positivo
        if (commonValidator.validaTasso(modificaInteresseDto.getTassoInteresse(), errori)) {
            validaTassoDiversoDaPrecedente(modificaInteresseDto.getTassoInteresse(), interessePrecedente, errori);
        }
        return errori;
    }
}
