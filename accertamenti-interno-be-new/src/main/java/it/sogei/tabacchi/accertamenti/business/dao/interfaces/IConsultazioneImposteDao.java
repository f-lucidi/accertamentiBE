package it.sogei.tabacchi.accertamenti.business.dao.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;

import java.util.List;

public interface IConsultazioneImposteDao {

    List<WTBAccertamentiCategorieProd> findCategorieProdotti();

    WTBAccertamentiCategorieProd findCategoriaBySigla(String siglaCategoria);

    List<Integer> findAnniContabili(String categoriaProdotto);
}
