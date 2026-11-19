package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

// validazioni degli interessi legali (un tasso per anno, niente decorrenza). Nessun metodo lancia eccezioni:
// ogni regola violata aggiunge il suo messaggio alla lista errori creata dal controller, che la riceve indietro
// e lancia una sola BusinessException. Non legge il DB: i dati che servono (es. anno già occupato) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiLegaliValidator {

    @Inject
    private CommonValidator commonValidator;

    // Body e tipo operazione li controlla Bean Validation prima del controller:
    // annotazioni su ModificaInteresseLegaleDto e @Valid @NotNull sul parametro di IGestioneInteressiLegaliService.scriviInteresseLegale

    // 2° passo, ramo INSERIMENTO: niente id, niente cancellazione, anno e tasso obbligatori (tasso > 0).
    // Nessun limite sull'anno (l'analisi non ne prevede).
    // Che l'anno sia libero si controlla dopo (validaAnnoLibero): serve una query con l'anno già validato
    public List<String> validaInserimentoInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, List<String> errori) {
        if (null != modificaInteresseLegaleDto.getIdInteresse()) {
            errori.add(Constants.ErrorMessages.ID_INTERESSE_NON_AMMESSO);
        }
        if (Boolean.TRUE.equals(modificaInteresseLegaleDto.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.CANCELLAZIONE_INTERESSE_NON_AMMESSA);
        }
        commonValidator.validaObbligatorio(modificaInteresseLegaleDto.getAnno(), Constants.ErrorMessages.ANNO_INTERESSE_LEGALE_OBBLIGATORIO, errori);
        commonValidator.validaTasso(modificaInteresseLegaleDto.getTassoInteresseLegale(), errori);
        return errori;
    }

    // l'anno (già validato) non ha già un tasso non cancellato: isAnnoOccupato lo calcola il controller con una query
    public List<String> validaAnnoLibero(Integer anno, boolean isAnnoOccupato, List<String> errori) {
        if (isAnnoOccupato) {
            errori.add(String.format(Constants.ErrorMessages.ANNO_INTERESSE_LEGALE_GIA_PRESENTE, anno));
        }
        return errori;
    }

    // 2° passo, ramo MODIFICA, prima di leggere il DB: l'id dell'interesse da modificare
    public List<String> validaRichiestaModifica(ModificaInteresseLegaleDto modificaInteresseLegaleDto, List<String> errori) {
        commonValidator.validaObbligatorio(modificaInteresseLegaleDto.getIdInteresse(), Constants.ErrorMessages.ID_INTERESSE_OBBLIGATORIO, errori);
        return errori;
    }

    // 3° passo, ramo MODIFICA: l'interesse letto per id esiste, non è cancellato ed è ancora modificabile.
    // findById restituisce anche le righe cancellate: per il frontend una riga cancellata non esiste.
    // Modificabile (regola del BE del collega): anno futuro oppure inserito oggi
    public List<String> validaInteresseLegaleModificabile(WTBAccertamentiInteresseLegale interesseLegaleSalvato, List<String> errori) {
        LocalDate oggi = LocalDate.now();
        // flagCancella != 0 (1 o 2) = cancellata
        if (null == interesseLegaleSalvato || !Constants.GestioneParametri.FLAG_CANCELLA_NO.equals(interesseLegaleSalvato.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.INTERESSE_LEGALE_NON_TROVATO);
        } else if (interesseLegaleSalvato.getAnnoValid() <= oggi.getYear() && !oggi.equals(interesseLegaleSalvato.getDtInserimento())) {
            errori.add(String.format(Constants.ErrorMessages.INTERESSE_LEGALE_NON_MODIFICABILE, interesseLegaleSalvato.getAnnoValid()));
        }
        return errori;
    }

    // 4° passo, ramo MODIFICA quando non è una cancellazione: solo il tasso.
    // L'anno non si modifica (analisi: "non editabile"), quindi non si valida
    public List<String> validaModificaInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, List<String> errori) {
        commonValidator.validaTasso(modificaInteresseLegaleDto.getTassoInteresseLegale(), errori);
        return errori;
    }
}
