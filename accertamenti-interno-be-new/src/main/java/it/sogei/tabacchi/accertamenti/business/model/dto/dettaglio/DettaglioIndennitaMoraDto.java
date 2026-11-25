package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.util.List;

@Data
public class DettaglioIndennitaMoraDto {

    private List<AliquotaApplicataDto> aliquoteApplicate;
    private List<RigaRiepilogoImportiDto> pagamentiMora;
}
