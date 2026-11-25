package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import lombok.Data;

// risposta di GET /dettaglio/{idPosizione}: i blocchi null non vanno mostrati
@Data
public class DettaglioDto {

    private DettaglioPagamentoDto dettaglioPagamento;
    private RisultatoPaginatoDto<DettaglioImpostaDovutaDto> dettaglioImpostaDovuta;
    private DettaglioIndennitaMoraDto dettaglioIndennitaMora;        // null se moraTotaleDovuto = 0
    private DettaglioInteresseDifferitoDto interesseDifferito;       // null se interesse differito = 0
    private DettaglioInteressiLegaliDto dettaglioInteressiLegali;    // null se la posizione è pagata
    private DettaglioSanzioniDto dettaglioSanzioni;                  // null se sanzione = 0
}
