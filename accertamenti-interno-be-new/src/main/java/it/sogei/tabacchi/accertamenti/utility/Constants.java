package it.sogei.tabacchi.accertamenti.utility;

import java.time.LocalDate;
import java.util.Set;

public class Constants {

    public static final Integer DEFAULT_ELEMENTS_PER_PAGE = 10;
    public static final Integer MAX_ELEMENTS_PER_PAGE = 100;
    public static final String CATEGORIA_PRODOTTO_NON_DISPONIBILE = "Categoria prodotto non disponibile";
    public static final String RAGIONE_SOCIALE_NON_DISPONIBILE = "Ragione sociale non disponibile";
    // W_TB_ACCERTAMENTI_INTERESSI_LEGALI_PER_ANNO.STATO_PAGAMENTO_INTERESSE_LEGALE
    public static final String INTERESSE_LEGALE_PAGATO = "1";


    public static class Periodo {

        // codici del periodo contabile accettati dal filtro (oltre a vuoto e FILTRO_TUTTE)
        public static final Set<String> CODICI_VALIDI = Set.of(
                // trimestri
                "T1", "T2", "T3", "T4",
                // mesi
                "M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10", "M11", "M12",
                // quindicine: Q(2m-1) = prima quindicina del mese m, Q(2m) = seconda quindicina del mese m
                "Q1", "Q2", "Q3", "Q4", "Q5", "Q6", "Q7", "Q8", "Q9", "Q10", "Q11", "Q12",
                "Q13", "Q14", "Q15", "Q16", "Q17", "Q18", "Q19", "Q20", "Q21", "Q22", "Q23", "Q24");

        public static final String FILTRO_TUTTE = "TUTTE";

        // valore di TIPO_PERIODO per le posizioni a quindicina ("M" per le mensilità)
        public static final String TIPO_PERIODO_QUINDICINA = "Q";
        // valori di NUM_QUINDICINA (0 per le mensilità)
        public static final Integer PRIMA_QUINDICINA = 1;
        public static final Integer SECONDA_QUINDICINA = 2;
        // la prima quindicina termina il 15 del mese, la seconda l'ultimo giorno del mese
        public static final int ULTIMO_GIORNO_PRIMA_QUINDICINA = 15;

    }

    // regole delle aliquote di mora storicizzate (gruppi con decorrenza e fine validità)
    public static class GestioneAliquote {

        // DATA_FINE_VAL di un gruppo ancora aperto (vigente o futuro): "nessuna fine"
        public static final LocalDate DATA_FINE_VALIDITA_APERTA = LocalDate.of(2999, 12, 31);
        // CF_INSERIMENTO quando non si conosce l'utente
        public static final String CF_INSERIMENTO_SISTEMA = "SYSTEM";
        // il primo scaglione di un gruppo deve partire da questo giorno di ritardo
        public static final Integer PRIMO_GIORNO_RITARDO = 1;

    }

    public static class Application {

        public static final String CONTEXT_ROOT = "/Accertamenti";
        public static final String ROLES_ALLOWED = "ALL_AUTHENTICATED_ROLE";
        public static final String PROPERTIES_JNDI_NAME = "propertiesFilePath";
        public static final String EXPORT_PROPERTIES_JNDI_NAME = "exportPropertiesFilePath";

    }

    public static class Parametri {

        public static final String ID_POSIZIONE = "idPosizione";
        public static final String ID_VERSAMENTO = "idVersamento";
        public static final String CATEGORIA_PRODOTTO = "categoriaProdotto";
        public static final String PAGINA = "pagina";
        public static final String ANNO = "anno";
        public static final String ELEMENTI_PER_PAGINA = "elementiPerPagina";
        public static final String DEFAULT_PAGINA = "1";
        public static final String DEFAULT_ELEMENTI_PER_PAGINA = "5";

    }

    public static class Endpoint {

        public static final String EMPTY = "/";

        /* Utente */
        public static final String USER_INFO = "/user-info";
        public static final String LOGOUT = "/logout";
        public static final String REACTIVATE_SESSION = "/reactivate-session";

        /* Categorie Prodotto */
        public static final String CATEGORIE_PRODOTTO = "/categorie-prodotto";
        /* Filtri Ricerca */
        public static final String FILTRI_RICERCA = "/filtri-ricerca";
        public static final String CONSULTAZIONE_IMPOSTE = "/consultazione-imposte";
        /* Lista Posizioni */
        public static final String LISTA_POSIZIONI = "/lista-posizioni";
        /* Gestione parametri */
        public static final String GESTIONE_PARAMETRI = "/gestione-parametri";
        /* Aliquote di mora */
        public static final String ALIQUOTE_LISTA = "/aliquote/lista";
        public static final String ALIQUOTE_SALVA = "/aliquote/salva";
        public static final String ALIQUOTE_INSERIMENTO_ABILITATO = "/aliquote/inserimento-abilitato";
    }


    public static class Properties {

        public static final String SESSION_TIMEOUT = "SESSION_TIMEOUT";
        public static final String SESSION_EXPIRING = "SESSION_EXPIRING";


    }

