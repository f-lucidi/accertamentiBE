package it.sogei.tabacchi.accertamenti.business.controllers.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;

public interface IGestioneInteressiController {

    RisultatoPaginatoDto<InteresseDto> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDto, Integer pagina, Integer elementiPerPagina);
}
