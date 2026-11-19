package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// validazioni delle aliquote di mora. Nessun metodo lancia eccezioni: ogni regola violata aggiunge il suo messaggio
// alla lista errori creata dal controller, che la riceve indietro e lancia una sola BusinessException con tutti i messaggi.
// Non legge il DB: i dati che servono (es. gruppo salvato) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteValidator {

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco aliquote (date di decorrenza da/a coerenti)
    public List<String> validaFiltri(FiltriGestioneParametriDto filtriGestioneParametriDto, List<String> errori) {
        return commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto, errori);
    }

    // Body, tipo operazione e scaglioni (né null né vuoti) li controlla Bean Validation prima del controller:
    // annotazioni su ModificaAliquoteDto e @Valid @NotNull sul parametro di IGestioneAliquoteService.salvaAliquote

    // 2° passo, ramo INSERIMENTO: nessun id gruppo, decorrenza, ogni aliquota, continuità dei giorni.
    // Che non esista già un gruppo modificabile lo controlla prima il controller (errore 403, non sui dati)
    public List<String> validaInserimentoAliquote(ModificaAliquoteDto modificaAliquote, List<String> errori) {
        if (null != modificaAliquote.getIdGruppo()) {
            errori.add(Constants.ErrorMessages.ID_GRUPPO_NON_AMMESSO);
        }
        commonValidator.validaDataDecorrenza(modificaAliquote.getDataDecorrenza(), errori);

        // scaglioni non vuoti (@NotEmpty): tutte le aliquote vengono controllate; la continuità solo se i giorni di ognuna sono validi
        List<AliquotaModificaDto> aliquote = modificaAliquote.getScaglioni();
        boolean isAliquoteValide = true;
        for (int indice = 0; indice < aliquote.size(); indice++) {
            isAliquoteValide = validaAliquotaNuova(aliquote.get(indice), indice + 1, errori) && isAliquoteValide;
        }
        if (isAliquoteValide) {
            validaContinuitaGiorni(aliquote, errori);
        }
        return errori;
    }

    // 2° passo, ramo MODIFICA: id del gruppo presente, righe senza elementi vuoti (che l'elenco ci sia lo garantisce @NotEmpty).
    // Che contenga tutte le righe già salvate lo controlla validaModificaAliquote, col gruppo letto dal DB
    public List<String> validaElencoModifica(ModificaAliquoteDto modificaAliquote, List<String> errori) {
        commonValidator.validaObbligatorio(modificaAliquote.getIdGruppo(), Constants.ErrorMessages.ID_GRUPPO_OBBLIGATORIO, errori);
        List<AliquotaModificaDto> aliquote = modificaAliquote.getScaglioni();
        for (int indice = 0; indice < aliquote.size(); indice++) {
            commonValidator.validaObbligatorio(aliquote.get(indice),
                    String.format(Constants.ErrorMessages.ALIQUOTA_VUOTA, indice + 1), errori);
        }
        return errori;
    }

    // il gruppo (righe non cancellate con decorrenza ricavata dall'idGruppo) esiste ed è ancora modificabile.
    // Stessa regola di countAliquoteModificabili: decorrenza futura oppure almeno una riga inserita oggi
    public List<String> validaGruppoModificabile(List<WTBAccertamentiAliquotaMora> gruppoSalvato, LocalDate dataInizioGruppo,
                                                 List<String> errori) {
        if (gruppoSalvato.isEmpty()) {
            errori.add(Constants.ErrorMessages.ALIQUOTE_DA_MODIFICARE_NON_TROVATE);
        } else {
            LocalDate oggi = LocalDate.now();
            boolean isInseritoOggi = false;
            for (WTBAccertamentiAliquotaMora aliquotaSalvata : gruppoSalvato) {
                if (oggi.equals(aliquotaSalvata.getDtInserimento())) {
                    isInseritoOggi = true;
                }
            }
            if (!dataInizioGruppo.isAfter(oggi) && !isInseritoOggi) {
                errori.add(String.format(Constants.ErrorMessages.GRUPPO_ALIQUOTE_NON_MODIFICABILE,
                        Utility.formattaData(dataInizioGruppo)));
            }
        }
        return errori;
    }

    // 3° passo, ramo MODIFICA: righe del gruppo, poi decorrenza, valori e continuità delle righe che restano.
    // Se sono tutte cancellate è l'eliminazione del gruppo: decorrenza, valori e continuità non contano
    public List<String> validaModificaAliquote(ModificaAliquoteDto modificaAliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato,
                                               LocalDate dataInizioPrecedente, List<String> errori) {
        validaRigheDelGruppo(modificaAliquote.getScaglioni(), gruppoSalvato, errori);

        // righe che restano, aggiungo alla lista delle aliquoteRimaste solo quelle
        // con flagCancella == false.
        List<AliquotaModificaDto> aliquoteRimaste = new ArrayList<>();
        for (AliquotaModificaDto aliquota : modificaAliquote.getScaglioni()) {
            if (!Boolean.TRUE.equals(aliquota.getFlagCancella())) {
                aliquoteRimaste.add(aliquota);
            }
        }
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
                isAliquoteValide = validaValoriAliquota(aliquoteRimaste.get(indice), indice + 1, errori) && isAliquoteValide;
            }
            if (isAliquoteValide) {
                validaContinuitaGiorni(aliquoteRimaste, errori);
            }
        }
        return errori;
    }

    // ogni aliquota salvata del gruppo una volta sola, e nessuna di altri gruppi:
    // se ne mancasse una, cambiando la decorrenza resterebbe con la data vecchia
    private void validaRigheDelGruppo(List<AliquotaModificaDto> aliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato,
                                      List<String> errori) {
        List<Long> idRichiesti = new ArrayList<>();
        for (AliquotaModificaDto aliquota : aliquote) {
            if (null != aliquota.getIdAliquota()) {
                idRichiesti.add(aliquota.getIdAliquota());
            }
        }
        List<Long> idSalvati = new ArrayList<>();
        for (WTBAccertamentiAliquotaMora aliquotaSalvata : gruppoSalvato) {
            idSalvati.add(aliquotaSalvata.getId());
        }
        if (idRichiesti.size() != idSalvati.size() || !idRichiesti.containsAll(idSalvati)) {
            errori.add(Constants.ErrorMessages.GRUPPO_ALIQUOTE_INCOMPLETO);
        }
    }

    // numeroAliquota parte da 1, come i riquadri "Aliquota N" del frontend.
    // true se l'aliquota c'è e i suoi giorni sono validi (serve alla continuità)
    private boolean validaAliquotaNuova(AliquotaModificaDto aliquota, int numeroAliquota, List<String> errori) {
        boolean isPresente = commonValidator.validaObbligatorio(aliquota,
                String.format(Constants.ErrorMessages.ALIQUOTA_VUOTA, numeroAliquota), errori);
        if (isPresente && null != aliquota.getIdAliquota()) {
            errori.add(String.format(Constants.ErrorMessages.ID_ALIQUOTA_NON_AMMESSO, numeroAliquota));
        }
        // && corto: i valori si controllano solo se l'aliquota c'è
        return isPresente && validaValoriAliquota(aliquota, numeroAliquota, errori);
    }

    // valore > 0, giorni da obbligatori, giorni a (se c'è) maggiore dei giorni da.
    // true se i giorni sono validi: solo allora si può controllare la continuità
    private boolean validaValoriAliquota(AliquotaModificaDto aliquota, int numeroAliquota, List<String> errori) {
        if (commonValidator.validaObbligatorio(aliquota.getValoreAliquota(),
                String.format(Constants.ErrorMessages.VALORE_ALIQUOTA_OBBLIGATORIO, numeroAliquota), errori)
                && aliquota.getValoreAliquota().compareTo(BigDecimal.ZERO) <= 0) {
            errori.add(String.format(Constants.ErrorMessages.VALORE_ALIQUOTA_NON_POSITIVO, numeroAliquota));
        }

        boolean isGiorniDaPresente = commonValidator.validaObbligatorio(aliquota.getGiorniDa(),
                String.format(Constants.ErrorMessages.GIORNI_DA_OBBLIGATORIO, numeroAliquota), errori);
        // senza giorni da non c'è niente con cui confrontare i giorni a
        boolean isGiorniAValido = !isGiorniDaPresente || null == aliquota.getGiorniA() || aliquota.getGiorniA() > aliquota.getGiorniDa();
        if (!isGiorniAValido) {
            errori.add(String.format(Constants.ErrorMessages.GIORNI_A_NON_VALIDO, numeroAliquota));
        }
        return isGiorniDaPresente && isGiorniAValido;
    }

    // le aliquote (tutte con giorni validi) arrivano in ordine (il frontend calcola "giorni da" dalla riga precedente):
    // la prima parte da 1, ognuna riprende dal giorno dopo la precedente, solo l'ultima è aperta
    private void validaContinuitaGiorni(List<AliquotaModificaDto> aliquote, List<String> errori) {
        if (!Constants.GestioneParametri.PRIMO_GIORNO_RITARDO.equals(aliquote.get(0).getGiorniDa())) {
            errori.add(String.format(Constants.ErrorMessages.PRIMA_ALIQUOTA_NON_DA_PRIMO_GIORNO,
                    Constants.GestioneParametri.PRIMO_GIORNO_RITARDO));
        }

        for (int indice = 1; indice < aliquote.size(); indice++) {
            AliquotaModificaDto precedente = aliquote.get(indice - 1);
            AliquotaModificaDto corrente = aliquote.get(indice);
            if (null == precedente.getGiorniA()) {
                errori.add(String.format(Constants.ErrorMessages.SOLO_ULTIMA_ALIQUOTA_APERTA, indice));
            // equals e non !=: sono Integer, != confronterebbe gli oggetti e non i numeri
            } else if (!corrente.getGiorniDa().equals(precedente.getGiorniA() + 1)) {
                errori.add(String.format(Constants.ErrorMessages.GIORNI_NON_CONSECUTIVI, indice, indice + 1));
            }
        }

        AliquotaModificaDto ultima = aliquote.get(aliquote.size() - 1);
        if (null != ultima.getGiorniA()) {
            errori.add(String.format(Constants.ErrorMessages.ULTIMA_ALIQUOTA_NON_APERTA, ultima.getGiorniA()));
        }
    }
}
