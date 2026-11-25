package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.util.List;

@Data
public class DettaglioInteresseDifferitoDto {

    private List<InteresseVigenteDto> interessiVigenti;
    private List<RigaRiepilogoImportiDto> dettaglioPagamentiInteressi;
}
