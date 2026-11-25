package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// validazioni delle sanzioni: stesse fasi e regole delle aliquote di mora (GestioneAliquoteValidator).
// Ogni metodo pubblico è una fase: void, raccoglie gli errori in una lista propria e se non è vuota lancia lui
// la BusinessException con i messaggi concatenati (commonValidator.checkAndThrowIfErrors); il controller li chiama in fila.
// Le regole sugli scaglioni (valori, giorni, continuità, gruppo) sono in CommonValidator, condivise con le aliquote.
// moltiplicatoPerGiorni non si valida: null vale "no".
// Non legge il DB: i dati che servono (es. gruppo salvato) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneSanzioniValidator {

    // "Sanzione" a inizio frase nei messaggi delle righe
    private static final String NOME_SANZIONE_MAIUSCOLO = StringUtils.capitalize(Constants.GestioneParametri.NOME_SANZIONE);

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco sanzioni (date di decorrenza da/a coerenti)
    public void validaFiltri(FiltriGestioneParametriDto filtriGestioneParametriDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // Body, tipo operazione e scaglioni (né null né vuoti) li controlla Bean Validation prima del controller:
    // annotazioni su ModificaSanzioniDto e @Valid @NotNull sul parametro di IGestioneSanzioniService.salvaSanzioni

    // 2° passo, ramo INSERIMENTO: decorrenza, ogni sanzione, continuità dei giorni.
    // Che non esista già un gruppo modificabile lo controlla prima il controller (errore 403, non sui dati)
    public void validaInserimentoSanzioni(ModificaSanzioniDto modificaSanzioniDto) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaDataDecorrenza(modificaSanzioniDto.getDataDecorrenza(), errori);

        // scaglioni non vuoti (@NotEmpty): tutte le sanzioni vengono controllate; la continuità solo se i giorni di ognuna sono validi
        List<SanzioneModificaDto> sanzioni = modificaSanzioniDto.getScaglioni();
        boolean isSanzioniValide = true;
        for (int indice = 0; indice < sanzioni.size(); indice++) {
            SanzioneModificaDto sanzione = sanzioni.get(indice);
            int numeroSanzione = indice + 1;
            boolean isPresente = commonValidator.validaObbligatorio(sanzione,
                    String.format(Constants.ErrorMessages.SCAGLIONE_VUOTO, NOME_SANZIONE_MAIUSCOLO, numeroSanzione), errori);
            if (isPresente && null != sanzione.getIdSanzione()) {
                errori.add(String.format(Constants.ErrorMessages.ID_SCAGLIONE_NON_AMMESSO, NOME_SANZIONE_MAIUSCOLO, numeroSanzione));
            }
            // && corto: i valori si controllano solo se la sanzione c'è
            isSanzioniValide = isPresente && commonValidator.validaValoriScaglione(sanzione.getValoreSanzione(), sanzione,
                    Constants.GestioneParametri.NOME_SANZIONE, numeroSanzione, errori) && isSanzioniValide;
        }
        if (isSanzioniValide) {
            commonValidator.validaContinuitaGiorni(sanzioni, Constants.GestioneParametri.NOME_SANZIONE,
                    Constants.GestioneParametri.NOME_SANZIONI, errori);
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo INSERIMENTO: le nuove sanzioni (già validate) non possono ripetere il gruppo precedente,
    // letto dal controller (lista vuota = nessun gruppo precedente). Conta anche "moltiplicato per giorni"
    public void validaSanzioniDiverseDaPrecedenti(List<SanzioneModificaDto> sanzioni, List<WTBAccertamentiSanzioni> gruppoPrecedente) {
        List<String> errori = new ArrayList<>();
        List<String> firmeNuove = sanzioni.stream()
                .map(sanzione -> commonValidator.firmaScaglione(sanzione.getValoreSanzione(), sanzione.getGiorniDa(), sanzione.getGiorniA())
                        + "|" + Boolean.TRUE.equals(sanzione.getMoltiplicatoPerGiorni()))
                .toList();
        List<String> firmePrecedenti = gruppoPrecedente.stream()
                .map(sanzione -> commonValidator.firmaScaglione(sanzione.getSanzione(), sanzione.getGiorniRitardoDa(), sanzione.getGiorniRitardoA())
                        + "|" + Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_SI.equals(sanzione.getMoltiplicatoPerGiorniRitardo()))
                .toList();
        commonValidator.validaGruppoDiversoDaPrecedente(firmeNuove, firmePrecedenti,
                Constants.ErrorMessages.SANZIONI_UGUALI_PRECEDENTI, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 2° passo, ramo MODIFICA: almeno una sanzione già salvata (da lì si ricava il gruppo), righe senza elementi vuoti.
    // Che contenga tutte le righe già salvate lo controlla validaModificaSanzioni, col gruppo letto dal DB
    public void validaElencoModifica(ModificaSanzioniDto modificaSanzioniDto) {
        List<String> errori = new ArrayList<>();
        List<SanzioneModificaDto> sanzioni = modificaSanzioniDto.getScaglioni();
        boolean isSanzioneSalvataPresente = false;
        for (int indice = 0; indice < sanzioni.size(); indice++) {
            SanzioneModificaDto sanzione = sanzioni.get(indice);
            boolean isPresente = commonValidator.validaObbligatorio(sanzione,
                    String.format(Constants.ErrorMessages.SCAGLIONE_VUOTO, NOME_SANZIONE_MAIUSCOLO, indice + 1), errori);
            if (isPresente && null != sanzione.getIdSanzione()) {
                isSanzioneSalvataPresente = true;
            }
        }
        if (!isSanzioneSalvataPresente) {
            errori.add(Constants.ErrorMessages.SANZIONE_SALVATA_OBBLIGATORIA);
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // il gruppo (righe con la decorrenza della sanzione salvata indicata) esiste ed è ancora modificabile.
    // Stessa regola di countSanzioniModificabili: decorrenza futura oppure almeno una riga inserita oggi
    public void validaGruppoModificabile(List<WTBAccertamentiSanzioni> gruppoSalvato, LocalDate dataInizioGruppo) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaGruppoModificabile(gruppoSalvato.stream().map(WTBAccertamentiSanzioni::getDtInserimento).toList(),
                dataInizioGruppo, Constants.ErrorMessages.SANZIONI_DA_MODIFICARE_NON_TROVATE,
                Constants.ErrorMessages.GRUPPO_SANZIONI_NON_MODIFICABILE, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }

    // 3° passo, ramo MODIFICA: righe del gruppo, poi decorrenza, valori e continuità delle righe che restano.
    // Se sono tutte cancellate è l'eliminazione del gruppo: decorrenza, valori e continuità non contano
    public void validaModificaSanzioni(ModificaSanzioniDto modificaSanzioniDto, List<WTBAccertamentiSanzioni> gruppoSalvato,
                                       LocalDate dataInizioPrecedente) {
        List<String> errori = new ArrayList<>();
        // id delle righe già salvate (le nuove non hanno id) contro quelli del gruppo letto dal DB
        commonValidator.validaGruppoCompleto(
                modificaSanzioniDto.getScaglioni().stream().map(SanzioneModificaDto::getIdSanzione).filter(Objects::nonNull).toList(),
                gruppoSalvato.stream().map(WTBAccertamentiSanzioni::getId).toList(),
                Constants.ErrorMessages.GRUPPO_SANZIONI_INCOMPLETO, errori);

        // righe che restano: quelle non tolte col cestino
        List<SanzioneModificaDto> sanzioniRimaste = modificaSanzioniDto.getScaglioni().stream()
                .filter(sanzione -> !Boolean.TRUE.equals(sanzione.getFlagCancella()))
                .toList();
        // nessuna riga rimasta = eliminazione del gruppo: niente decorrenza, valori né continuità da controllare
        if (!sanzioniRimaste.isEmpty()) {
            LocalDate dataDecorrenza = modificaSanzioniDto.getDataDecorrenza();
            // il confronto col precedente ha senso solo su una decorrenza valida
            if (commonValidator.validaDataDecorrenza(dataDecorrenza, errori)) {
                commonValidator.validaDecorrenzaSuccessivaPrecedente(dataDecorrenza, dataInizioPrecedente,
                        Constants.ErrorMessages.DECORRENZA_SANZIONI_NON_SUCCESSIVA_PRECEDENTE, errori);
            }
            boolean isSanzioniValide = true;
            for (int indice = 0; indice < sanzioniRimaste.size(); indice++) {
                SanzioneModificaDto sanzione = sanzioniRimaste.get(indice);
                isSanzioniValide = commonValidator.validaValoriScaglione(sanzione.getValoreSanzione(), sanzione,
                        Constants.GestioneParametri.NOME_SANZIONE, indice + 1, errori) && isSanzioniValide;
            }
            if (isSanzioniValide) {
                commonValidator.validaContinuitaGiorni(sanzioniRimaste, Constants.GestioneParametri.NOME_SANZIONE,
                        Constants.GestioneParametri.NOME_SANZIONI, errori);
            }
        }
        commonValidator.checkAndThrowIfErrors(errori);
    }
}
