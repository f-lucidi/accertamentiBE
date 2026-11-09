package it.sogei.tabacchi.accertamenti.utility;

public class Constants {

    public static final Integer DEFAULT_ELEMENTS_PER_PAGE = 10;
    public static final String CATEGORIA_PRODOTTO_NON_DISPONIBILE = "Categoria prodotto non disponibile";


    public static class Application {

        public static final String CONTEXT_ROOT = "/Accertamenti";
        public static final String ROLES_ALLOWED = "ALL_AUTHENTICATED_ROLE";
        public static final String PROPERTIES_JNDI_NAME = "propertiesFilePath";
        public static final String EXPORT_PROPERTIES_JNDI_NAME = "exportPropertiesFilePath";

    }

    public static class Endpoint {

        public static final String EMPTY = "/";

        /* Utente */
        public static final String USER_INFO = "/user-info";
        public static final String LOGOUT = "/logout";
        public static final String REACTIVATE_SESSION = "/reactivate-session";

        /* Categorie Prodotto */
        public static final String CATEGORIE_PRODOTTO = "/categorie-prodotto";
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
}
