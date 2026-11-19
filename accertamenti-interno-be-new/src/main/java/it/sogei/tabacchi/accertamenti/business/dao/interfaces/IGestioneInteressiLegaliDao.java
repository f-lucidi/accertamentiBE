package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;

import java.util.List;

public interface IGestioneInteressiLegaliDao {

    // unico filtro: anno (null = tutti gli anni); gli interessi cancellati non escono mai
    List<WTBAccertamentiInteresseLegale> findInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDto, int pagina, int elementiPerPagina);

    Long countInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDto);

    // interessi non cancellati di quell'anno: > 0 = interesse già presente nell'anno
    Long countInteressiLegaliByAnno(Integer anno);

    // l'interesse non cancellato di quell'anno, null se non esiste
    WTBAccertamentiInteresseLegale findInteresseLegaleByAnno(Integer anno);

    // anni con un interesse non cancellato, dal più vecchio (per cercare il primo anno mancante)
    List<Integer> findAnniInteressiLegaliCrescenti();

    // null se non esiste (anche le righe cancellate: il controllo lo fa il validator)
    WTBAccertamentiInteresseLegale findInteresseLegaleById(Long idInteresse);

    void insertInteresseLegale(WTBAccertamentiInteresseLegale interesseLegale);

    void updateInteresseLegale(WTBAccertamentiInteresseLegale interesseLegale);
}
