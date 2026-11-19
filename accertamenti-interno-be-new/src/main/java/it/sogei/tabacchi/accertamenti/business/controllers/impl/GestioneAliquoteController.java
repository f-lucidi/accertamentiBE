package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneAliquoteController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneAliquoteDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaModificaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiAliquotaMora;
import it.sogei.tabacchi.accertamenti.business.validation.GestioneAliquoteValidator;
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

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class GestioneAliquoteController implements IGestioneAliquoteController {

    @Inject
    private IGestioneAliquoteDao gestioneAliquoteDao;

    // ogni fase di validazione lancia lei la BusinessException se trova errori: qui non servono liste
    @Inject
    private GestioneAliquoteValidator validator;

    @Inject
    private CustomMapper customMapper;

    // ------------ ELENCO ALIQUOTE ------------

    @Override
    public RisultatoPaginatoDto<AliquotaDto> findAliquote(FiltriGestioneParametriDto filtriGestioneParametriDtoRichiesto,
                                                          Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // body JSON null = nessun filtro, come {} (tutte le aliquote non cancellate)
        FiltriGestioneParametriDto filtriGestioneParametriDto = Utility.getOrDefault(filtriGestioneParametriDtoRichiesto, new FiltriGestioneParametriDto());
        validator.validaFiltri(filtriGestioneParametriDto);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiAliquotaMora> aliquote = gestioneAliquoteDao.findAliquote(filtriGestioneParametriDto, pagina, elementiPerPagina);
        Long totaleElementi = gestioneAliquoteDao.countAliquote(filtriGestioneParametriDto);
        return RisultatoPaginatoDto.of(customMapper.toAliquoteDto(aliquote), totaleElementi, pagina, elementiPerPagina);
    }

    // ------------ DETTAGLIO GRUPPO ------------

    // idGruppo -> decorrenza come in modifica (20270101 -> 2027-01-01; non è una data valida -> 400).
    // Gruppo senza righe = errore di stato con ExceptionType (404): resta nel controller
    @Override
    public List<AliquotaDto> findAliquoteByIdGruppo(Integer idGruppo) {
        List<WTBAccertamentiAliquotaMora> gruppo = gestioneAliquoteDao.findAliquoteByDataInizio(Utility.toDataInizioGruppo(idGruppo));
        if (gruppo.isEmpty()) {
            throw new BusinessException(ExceptionType.NOT_FOUND);
        } else {
            return customMapper.toAliquoteDto(gruppo);
        }
    }

    // ------------ SALVATAGGIO ALIQUOTE ------------

    @Override
    public boolean isInserimentoAliquoteAbilitato() {
        return gestioneAliquoteDao.countAliquoteModificabili(LocalDate.now()) == 0;
    }

    // @Transactional: le scritture del gruppo e la nuova fine del gruppo precedente vanno a buon fine insieme;
    // un errore a metà (es. DB) annulla anche le scritture già fatte
    @Override
    @Transactional
    public void salvaAliquote(ModificaAliquoteDto modificaAliquote, String cfUtente) {
        // body e tipo operazione già garantiti da Bean Validation (@Valid sul service)
        switch (modificaAliquote.getTipoOperazione()) {
            case INSERIMENTO -> inserisciGruppoAliquote(modificaAliquote, cfUtente);
            case MODIFICA -> modificaGruppoAliquote(modificaAliquote, cfUtente);
        }
    }

    // ------------ INSERIMENTO ------------

    private void inserisciGruppoAliquote(ModificaAliquoteDto modificaAliquote, String cfUtente) {
        // errore di stato e non sui dati (403): esiste già un gruppo futuro o inserito oggi
        if (!isInserimentoAliquoteAbilitato()) {
            throw new BusinessException(ExceptionType.ALIQUOTA_FUTURA_ALREADY_CREATED);
        } else {
            validator.validaInserimentoAliquote(modificaAliquote);
            LocalDate oggi = LocalDate.now();
            LocalDate dataDecorrenza = modificaAliquote.getDataDecorrenza();
            for (AliquotaModificaDto aliquotaModifica : modificaAliquote.getScaglioni()) {
                gestioneAliquoteDao.insertAliquota(creaAliquota(aliquotaModifica, dataDecorrenza, cfUtente, oggi));
            }
            chiudiGruppoPrecedente(dataDecorrenza, oggi);
        }
    }

    // ------------ MODIFICA ------------

    // MODIFICA di un gruppo ancora modificabile: aggiorna, aggiunge o cancella righe.
    // Il gruppo si riconosce dall'idGruppo che il frontend rimanda (la decorrenza attuale del gruppo)
    private void modificaGruppoAliquote(ModificaAliquoteDto modificaAliquote, String cfUtente) {
        // id del gruppo presente, righe presenti e non vuote
        validator.validaElencoModifica(modificaAliquote);
        // decorrenza attuale del gruppo, ricavata dal suo id (20270101 -> 2027-01-01): è quella salvata, non la nuova scelta dall'utente
        LocalDate dataInizioGruppo = Utility.toDataInizioGruppo(modificaAliquote.getIdGruppo());
        // tutte le righe non cancellate del gruppo, così come sono a DB
        List<WTBAccertamentiAliquotaMora> gruppoSalvato = gestioneAliquoteDao.findAliquoteByDataInizio(dataInizioGruppo);
        // il gruppo esiste ed è ancora modificabile (decorrenza futura o inserito oggi)
        validator.validaGruppoModificabile(gruppoSalvato, dataInizioGruppo);
        // inizio del gruppo precedente (null se non c'è): alla fine la sua data di fine si sposta
        // (decorrenza cambiata) o si riapre (gruppo cancellato)
        LocalDate dataInizioPrecedente = gestioneAliquoteDao.findDataInizioGruppoPrecedente(dataInizioGruppo);
        // decorrenza, righe del gruppo complete, valori e giorni consecutivi delle righe che restano
        validator.validaModificaAliquote(modificaAliquote, gruppoSalvato, dataInizioPrecedente);
        applicaModificaGruppo(modificaAliquote, gruppoSalvato, dataInizioGruppo, dataInizioPrecedente, cfUtente);
    }

    // scritture della MODIFICA, a validazione superata: aggiorna, aggiunge o cancella le righe e
    // sposta (decorrenza cambiata) o riapre (gruppo cancellato) la fine del gruppo precedente
    private void applicaModificaGruppo(ModificaAliquoteDto modificaAliquote, List<WTBAccertamentiAliquotaMora> gruppoSalvato,
                                       LocalDate dataInizioGruppo, LocalDate dataInizioPrecedente, String cfUtente) {
        // una sola data per tutte le scritture, anche se la richiesta finisce a cavallo della mezzanotte
        LocalDate oggi = LocalDate.now();
        // nuova decorrenza scelta dall'utente (uguale a dataInizioGruppo se non l'ha cambiata)
        LocalDate dataDecorrenza = modificaAliquote.getDataDecorrenza();
        // righe salvate indicizzate per id: nel ciclo dopo si trovano subito, senza altre query
        Map<Long, WTBAccertamentiAliquotaMora> aliquoteSalvatePerId = new HashMap<>();
        // riempie la mappa con le righe lette dal DB
        for (WTBAccertamentiAliquotaMora aliquotaSalvata : gruppoSalvato) {
            // chiave = id della riga, valore = la riga
            aliquoteSalvatePerId.put(aliquotaSalvata.getId(), aliquotaSalvata);
        }
        // diventa false appena una riga resta nel gruppo (aggiornata o inserita)
        boolean isGruppoCancellato = true;
        // una riga della richiesta alla volta, nell'ordine del frontend
        for (AliquotaModificaDto aliquotaModifica : modificaAliquote.getScaglioni()) {
            // true se l'utente l'ha tolta col cestino
            boolean isDaCancellare = Boolean.TRUE.equals(aliquotaModifica.getFlagCancella());
            // senza id: riga aggiunta adesso, il DB non l'ha mai vista
            if (null == aliquotaModifica.getIdAliquota()) {
                // se arriva già cancellata non c'è niente da fare e si ignora
                if (!isDaCancellare) {
                    // la inserisce con la nuova decorrenza e fine validità aperta
                    gestioneAliquoteDao.insertAliquota(creaAliquota(aliquotaModifica, dataDecorrenza, cfUtente, oggi));
                    // il gruppo ha almeno una riga: non è un'eliminazione
                    isGruppoCancellato = false;
                }
            // con id e tolta col cestino: riga salvata da cancellare
            } else if (isDaCancellare) {
                // cancellazione logica della riga salvata (flagCancella a true)
                cancellaAliquota(aliquoteSalvatePerId.get(aliquotaModifica.getIdAliquota()), oggi);
            // con id e non cancellata: riga salvata da aggiornare
            } else {
                // nuovi valori, giorni e decorrenza sulla riga salvata
                aggiornaAliquota(aliquoteSalvatePerId.get(aliquotaModifica.getIdAliquota()), aliquotaModifica, dataDecorrenza, oggi);
                // il gruppo ha almeno una riga: non è un'eliminazione
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
    private void aggiornaAliquota(WTBAccertamentiAliquotaMora aliquotaSalvata, AliquotaModificaDto aliquotaModifica,
                                  LocalDate dataDecorrenza, LocalDate oggi) {
        aliquotaSalvata.setAliquota(aliquotaModifica.getValoreAliquota());
        aliquotaSalvata.setGiorniRitardoDa(aliquotaModifica.getGiorniDa());
        aliquotaSalvata.setGiorniRitardoA(aliquotaModifica.getGiorniA());
        aliquotaSalvata.setDataInizioVal(dataDecorrenza);
        aliquotaSalvata.setDataModifica(oggi);
        gestioneAliquoteDao.updateAliquota(aliquotaSalvata);
    }

    // cancellazione logica, come il resto della tabella: la riga resta con flagCancella
    private void cancellaAliquota(WTBAccertamentiAliquotaMora aliquotaSalvata, LocalDate oggi) {
        aliquotaSalvata.setFlagCancella(true);
        aliquotaSalvata.setDataModifica(oggi);
        gestioneAliquoteDao.updateAliquota(aliquotaSalvata);
    }

    // nuova riga del gruppo: il mapper copia valori e giorni, qui i campi della storicizzazione
    private WTBAccertamentiAliquotaMora creaAliquota(AliquotaModificaDto aliquotaModifica, LocalDate dataDecorrenza,
                                                    String cfUtente, LocalDate oggi) {
        WTBAccertamentiAliquotaMora nuovaAliquota = customMapper.toAliquotaMora(aliquotaModifica, dataDecorrenza);
        nuovaAliquota.setDataFineVal(Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA);
        nuovaAliquota.setFlagCancella(false);
        nuovaAliquota.setDtInserimento(oggi);
        nuovaAliquota.setCfInserimento(Utility.getOrDefault(cfUtente, Constants.GestioneParametri.CF_INSERIMENTO_SISTEMA));
        return nuovaAliquota;
    }

    // il gruppo che precede la nuova decorrenza finisce il giorno prima: così ogni giorno ha un solo gruppo vigente
    private void chiudiGruppoPrecedente(LocalDate dataDecorrenza, LocalDate oggi) {
        LocalDate dataInizioPrecedente = gestioneAliquoteDao.findDataInizioGruppoPrecedente(dataDecorrenza);
        impostaFineValiditaGruppo(dataInizioPrecedente, dataDecorrenza.minusDays(1), oggi);
    }

    // nuova fine validità per tutte le righe del gruppo; dataInizioGruppo null = nessun gruppo, non fa niente
    private void impostaFineValiditaGruppo(LocalDate dataInizioGruppo, LocalDate dataFineValidita, LocalDate oggi) {
        if (null == dataInizioGruppo) {
            return;
        }
        for (WTBAccertamentiAliquotaMora aliquota : gestioneAliquoteDao.findAliquoteByDataInizio(dataInizioGruppo)) {
            aliquota.setDataFineVal(dataFineValidita);
            aliquota.setDataModifica(oggi);
            gestioneAliquoteDao.updateAliquota(aliquota);
        }
    }
}
