package it.sogei.tabacchi.accertamenti.business.model.dto;

import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import lombok.Data;

import java.util.List;

@Data
public class ListeFiltriRicercaDto {

    private List<AnniContabiliDto> listaAnniContabili;
    private List<RagioneSocialePIvaDto> listaRagioneSocialePIva;
    private List<TipoPagamentoDto> tipiPagamenti;
}
