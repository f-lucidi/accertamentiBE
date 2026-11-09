package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.ICategoriaProdottoController;
import it.sogei.tabacchi.accertamenti.business.dao.impl.CategoriaProdottoDao;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.enterprise.context.RequestScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

// requestScoped indica che l'istanza della classe sarà creata per ogni richiesta HTTP.
@RequestScoped
// @Slf4j è un'annotazione di Lombok che genera automaticamente un logger per la classe.
@Slf4j
// annotazione per loggare l'invocazione dei metodi della classe, senza loggare i parametri
@LogMethodInvocation(logParameters = false)
public class CategoriaProdottoController implements ICategoriaProdottoController {

    @Override
    public String getDescrizioneCategoria(String siglaCategoria) {
        if (siglaCategoria == null || siglaCategoria.isEmpty()) {
            throw new BusinessException("La sigla della categoria non può essere nulla o vuota.");
        }
        WTBAccertamentiCategorieProd categoria = new CategoriaProdottoDao().findCategoriaBySigla(siglaCategoria);
        return categoria != null ? categoria.getDescrizione() : Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE;
    }

    @Override
    public List<WTBAccertamentiCategorieProd> getAllCategorieProdotto() {
        return new CategoriaProdottoDao().findAllCategorieProdotto();
    }

}
