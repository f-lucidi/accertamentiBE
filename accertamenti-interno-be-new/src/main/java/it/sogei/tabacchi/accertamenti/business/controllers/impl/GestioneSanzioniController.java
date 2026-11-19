package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneSanzioniController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneSanzioniDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiSanzioni;
import it.sogei.tabacchi.accertamenti.business.validation.GestioneSanzioniValidator;
import it.sogei.tabacchi.accertamenti.mappers.CustomMapper;
import it.sogei.tabacchi.accertamenti.server.exceptions.BusinessException;
import it.sogei.tabacchi.accertamenti.server.exceptions.enums.ExceptionType;
import it.sogei.tabacchi.accertamenti.server.interceptors.LogMethodInvocation;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import it.sogei.tabacchi.accertamenti.utility.Utility;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// stessa logica delle aliquote (GestioneAliquoteController): si salva sempre l'intero set di sanzioni (analisi: "set di sanzioni").
// Differenze:
// - in modifica il gruppo non arriva come idGruppo: si ricava dalla decorrenza della prima sanzione già salvata della richiesta
//   (come nel BE e nel FE del collega)
// - la cancellazione di uno scaglione è una DELETE (la tabella non ha FLAG_CANCELLA)
// - c'è moltiplicatoPerGiorni (Boolean nel DTO, 1/0 a DB: Utility.toMoltiplicatoPerGiorniRitardo)
@RequestScoped
@LogMethodInvocation(logParameters = false)
public class GestioneSanzioniController implements IGestioneSanzioniController {

    @Inject
    private IGestioneSanzioniDao gestioneSanzioniDao;

    // ogni fase di validazione lancia lei la BusinessException se trova errori: qui non servono liste
    @Inject
    private GestioneSanzioniValidator validator;

    @Inject
    private CustomMapper customMapper;

    // ------------ ELENCO SANZIONI ------------

    @Override
    public RisultatoPaginatoDto<SanzioneDto> findSanzioni(FiltriGestioneParametriDto filtriGestioneParametriDtoRichiesto,
                                                          Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // body JSON null = nessun filtro, come {} (tutte le sanzioni)
        FiltriGestioneParametriDto filtriGestioneParametriDto = Utility.getOrDefault(filtriGestioneParametriDtoRichiesto, new FiltriGestioneParametriDto());
        validator.validaFiltri(filtriGestioneParametriDto);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiSanzioni> sanzioni = gestioneSanzioniDao.findSanzioni(filtriGestioneParametriDto, pagina, elementiPerPagina);
        Long totaleElementi = gestioneSanzioniDao.countSanzioni(filtriGestioneParametriDto);
        return RisultatoPaginatoDto.of(customMapper.toSanzioniDto(sanzioni), totaleElementi, pagina, elementiPerPagina);
    }

    // ------------ DETTAGLIO GRUPPO ------------

    // idGruppo -> decorrenza come per le aliquote (20270101 -> 2027-01-01; non è una data valida -> 400).
    // Gruppo senza righe = errore di stato con ExceptionType (404): resta nel controller
    @Override
    public List<SanzioneDto> findSanzioniByIdGruppo(Integer idGruppo) {
        List<WTBAccertamentiSanzioni> gruppo = gestioneSanzioniDao.findSanzioniByDataInizio(Utility.toDataInizioGruppo(idGruppo));
        if (gruppo.isEmpty()) {
            throw new BusinessException(ExceptionType.NOT_FOUND);
        } else {
            return customMapper.toSanzioniDto(gruppo);
        }
    }

    // ------------ SALVATAGGIO SANZIONI ------------

    @Override
    public boolean isInserimentoSanzioniAbilitato() {
        return gestioneSanzioniDao.countSanzioniModificabili(LocalDate.now()) == 0;
    }

    // @Transactional: le scritture del gruppo e la nuova fine del gruppo precedente vanno a buon fine insieme;
    // un errore a metà (es. DB) annulla anche le scritture già fatte
    @Override
    @Transactional
    public void salvaSanzioni(ModificaSanzioniDto modificaSanzioniDto, String cfUtente) {
        // body e tipo operazione già garantiti da Bean Validation (@Valid sul service)
        switch (modificaSanzioniDto.getTipoOperazione()) {
            case INSERIMENTO -> inserisciGruppoSanzioni(modificaSanzioniDto, cfUtente);
            case MODIFICA -> modificaGruppoSanzioni(modificaSanzioniDto, cfUtente);
        }
    }

    // ------------ INSERIMENTO ------------

