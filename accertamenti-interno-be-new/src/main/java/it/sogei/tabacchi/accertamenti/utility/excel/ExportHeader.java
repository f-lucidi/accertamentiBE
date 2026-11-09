package it.sogei.tabacchi.accertamenti.utility.excel;

import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public enum ExportHeader {

    EXPORT_FILE_NAME("export.fileName", null),
    /* FOGLIO DATI COMPLETI */
    DATI_SHEET_NAME("dati.sheetName", null),

    DATI_RAGIONE_SOCIALE  ("dati.ragioneSociale",   ExportHeaderColor.WHITE),
    DATI_PARTITA_IVA      ("dati.partitaIva",       ExportHeaderColor.WHITE),
    DATI_DATA_ATTIVAZIONE ("dati.dataAttivazione",  ExportHeaderColor.WHITE),
    DATI_DATA_CESSAZIONE  ("dati.dataCessazione",   ExportHeaderColor.WHITE),
    DATI_ANNO_CONTABILE   ("dati.annoContabile",    ExportHeaderColor.WHITE),
    DATI_PERIODO_CONTABILE("dati.periodoContabile", ExportHeaderColor.WHITE),

    DATI_IMPOSTA_DOVUTA         ("dati.impostaDovuta",         ExportHeaderColor.GREEN),
    DATI_IMPOSTA_VERSATA        ("dati.impostaVersata",        ExportHeaderColor.GREEN),
    DATI_IMPOSTA_RESTANTE       ("dati.impostaRestante",       ExportHeaderColor.GREEN),
    DATI_IMPOSTA_DATA_VERSAMENTO("dati.impostaDataVersamento", ExportHeaderColor.GREEN),
    DATI_GIORNI_RITARDO         ("dati.giorniRitardo",         ExportHeaderColor.GREEN),

    DATI_MORA_DOVUTA         ("dati.moraDovuta",         ExportHeaderColor.YELLOW),
    DATI_MORA_VERSATA        ("dati.moraVersata",        ExportHeaderColor.YELLOW),
    DATI_MORA_RESTANTE       ("dati.moraRestante",       ExportHeaderColor.YELLOW),
    DATI_MORA_DATA_VERSAMENTO("dati.moraDataVersamento", ExportHeaderColor.YELLOW),

    DATI_INTERESSI_TARDATO_DOVUTI         ("dati.interessiTardatoDovuti",         ExportHeaderColor.ORANGE),
    DATI_INTERESSI_TARDATO_VERSATI        ("dati.interessiTardatoVersati",        ExportHeaderColor.ORANGE),
    DATI_INTERESSI_TARDATO_RESTANTE       ("dati.interessiTardatoRestante",       ExportHeaderColor.ORANGE),
    DATI_INTERESSI_TARDATO_DATA_VERSAMENTO("dati.interessiTardatoDataVersamento", ExportHeaderColor.ORANGE),

    DATI_INTERESSI_LEGALI_DOVUTI         ("dati.interessiLegaliDovuti",         ExportHeaderColor.LIGHT_BLUE),
    DATI_INTERESSI_LEGALI_VERSATI        ("dati.interessiLegaliVersati",        ExportHeaderColor.LIGHT_BLUE),
    DATI_INTERESSI_LEGALI_RESTANTE       ("dati.interessiLegaliRestante",       ExportHeaderColor.LIGHT_BLUE),
    DATI_INTERESSI_LEGALI_DATA_VERSAMENTO("dati.interessiLegaliDataVersamento", ExportHeaderColor.LIGHT_BLUE),

    DATI_SANZIONE_DOVUTA         ("dati.sanzioneDovuta",         ExportHeaderColor.VIOLET),
    DATI_SANZIONE_VERSATA        ("dati.sanzioneVersata",        ExportHeaderColor.VIOLET),
    DATI_SANZIONE_RESTANTE       ("dati.sanzioneRestante",       ExportHeaderColor.VIOLET),
    DATI_SANZIONE_IMPORTO_VERSATO("dati.sanzioneImportoVersato", ExportHeaderColor.VIOLET),
    DATI_SANZIONE_DATA_VERSAMENTO("dati.sanzioneDataVersamento", ExportHeaderColor.VIOLET),

    /* FOGLIO ALLEGATO OPERATORE */
    ALLEGATO_SHEET_NAME("allegato.sheetName", null),

    ALLEGATO_TESTATA_RAGIONE_SOCIALE("allegato.testataRagioneSociale", ExportHeaderColor.LIGHT_GREEN),
    ALLEGATO_TESTATA_PARTITA_IVA    ("allegato.testataPartitaIva",     ExportHeaderColor.LIGHT_GREEN),
    ALLEGATO_TESTATA_DATA_RICHIESTA ("allegato.testataDataRichiesta",  ExportHeaderColor.LIGHT_GREEN),

    ALLEGATO_PERIODO_CONTABILE        ("allegato.periodoContabile",        ExportHeaderColor.WHITE),
    ALLEGATO_DICHIARATO_DOVUTO        ("allegato.dichiaratoDovuto",        ExportHeaderColor.WHITE),
    ALLEGATO_VERSATO                  ("allegato.versato",                 ExportHeaderColor.WHITE),
    ALLEGATO_DIFFERENZA               ("allegato.differenza",              ExportHeaderColor.WHITE),
    ALLEGATO_DATA_ULTIMO_VERSAMENTO   ("allegato.dataUltimoVersamento",    ExportHeaderColor.WHITE),
    ALLEGATO_SCADENZA                 ("allegato.scadenza",                ExportHeaderColor.WHITE),
    ALLEGATO_MORA_VERSATA             ("allegato.moraVersata",             ExportHeaderColor.WHITE),
    ALLEGATO_INTERESSI_DIFFERITI      ("allegato.interessiDifferiti",      ExportHeaderColor.WHITE),
    ALLEGATO_INTERESSI_LEGALI_DOGANALI("allegato.interessiLegaliDoganali", ExportHeaderColor.WHITE),
    ALLEGATO_SANZIONE                 ("allegato.sanzione",                ExportHeaderColor.WHITE),

    ALLEGATO_TITOLO_CALCOLO           ("allegato.titoloCalcolo",           ExportHeaderColor.CYAN),
    ALLEGATO_MORA_NETTA               ("allegato.moraNetta",               ExportHeaderColor.CYAN),
    ALLEGATO_INTERESSI_DIFFERITI_NETTI("allegato.interessiDifferitiNetti", ExportHeaderColor.CYAN),
    ALLEGATO_INTERESSI_LEGALI_2025    ("allegato.interessiLegali2025",     ExportHeaderColor.CYAN),
    ALLEGATO_INTERESSI_LEGALI_2026    ("allegato.interessiLegali2026",     ExportHeaderColor.CYAN),
    ALLEGATO_TOTALE_INTERESSI_LEGALI  ("allegato.totaleInteressiLegali",   ExportHeaderColor.CYAN),
    ALLEGATO_SANZIONE_FINALE          ("allegato.sanzioneFinale",          ExportHeaderColor.CYAN);

    public static final List<ExportHeader> DATI_COMPLETI_HEADERS = Arrays.stream(values())
            .filter(property -> {
                String key = property.valueKey;
                return key.startsWith("dati.")
                    && !key.endsWith(".sheetName");
            }).toList();

    public static final List<ExportHeader> ALLEGATO_OPERATORE_HEADERS = Arrays.stream(values())
            .filter(property -> {
                String key = property.valueKey;
                return key.startsWith("allegato.")
                    && !key.endsWith(".sheetName")
                    && !key.contains("testata")
                    && !key.contains("titoloCalcolo");
            }).toList();

    private final String valueKey;
    private final ExportHeaderColor exportHeaderColor;

    ExportHeader(String valueKey, ExportHeaderColor exportHeaderColor) {
        this.valueKey = valueKey;
        this.exportHeaderColor = exportHeaderColor;
    }

    public String getValue() {
        return ExportUtils.getProperty(this.valueKey);
    }

    public XSSFColor getBacgkroundColor() {
        return Objects.nonNull(this.exportHeaderColor)
                ? this.exportHeaderColor.getXSSFColor()
                : null;
    }

    public enum ExportHeaderColor {

        WHITE("bgColor.white"),
        GREEN("bgColor.green"),
        LIGHT_GREEN("bgColor.lightGreen"),
        YELLOW("bgColor.yellow"),
        ORANGE("bgColor.orange"),
        LIGHT_BLUE("bgColor.lightBlue"),
        VIOLET("bgColor.violet"),
        CYAN("bgColor.cyan");

        private final String colorKey;
        private XSSFColor xssfColor;

        ExportHeaderColor(String colorKey) {
            this.colorKey = colorKey;
        }

        public XSSFColor getXSSFColor(){
            if(Objects.isNull(this.xssfColor)){
                initXSSFColor();
            }
            return this.xssfColor;
        }

        private void initXSSFColor() {
            try {
                log.debug("Inizializzazione colore: {}", this.colorKey);
                String hexColor = ExportUtils.getProperty(this.colorKey);
                byte[] binaryColor = DatatypeConverter.parseHexBinary(hexColor);
                this.xssfColor = new XSSFColor(binaryColor, null);
            } catch (Exception e) {
                log.error("Errore durante l'inizializzazione del colore: {}", this.colorKey, e);
            }
        }
    }
}
