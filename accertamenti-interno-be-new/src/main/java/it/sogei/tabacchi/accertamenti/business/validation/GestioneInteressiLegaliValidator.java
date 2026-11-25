package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// validazioni degli interessi legali (un tasso per anno, niente decorrenza).
// Ogni metodo pubblico è una fase: void, raccoglie gli errori in una lista propria e se non è vuota lancia lui
// la BusinessException con i messaggi concatenati (commonValidator.checkAndThrowIfErrors); il controller li chiama in fila.
// Non legge il DB: i dati che servono (es. interesse già presente nell'anno) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiLegaliValidator {

    @Inject
    private CommonValidator commonValidator;

    // Body e tipo operazione li controlla Bean Validation prima del controller:
    // annotazioni su ModificaInteresseLegaleDto e @Valid @NotNull sul parametro di IGestioneInteressiLegaliService.scriviInteresseLegale

    // 2° passo, ramo INSERIMENTO: niente id, niente cancellazione, anno e tasso obbligatori (tasso > 0).
    // Nessun limite sull'anno (l'analisi non ne prevede).
    // Che l'anno sia libero si controlla dopo (validaAnnoLibero): serve una query con l'anno già validato.
    // Raccoglie tutti gli errori in una lista sua: se ce n'è almeno uno lancia qui la BusinessException
    // con i messaggi concatenati, altrimenti non fa niente e il controller prosegue
    public void validaInserimentoInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaNuovoInteresse(modificaInteresseLegaleDto.getIdInteresse(), modificaInteresseLegaleDto.getFlagCancella(), errori);
        commonValidator.validaObbligatorio(modificaInteresseLegaleDto.getAnno(), Constants.ErrorMessages.ANNO_INTERESSE_LEGALE_OBBLIGATORIO, errori);
        commonValidator.validaTasso(modificaInteresseLegaleDto.getTassoInteresseLegale(), errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // l'anno (già validato) non ha già un tasso non cancellato: isInteressePresenteInAnno lo calcola il controller con una query.
    // Come validaInserimentoInteresseLegale: lista propria, lancia qui la BusinessException se c'è un errore
    public void validaAnnoLibero(Integer anno, boolean isInteressePresenteInAnno) {
        List<String> errori = new ArrayList<>();
        if (isInteressePresenteInAnno) {
            errori.add(String.format(Constants.ErrorMessages.ANNO_INTERESSE_LEGALE_GIA_PRESENTE, anno));
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 2° passo, ramo MODIFICA, prima di leggere il DB: l'id dell'interesse da modificare
    public void validaRichiestaModifica(ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaObbligatorio(modificaInteresseLegaleDto.getIdInteresse(), Constants.ErrorMessages.ID_INTERESSE_OBBLIGATORIO, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo MODIFICA: l'interesse letto per id esiste, non è cancellato ed è ancora modificabile.
    // findById restituisce anche le righe cancellate: per il frontend una riga cancellata non esiste.
    // Modificabile (regola del BE del collega): anno futuro oppure inserito oggi
    public void validaInteresseLegaleModificabile(WTBAccertamentiInteresseLegale interesseLegaleSalvato) {
        List<String> errori = new ArrayList<>();
        LocalDate oggi = LocalDate.now();
        // flagCancella != 0 (1 o 2) = cancellata
        if (null == interesseLegaleSalvato || !Constants.GestioneParametri.FLAG_CANCELLA_NO.equals(interesseLegaleSalvato.getFlagCancella())) {
            errori.add(Constants.ErrorMessages.INTERESSE_LEGALE_NON_TROVATO);
        } else if (interesseLegaleSalvato.getAnnoValid() <= oggi.getYear() && !oggi.equals(interesseLegaleSalvato.getDtInserimento())) {
            errori.add(String.format(Constants.ErrorMessages.INTERESSE_LEGALE_NON_MODIFICABILE, interesseLegaleSalvato.getAnnoValid()));
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 4° passo, ramo MODIFICA quando non è una cancellazione: solo il tasso.
    // L'anno non si modifica (analisi: "non editabile"), quindi non si valida
    public void validaModificaInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaTasso(modificaInteresseLegaleDto.getTassoInteresseLegale(), errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }
}