    private void inserisciGruppoSanzioni(ModificaSanzioniDto modificaSanzioniDto, String cfUtente) {
        // errore di stato e non sui dati (403): esiste già un gruppo futuro o inserito oggi
        if (!isInserimentoSanzioniAbilitato()) {
            throw new BusinessException(ExceptionType.SANZIONE_FUTURA_ALREADY_CREATED);
        } else {
            validator.validaInserimentoSanzioni(modificaSanzioniDto);
            LocalDate oggi = LocalDate.now();
            LocalDate dataDecorrenza = modificaSanzioniDto.getDataDecorrenza();
            for (SanzioneModificaDto sanzioneModifica : modificaSanzioniDto.getScaglioni()) {
                gestioneSanzioniDao.insertSanzione(creaSanzione(sanzioneModifica, dataDecorrenza, cfUtente, oggi));
            }
            // il gruppo che precede la nuova decorrenza finisce il giorno prima: così ogni giorno ha un solo gruppo vigente
            impostaFineValiditaGruppo(gestioneSanzioniDao.findDataInizioGruppoPrecedente(dataDecorrenza), dataDecorrenza.minusDays(1), oggi);
        }
    }

    // ------------ MODIFICA ------------

    // MODIFICA di un gruppo ancora modificabile: aggiorna, aggiunge o cancella righe.
    // Il gruppo si riconosce dalle sanzioni già salvate che il frontend rimanda
    private void modificaGruppoSanzioni(ModificaSanzioniDto modificaSanzioniDto, String cfUtente) {
        // almeno una sanzione già salvata, righe presenti e non vuote
        validator.validaElencoModifica(modificaSanzioniDto);
        // decorrenza attuale del gruppo (quella salvata, non la nuova scelta dall'utente); null se la sanzione non è a DB
        LocalDate dataInizioGruppo = findDataInizioGruppo(modificaSanzioniDto);
        // tutte le righe del gruppo, così come sono a DB; nessuna se la sanzione indicata non esiste
        List<WTBAccertamentiSanzioni> gruppoSalvato = null == dataInizioGruppo
                ? List.of()
                : gestioneSanzioniDao.findSanzioniByDataInizio(dataInizioGruppo);
        // il gruppo esiste ed è ancora modificabile (decorrenza futura o inserito oggi)
        validator.validaGruppoModificabile(gruppoSalvato, dataInizioGruppo);
        // inizio del gruppo precedente (null se non c'è): alla fine la sua data di fine si sposta
        // (decorrenza cambiata) o si riapre (gruppo cancellato)
        LocalDate dataInizioPrecedente = gestioneSanzioniDao.findDataInizioGruppoPrecedente(dataInizioGruppo);
        // decorrenza, righe del gruppo complete, valori e giorni consecutivi delle righe che restano
        validator.validaModificaSanzioni(modificaSanzioniDto, gruppoSalvato, dataInizioPrecedente);
        applicaModificaGruppo(modificaSanzioniDto, gruppoSalvato, dataInizioGruppo, dataInizioPrecedente, cfUtente);
    }

    // decorrenza letta dalla prima sanzione già salvata della richiesta (validaElencoModifica garantisce che ci sia);
    // null se quella sanzione non è a DB
    private LocalDate findDataInizioGruppo(ModificaSanzioniDto modificaSanzioniDto) {
        Long idPrimaSanzioneSalvata = modificaSanzioniDto.getScaglioni().stream()
                .map(SanzioneModificaDto::getIdSanzione)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        WTBAccertamentiSanzioni sanzioneSalvata = null == idPrimaSanzioneSalvata ? null : gestioneSanzioniDao.findSanzioneById(idPrimaSanzioneSalvata);
        return null == sanzioneSalvata ? null : sanzioneSalvata.getDataInizioVal();
    }

