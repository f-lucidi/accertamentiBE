package it.sogei.tabacchi.accertamenti.mappers;

import it.sogei.tabacchi.accertamenti.business.model.dto.*;
import it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio.*;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.*;
import it.sogei.tabacchi.accertamenti.business.model.enums.StatoRendicontazione;
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
                Utility.class,
                StatoRendicontazione.class
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

    // riga dell'elenco aliquote: date LocalDate (yyyy-MM-dd nel JSON), fine validità null se il gruppo è aperto.
    // aliquota, giorniRitardoDa e giorniRitardoA hanno lo stesso nome: MapStruct li copia da solo
    @Mapping(target = "idAliquota", source = "id")
    @Mapping(target = "idGruppo", expression = "java(Utility.toIdGruppo(aliquotaMora.getDataInizioVal()))")
    @Mapping(target = "dataInizioValidita", source = "dataInizioVal")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.fineValiditaVisibile(aliquotaMora.getDataFineVal()))")
    @Mapping(target = "dataInserimento", source = "dtInserimento")
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
    WTBAccertamentiAliquotaMora toAliquotaMora(AliquotaModificaDto aliquotaModifica, LocalDate dataDecorrenza);

    // riga dell'elenco sanzioni: date LocalDate (yyyy-MM-dd nel JSON), fine validità null se il gruppo è aperto,
    // moltiplicatore 1/0 del DB -> Boolean. sanzione, giorniRitardoDa e giorniRitardoA hanno lo stesso nome
    @Mapping(target = "idSanzione", source = "id")
    @Mapping(target = "idGruppo", expression = "java(Utility.toIdGruppo(sanzione.getDataInizioVal()))")
    @Mapping(target = "moltiplicatoPerGiorni",
            expression = "java(Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_SI.equals(sanzione.getMoltiplicatoPerGiorniRitardo()))")
    @Mapping(target = "dataInizioValidita", source = "dataInizioVal")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.fineValiditaVisibile(sanzione.getDataFineVal()))")
    @Mapping(target = "dataInserimento", source = "dtInserimento")
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
    WTBAccertamentiSanzioni toSanzione(SanzioneModificaDto sanzioneModifica, LocalDate dataDecorrenza);

    // riga dell'elenco interessi sul tardato pagamento (tassoInteresse copiato da solo)
    @Mapping(target = "idInteresse", source = "id")
    @Mapping(target = "dataInizioValidita", source = "dataInizioVal")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.fineValiditaVisibile(interesseRitPagam.getDataFineVal()))")
    @Mapping(target = "dataInserimento", source = "dtInserimento")
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
    WTBAccertamentiInteresseRitPagam toInteresseRitPagam(ModificaInteresseDto modificaInteresseDto);

    // riga dell'elenco interessi legali
    @Mapping(target = "idInteresse", source = "id")
    @Mapping(target = "anno", source = "annoValid")
    @Mapping(target = "tassoInteresseLegale", source = "tassoInteresse")
    @Mapping(target = "dataInserimento", source = "dtInserimento")
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

    // blocco in testa al dettaglio: ragione sociale, categoria e importo totale
    // stanno su altre tabelle o vanno calcolati: li valorizza il controller.
    // Stato rendicontazione da FLAG_CANCELLA: true = cancellata; false o null = trasmessa
    @Mapping(target = "idPosizione", source = "id")
    @Mapping(target = "piva", source = "cfSogObbligato")
    @Mapping(target = "statoPosizione", source = "statoPagamento.descrizione")
    @Mapping(target = "periodoContabile",
            expression = "java(Utility.formattaPeriodoContabile(posizione.getTipoPeriodo(), posizione.getAnno(), posizione.getMese(), posizione.getNumQuindicina()))")
    @Mapping(target = "dataScadenza", source = "scadenza")
    @Mapping(target = "ragioneSociale", ignore = true)
    @Mapping(target = "categoriaProdotto", ignore = true)
    @Mapping(target = "statoRendicontazione", expression = "java(Boolean.TRUE.equals(posizione.getFlagCancella()) "
            + "? StatoRendicontazione.CANCELLATA.getDescrizione() : StatoRendicontazione.TRASMESSA.getDescrizione())")
    @Mapping(target = "importoTotaleDaPagare", ignore = true)
    DettaglioPagamentoDto toDettaglioPagamentoDto(WTBAccertamentiElencoPosizioni posizione);

    // aliquota di mora vigente; giorniRitardo è quello della posizione (secondo parametro, stesso nome del campo)
    @Mapping(target = "idAliquota", source = "aliquotaMora.id")
    @Mapping(target = "aliquota", source = "aliquotaMora.aliquota")
    @Mapping(target = "dataInizioValidita", source = "aliquotaMora.dataInizioVal")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.fineValiditaVisibile(aliquotaMora.getDataFineVal()))")
    AliquotaApplicataDto toAliquotaApplicataDto(WTBAccertamentiAliquotaMora aliquotaMora, Integer giorniRitardo);

    // sanzione vigente: come l'aliquota di mora
    @Mapping(target = "idSanzione", source = "sanzione.id")
    @Mapping(target = "sanzione", source = "sanzione.sanzione")
    @Mapping(target = "dataInizioValidita", source = "sanzione.dataInizioVal")
    @Mapping(target = "dataFineValidita", expression = "java(Utility.fineValiditaVisibile(sanzione.getDataFineVal()))")
    SanzioneApplicataDto toSanzioneApplicataDto(WTBAccertamentiSanzioni sanzione, Integer giorniRitardo);

    // tasso sul tardato pagamento vigente alla scadenza
    @Mapping(target = "interessiVigenti", source = "tassoInteresse")
    @Mapping(target = "inizioValiditaInteresse", source = "dataInizioVal")
    @Mapping(target = "fineValiditaInteresse", expression = "java(Utility.fineValiditaVisibile(interesseRitPagam.getDataFineVal()))")
    InteresseVigenteDto toInteresseVigenteDto(WTBAccertamentiInteresseRitPagam interesseRitPagam);

    // tasso di interesse legale di un anno
    @Mapping(target = "anno", source = "annoValid")
    @Mapping(target = "tassoInteresseLegale", source = "tassoInteresse")
    TassoInteresseLegaleDto toTassoInteresseLegaleDto(WTBAccertamentiInteresseLegale interesseLegale);

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
