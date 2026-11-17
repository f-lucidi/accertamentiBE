package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// validazioni delle aliquote di mora: ogni regola violata lancia BusinessException.
// Non legge il DB: i dati che servono (es. gruppo salvato) li passa il controller
@Dependent
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteValidator {

    @Inject
    private CommonValidator commonValidator;

    // filtri dell'elenco, già normalizzati dal controller (null = filtri vuoti)
    public void validaFiltriAliquote(FiltriGestioneAliquoteDto filtri) {
        commonValidator.validaIntervalloDate(filtri.getDataInizioValiditaDa(), filtri.getDataInizioValiditaA(),
                Constants.ErrorMessages.INTERVALLO_DECORRENZA_NON_VALIDO);
    }

    // 1° passo del salvataggio: body e tipo operazione presenti (serve al controller per scegliere il ramo)
    public void validaRichiestaAliquote(ModificaAliquoteDto modificaAliquote) {
        commonValidator.validaObbligatorio(modificaAliquote, Constants.ErrorMessages.DATI_ALIQUOTE_OBBLIGATORI);
        commonValidator.validaObbligatorio(modificaAliquote.getTipoOperazione(), Constants.ErrorMessages.TIPO_OPERAZIONE_OBBLIGATORIO);
    }

    // 2° passo, ramo INSERIMENTO: decorrenza, nessun gruppo futuro già presente, ogni aliquota, continuità dei giorni
    public void validaInserimentoAliquote(ModificaAliquoteDto modificaAliquote, boolean esisteGruppoModificabile) {
        validaDataDecorrenza(modificaAliquote.getDataDecorrenza());
        if (esisteGruppoModificabile) {
            throw new BusinessException(ExceptionType.ALIQUOTA_FUTURA_ALREADY_CREATED);
        }

        List<AliquotaModificaDto> aliquote = modificaAliquote.getScaglioni();
        if (null == aliquote || aliquote.isEmpty()) {
            throw new BusinessException(Constants.ErrorMessages.ALIQUOTE_OBBLIGATORIE);
        }
        for (int indice = 0; indice < aliquote.size(); indice++) {
            validaAliquotaNuova(aliquote.get(indice), indice + 1);
        }
        validaContinuitaGiorni(aliquote);
    }

    private void validaDataDecorrenza(LocalDate dataDecorrenza) {
        commonValidator.validaObbligatorio(dataDecorrenza, Constants.ErrorMessages.DATA_DECORRENZA_OBBLIGATORIA);
        if (dataDecorrenza.isBefore(LocalDate.now())) {
            throw new BusinessException(Constants.ErrorMessages.DATA_DECORRENZA_PASSATA);
        }
    }

    // 2° passo, ramo MODIFICA: elenco presente, senza righe vuote e con almeno una riga già salvata
    public void validaElencoModifica(List<AliquotaModificaDto> aliquote) {
        if (null == aliquote || aliquote.isEmpty()) {
            throw new BusinessException(Constants.ErrorMessages.ALIQUOTE_OBBLIGATORIE);
        } else {
            boolean isPresenteAliquotaSalvata = false;
            for (int i = 0; i < aliquote.size(); i++) {
                commonValidator.validaObbligatorio(aliquote.get(i), String.format(Constants.ErrorMessages.ALIQUOTA_VUOTA, i + 1));
                if (null != aliquote.get(i).getIdAliquota()) {
                    isPresenteAliquotaSalvata = true;
                }
            }
            if (!isPresenteAliquotaSalvata) {
                throw new BusinessException(Constants.ErrorMessages.ALIQUOTE_SALVATE_OBBLIGATORIE);
            }
        }
    }

    // stessa regola di countAliquoteModificabili: decorrenza futura oppure inserita oggi
    public void validaGruppoModificabile(WTBAccertamentiAliquotaMora aliquotaSalvata) {
        if (null == aliquotaSalvata || Boolean.TRUE.equals(aliquotaSalvata.getFlagCancella())) {
            throw new BusinessException(Constants.ErrorMessages.ALIQUOTE_DA_MODIFICARE_NON_TROVATE);
        }
        LocalDate oggi = LocalDate.now();
        if (!aliquotaSalvata.getDataInizioVal().isAfter(oggi) && !oggi.equals(aliquotaSalvata.getDtInserimento())) {
            throw new BusinessException(String.format(Constants.ErrorMessages.GRUPPO_ALIQUOTE_NON_MODIFICABILE,
                    Utility.formattaData(aliquotaSalvata.getDataInizioVal())));
        }
    }

    // 3° passo, ramo MODIFICA: decorrenza, righe del gruppo, valori e continuità delle righe che restano.
    // Se sono tutte cancellate è l'eliminazione del gruppo: valori e continuità non contano
    public void validaModificaAliquote(ModificaAliquoteDto modificaAliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato,
                                       LocalDate dataInizioPrecedente) {
        LocalDate dataDecorrenza = modificaAliquote.getDataDecorrenza();
        validaDataDecorrenza(dataDecorrenza);
        // altrimenti il gruppo precedente finirebbe prima di iniziare
        if (null != dataInizioPrecedente && !dataDecorrenza.isAfter(dataInizioPrecedente)) {
            throw new BusinessException(String.format(Constants.ErrorMessages.DECORRENZA_NON_SUCCESSIVA_PRECEDENTE,
                    Utility.formattaData(dataInizioPrecedente)));
        }
        validaRigheDelGruppo(modificaAliquote.getScaglioni(), gruppoSalvato);

        // righe che restano, nell'ordine del frontend ("Aliquota N" conta solo quelle visibili)
        List<AliquotaModificaDto> aliquoteRimaste = new ArrayList<>();
        for (AliquotaModificaDto aliquota : modificaAliquote.getScaglioni()) {
            if (!Boolean.TRUE.equals(aliquota.getFlagCancella())) {
                aliquoteRimaste.add(aliquota);
            }
        }
        if (aliquoteRimaste.isEmpty()) {
            return;
        }
        for (int indice = 0; indice < aliquoteRimaste.size(); indice++) {
            validaValoriAliquota(aliquoteRimaste.get(indice), indice + 1);
        }
        validaContinuitaGiorni(aliquoteRimaste);
    }

    // ogni aliquota salvata del gruppo una volta sola, e nessuna di altri gruppi:
    // se ne mancasse una, cambiando la decorrenza resterebbe con la data vecchia
    private void validaRigheDelGruppo(List<AliquotaModificaDto> aliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato) {
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
            throw new BusinessException(Constants.ErrorMessages.GRUPPO_ALIQUOTE_INCOMPLETO);
        }
    }

    // numeroAliquota parte da 1, come i riquadri "Aliquota N" del frontend
    private void validaAliquotaNuova(AliquotaModificaDto aliquota, int numeroAliquota) {
        commonValidator.validaObbligatorio(aliquota, String.format(Constants.ErrorMessages.ALIQUOTA_VUOTA, numeroAliquota));
        if (null != aliquota.getIdAliquota()) {
            throw new BusinessException(String.format(Constants.ErrorMessages.ID_ALIQUOTA_NON_AMMESSO, numeroAliquota));
        }
        validaValoriAliquota(aliquota, numeroAliquota);
    }

    // valore > 0, giorni da obbligatori, giorni a (se c'è) maggiore dei giorni da
    private void validaValoriAliquota(AliquotaModificaDto aliquota, int numeroAliquota) {
        commonValidator.validaObbligatorio(aliquota.getValoreAliquota(),
                String.format(Constants.ErrorMessages.VALORE_ALIQUOTA_OBBLIGATORIO, numeroAliquota));
        if (aliquota.getValoreAliquota().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(String.format(Constants.ErrorMessages.VALORE_ALIQUOTA_NON_POSITIVO, numeroAliquota));
        }

        commonValidator.validaObbligatorio(aliquota.getGiorniDa(),
                String.format(Constants.ErrorMessages.GIORNI_DA_OBBLIGATORIO, numeroAliquota));
        if (null != aliquota.getGiorniA() && aliquota.getGiorniA() <= aliquota.getGiorniDa()) {
            throw new BusinessException(String.format(Constants.ErrorMessages.GIORNI_A_NON_VALIDO, numeroAliquota));
        }
    }

    // le aliquote arrivano in ordine (il frontend calcola "giorni da" dalla riga precedente):
    // la prima parte da 1, ognuna riprende dal giorno dopo la precedente, solo l'ultima è aperta
    private void validaContinuitaGiorni(List<AliquotaModificaDto> aliquote) {
        if (!Constants.GestioneAliquote.PRIMO_GIORNO_RITARDO.equals(aliquote.get(0).getGiorniDa())) {
            throw new BusinessException(String.format(Constants.ErrorMessages.PRIMA_ALIQUOTA_NON_DA_PRIMO_GIORNO,
                    Constants.GestioneAliquote.PRIMO_GIORNO_RITARDO));
        }

        for (int indice = 1; indice < aliquote.size(); indice++) {
            AliquotaModificaDto precedente = aliquote.get(indice - 1);
            AliquotaModificaDto corrente = aliquote.get(indice);
            if (null == precedente.getGiorniA()) {
                throw new BusinessException(String.format(Constants.ErrorMessages.SOLO_ULTIMA_ALIQUOTA_APERTA, indice));
            }
            // equals e non !=: sono Integer, != confronterebbe gli oggetti e non i numeri
            if (!corrente.getGiorniDa().equals(precedente.getGiorniA() + 1)) {
                throw new BusinessException(String.format(Constants.ErrorMessages.GIORNI_NON_CONSECUTIVI, indice, indice + 1));
            }
        }

        AliquotaModificaDto ultima = aliquote.get(aliquote.size() - 1);
        if (null != ultima.getGiorniA()) {
            throw new BusinessException(String.format(Constants.ErrorMessages.ULTIMA_ALIQUOTA_NON_APERTA, ultima.getGiorniA()));
        }
    }
}
