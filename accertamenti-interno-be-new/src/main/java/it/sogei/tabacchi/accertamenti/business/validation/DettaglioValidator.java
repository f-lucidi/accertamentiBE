package it.sogei.tabacchi.accertamenti.business.validation;

import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * Validazioni della funzionalità "Dettaglio posizione".
 * Stesso schema di {@link ConsultazioneImposteValidator}: ogni metodo raccoglie gli errori
 * e, se ce ne sono, lancia la BusinessException tramite {@code commonValidator.checkAndThrowIfErrors}.
 */
@Dependent
@LogMethodInvocation(logParameters = false)
public class DettaglioValidator {

    @Inject
    private CommonValidator commonValidator;

    // id della posizione (dal path) obbligatorio
    public void validaIdPosizione(Long idPosizione) {
        List<String> errori = new ArrayList<>();
        commonValidator.validaObbligatorio(idPosizione, Constants.ErrorMessages.ID_POSIZIONE_OBBLIGATORIO, errori);
        commonValidator.checkAndThrowIfErrors(errori);
    }
}
