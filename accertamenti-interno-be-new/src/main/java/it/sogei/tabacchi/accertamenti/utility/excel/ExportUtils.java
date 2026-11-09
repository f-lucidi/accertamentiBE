package it.sogei.tabacchi.accertamenti.utility.excel;

import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.naming.InitialContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

@Slf4j
public class ExportUtils {

    private static Properties properties;
    private ExportUtils() {}

    public static byte[] export(Consumer<Workbook> workbookConsumer) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbookConsumer.accept(workbook);
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Errore durante l'esportazione dei dati", e);
            throw BusinessException.getOrElse(ExceptionType.EXPORT_ERROR, e);
        }
    }

    public static String getProperty(String key) {
        return getProperty(key, "");
    }

    public static String getProperty(String key, String defaultValue) {
        if (Objects.isNull(properties)) {
            initProperties();
        }
        return properties.getProperty(key, defaultValue);
    }

    private static void initProperties() {
        properties = new Properties();
        try {
            String exportPropertiesFilePath = InitialContext.doLookup(Constants.Application.EXPORT_PROPERTIES_JNDI_NAME);

            log.debug("Path del file export.properties: {}", exportPropertiesFilePath);
            try(InputStream is = Files.newInputStream(Paths.get(exportPropertiesFilePath))) {
                properties.load(is);
            }
        } catch (Exception e) {
            log.error("Errore nel caricamento del file export.properties", e);
        }
    }

}
