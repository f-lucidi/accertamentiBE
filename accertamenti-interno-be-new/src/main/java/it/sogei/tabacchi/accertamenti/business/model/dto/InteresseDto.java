package it.sogei.tabacchi.accertamenti.business.model.dto;

import lombok.Data;

import java.math.BigDecimal;

// riga dell'elenco interessi sul tardato pagamento (tipo Interesse del frontend).
// Date già formattate dd/MM/yyyy: il frontend le mostra così come arrivano
@Data
public class InteresseDto {
    // id del gruppo (decorrenza yyyyMMdd, es. 20270101): il frontend lo rimanda in ModificaInteresseDto per operare sul tasso
    private Integer idGruppo;
    private Long idInteresse;
    private BigDecimal tassoInteresse;
    private String dataInizioValidita;
    private String dataFineValidita;        // null = tasso ancora aperto (2999-12-31)
    private String dataInserimento;
}
