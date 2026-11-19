package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneInteressiController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneInteressiDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseRitPagam;
import it.sogei.tabacchi.accertamenti.business.validation.GestioneInteressiValidator;
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
import java.util.List;

@RequestScoped
@LogMethodInvocation(logParameters = false)
public class GestioneInteressiController implements IGestioneInteressiController {

    @Inject
    private IGestioneInteressiDao gestioneInteressiDao;

    @Inject
    private CustomMapper customMapper;

    // ogni fase di validazione lancia lei la BusinessException se trova errori: qui non servono liste
    @Inject
    private GestioneInteressiValidator validator;

    // ------------ ELENCO INTERESSI ------------

    @Override
    public RisultatoPaginatoDto<InteresseDto> findInteressi(FiltriGestioneParametriDto filtriGestioneParametriDtoRichiesto,
                                                            Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // body JSON null = nessun filtro, come {} (tutti gli interessi con flagCancella = false)
        FiltriGestioneParametriDto filtriGestioneParametriDto = Utility.getOrDefault(filtriGestioneParametriDtoRichiesto, new FiltriGestioneParametriDto());
        validator.validaFiltri(filtriGestioneParametriDto);
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiInteresseRitPagam> interessi = gestioneInteressiDao.findInteressi(filtriGestioneParametriDto, pagina, elementiPerPagina);
        Long totaleElementi = gestioneInteressiDao.countInteressi(filtriGestioneParametriDto);
        return RisultatoPaginatoDto.of(customMapper.toInteressiDto(interessi), totaleElementi, pagina, elementiPerPagina);
    }

    // ------------ SALVATAGGIO INTERESSI ------------

    @Override
    public boolean isInserimentoInteressiAbilitato() {
        return gestioneInteressiDao.countInteressiModificabili(LocalDate.now()) == 0;
    }

    @Transactional
    @Override
    public void scriviInteresse(ModificaInteresseDto modificaInteresseDto, String cfUtente){
        // body e tipo operazione già garantiti da Bean Validation (@Valid sul service)
        switch (modificaInteresseDto.getTipoOperazione()) {
            case INSERIMENTO -> inserisciInteresse(modificaInteresseDto, cfUtente);
            case MODIFICA -> modificaInteresse(modificaInteresseDto);
        }
    }

    // ------------ INSERIMENTO ------------

    private void inserisciInteresse(ModificaInteresseDto modificaInteresseDto, String cfUtente){
        // errore di stato e non sui dati (403): esiste già un interesse futuro o inserito oggi
        if (!isInserimentoInteressiAbilitato()) {
            throw new BusinessException(ExceptionType.INTERESSE_FUTURO_ALREADY_CREATED);
        }
        // id, cancellazione, decorrenza, tasso: tutti gli errori insieme
        validator.validaInserimentoInteresse(modificaInteresseDto);
        // una sola data per tutte le scritture
        LocalDate oggi = LocalDate.now();
        LocalDate dataDecorrenza = modificaInteresseDto.getDataDecorrenza();
        // interesse che precede la nuova decorrenza (null se non c'è): il nuovo tasso non può ripeterlo e alla fine va chiuso
        WTBAccertamentiInteresseRitPagam interessePrecedente = findInteressePrecedente(dataDecorrenza);
        validator.validaTassoDiversoDaPrecedente(modificaInteresseDto.getTassoInteresse(), interessePrecedente);
        gestioneInteressiDao.insertInteresse(creaInteresse(modificaInteresseDto, cfUtente, oggi));
        // il precedente finisce il giorno prima della nuova decorrenza: così ogni giorno ha un solo tasso vigente
        impostaFineValidita(interessePrecedente, dataDecorrenza.minusDays(1), oggi);
    }

    // nuovo tasso: il mapper copia tasso e decorrenza, qui i campi della storicizzazione
    private WTBAccertamentiInteresseRitPagam creaInteresse(ModificaInteresseDto modificaInteresseDto, String cfUtente, LocalDate oggi) {
        WTBAccertamentiInteresseRitPagam nuovoInteresse = customMapper.toInteresseRitPagam(modificaInteresseDto);
        nuovoInteresse.setDataFineVal(Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA);
        nuovoInteresse.setFlagCancella(false);
        nuovoInteresse.setDtInserimento(oggi);
        nuovoInteresse.setCfInserimento(Utility.getOrDefault(cfUtente, Constants.GestioneParametri.CF_INSERIMENTO_SISTEMA));
        return nuovoInteresse;
    }