    // scritture della MODIFICA, a validazione superata: aggiorna, aggiunge o cancella (DELETE) le righe e
    // sposta (decorrenza cambiata) o riapre (gruppo cancellato) la fine del gruppo precedente
    private void applicaModificaGruppo(ModificaSanzioniDto modificaSanzioniDto, List<WTBAccertamentiSanzioni> gruppoSalvato,
                                       LocalDate dataInizioGruppo, LocalDate dataInizioPrecedente, String cfUtente) {
        // una sola data per tutte le scritture, anche se la richiesta finisce a cavallo della mezzanotte
        LocalDate oggi = LocalDate.now();
        // nuova decorrenza scelta dall'utente (uguale a dataInizioGruppo se non l'ha cambiata)
        LocalDate dataDecorrenza = modificaSanzioniDto.getDataDecorrenza();
        // righe salvate indicizzate per id: nel ciclo dopo si trovano subito, senza altre query
        Map<Long, WTBAccertamentiSanzioni> sanzioniSalvatePerId = new HashMap<>();
        for (WTBAccertamentiSanzioni sanzioneSalvata : gruppoSalvato) {
            sanzioniSalvatePerId.put(sanzioneSalvata.getId(), sanzioneSalvata);
        }
        // diventa false appena una riga resta nel gruppo (aggiornata o inserita)
        boolean isGruppoCancellato = true;
        // una riga della richiesta alla volta, nell'ordine del frontend
        for (SanzioneModificaDto sanzioneModifica : modificaSanzioniDto.getScaglioni()) {
            // true se l'utente l'ha tolta col cestino
            boolean isDaCancellare = Boolean.TRUE.equals(sanzioneModifica.getFlagCancella());
            // senza id: riga aggiunta adesso, il DB non l'ha mai vista
            if (null == sanzioneModifica.getIdSanzione()) {
                // se arriva già cancellata non c'è niente da fare e si ignora
                if (!isDaCancellare) {
                    gestioneSanzioniDao.insertSanzione(creaSanzione(sanzioneModifica, dataDecorrenza, cfUtente, oggi));
                    isGruppoCancellato = false;
                }
            // con id e tolta col cestino: riga salvata da cancellare, con una DELETE
            } else if (isDaCancellare) {
                gestioneSanzioniDao.deleteSanzione(sanzioniSalvatePerId.get(sanzioneModifica.getIdSanzione()));
            // con id e non cancellata: riga salvata da aggiornare
            } else {
                aggiornaSanzione(sanzioniSalvatePerId.get(sanzioneModifica.getIdSanzione()), sanzioneModifica, dataDecorrenza, oggi);
                isGruppoCancellato = false;
            }
        }
        // nessuna riga rimasta: è l'eliminazione dell'intero gruppo
        if (isGruppoCancellato) {
            // senza questo gruppo torna in vigore il precedente: la sua fine si riapre
            impostaFineValiditaGruppo(dataInizioPrecedente, Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA, oggi);
        // il gruppo resta ma l'utente ha cambiato la decorrenza
        } else if (!dataDecorrenza.equals(dataInizioGruppo)) {
            // il precedente ora finisce il giorno prima della nuova decorrenza
            impostaFineValiditaGruppo(dataInizioPrecedente, dataDecorrenza.minusDays(1), oggi);
        }
    }

    // la decorrenza vale per tutto il gruppo: si aggiorna anche se è cambiata
    private void aggiornaSanzione(WTBAccertamentiSanzioni sanzioneSalvata, SanzioneModificaDto sanzioneModifica,
                                  LocalDate dataDecorrenza, LocalDate oggi) {
        sanzioneSalvata.setSanzione(sanzioneModifica.getValoreSanzione());
        sanzioneSalvata.setMoltiplicatoPerGiorniRitardo(Utility.toMoltiplicatoPerGiorniRitardo(sanzioneModifica.getMoltiplicatoPerGiorni()));
        sanzioneSalvata.setGiorniRitardoDa(sanzioneModifica.getGiorniDa());
        sanzioneSalvata.setGiorniRitardoA(sanzioneModifica.getGiorniA());
        sanzioneSalvata.setDataInizioVal(dataDecorrenza);
        sanzioneSalvata.setDtModifica(oggi);
        gestioneSanzioniDao.updateSanzione(sanzioneSalvata);
    }

    // nuova riga del gruppo: il mapper copia valori, giorni e moltiplicatore, qui i campi della storicizzazione
    private WTBAccertamentiSanzioni creaSanzione(SanzioneModificaDto sanzioneModifica, LocalDate dataDecorrenza,
                                                 String cfUtente, LocalDate oggi) {
        WTBAccertamentiSanzioni nuovaSanzione = customMapper.toSanzione(sanzioneModifica, dataDecorrenza);
        nuovaSanzione.setDataFineVal(Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA);
        nuovaSanzione.setDtInserimento(oggi);
        nuovaSanzione.setCfInserimento(Utility.getOrDefault(cfUtente, Constants.GestioneParametri.CF_INSERIMENTO_SISTEMA));
        return nuovaSanzione;
    }

    // nuova fine validità per tutte le righe del gruppo; dataInizioGruppo null = nessun gruppo, non fa niente
    private void impostaFineValiditaGruppo(LocalDate dataInizioGruppo, LocalDate dataFineValidita, LocalDate oggi) {
        if (null != dataInizioGruppo) {
            for (WTBAccertamentiSanzioni sanzione : gestioneSanzioniDao.findSanzioniByDataInizio(dataInizioGruppo)) {
                sanzione.setDataFineVal(dataFineValidita);
                sanzione.setDtModifica(oggi);
                gestioneSanzioniDao.updateSanzione(sanzione);
            }
        }
    }
}
