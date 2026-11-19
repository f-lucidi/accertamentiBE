package it.sogei.tabacchi.accertamenti.mappers;

import it.sogei.tabacchi.accertamenti.business.model.dto.*;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.*;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "cdi",
        imports = {
                LocalDate.class,
                Objects.class,
                Constants.class,
                Utility.class
        }
)
public interface CustomMapper {

    CategoriaProdottoDto toCategoriaProdottoDto(WTBAccertamentiCategorieProd categoria);

    List<CategoriaProdottoDto> toCategorieProdottoDto(List<WTBAccertamentiCategorieProd> categorie);

    @Mapping(target = "partitaIva", source = "cfPiva")
    @Mapping(target = "ragioneSociale", source = "denominazione")
    RagioneSocialePIvaDto toRagioneSocialePIvaDto(WTBAccertamentiSoggettiV soggetto);

    List<RagioneSocialePIvaDto> toRagioneSocialePIvaDto(List<WTBAccertamentiSoggettiV> soggetti);

    TipoPagamentoDto toTipoPagamentoDto(WTBAccertamentiTipoPagamento tipoPagamento);

    List<TipoPagamentoDto> toTipiPagamentoDto(List<WTBAccertamentiTipoPagamento> tipiPagamento);

    // importi "dovuti" della posizione; ragioneSociale, dataUltimoVersamento e interessiLegali
    // stanno su altre tabelle: li valorizza il controller
    @Mapping(target = "idPosizione", source = "id")
    @Mapping(target = "partitaIva", source = "cfSogObbligato")
    @Mapping(target = "periodoContabile",
            expression = "java(Utility.formattaPeriodoContabile(posizione.getTipoPeriodo(), posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina()))")
    @Mapping(target = "totaleImpostaDovuta", source = "impostaTotaleDovuto", defaultValue = "0")
    @Mapping(target = "statoPosizione", source = "statoPagamento.descrizione")
    @Mapping(target = "totaleImpostaVersata", source = "impostaTotalePagato", defaultValue = "0")
    @Mapping(target = "indennitaMora", source = "moraTotaleDovuto", defaultValue = "0")
    @Mapping(target = "interessiTardatoPagamento", source = "interesseTardatoPagamentoTotaleDovuto", defaultValue = "0")
    @Mapping(target = "sanzione", source = "sanzioneTotaleDovuto", defaultValue = "0")
    @Mapping(target = "ragioneSociale", ignore = true)
    @Mapping(target = "dataUltimoVersamento", ignore = true)
    @Mapping(target = "interessiLegali", ignore = true)
    PosizioneDto toPosizioneDto(WTBAccertamentiElencoPosizioni posizione);

