package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ScaglioneGiorniRitardo;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.Dependent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// validazioni generiche riutilizzabili: i validator delle singole funzionalità (bean @Dependent) lo iniettano con @Inject.
// Le regole (valida*) non lanciano eccezioni: ogni regola violata aggiunge il suo messaggio alla lista errori ricevuta,
// così un validator raccoglie tutti gli errori di una fase. Solo checkAndThrowIfErrors lancia: chiude la fase
// con una sola BusinessException con tutti i messaggi.
// I metodi che restituiscono boolean dicono se il dato è valido: serve a non controllare un dato che dipende da uno già sbagliato
@Dependent
@Slf4j
@LogMethodInvocation(logParameters = false)
public class CommonValidator {

    // chiude una fase di validazione: se sono stati raccolti errori lancia una sola BusinessException
    // con tutti i messaggi concatenati, altrimenti non fa niente e il chiamante prosegue
    public void checkAndThrowIfErrors(List<String> errori) {
        if (CollectionUtils.isNotEmpty(errori)) {
            throw new BusinessException(String.join(Constants.ErrorMessages.SEPARATORE_ERRORI, errori));
        }
    }

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
    public void validaFiltriGestioneParametri(FiltriGestioneParametriDto filtriGestioneParametriDto, List<String> errori) {
        validaIntervalloDate(filtriGestioneParametriDto.getDataInizioValiditaDa(), filtriGestioneParametriDto.getDataInizioValiditaA(),
                Constants.ErrorMessages.INTERVALLO_DECORRENZA_NON_VALIDO, errori);
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

    // tasso di un parametro (interessi sul tardato pagamento, interessi legali): obbligatorio e maggiore di zero.
    // true se valido: solo allora si può confrontare con altri tassi
    public boolean validaTasso(BigDecimal tasso, List<String> errori) {
        boolean isPresente = validaObbligatorio(tasso, Constants.ErrorMessages.TASSO_INTERESSE_OBBLIGATORIO, errori);
        boolean isPositivo = !isPresente || tasso.compareTo(BigDecimal.ZERO) > 0;
        if (!isPositivo) {
            errori.add(Constants.ErrorMessages.TASSO_INTERESSE_NON_POSITIVO);
        }
        return isPresente && isPositivo;
    }

    // inserimento di un interesse (tardato pagamento o legale): un tasso nuovo non ha id e non può essere cancellato
    public void validaNuovoInteresse(Long idInteresse, Boolean flagCancella, List<String> errori) {
        if (null != idInteresse) {
            errori.add(Constants.ErrorMessages.ID_INTERESSE_NON_AMMESSO);
        }
        if (Boolean.TRUE.equals(flagCancella)) {
            errori.add(Constants.ErrorMessages.CANCELLAZIONE_INTERESSE_NON_AMMESSA);
        }
    }

    // nessuno dei valori è valorizzato (null, vuoto o di soli spazi)
    public void validaAlmenoUnoValorizzato(String messaggio, List<String> errori, String... valori) {
        boolean almenoUnoValorizzato = Arrays.stream(valori).anyMatch(valore -> null != valore && !valore.isBlank());
        if (!almenoUnoValorizzato) {
            errori.add(messaggio);
        }
    }

    // ------------ GRUPPI A SCAGLIONI DI GIORNI DI RITARDO (aliquote di mora, sanzioni) ------------
    // nomeScaglione/nomeScaglioni: Constants.GestioneParametri.NOME_* (minuscoli), finiscono nei messaggi.
    // numero parte da 1, come i riquadri "Aliquota N"/"Sanzione N" del frontend

    // il gruppo salvato (righe con la decorrenza della prima riga salvata della richiesta) esiste ed è ancora modificabile:
    // decorrenza futura oppure almeno una riga inserita oggi. dateInserimento = una per riga salvata, vuota se il gruppo non c'è
    public void  validaGruppoModificabile(List<LocalDate> dateInserimento, LocalDate dataInizioGruppo,
                                         String messaggioNonTrovato, String messaggioNonModificabile, List<String> errori) {
        LocalDate oggi = LocalDate.now();
        if (dateInserimento.isEmpty()) {
            errori.add(messaggioNonTrovato);
        } else if (!dataInizioGruppo.isAfter(oggi) && !dateInserimento.contains(oggi)) {
            errori.add(String.format(messaggioNonModificabile, Utility.formattaData(dataInizioGruppo)));
        }
    }

    // in modifica arrivano tutte e sole le righe già salvate del gruppo (ognuna una volta):
    // se ne mancasse una, cambiando la decorrenza resterebbe con la data vecchia
    public void validaGruppoCompleto(List<Long> idRichiesti, List<Long> idSalvati, String messaggio, List<String> errori) {
        if (idRichiesti.size() != idSalvati.size() || !idRichiesti.containsAll(idSalvati)) {
            errori.add(messaggio);
        }
    }

    // "firma" di una riga per il confronto tra gruppi: valore senza zeri finali (2.5 e 2.50 uguali) e giorni
    public String firmaScaglione(BigDecimal valore, Integer giorniDa, Integer giorniA) {
        return valore.stripTrailingZeros().toPlainString() + "|" + giorniDa + "|" + giorniA;
    }

    // in inserimento il nuovo gruppo non può ripetere il precedente: stesse righe nello stesso ordine.
    // firmePrecedenti vuota = nessun gruppo precedente, nessun vincolo
    public void validaGruppoDiversoDaPrecedente(List<String> firmeNuove, List<String> firmePrecedenti, String messaggio,
                                                List<String> errori) {
        if (!firmePrecedenti.isEmpty() && firmeNuove.equals(firmePrecedenti)) {
            errori.add(messaggio);
        }
    }

    // valore > 0, giorni da obbligatori, giorni a (se c'è) maggiore dei giorni da.
    // true se i giorni sono validi: solo allora si può controllare la continuità
    public boolean validaValoriScaglione(BigDecimal valore, ScaglioneGiorniRitardo scaglione, String nomeScaglione, int numero,
                                         List<String> errori) {
        String nome = StringUtils.capitalize(nomeScaglione);
        if (validaObbligatorio(valore, String.format(Constants.ErrorMessages.VALORE_SCAGLIONE_OBBLIGATORIO, nome, numero), errori)
                && valore.compareTo(BigDecimal.ZERO) <= 0) {
            errori.add(String.format(Constants.ErrorMessages.VALORE_SCAGLIONE_NON_POSITIVO, nome, numero));
        }

        boolean isGiorniDaPresente = validaObbligatorio(scaglione.getGiorniDa(),
                String.format(Constants.ErrorMessages.GIORNI_DA_OBBLIGATORIO, nome, numero), errori);
        // senza giorni da non c'è niente con cui confrontare i giorni a
        boolean isGiorniAValido = !isGiorniDaPresente || null == scaglione.getGiorniA() || scaglione.getGiorniA() > scaglione.getGiorniDa();
        if (!isGiorniAValido) {
            errori.add(String.format(Constants.ErrorMessages.GIORNI_A_NON_VALIDO, nome, numero));
        }
        return isGiorniDaPresente && isGiorniAValido;
    }

    // gli scaglioni (tutti con giorni validi) arrivano in ordine (il frontend calcola "giorni da" dalla riga precedente):
    // il primo parte da 1, ognuno riprende dal giorno dopo il precedente, solo l'ultimo è aperto
    public void validaContinuitaGiorni(List<? extends ScaglioneGiorniRitardo> scaglioni, String nomeScaglione, String nomeScaglioni,
                                       List<String> errori) {
        if (!Constants.GestioneParametri.PRIMO_GIORNO_RITARDO.equals(scaglioni.get(0).getGiorniDa())) {
            errori.add(String.format(Constants.ErrorMessages.PRIMO_SCAGLIONE_NON_DA_PRIMO_GIORNO,
                    nomeScaglione, Constants.GestioneParametri.PRIMO_GIORNO_RITARDO));
        }

        for (int indice = 1; indice < scaglioni.size(); indice++) {
            ScaglioneGiorniRitardo precedente = scaglioni.get(indice - 1);
            ScaglioneGiorniRitardo corrente = scaglioni.get(indice);
            if (null == precedente.getGiorniA()) {
                errori.add(String.format(Constants.ErrorMessages.SOLO_ULTIMO_SCAGLIONE_APERTO,
                        StringUtils.capitalize(nomeScaglione), indice, nomeScaglione));
            // equals e non !=: sono Integer, != confronterebbe gli oggetti e non i numeri
            } else if (!corrente.getGiorniDa().equals(precedente.getGiorniA() + 1)) {
                errori.add(String.format(Constants.ErrorMessages.GIORNI_NON_CONSECUTIVI,
                        StringUtils.capitalize(nomeScaglioni), indice, indice + 1));
            }
        }

        ScaglioneGiorniRitardo ultimo = scaglioni.get(scaglioni.size() - 1);
        if (null != ultimo.getGiorniA()) {
            errori.add(String.format(Constants.ErrorMessages.ULTIMO_SCAGLIONE_NON_APERTO, nomeScaglione, ultimo.getGiorniA()));
        }
    }
}
