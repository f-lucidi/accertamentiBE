package it.sogei.tabacchi.accertamenti.business.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// chiave di W_TB_ACCERTAMENTI_SOGGETTI_V: nella vista CF_PIVA da solo non è univoco (lo stesso soggetto
// compare su più categorie). Con @Id sul solo CF_PIVA Hibernate restituirebbe la stessa istanza per tutte
// le righe con quel CF, perdendo i dati delle altre righe.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WTBAccertamentiSoggettiVId implements Serializable {

    private String cfPiva;
    private String denominazione;
    private String categoria;
}
