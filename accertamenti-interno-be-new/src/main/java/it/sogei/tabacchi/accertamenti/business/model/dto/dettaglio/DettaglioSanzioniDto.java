package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.util.List;

@Data
public class DettaglioSanzioniDto {

    private List<SanzioneApplicataDto> sanzioniApplicate;
    private List<RigaRiepilogoImportiDto> dettaglioPagamentiSanzioni;
}