    // nuova fine validità di un interesse; interesse null = nessun interesse da aggiornare
    private void impostaFineValidita(WTBAccertamentiInteresseRitPagam interesse, LocalDate dataFineValidita, LocalDate oggi) {
        if (null != interesse) {
            interesse.setDataFineVal(dataFineValidita);
            interesse.setDataModifica(oggi);
            gestioneInteressiDao.updateInteresse(interesse);
        }
    }

    // ------------ MODIFICA ------------

    // MODIFICA di un interesse ancora modificabile, riconosciuto dal suo id (una sola riga per decorrenza, niente blocco):
    // aggiorna tasso e decorrenza, oppure lo cancella (flagCancella) riaprendo il precedente
    private void modificaInteresse(ModificaInteresseDto modificaInteresseDto) {
        // id dell'interesse presente
        validator.validaRichiestaModifica(modificaInteresseDto);
        WTBAccertamentiInteresseRitPagam interesseSalvato = gestioneInteressiDao.findInteresseById(modificaInteresseDto.getIdInteresse());
        // esiste, non è cancellato ed è ancora modificabile (decorrenza futura o inserito oggi)
        validator.validaInteresseModificabile(interesseSalvato);
        // decorrenza attuale, quella salvata (non la nuova scelta dall'utente)
        LocalDate dataInizioInteresse = interesseSalvato.getDataInizioVal();
        // interesse che precede quello salvato (null se non c'è): alla fine la sua fine validità si riapre o si sposta
        WTBAccertamentiInteresseRitPagam interessePrecedente = findInteressePrecedente(dataInizioInteresse);
        // una sola data per tutte le scritture
        LocalDate oggi = LocalDate.now();
        // tolto col cestino: niente da validare sui valori
        // NB: Boolean.true se non mi manda il flagCancella la sentinella su Boolean.TRUE
        // restituisce false. se faccio == true invece restituisce NPE.
        if (Boolean.TRUE.equals(modificaInteresseDto.getFlagCancella())) {
            cancellaInteresse(interesseSalvato, oggi);
            // senza questo interesse torna in vigore il precedente: la sua fine si riapre
            impostaFineValidita(interessePrecedente, Constants.GestioneParametri.DATA_FINE_VALIDITA_APERTA, oggi);
        } else {
            // nuova decorrenza (valida, dopo il precedente) e nuovo tasso (valido, diverso dal precedente)
            validator.validaModificaInteresse(modificaInteresseDto, interessePrecedente);
            LocalDate dataDecorrenza = modificaInteresseDto.getDataDecorrenza();
            aggiornaInteresse(interesseSalvato, modificaInteresseDto, oggi);
            // decorrenza cambiata: il precedente ora finisce il giorno prima della nuova
            if (!dataDecorrenza.equals(dataInizioInteresse)) {
                impostaFineValidita(interessePrecedente, dataDecorrenza.minusDays(1), oggi);
            }
        }
    }

    // nuovi tasso e decorrenza sulla riga salvata
    private void aggiornaInteresse(WTBAccertamentiInteresseRitPagam interesseSalvato, ModificaInteresseDto modificaInteresseDto,
                                   LocalDate oggi) {
        interesseSalvato.setTassoInteresse(modificaInteresseDto.getTassoInteresse());
        interesseSalvato.setDataInizioVal(modificaInteresseDto.getDataDecorrenza());
        interesseSalvato.setDataModifica(oggi);
        gestioneInteressiDao.updateInteresse(interesseSalvato);
    }

    // cancellazione logica, come per le aliquote: la riga resta con flagCancella
    private void cancellaInteresse(WTBAccertamentiInteresseRitPagam interesseSalvato, LocalDate oggi) {
        interesseSalvato.setFlagCancella(true);
        interesseSalvato.setDataModifica(oggi);
        gestioneInteressiDao.updateInteresse(interesseSalvato);
    }

    // interesse non cancellato che precede la data indicata; null se non esiste
    private WTBAccertamentiInteresseRitPagam findInteressePrecedente(LocalDate data) {
        LocalDate dataInizioPrecedente = gestioneInteressiDao.findDataInizioInteressePrecedente(data);
        return null != dataInizioPrecedente ? gestioneInteressiDao.findInteresseByDataInizio(dataInizioPrecedente) : null;
    }
}
