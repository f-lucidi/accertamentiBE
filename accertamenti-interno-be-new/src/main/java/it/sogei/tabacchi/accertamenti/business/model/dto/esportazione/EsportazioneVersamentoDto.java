package it.sogei.tabacchi.accertamenti.business.model.dto.esportazione;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// un versamento della posizione: il tipo dice in quali colonne dell'Excel va
@Data
public class EsportazioneVersamentoDto {

    private String tipo;               // IMPOSTA, MORA, INTERESSI, INTERESSI_LEGALI, SANZIONI
    private BigDecimal importo;
    private LocalDate dataPagamento;
    private Integer giorniRitardo;     // valorizzato solo per IMPOSTA
}
