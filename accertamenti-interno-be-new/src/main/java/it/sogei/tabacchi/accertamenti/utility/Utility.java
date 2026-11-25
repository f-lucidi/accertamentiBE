package it.sogei.tabacchi.accertamenti.utility;

import it.sogei.tabacchi.accertamenti.business.model.dto.PeriodoContabileDto;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import jakarta.ws.rs.container.ResourceInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Utility {

    public static boolean isAnnotationPresent(ResourceInfo resourceInfo, Class<? extends Annotation> annotationClass) {
        try {
            Class<?> resourceClass = resourceInfo.getResourceClass();
            Method resourceMethod = resourceInfo.getResourceMethod();

            return resourceMethod.isAnnotationPresent(annotationClass) || resourceClass.isAnnotationPresent(annotationClass);
        } catch (Exception e) {
            return false;
        }
    }

    public static <T> T getOrDefault(T obj, T defaultValue) {
        return Objects.isNull(obj) ? defaultValue : obj;
    }

    // numero di pagina valido (parte da 1): se nullo o < 1 restituisce la prima pagina
    public static int normalizzaPagina(Integer pagina) {
        return (null != pagina && pagina >= 1) ? pagina : 1;
    }

    // elementi per pagina validi: se nullo o < 1 restituisce il default, altrimenti applica il massimo consentito
    public static int normalizzaElementiPerPagina(Integer elementiPerPagina) {
        return (null != elementiPerPagina && elementiPerPagina >= 1)
                ? Math.min(elementiPerPagina, Constants.MAX_ELEMENTS_PER_PAGE)
                : Constants.DEFAULT_ELEMENTS_PER_PAGE;
    }

    /**
     * Interpreta il filtro "periodo contabile". Formati accettati:
     * <ul>
     *   <li>{@code T1}..{@code T4}: trimestre</li>
     *   <li>{@code M1}..{@code M12}: mese</li>
     *   <li>{@code Q1}..{@code Q24}: quindicina. Per il mese m, {@code Q(2m-1)} è la prima quindicina
     *       (NUM_QUINDICINA = 1) e {@code Q(2m)} la seconda (NUM_QUINDICINA = 2)</li>
     *   <li>vuoto o {@code TUTTE}: nessun filtro</li>
     * </ul>
     *
     * @return il filtro, oppure {@code null} se non va applicato nessun filtro
     * @throws BusinessException se il formato non è valido
     */
    public static PeriodoContabileDto parsePeriodoContabile(String valore) {
        if (null == valore || valore.isBlank() || Constants.Periodo.FILTRO_TUTTE.equalsIgnoreCase(valore.trim())) {
            return null;
        } else {
            String value = valore.trim().toUpperCase();
            // il controllo del formato è tutto qui: dopo questo punto il valore è sicuramente T1..T4, M1..M12 o Q1..Q24
            if (!Constants.Periodo.CODICI_VALIDI.contains(value)) {
                throw new BusinessException(String.format(Constants.ErrorMessages.PERIODO_CONTABILE_NON_VALIDO, valore));
            } else {
                // trimestri: l'elenco dei 3 mesi che li compongono
                List<Integer> mesiTrimestre = switch (value) {
                    case "T1" -> List.of(1, 2, 3);      // primo trimestre
                    case "T2" -> List.of(4, 5, 6);      // secondo trimestre
                    case "T3" -> List.of(7, 8, 9);      // terzo trimestre
                    case "T4" -> List.of(10, 11, 12);   // quarto trimestre
                    default -> null;
                };
                if (null != mesiTrimestre) {
                    return new PeriodoContabileDto(mesiTrimestre, null);
                }
                // mese (M1..M12): l'elenco con quel solo mese
                if (value.startsWith("M")) {
                    return new PeriodoContabileDto(List.of(Integer.parseInt(value.substring(1))), null);
                }
                // tutto ciò che resta è una quindicina (Q1..Q24)
                int quindicina = Integer.parseInt(value.substring(1));
                // ogni mese ha 2 quindicine, quindi il mese è (n + 1) / 2 con divisione intera: Q1,Q2 -> 1; Q3,Q4 -> 2; ... Q23,Q24 -> 12
                // dispari = prima quindicina del mese, pari = seconda
                return new PeriodoContabileDto(List.of((quindicina + 1) / 2),
                        1 == quindicina % 2 ? Constants.Periodo.PRIMA_QUINDICINA : Constants.Periodo.SECONDA_QUINDICINA);
            }
        }
    }

    // stringa vuota o di soli spazi -> null (il frontend invia "" per i filtri non compilati)
    public static String nullIfBlank(String valore) {
        return (null == valore || valore.isBlank()) ? null : valore.trim();
    }

    public static Integer parseInteger(String valore, String nomeCampo) {
        String value = nullIfBlank(valore);
        if (null == value) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException eccezione) {
            throw new BusinessException(String.format(Constants.ErrorMessages.VALORE_NON_VALIDO, nomeCampo, valore));
        }
    }

    // formato yyyy-MM-dd (quello degli input type="date"); vuoto -> null
    public static LocalDate parseData(String valore, String nomeCampo) {
        String value = nullIfBlank(valore);
        if (null == value) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new BusinessException(String.format(Constants.ErrorMessages.DATA_NON_VALIDA, nomeCampo, valore));
        }
    }

    // ultimo giorno del periodo contabile: il 15 per la prima quindicina, altrimenti l'ultimo giorno del mese.
    // null se mancano anno o mese
    public static LocalDate calcolaDataFinePeriodo(String tipoPeriodo, Integer anno, Integer mese, Integer numQuindicina) {
        if (null == anno || null == mese) {
            return null;
        }
        // YearMonth tiene solo anno e mese: il giorno si sceglie dopo con atDay(15) o atEndOfMonth()
        YearMonth meseAnno = YearMonth.of(anno, mese);
        boolean isPrimaQuindicina = Constants.Periodo.TIPO_PERIODO_QUINDICINA.equals(tipoPeriodo)
                && Objects.equals(Constants.Periodo.PRIMA_QUINDICINA, numQuindicina);
        return isPrimaQuindicina ? meseAnno.atDay(Constants.Periodo.ULTIMO_GIORNO_PRIMA_QUINDICINA) : meseAnno.atEndOfMonth();
    }

    // "MM/yyyy" per le mensilità; per le quindicine la data di fine quindicina "dd/MM/yyyy" (vedi calcolaDataFinePeriodo).
    // Il periodo si ricava da anno + mese + quindicina e non da SCADENZA, che per le quindicine è sempre a fine mese
    public static String formattaPeriodoContabile(String tipoPeriodo, Integer anno, Integer mese, Integer numQuindicina) {
        if (null == anno || null == mese) {
            return null;
        }
        boolean isQuindicina = Constants.Periodo.TIPO_PERIODO_QUINDICINA.equals(tipoPeriodo);
        return isQuindicina
                ? calcolaDataFinePeriodo(tipoPeriodo, anno, mese, numQuindicina).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : YearMonth.of(anno, mese).format(DateTimeFormatter.ofPattern("MM/yyyy"));
    }

    // giorni dalla scadenza a oggi (0 se non ancora scaduta), null se la scadenza manca
    public static Integer calcolaGiorniRitardo(LocalDate scadenza) {
        return null == scadenza ? null : Math.toIntExact(Math.max(0, ChronoUnit.DAYS.between(scadenza, LocalDate.now())));
    }

    // data in formato dd/MM/yyyy, null se la data manca
    public static String formattaData(LocalDate data) {
        return null == data ? null : data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    // fine validità da mostrare: null se il gruppo è ancora aperto (2999-12-31)
    public static String formattaDataFineValidita(LocalDate dataFineValidita) {
        return Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA.equals(dataFineValidita) ? null : formattaData(dataFineValidita);
    }

    // moltiplicatoPerGiorni delle sanzioni: true -> 1, false o null -> 0 (colonna numerica MOLTIPLICATO_PER_GIORNI_RITARDO)
    public static Integer toMoltiplicatoPerGiorniRitardo(Boolean moltiplicatoPerGiorni) {
        return Boolean.TRUE.equals(moltiplicatoPerGiorni)
                ? Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_SI
                : Constants.GestioneParametri.MOLTIPLICATO_PER_GIORNI_NO;
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
