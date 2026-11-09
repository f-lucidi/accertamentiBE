package it.sogei.tabacchi.accertamenti.utility;

import lombok.extern.slf4j.Slf4j;

import javax.naming.InitialContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public class PropertyUtils {

    private static final String DEFAULT_LIST_SEPARATOR = ",";

    private static Properties properties;
    private PropertyUtils() {}

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static String getProperty(String key, String defaultValue) {
        if (Objects.isNull(properties)) {
            initProperties();
        }
        return properties.getProperty(key, defaultValue);
    }

    public static Integer getAsInteger(String key) {
        return getAndMap(key, Integer::valueOf);
    }

    public static <T> List<T> getAsList(String key, String separator, Function<String, T> mappingFunction) {
        List<T> list;

        try {
            String value = getProperty(key);
            list = Stream.of(value.split(separator))
                    .map(String::trim)
                    .map(mappingFunction)
                    .toList();
        } catch (Exception e) {
            list = Collections.emptyList();
        }

        return list;
    }

    public static List<Integer> getAsIntegerList(String key, String separator) {
        return getAsList(key, separator, Integer::valueOf);
    }

    public static List<Integer> getAsIntegerList(String key) {
        return getAsIntegerList(key, DEFAULT_LIST_SEPARATOR);
    }

    public static List<String> getAsStringList(String key, String separator) {
        return getAsList(key, separator, Function.identity());
    }

    public static List<String> getAsStringList(String key) {
        return getAsStringList(key, DEFAULT_LIST_SEPARATOR);
    }


    public static <T> T getAndMap(String key, Function<String, T> mappingFunction) {
        try {
            return mappingFunction.apply(getProperty(key));
        } catch(Exception e) {
            log.error("Errore mapping di properties", e);
            return null;
        }
    }

    private static void initProperties() {
        properties = new Properties();
        try {
            String propertiesFilePath = InitialContext.doLookup(Constants.Application.PROPERTIES_JNDI_NAME);

            log.debug("Path del file ini.properties: {}", propertiesFilePath);
            try(InputStream is = Files.newInputStream(Paths.get(propertiesFilePath))) {
                properties.load(is);
            }
        } catch (Exception e) {
            log.error("Errore nel caricamento del file ini.properties", e);
        }
    }

}
