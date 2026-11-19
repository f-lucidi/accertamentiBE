package it.sogei.tabacchi.accertamenti.utility;

import java.math.BigDecimal;
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

    // regole dei parametri storicizzati (aliquote di mora, interessi sul tardato pagamento): decorrenza e fine validità
    public static class GestioneParametri {

        // DATA_FINE_VAL di un gruppo (o di un tasso) ancora aperto (vigente o futuro): "nessuna fine"
        public static final LocalDate DATA_FINE_VALIDITA_APERTA = LocalDate.of(2999, 12, 31);
        // CF_INSERIMENTO quando non si conosce l'utente
        public static final String CF_INSERIMENTO_SISTEMA = "SYSTEM";
        // il primo scaglione di un gruppo deve partire da questo giorno di ritardo
        public static final Integer PRIMO_GIORNO_RITARDO = 1;
        // W_TB_ACCERTAMENTI_INTERESSE_LEGALE.FLAG_CANCELLA (numerico): 0 = riga valida, qualsiasi altro valore (1 o 2) = cancellata.
        // Le letture filtrano su = 0, così escludono sia 1 sia 2
        public static final Integer FLAG_CANCELLA_NO = 0;
        // valore scritto in cancellazione (2 a DB ha lo stesso significato di 1)
        public static final Integer FLAG_CANCELLA_SI = 1;
        // tasso proposto per il primo anno mancante quando l'anno corrente non ha un interesse legale
        public static final BigDecimal TASSO_INTERESSE_LEGALE_DEFAULT = new BigDecimal("0.2");
        // nomi degli scaglioni nei messaggi di errore condivisi (CommonValidator): singolare e plurale, minuscoli
        public static final String NOME_ALIQUOTA = "aliquota";
        public static final String NOME_ALIQUOTE = "aliquote";
        public static final String NOME_SANZIONE = "sanzione";
        public static final String NOME_SANZIONI = "sanzioni";
        // W_TB_ACCERTAMENTI_SANZIONI.MOLTIPLICATO_PER_GIORNI_RITARDO (numerico): 1 = sì, 0 = no
        public static final Integer MOLTIPLICATO_PER_GIORNI_SI = 1;
        public static final Integer MOLTIPLICATO_PER_GIORNI_NO = 0;

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
        public static final String TIPO_PARAMETRO = "tipoParametro";
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
        public static final String ALIQUOTE_MODIFICA = "/aliquote/modifica";
        public static final String ALIQUOTE_INSERIMENTO_ABILITATO = "/aliquote/inserimento-abilitato";
        /* Interessi sul tardato pagamento */
        public static final String INTERESSI_LISTA = "/interessi/lista";
        public static final String INTERESSI_MODIFICA = "/interessi/modifica";
        public static final String INTERESSI_INSERIMENTO_ABILITATO = "/interessi/inserimento-abilitato";
        /* Interessi legali */
        public static final String INTERESSI_LEGALI_LISTA = "/interessi-legali/lista";
        public static final String INTERESSI_LEGALI_MODIFICA = "/interessi-legali/modifica";
        public static final String INTERESSI_LEGALI_PRIMO_ANNO_MANCANTE = "/interessi-legali/primo-anno-mancante";
        /* Sanzioni */
        public static final String SANZIONI_LISTA = "/sanzioni/lista";
        public static final String SANZIONI_MODIFICA = "/sanzioni/modifica";
        public static final String SANZIONI_INSERIMENTO_ABILITATO = "/sanzioni/inserimento-abilitato";
        /* Anni per il box di ricerca */
        public static final String ANNI = "/anni";
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

        // tra i messaggi di validazione raccolti, uniti in un'unica BusinessException (i messaggi finiscono già col punto)
        public static final String SEPARATORE_ERRORI = " ";

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

        /* Gestione parametri - aliquote di mora*/
        public static final String INTERVALLO_DECORRENZA_NON_VALIDO = "La data inizio validità da non può essere successiva alla data inizio validità a.";
        public static final String DATI_ALIQUOTE_OBBLIGATORI = "I dati delle aliquote sono obbligatori.";
        public static final String TIPO_OPERAZIONE_OBBLIGATORIO = "Il tipo operazione è obbligatorio.";
        public static final String DATA_DECORRENZA_OBBLIGATORIA = "La data di decorrenza è obbligatoria.";
        public static final String DATA_DECORRENZA_PASSATA = "La data di decorrenza non può essere nel passato.";
        public static final String ID_GRUPPO_OBBLIGATORIO = "In modifica l'id del gruppo è obbligatorio.";
        public static final String ID_GRUPPO_NON_AMMESSO = "In inserimento il gruppo non deve avere un id.";
        public static final String ID_GRUPPO_NON_VALIDO = "L'id del gruppo %s non è valido.";
        public static final String ALIQUOTE_OBBLIGATORIE = "Indicare almeno un'aliquota.";
        public static final String ALIQUOTE_DA_MODIFICARE_NON_TROVATE = "Aliquote da modificare non trovate.";
        public static final String GRUPPO_ALIQUOTE_NON_MODIFICABILE = "Le aliquote con decorrenza %s sono già in vigore e non sono più modificabili.";
        public static final String GRUPPO_ALIQUOTE_INCOMPLETO = "Indicare tutte e sole le aliquote già salvate del gruppo.";
        public static final String DECORRENZA_NON_SUCCESSIVA_PRECEDENTE = "La data di decorrenza deve essere successiva al %s, inizio delle aliquote precedenti.";

        /* Gestione parametri - righe dei gruppi a scaglioni (aliquote, sanzioni).
           %s del nome: NOME_ALIQUOTA/NOME_SANZIONE (o il plurale), già con la maiuscola dove inizia la frase */
        public static final String SCAGLIONE_VUOTO = "%s %s: dati mancanti.";
        public static final String ID_SCAGLIONE_NON_AMMESSO = "%s %s: in inserimento non deve avere un id.";
        public static final String VALORE_SCAGLIONE_OBBLIGATORIO = "%s %s: il valore è obbligatorio.";
        public static final String VALORE_SCAGLIONE_NON_POSITIVO = "%s %s: il valore deve essere maggiore di zero.";
        public static final String GIORNI_DA_OBBLIGATORIO = "%s %s: i giorni di ritardo da sono obbligatori.";
        public static final String GIORNI_A_NON_VALIDO = "%s %s: i giorni di ritardo a devono essere maggiori dei giorni di ritardo da.";
        public static final String PRIMO_SCAGLIONE_NON_DA_PRIMO_GIORNO = "La prima %s deve partire da %s giorno di ritardo.";
        public static final String SOLO_ULTIMO_SCAGLIONE_APERTO = "%s %s: solo l'ultima %s può non avere i giorni di ritardo a.";
        public static final String GIORNI_NON_CONSECUTIVI = "%s %s e %s: i giorni di ritardo devono essere consecutivi, senza buchi né sovrapposizioni.";
        public static final String ULTIMO_SCAGLIONE_NON_APERTO = "Nessuna %s definita oltre i %s giorni di ritardo.";

        /* Gestione parametri - sanzioni */
        public static final String DATI_SANZIONI_OBBLIGATORI = "I dati delle sanzioni sono obbligatori.";
        public static final String SANZIONI_OBBLIGATORIE = "Indicare almeno una sanzione.";
        public static final String SANZIONI_DA_MODIFICARE_NON_TROVATE = "Sanzioni da modificare non trovate.";
        public static final String GRUPPO_SANZIONI_NON_MODIFICABILE = "Le sanzioni con decorrenza %s sono già in vigore e non sono più modificabili.";
        public static final String GRUPPO_SANZIONI_INCOMPLETO = "Indicare tutte e sole le sanzioni già salvate del gruppo.";
        public static final String DECORRENZA_SANZIONI_NON_SUCCESSIVA_PRECEDENTE = "La data di decorrenza deve essere successiva al %s, inizio delle sanzioni precedenti.";

        /* Gestione parametri - interessi sul tardato pagamento */
        public static final String DATI_INTERESSE_OBBLIGATORI = "I dati del tasso di interesse sono obbligatori.";
        public static final String TASSO_INTERESSE_OBBLIGATORIO = "Il tasso di interesse è obbligatorio.";
        public static final String TASSO_INTERESSE_NON_POSITIVO = "Il tasso di interesse deve essere maggiore di zero.";
        public static final String TASSO_INTERESSE_UGUALE_VIGENTE = "Il tasso di interesse non può essere uguale a quello in vigore (%s).";
        public static final String ID_INTERESSE_OBBLIGATORIO = "In modifica l'id del tasso di interesse è obbligatorio.";
        public static final String ID_INTERESSE_NON_AMMESSO ="In inserimento il tasso di interesse non deve avere un id.";
        public static final String CANCELLAZIONE_INTERESSE_NON_AMMESSA ="In inserimento il tasso di interesse non può essere cancellato.";
        public static final String INTERESSE_NON_TROVATO = "Tasso di interesse da modificare non trovato.";
        public static final String INTERESSE_NON_MODIFICABILE = "Il tasso di interesse con decorrenza %s è già in vigore e non è più modificabile.";
        public static final String DECORRENZA_INTERESSE_NON_SUCCESSIVA_PRECEDENTE = "La data di decorrenza deve essere successiva al %s, inizio del tasso di interesse precedente.";

        /* Gestione parametri - interessi legali (id, cancellazione e tasso usano i messaggi degli interessi) */
        public static final String DATI_INTERESSE_LEGALE_OBBLIGATORI = "I dati del tasso di interesse legale sono obbligatori.";
        public static final String ANNO_INTERESSE_LEGALE_OBBLIGATORIO = "L'anno del tasso di interesse legale è obbligatorio.";
        public static final String ANNO_INTERESSE_LEGALE_GIA_PRESENTE = "Esiste già un tasso di interesse legale per l'anno %s.";
        public static final String INTERESSE_LEGALE_NON_TROVATO = "Tasso di interesse legale da modificare non trovato.";
        public static final String INTERESSE_LEGALE_NON_MODIFICABILE = "Il tasso di interesse legale dell'anno %s è già in vigore e non è più modificabile.";
    }
}
