package it.sogei.tabacchi.accertamenti.business.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RisultatoPaginatoDto<T> {

    private List<T> data;
    private Long numeroTotaleElementi;
    private Long numeroTotalePagine;
    private Boolean isFirstPage;
    private Boolean isLastPage;

    public static <T> RisultatoPaginatoDto<T> of(List<T> data, Long numeroTotaleElementi, Long numeroTotalePagine, Boolean isFirstPage, Boolean isLastPage) {
        long totalElements = null != numeroTotaleElementi ? numeroTotaleElementi : 0L;
        long totalPages = null != numeroTotalePagine ? numeroTotalePagine : 0L;
        Boolean firstPage = null != isFirstPage ? isFirstPage : false;
        Boolean lastPage = null != isLastPage ? isLastPage : false;
        return new RisultatoPaginatoDto<>(data, totalElements, totalPages, firstPage, lastPage);
    }

    // calcola totale pagine e flag prima/ultima pagina a partire dal totale degli elementi
    // (pagina ed elementiPerPagina devono essere già normalizzati, con elementiPerPagina >= 1)
    public static <T> RisultatoPaginatoDto<T> of(List<T> data, long numeroTotaleElementi, int pagina, int elementiPerPagina) {
        long totalePagine = (numeroTotaleElementi + elementiPerPagina - 1) / elementiPerPagina;
        return of(data, numeroTotaleElementi, totalePagine, 1 == pagina, pagina >= totalePagine);
    }

}
