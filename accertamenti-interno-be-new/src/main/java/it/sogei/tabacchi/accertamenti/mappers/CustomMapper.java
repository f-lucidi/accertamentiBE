package it.sogei.tabacchi.accertamenti.mappers;

import it.sogei.tabacchi.accertamenti.business.model.dto.RagioneSocialePIvaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.TipoPagamentoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiCategorieProd;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiSoggettiV;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTbAccertamentiTipoPagamento;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "cdi",
        imports = {
                LocalDate.class,
                Objects.class,
                Constants.class
        }
)
public interface CustomMapper {

    CategoriaProdottoDto toCategoriaProdottoDto(WTBAccertamentiCategorieProd categoria);

    List<CategoriaProdottoDto> toCategorieProdottoDto(List<WTBAccertamentiCategorieProd> categorie);

    @Mapping(target = "partitaIva", source = "cfPiva")
    @Mapping(target = "ragioneSociale", source = "denominazione")
    RagioneSocialePIvaDto toRagioneSocialePIvaDto(WTbAccertamentiSoggettiV soggetto);

    List<RagioneSocialePIvaDto> toRagioneSocialePIvaDto(List<WTbAccertamentiSoggettiV> soggetti);

    TipoPagamentoDto toTipoPagamentoDto(WTbAccertamentiTipoPagamento tipoPagamento);

    List<TipoPagamentoDto> toTipiPagamentoDto(List<WTbAccertamentiTipoPagamento> tipiPagamento);

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
