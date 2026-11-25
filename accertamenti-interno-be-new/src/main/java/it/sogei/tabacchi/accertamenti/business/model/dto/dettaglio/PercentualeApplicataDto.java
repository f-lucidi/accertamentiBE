package it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio;

import lombok.Data;

import java.time.LocalDate;

// campi comuni ad aliquote di mora e sanzioni applicate (AliquotaApplicataDto, SanzioneApplicataDto)
@Data
public class PercentualeApplicataDto {

    private Integer giorniRitardo;
    private LocalDate dataInizioValidita;          // yyyy-MM-dd
    private LocalDate dataFineValidita;            // null se ancora in vigore
}
