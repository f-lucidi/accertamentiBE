package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoParametro;

import java.util.List;

public interface IGestioneParametriController {

    // anni distinti del parametro, dal più recente; lista vuota se non ci sono righe
    List<Integer> findAnni(TipoParametro tipoParametro);
}
