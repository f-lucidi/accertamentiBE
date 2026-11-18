package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneParametriController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneParametriDao;
import it.sogei.tabacchi.accertamenti.business.model.enums.TipoParametro;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class GestioneParametriController implements IGestioneParametriController {

    @Inject
    private IGestioneParametriDao gestioneParametriDao;

    // ------------ ANNI PER IL BOX DI RICERCA ------------

    @Override
    public List<Integer> findAnni(TipoParametro tipoParametro) {
        return switch (tipoParametro) {
            case ALIQUOTE_MORA -> gestioneParametriDao.findAnniAliquoteMora();
            case SANZIONI -> gestioneParametriDao.findAnniSanzioni();
            case INTERESSI_LEGALI -> gestioneParametriDao.findAnniInteressiLegali();
            case INTERESSI_RIT_PAGAM -> gestioneParametriDao.findAnniInteressiRitPagam();
        };
    }
}