    public static class SessionKeys {

        public static final String USER = "user";


    }

    public static class CacheKeys {


    }

    public static class ParamKeys {

    }

    // messaggi delle BusinessException: quelli con %s vanno completati con String.format(MESSAGGIO, valori...)
    public static class ErrorMessages {

        /* Categoria prodotto */
        public static final String CATEGORIA_PRODOTTO_OBBLIGATORIA = "La categoria prodotto è obbligatoria.";
        public static final String CATEGORIA_PRODOTTO_NON_VALIDA = "Categoria prodotto non valida: %s";

        /* Filtri di ricerca */
        public static final String FILTRI_RICERCA_OBBLIGATORI = "I filtri di ricerca sono obbligatori (anche vuoti).";
        public static final String ANNO_CONTABILE_OBBLIGATORIO = "L'anno contabile è obbligatorio.";
        public static final String PERIODO_CONTABILE_NON_VALIDO = "Periodo contabile non valido: %s";
        public static final String TIPO_PAGAMENTO_NON_VALIDO = "Tipo pagamento non valido: %s";
        public static final String INTERVALLO_DATE_PAGAMENTO_NON_VALIDO = "La data pagamento da non può essere successiva alla data pagamento a.";
        public static final String INTERVALLO_PERIODO_NON_VALIDO = "L'inizio periodo non può essere successivo alla fine periodo.";
        public static final String ALMENO_UN_CRITERIO_DI_RICERCA = "Indicare almeno uno tra partita IVA, periodo contabile e data pagamento da/a.";

        /* Formato dei valori ricevuti: %s = nome del campo, %s = valore */
        public static final String VALORE_NON_VALIDO = "Valore non valido per %s: %s";
        public static final String DATA_NON_VALIDA = "Data non valida per %s: %s";

        /* Utente */
        public static final String UFFICIO_UTENTE_NON_TROVATO = "Ufficio utente non trovato";

        /* Gestione parametri - aliquote di mora (%s = numero dell'aliquota, come nei riquadri del frontend) */
        public static final String INTERVALLO_DECORRENZA_NON_VALIDO = "La data inizio validità da non può essere successiva alla data inizio validità a.";
        public static final String DATI_ALIQUOTE_OBBLIGATORI = "I dati delle aliquote sono obbligatori.";
        public static final String TIPO_OPERAZIONE_OBBLIGATORIO = "Il tipo operazione è obbligatorio.";
        public static final String DATA_DECORRENZA_OBBLIGATORIA = "La data di decorrenza è obbligatoria.";
        public static final String DATA_DECORRENZA_PASSATA = "La data di decorrenza non può essere nel passato.";
        public static final String ALIQUOTE_OBBLIGATORIE = "Indicare almeno un'aliquota.";
        public static final String ALIQUOTA_VUOTA = "Aliquota %s: dati mancanti.";
        public static final String ID_ALIQUOTA_NON_AMMESSO = "Aliquota %s: in inserimento non deve avere un id.";
        public static final String VALORE_ALIQUOTA_OBBLIGATORIO = "Aliquota %s: il valore è obbligatorio.";
        public static final String VALORE_ALIQUOTA_NON_POSITIVO = "Aliquota %s: il valore deve essere maggiore di zero.";
        public static final String GIORNI_DA_OBBLIGATORIO = "Aliquota %s: i giorni di ritardo da sono obbligatori.";
        public static final String GIORNI_A_NON_VALIDO = "Aliquota %s: i giorni di ritardo a devono essere maggiori dei giorni di ritardo da.";
        public static final String PRIMA_ALIQUOTA_NON_DA_PRIMO_GIORNO = "La prima aliquota deve partire da %s giorno di ritardo.";
        public static final String SOLO_ULTIMA_ALIQUOTA_APERTA = "Aliquota %s: solo l'ultima aliquota può non avere i giorni di ritardo a.";
        public static final String GIORNI_NON_CONSECUTIVI = "Aliquote %s e %s: i giorni di ritardo devono essere consecutivi, senza buchi né sovrapposizioni.";
        public static final String ULTIMA_ALIQUOTA_NON_APERTA = "Nessuna aliquota definita oltre i %s giorni di ritardo.";
        public static final String ALIQUOTE_SALVATE_OBBLIGATORIE = "In modifica indicare le aliquote già salvate del gruppo.";
        public static final String ALIQUOTE_DA_MODIFICARE_NON_TROVATE = "Aliquote da modificare non trovate.";
        public static final String GRUPPO_ALIQUOTE_NON_MODIFICABILE = "Le aliquote con decorrenza %s sono già in vigore e non sono più modificabili.";
        public static final String GRUPPO_ALIQUOTE_INCOMPLETO = "Indicare tutte e sole le aliquote già salvate del gruppo.";
        public static final String DECORRENZA_NON_SUCCESSIVA_PRECEDENTE = "La data di decorrenza deve essere successiva al %s, inizio delle aliquote precedenti.";
    }
}
