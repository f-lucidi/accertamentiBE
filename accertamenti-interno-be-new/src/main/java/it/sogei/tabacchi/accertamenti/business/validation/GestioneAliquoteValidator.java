package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// validazioni delle aliquote di mora.
// Ogni metodo pubblico è una fase: void, raccoglie gli errori in una lista propria e se non è vuota lancia lui
// la BusinessException con i messaggi concatenati (commonValidator.checkAndThrowIfErrors); il controller li chiama in fila.
// Le regole sugli scaglioni (valori, giorni, continuità, gruppo) sono in CommonValidator: le usano anche le sanzioni.
// Non legge il DB: i dati che servono (es. gruppo salvato) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteValidator {

    // "Aliquota" a inizio frase nei messaggi delle righe
    private static final String NOME_ALIQUOTA_MAIUSCOLO = StringUtils.capitalize(Constants.GestioneParametri.NOME_ALIQUOTA);

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco aliquote (date di decorrenza da/a coerenti)
    public void validaFiltri(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // Body, tipo operazione e scaglioni (né null né vuoti) li controlla Bean Validation prima del controller:
    // annotazioni su ModificaAliquoteDto e @Valid @NotNull sul parametro di IGestioneAliquoteService.salvaAliquote

    // 2° passo, ramo INSERIMENTO: nessun id gruppo, decorrenza, ogni aliquota, continuità dei giorni.
    // Che non esista già un gruppo modificabile lo controlla prima il controller (errore 403, non sui dati)
    public void validaInserimentoAliquote(ModificaAliquoteDto modificaAliquote) {
        List<String> errori = new ArrayList<>();
        if (null != modificaAliquote.getIdGruppo()) {
            errori.add(Constants.ErrorMessages.ID_GRUPPO_NON_AMMESSO);
        }
        commonValidator.validaDataDecorrenza(modificaAliquote.getDataDecorrenza(), errori);

        // scaglioni non vuoti (@NotEmpty): tutte le aliquote vengono controllate; la continuità solo se i giorni di ognuna sono validi
        List<AliquotaModificaDto> aliquote = modificaAliquote.getScaglioni();
        boolean isAliquoteValide = true;
        for (int indice = 0; indice < aliquote.size(); indice++) {
            AliquotaModificaDto aliquota = aliquote.get(indice);
            int numeroAliquota = indice + 1;
            boolean isPresente = commonValidator.validaObbligatorio(aliquota,
                    String.format(Constants.ErrorMessages.SCAGLIONE_VUOTO, NOME_ALIQUOTA_MAIUSCOLO, numeroAliquota), errori);
            if (isPresente && null != aliquota.getIdAliquota()) {
                errori.add(String.format(Constants.ErrorMessages.ID_SCAGLIONE_NON_AMMESSO, NOME_ALIQUOTA_MAIUSCOLO, numeroAliquota));
            }
            // && corto: i valori si controllano solo se l'aliquota c'è
            isAliquoteValide = isPresente && commonValidator.validaValoriScaglione(aliquota.getValoreAliquota(), aliquota,
                    Constants.GestioneParametri.NOME_ALIQUOTA, numeroAliquota, errori) && isAliquoteValide;
        }
        if (isAliquoteValide) {
            commonValidator.validaContinuitaGiorni(aliquote, Constants.GestioneParametri.NOME_ALIQUOTA,
                    Constants.GestioneParametri.NOME_ALIQUOTE, errori);
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 2° passo, ramo MODIFICA: id del gruppo presente, righe senza elementi vuoti (che l'elenco ci sia lo garantisce @NotEmpty).
    // Che contenga tutte le righe già salvate lo controlla validaModificaAliquote, col gruppo letto dal DB
    public void validaElencoModifica(ModificaAliquoteDto modificaAliquote) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaObbligatorio(modificaAliquote.getIdGruppo(), Constants.ErrorMessages.ID_GRUPPO_OBBLIGATORIO, errori);
        List<AliquotaModificaDto> aliquote = modificaAliquote.getScaglioni();
        for (int indice = 0; indice < aliquote.size(); indice++) {
            commonValidator.validaObbligatorio(aliquote.get(indice),
                    String.format(Constants.ErrorMessages.SCAGLIONE_VUOTO, NOME_ALIQUOTA_MAIUSCOLO, indice + 1), errori);
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // il gruppo (righe non cancellate con decorrenza ricavata dall'idGruppo) esiste ed è ancora modificabile.
    // Stessa regola di countAliquoteModificabili: decorrenza futura oppure almeno una riga inserita oggi
    public void validaGruppoModificabile(List<WTBAccertamentiAliquotaMora> gruppoSalvato, LocalDate dataInizioGruppo) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaGruppoModificabile(gruppoSalvato.stream().map(WTBAccertamentiAliquotaMora::getDtInserimento).toList(),
                dataInizioGruppo, Constants.ErrorMessages.ALIQUOTE_DA_MODIFICARE_NON_TROVATE,
                Constants.ErrorMessages.GRUPPO_ALIQUOTE_NON_MODIFICABILE, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo MODIFICA: righe del gruppo, poi decorrenza, valori e continuità delle righe che restano.
    // Se sono tutte cancellate è l'eliminazione del gruppo: decorrenza, valori e continuità non contano
    public void validaModificaAliquote(ModificaAliquoteDto modificaAliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato,
                                       LocalDate dataInizioPrecedente) {
        List<String> errori = new ArrayList<>();
        // id delle righe già salvate (le nuove non hanno id) contro quelli del gruppo letto dal DB
        commonValidator.validaGruppoCompleto(
                modificaAliquote.getScaglioni().stream().map(AliquotaModificaDto::getIdAliquota).filter(Objects::nonNull).toList(),
                gruppoSalvato.stream().map(WTBAccertamentiAliquotaMora::getId).toList(),
                Constants.ErrorMessages.GRUPPO_ALIQUOTE_INCOMPLETO, errori);

        // righe che restano: quelle non tolte col cestino
        List<AliquotaModificaDto> aliquoteRimaste = modificaAliquote.getScaglioni().stream()
                .filter(aliquota -> !Boolean.TRUE.equals(aliquota.getFlagCancella()))
                .toList();
        // nessuna riga rimasta = eliminazione del gruppo: niente decorrenza, valori né continuità da controllare
        if (!aliquoteRimaste.isEmpty()) {
            LocalDate dataDecorrenza = modificaAliquote.getDataDecorrenza();
            // il confronto col precedente ha senso solo su una decorrenza valida
            if (commonValidator.validaDataDecorrenza(dataDecorrenza, errori)) {
                commonValidator.validaDecorrenzaSuccessivaPrecedente(dataDecorrenza, dataInizioPrecedente,
                        Constants.ErrorMessages.DECORRENZA_NON_SUCCESSIVA_PRECEDENTE, errori);
            }
            boolean isAliquoteValide = true;
            for (int indice = 0; indice < aliquoteRimaste.size(); indice++) {
                AliquotaModificaDto aliquota = aliquoteRimaste.get(indice);
                isAliquoteValide = commonValidator.validaValoriScaglione(aliquota.getValoreAliquota(), aliquota,
                        Constants.GestioneParametri.NOME_ALIQUOTA, indice + 1, errori) && isAliquoteValide;
            }
            if (isAliquoteValide) {
                commonValidator.validaContinuitaGiorni(aliquoteRimaste, Constants.GestioneParametri.NOME_ALIQUOTA,
                        Constants.GestioneParametri.NOME_ALIQUOTE, errori);
            }
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }
}
