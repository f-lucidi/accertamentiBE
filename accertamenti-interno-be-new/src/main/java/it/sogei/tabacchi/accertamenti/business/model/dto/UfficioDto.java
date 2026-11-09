package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UfficioDto {

    private String codiceUfficio;
    private String descrizione;

}
