package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private String username;
    private UfficioDto ufficio;
    private Integer sessionTimeout;
    private Integer sessionExpiring;
}