    // riga dell'elenco aliquote: date formattate dd/MM/yyyy, fine validità null se il gruppo è aperto.
    // idGruppo (calcolato dall'entity al caricamento), aliquota, giorniRitardoDa e giorniRitardoA hanno lo stesso nome:
    // MapStruct li copia da solo
    @Mapping(target = "idAliquota", source = "id")
    @Mapping(target = "dataInizioValidita", expression = "java(Utility.formattaData(aliquotaMora.getDataInizioVal()))")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.formattaDataFineValidita(aliquotaMora.getDataFineVal()))")
    @Mapping(target = "dataInserimento", expression = "java(Utility.formattaData(aliquotaMora.getDtInserimento()))")
    AliquotaDto toAliquotaDto(WTBAccertamentiAliquotaMora aliquotaMora);

    List<AliquotaDto> toAliquoteDto(List<WTBAccertamentiAliquotaMora> aliquoteMora);

    // aliquota da inserire: copia valori e giorni, la decorrenza del gruppo diventa l'inizio validità.
    // id dalla sequence; fine validità, flag e dati di inserimento li imposta il controller
    @Mapping(target = "aliquota", source = "aliquotaModifica.valoreAliquota")
    @Mapping(target = "giorniRitardoDa", source = "aliquotaModifica.giorniDa")
    @Mapping(target = "giorniRitardoA", source = "aliquotaModifica.giorniA")
    @Mapping(target = "dataInizioVal", source = "dataDecorrenza")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataFineVal", ignore = true)
    @Mapping(target = "flagCancella", ignore = true)
    @Mapping(target = "cfInserimento", ignore = true)
    @Mapping(target = "dtInserimento", ignore = true)
    @Mapping(target = "dataModifica", ignore = true)
    @Mapping(target = "idGruppo", ignore = true)
    WTBAccertamentiAliquotaMora toAliquotaMora(AliquotaModificaDto aliquotaModifica, LocalDate dataDecorrenza);

    // riga dell'elenco sanzioni: date formattate dd/MM/yyyy, fine validità null se il gruppo è aperto,
    // moltiplicatore 1/0 del DB -> Boolean. idGruppo, sanzione, giorniRitardoDa e giorniRitardoA hanno lo stesso nome
    @Mapping(target = "idSanzione", source = "id")
    @Mapping(target = "moltiplicatoPerGiorni",
            expression = "java(Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_SI.equals(sanzione.getMoltiplicatoPerGiorniRitardo()))")
    @Mapping(target = "dataInizioValidita", expression = "java(Utility.formattaData(sanzione.getDataInizioVal()))")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.formattaDataFineValidita(sanzione.getDataFineVal()))")
    @Mapping(target = "dataInserimento", expression = "java(Utility.formattaData(sanzione.getDtInserimento()))")
    SanzioneDto toSanzioneDto(WTBAccertamentiSanzioni sanzione);

    List<SanzioneDto> toSanzioniDto(List<WTBAccertamentiSanzioni> sanzioni);

    // sanzione da inserire: copia valori, giorni e moltiplicatore, la decorrenza del gruppo diventa l'inizio validità.
    // id dalla sequence; fine validità e dati di inserimento li imposta il controller
    @Mapping(target = "sanzione", source = "sanzioneModifica.valoreSanzione")
    @Mapping(target = "moltiplicatoPerGiorniRitardo",
            expression = "java(Utility.toMoltiplicatoPerGiorniRitardo(sanzioneModifica.getMoltiplicatoPerGiorni()))")
    @Mapping(target = "giorniRitardoDa", source = "sanzioneModifica.giorniDa")
    @Mapping(target = "giorniRitardoA", source = "sanzioneModifica.giorniA")
    @Mapping(target = "dataInizioVal", source = "dataDecorrenza")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataFineVal", ignore = true)
    @Mapping(target = "cfInserimento", ignore = true)
    @Mapping(target = "dtInserimento", ignore = true)
    @Mapping(target = "dtModifica", ignore = true)
    @Mapping(target = "idGruppo", ignore = true)
    WTBAccertamentiSanzioni toSanzione(SanzioneModificaDto sanzioneModifica, LocalDate dataDecorrenza);

    // riga dell'elenco interessi sul tardato pagamento (idGruppo e tassoInteresse copiati da soli)
    @Mapping(target = "idInteresse", source = "id")
    @Mapping(target = "dataInizioValidita", expression = "java(Utility.formattaData(interesseRitPagam.getDataInizioVal()))")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.formattaDataFineValidita(interesseRitPagam.getDataFineVal()))")
    @Mapping(target = "dataInserimento", expression = "java(Utility.formattaData(interesseRitPagam.getDtInserimento()))")
    InteresseDto toInteresseDto(WTBAccertamentiInteresseRitPagam interesseRitPagam);

    List<InteresseDto> toInteressiDto(List<WTBAccertamentiInteresseRitPagam> interessiRitPagam);

    // tasso da inserire
    @Mapping(target = "dataInizioVal", source = "dataDecorrenza")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataFineVal", ignore = true)
    @Mapping(target = "flagCancella", ignore = true)
    @Mapping(target = "cfInserimento", ignore = true)
    @Mapping(target = "dtInserimento", ignore = true)
    @Mapping(target = "dataModifica", ignore = true)
    // idGruppo dell'entity si calcola solo al caricamento dal DB: quello del DTO (gruppo da modificare) non va copiato
    @Mapping(target = "idGruppo", ignore = true)
    WTBAccertamentiInteresseRitPagam toInteresseRitPagam(ModificaInteresseDto modificaInteresseDto);

    // riga dell'elenco interessi legali
    @Mapping(target = "idInteresse", source = "id")
    @Mapping(target = "anno", source = "annoValid")
    @Mapping(target = "tassoInteresseLegale", source = "tassoInteresse")
    @Mapping(target = "dataInserimento", expression = "java(Utility.formattaData(interesseLegale.getDtInserimento()))")
    InteresseLegaleDto toInteresseLegaleDto(WTBAccertamentiInteresseLegale interesseLegale);

    List<InteresseLegaleDto> toInteressiLegaliDto(List<WTBAccertamentiInteresseLegale> interessiLegali);

    // tasso legale da inserire: anno e tasso; i campi di inserimento li imposta il controller
    @Mapping(target = "annoValid", source = "anno")
    @Mapping(target = "tassoInteresse", source = "tassoInteresseLegale")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "flagCancella", ignore = true)
    @Mapping(target = "cfInserimento", ignore = true)
    @Mapping(target = "dtInserimento", ignore = true)
    @Mapping(target = "dataModifica", ignore = true)
    WTBAccertamentiInteresseLegale toInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto);

    /* ESEMPIO  */

//    @Mapping(target = "id",                 source = "id")
//    @Mapping(target = "valoreSanzione",     source = "sanzione")
//    @Mapping(target = "giorniRitardoDa",    source = "giorniRitardoDa")
//    @Mapping(target = "giorniRitardoA",     source = "giorniRitardoA")
//    @Mapping(target = "dataInizioValidita", source = "dataInizioVal")
//    @Mapping(target = "dataFineValidita",   source = "dataFineVal")
//    @Mapping(target = "cfInserimento",      source = "cfInserimento")
//    @Mapping(target = "dataInserimento",    source = "dtInserimento")
//    @Mapping(target = "dataModifica",       source = "dataModifica")
//    @Mapping(target = "isMoltiplicatoPerGiorniRitardo", expression = "java(Objects.equals(sanzione.getMoltiplicatoPerGiorniRitardo(), 1))")
//    @Mapping(target = "isEditabile",        expression = "java(isEditabileSanzione(sanzione))")
//    SanzioneDto toDto(WTbAccertamentiSanzioni sanzione);
//    List<SanzioneDto> toListSanzione(List<WTbAccertamentiSanzioni> sanzioneList);


//
//    default Boolean isEditabileSanzione(WTbAccertamentiSanzioni sanzione) {
//        return Objects.equals(sanzione.getDtInserimento(), LocalDate.now()) || sanzione.getDataInizioVal().isAfter(LocalDate.now());
//    }
}
