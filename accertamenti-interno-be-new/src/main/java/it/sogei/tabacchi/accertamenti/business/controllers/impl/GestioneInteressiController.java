package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneInteressiController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneInteressiDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.validation.CommonValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiController implements IGestioneInteressiController {

    @Inject
    private IGestioneInteressiDao gestioneInteressiDao;

    @Inject
    private CommonValidator commonValidator;

    @Inject
    private CustomMapper customMapper;

    // ------------ ELENCO INTERESSI ------------

    @Override
    public RisultatoPaginatoDto<InteresseDto> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDtoRichiesto,
                                                            Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // body JSON null = nessun filtro, come {} (tutti gli interessi con flagCancella = false)
        FiltriGestioneParametriDto filtriGestioneParametriDto = Utility.getOrDefault(filtriGestioneParametriDtoRichiesto, new FiltriGestioneParametriDto());
        commonValidator.validaFiltriGestioneParametri(filtriGestioneParametriDto);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiInteresseRitPagam> interessi = gestioneInteressiDao.findInteressi(filtriGestioneParametriDto, pagina, elementiPerPagina);
        Long totaleElementi = gestioneInteressiDao.countInteressi(filtriGestioneParametriDto);
        return RisultatoPaginatoDto.of(customMapper.toInteressiDto(interessi), totaleElementi, pagina, elementiPerPagina);
    }
}
