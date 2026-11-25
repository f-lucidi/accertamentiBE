package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.util.List;

@Data
public class DettaglioInteressiLegaliDto {

    private List<TassoInteresseLegaleDto> valoriInteressi;
    private List<RigaRiepilogoImportiDto> pagamentiInteressiLegali;
}
