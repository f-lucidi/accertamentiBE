package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IDettaglioController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IConsultazioneImposteDao;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IDettaglioDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio.*;
import it.sogei.tabacchi.accertamenti.business.model.entity.*;
import it.sogei.tabacchi.accertamenti.business.validation.DettaglioValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class DettaglioController implements IDettaglioController {

    @Inject
    private IDettaglioDao dettaglioDao;

    // ragione sociale, categoria e interessi legali non pagati: query già esistenti della consultazione
    @Inject
    private IConsultazioneImposteDao consultazioneImposteDao;

    @Inject
    private DettaglioValidator validator;

    @Inject
    private CustomMapper customMapper;

    @Override
    public DettaglioDto getDettaglio(Long idPosizione, Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        validator.validaIdPosizione(idPosizione);
        WTBAccertamentiElencoPosizioni posizione = dettaglioDao.findPosizioneById(idPosizione);
        if (null == posizione) {
            throw new BusinessException(String.format(Constants.ErrorMessages.POSIZIONE_NON_TROVATA, idPosizione));
        } else {
            DettaglioDto dettaglioDto = new DettaglioDto();
            // usata sia nell'importo totale da pagare sia nella riga "restanti" degli interessi legali
            BigDecimal interessiLegaliNonPagati = consultazioneImposteDao.findInteressiLegaliNonPagati(idPosizione);
            dettaglioDto.setDettaglioPagamento(popolaDettaglioPagamento(posizione, interessiLegaliNonPagati));
            // dettaglio imposta dovuta paginato come da PowerPoint
            dettaglioDto.setDettaglioImpostaDovuta(popolaImpostaDovuta(idPosizione, paginaRichiesta, elementiPerPaginaRichiesti));
            // ogni blocco solo se la sua condizione è vera: altrimenti resta null e il FE non lo mostra
            if (isImportoPositivo(posizione.getMoraTotaleDovuto())) {
                // come nel BE del collega: aliquote per i giorni di ritardo delle righe della pagina mostrata
                dettaglioDto.setDettaglioIndennitaMora(
                        popolaIndennitaMora(posizione, dettaglioDto.getDettaglioImpostaDovuta().getData()));
            }
            if (isImportoPositivo(posizione.getInteresseTardatoPagamentoTotaleDovuto())) {
                dettaglioDto.setInteresseDifferito(popolaInteresseDifferito(posizione));
            }
            if (!isPosizionePagata(posizione)) {
                dettaglioDto.setDettaglioInteressiLegali(popolaInteressiLegali(posizione, interessiLegaliNonPagati));
            }
            if (isImportoPositivo(posizione.getSanzioneTotaleDovuto())) {
                dettaglioDto.setDettaglioSanzioni(popolaSanzioni(posizione));
            }
            return dettaglioDto;
        }
    }

    // ------------ CONDIZIONI DEI BLOCCHI ------------

    private boolean isImportoPositivo(BigDecimal importo) {
        return null != importo && importo.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isPosizionePagata(WTBAccertamentiElencoPosizioni posizione) {
        return null != posizione.getStatoPagamento()
                && Constants.STATO_PAGAMENTO_PAGATO.equals(posizione.getStatoPagamento().getId());
    }

    // ------------ DETTAGLIO PAGAMENTO ------------

    // campi diretti dal mapper; gli altri da altre tabelle o calcolati
    private DettaglioPagamentoDto popolaDettaglioPagamento(WTBAccertamentiElencoPosizioni posizione, BigDecimal interessiLegaliNonPagati) {
        DettaglioPagamentoDto dettaglioPagamentoDto = customMapper.toDettaglioPagamentoDto(posizione);
        dettaglioPagamentoDto.setRagioneSociale(Utility.getOrDefault(
                consultazioneImposteDao.findRagioneSociale(posizione.getCategoria(), posizione.getCfSogObbligato()),
                Constants.RAGIONE_SOCIALE_NON_DISPONIBILE));
        dettaglioPagamentoDto.setCategoriaProdotto(Utility.getOrDefault(
                consultazioneImposteDao.findDescrizioneCategoria(posizione.getCategoria()),
                Constants.CATEGORIA_PRODOTTO_NON_DISPONIBILE));
        dettaglioPagamentoDto.setImportoTotaleDaPagare(calcolaImportoTotaleDaPagare(posizione, interessiLegaliNonPagati));
        return dettaglioPagamentoDto;
    }

    // interessi legali non pagati + i quattro "restante da pagare" della posizione (i null non contano)
    private BigDecimal calcolaImportoTotaleDaPagare(WTBAccertamentiElencoPosizioni posizione, BigDecimal interessiLegaliNonPagati) {
        BigDecimal restantiDaPagare = Stream.of(
                        posizione.getImpostaRestanteDaPagare(),
                        posizione.getMoraRestanteDaPagare(),
                        posizione.getInteresseTardatoPagamentoRestanteDaPagare(),
                        posizione.getSanzioneRestanteDaPagare())
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return restantiDaPagare.add(interessiLegaliNonPagati);
    }

    // ------------ IMPOSTA DOVUTA (paginata) ------------

    private RisultatoPaginatoDto<DettaglioImpostaDovutaDto> popolaImpostaDovuta(Long idPosizione, Integer paginaRichiesta,
                                                                              Integer elementiPerPaginaRichiesti) {
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<DettaglioImpostaDovutaDto> righe = dettaglioDao.findDettaglioImposte(idPosizione, pagina, elementiPerPagina)
                .stream()
                .map(this::toImpostaDovutaDto)
                .toList();
        Long totaleElementi = dettaglioDao.countDettaglioImposte(idPosizione);
        return RisultatoPaginatoDto.of(righe, totaleElementi, pagina, elementiPerPagina);
    }

    // riga mancante: giorni di ritardo dalla scadenza a oggi; riga versata: data, metodo e giorni di ritardo del pagamento
    private DettaglioImpostaDovutaDto toImpostaDovutaDto(WTBAccertamentiDettaglioImposte rigaImposta) {
        DettaglioImpostaDovutaDto impostaDovutaDto = new DettaglioImpostaDovutaDto();
        impostaDovutaDto.setIdVersamento(rigaImposta.getId());
        impostaDovutaDto.setImporto(rigaImposta.getImpostaImportoParziale());
        if (Constants.Dettaglio.STATO_IMPORTO_MANCANTE.equals(rigaImposta.getStatoImportoParziale())) {
            impostaDovutaDto.setTipoImporto(Constants.Dettaglio.TIPO_IMPORTO_MANCANTE);
            impostaDovutaDto.setGiorniRitardoPagamento(Utility.calcolaGiorniRitardo(rigaImposta.getScadenza()));
        } else {
            impostaDovutaDto.setTipoImporto(Constants.Dettaglio.TIPO_IMPORTO_VERSATO);
            impostaDovutaDto.setDataPagamento(rigaImposta.getDataPagamento());
            impostaDovutaDto.setMetodoPagamento(findDescrizioneTipoPagamento(rigaImposta.getTipoPagamento()));
            // ritardo del pagamento, già calcolato a DB
            impostaDovutaDto.setGiorniRitardoPagamento(rigaImposta.getGiorniRitardo());
        }
        return impostaDovutaDto;
    }

    // descrizione del metodo di pagamento, "N/D" se manca l'id o non esiste
    private String findDescrizioneTipoPagamento(Integer idTipoPagamento) {
        if (null == idTipoPagamento) {
            return Constants.Dettaglio.VALORE_NON_DISPONIBILE;
        }
        WTBAccertamentiTipoPagamento tipoPagamento = dettaglioDao.findTipoPagamentoById(idTipoPagamento.longValue());
        return null != tipoPagamento ? tipoPagamento.getDescrizione() : Constants.Dettaglio.VALORE_NON_DISPONIBILE;
    }

    // ------------ INDENNITÀ DI MORA ------------

    // per ogni valore distinto di giorni di ritardo delle righe mostrate, le aliquote vigenti a fine periodo
    private DettaglioIndennitaMoraDto popolaIndennitaMora(WTBAccertamentiElencoPosizioni posizione,
                                                          List<DettaglioImpostaDovutaDto> righePagina) {
        LocalDate finePeriodo = calcolaDataFinePeriodo(posizione);
        // giorni di ritardo già cercati: righe con lo stesso valore danno le stesse aliquote
        // Un Set è una collezione di elementi che non ammette duplicati
        Set<Integer> giorniCercati = new HashSet<>();
        List<AliquotaApplicataDto> aliquoteApplicate = new ArrayList<>();
        for (DettaglioImpostaDovutaDto riga : righePagina) {
            Integer giorniRitardo = riga.getGiorniRitardoPagamento();
            // l'aggiunta al Set dei giorniRitardo restituisce un Boolean proprio per la natura del set
            // se esisteva già non va a buon fine e torna false
            if (null != giorniRitardo && giorniRitardo >= Constants.Dettaglio.GIORNO_MINIMO_PER_MORA && giorniCercati.add(giorniRitardo)) {
                for (WTBAccertamentiAliquotaMora aliquotaMora : dettaglioDao.findAliquoteMoraVigenti(giorniRitardo, finePeriodo)) {
                    aliquoteApplicate.add(customMapper.toAliquotaApplicataDto(aliquotaMora, giorniRitardo));
                }
            }
        }
        DettaglioIndennitaMoraDto indennitaMoraDto = new DettaglioIndennitaMoraDto();
        indennitaMoraDto.setAliquoteApplicate(aliquoteApplicate);
        indennitaMoraDto.setPagamentiMora(creaRigheDovutoVersatoRestante(posizione, Constants.Dettaglio.TIPO_VERSAMENTO_MORA));
        return indennitaMoraDto;
    }

    // ------------ INTERESSE DIFFERITO ------------

    // tassi vigenti alla scadenza della posizione
    private DettaglioInteresseDifferitoDto popolaInteresseDifferito(WTBAccertamentiElencoPosizioni posizione) {
        List<InteresseVigenteDto> interessiVigenti = null == posizione.getScadenza() ? List.of()
                : dettaglioDao.findInteressiRitPagamVigenti(posizione.getScadenza()).stream()
                        .map(customMapper::toInteresseVigenteDto)
                        .toList();
        DettaglioInteresseDifferitoDto interesseDifferitoDto = new DettaglioInteresseDifferitoDto();
        interesseDifferitoDto.setInteressiVigenti(interessiVigenti);
        interesseDifferitoDto.setDettaglioPagamentiInteressi(creaRigheDovutoVersatoRestante(posizione, Constants.Dettaglio.TIPO_VERSAMENTO_INTERESSE));
        return interesseDifferitoDto;
    }

    // ------------ INTERESSI LEGALI ------------

    // tassi dall'anno della posizione; righe: un "dovuti" per anno, poi "versati" (anni pagati) e "restanti"
    private DettaglioInteressiLegaliDto popolaInteressiLegali(WTBAccertamentiElencoPosizioni posizione, BigDecimal interessiLegaliNonPagati) {
        List<TassoInteresseLegaleDto> valoriInteressi = dettaglioDao.findInteressiLegaliDaAnno(posizione.getAnno()).stream()
                .map(customMapper::toTassoInteresseLegaleDto)
                .toList();

        List<WTBAccertamentiInteressiLegaliPerAnno> interessiPerAnno = dettaglioDao.findInteressiLegaliPerAnno(posizione.getId());
        List<RigaRiepilogoImportiDto> righe = new ArrayList<>();
        for (WTBAccertamentiInteressiLegaliPerAnno interesseAnno : interessiPerAnno) {
            RigaRiepilogoImportiDto rigaDovuto = creaRiga(Constants.Dettaglio.INTERESSI_LEGALI_DOVUTI, interesseAnno.getInteresseLegale());
            rigaDovuto.setAnno(interesseAnno.getAnno());
            righe.add(rigaDovuto);
        }
        BigDecimal versati = interessiPerAnno.stream()
                .filter(interesseAnno -> Constants.INTERESSE_LEGALE_PAGATO.equals(interesseAnno.getStatoPagamentoInteresseLegale()))
                .map(WTBAccertamentiInteressiLegaliPerAnno::getInteresseLegale)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        righe.add(creaRigaVersata(Constants.Dettaglio.INTERESSI_LEGALI_VERSATI, versati,
                posizione, Constants.Dettaglio.TIPO_VERSAMENTO_INTERESSI_LEGALI));
        righe.add(creaRiga(Constants.Dettaglio.INTERESSI_LEGALI_RESTANTI, interessiLegaliNonPagati));

        DettaglioInteressiLegaliDto interessiLegaliDto = new DettaglioInteressiLegaliDto();
        interessiLegaliDto.setValoriInteressi(valoriInteressi);
        interessiLegaliDto.setPagamentiInteressiLegali(righe);
        return interessiLegaliDto;
    }

    // ------------ SANZIONI ------------

    // sanzioni vigenti a fine periodo per i giorni dalla scadenza a oggi
    private DettaglioSanzioniDto popolaSanzioni(WTBAccertamentiElencoPosizioni posizione) {
        Integer giorniRitardo = Utility.calcolaGiorniRitardo(posizione.getScadenza());
        List<SanzioneApplicataDto> sanzioniApplicate = new ArrayList<>();
        if (null != giorniRitardo) {
            for (WTBAccertamentiSanzioni sanzione : dettaglioDao.findSanzioniVigenti(giorniRitardo, calcolaDataFinePeriodo(posizione))) {
                sanzioniApplicate.add(customMapper.toSanzioneApplicataDto(sanzione, giorniRitardo));
            }
        }

        DettaglioSanzioniDto sanzioniDto = new DettaglioSanzioniDto();
        sanzioniDto.setSanzioniApplicate(sanzioniApplicate);
        sanzioniDto.setDettaglioPagamentiSanzioni(creaRigheDovutoVersatoRestante(posizione, Constants.Dettaglio.TIPO_VERSAMENTO_SANZIONE));
        return sanzioniDto;
    }

    // ------------ RIGHE DOVUTO / VERSATO / RESTANTE ------------

    // righe dovuto / versato / restante del blocco indicato dal tipo di versamento;
    // il versato con data e metodo dell'ultimo versamento di quel tipo.
    // Gli interessi legali non passano di qui: hanno un "dovuti" per anno (popolaInteressiLegali)
    private List<RigaRiepilogoImportiDto> creaRigheDovutoVersatoRestante(WTBAccertamentiElencoPosizioni posizione, String tipoVersamento) {
        // etichette e importi cambiano per blocco; le tre righe si creano sempre allo stesso modo
        record ImportiBlocco(String etichettaDovuto, BigDecimal dovuto, String etichettaVersato, BigDecimal versato,
                             String etichettaRestante, BigDecimal restante) {
        }
        ImportiBlocco importiBlocco = switch (tipoVersamento) {
            case Constants.Dettaglio.TIPO_VERSAMENTO_MORA -> new ImportiBlocco(
                    Constants.Dettaglio.MORA_DOVUTA, posizione.getMoraTotaleDovuto(),
                    Constants.Dettaglio.MORA_VERSATA, posizione.getMoraTotalePagato(),
                    Constants.Dettaglio.MORA_RESTANTE, posizione.getMoraRestanteDaPagare());
            case Constants.Dettaglio.TIPO_VERSAMENTO_INTERESSE -> new ImportiBlocco(
                    Constants.Dettaglio.INTERESSE_DIFFERITO_DOVUTO, posizione.getInteresseTardatoPagamentoTotaleDovuto(),
                    Constants.Dettaglio.INTERESSE_DIFFERITO_VERSATO, posizione.getInteresseTardatoPagamentoTotalePagato(),
                    Constants.Dettaglio.INTERESSE_DIFFERITO_RESTANTE, posizione.getInteresseTardatoPagamentoRestanteDaPagare());
            case Constants.Dettaglio.TIPO_VERSAMENTO_SANZIONE -> new ImportiBlocco(
                    Constants.Dettaglio.SANZIONE_DOVUTA, posizione.getSanzioneTotaleDovuto(),
                    Constants.Dettaglio.SANZIONE_VERSATA, posizione.getSanzioneTotalePagato(),
                    Constants.Dettaglio.SANZIONE_RESTANTE, posizione.getSanzioneRestanteDaPagare());
            // solo per un errore di programmazione (es. TIPO_VERSAMENTO_INTERESSI_LEGALI): non dipende dai dati
            default -> throw new IllegalArgumentException("Tipo versamento senza righe dovuto/versato/restante: " + tipoVersamento);
        };
        return List.of(
                creaRiga(importiBlocco.etichettaDovuto(), importiBlocco.dovuto()),
                creaRigaVersata(importiBlocco.etichettaVersato(), importiBlocco.versato(), posizione, tipoVersamento),
                creaRiga(importiBlocco.etichettaRestante(), importiBlocco.restante()));
    }

    // importo null mostrato come 0
    private RigaRiepilogoImportiDto creaRiga(String tipoOperazione, BigDecimal importo) {
        RigaRiepilogoImportiDto riga = new RigaRiepilogoImportiDto();
        riga.setTipoOperazione(tipoOperazione);
        riga.setImporto(Utility.getOrDefault(importo, BigDecimal.ZERO));
        return riga;
    }

    // riga "versato" con data e metodo dell'ultimo versamento di quel tipo nel periodo della posizione (se c'è)
    private RigaRiepilogoImportiDto creaRigaVersata(String tipoOperazione, BigDecimal importo,
                                                    WTBAccertamentiElencoPosizioni posizione, String tipoVersamento) {
        RigaRiepilogoImportiDto riga = creaRiga(tipoOperazione, importo);
        // la lista è ordinata per data decrescente: il primo con la data valorizzata è l'ultimo pagamento
        dettaglioDao.findVersamenti(posizione, tipoVersamento).stream()
                .filter(versamento -> null != versamento.getDataPagamento())
                .findFirst()
                .ifPresent(ultimoVersamento -> {
                    riga.setDataUltimoPagamento(ultimoVersamento.getDataPagamento());
                    riga.setMetodoDiPagamento(findDescrizioneTipoPagamento(ultimoVersamento.getTipoPagamento()));
                });
        return riga;
    }

    private LocalDate calcolaDataFinePeriodo(WTBAccertamentiElencoPosizioni posizione) {
        return Utility.calcolaDataFinePeriodo(posizione.getTipoPeriodo(), posizione.getAnno(),
                posizione.getMese(), posizione.getNumQuindicina());
    }
}
