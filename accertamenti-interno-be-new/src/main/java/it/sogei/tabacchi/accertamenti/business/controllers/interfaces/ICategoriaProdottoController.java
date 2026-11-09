package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;

import java.util.List;

public interface ICategoriaProdottoController {

    String getDescrizioneCategoria(String siglaCategoria);
    List <WTBAccertamentiCategorieProd> getAllCategorieProdotto();
}
