package it.sogei.tabacchi.accertamenti.mappers;

import it.sogei.tabacchi.accertamenti.business.model.dto.PosizioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RagioneSocialePIvaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiElencoPosizioni;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiTipoPagamento;
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
