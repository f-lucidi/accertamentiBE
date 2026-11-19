package it.sogei.tabacchi.accertamenti.business.controllers.impl;

import it.sogei.tabacchi.accertamenti.business.controllers.interfaces.IGestioneInteressiLegaliController;
import it.sogei.tabacchi.accertamenti.business.dao.interfaces.IGestioneInteressiLegaliDao;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PrimoAnnoMancanteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.entity.WTBAccertamentiInteresseLegale;
import it.sogei.tabacchi.accertamenti.business.validation.GestioneInteressiLegaliValidator;
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
public class GestioneInteressiLegaliController implements IGestioneInteressiLegaliController {

    @Inject
    private IGestioneInteressiLegaliDao gestioneInteressiLegaliDao;

    @Inject
    private CustomMapper customMapper;

    @Inject
    private GestioneInteressiLegaliValidator validator;

    // ------------ ELENCO INTERESSI LEGALI ------------

    // unico filtro: anno. Niente validazione: un anno qualsiasi (o null) è un filtro valido
    @Override
    public RisultatoPaginatoDto<InteresseLegaleDto> findInteressiLegali(FiltriGestioneParametriDto filtriGestioneParametriDtoRichiesto,
                                                                        Integer paginaRichiesta, Integer elementiPerPaginaRichiesti) {
        // body JSON null = nessun filtro, come {} (tutti gli interessi legali non cancellati)
        FiltriGestioneParametriDto filtriGestioneParametriDto = Utility.getOrDefault(filtriGestioneParametriDtoRichiesto, new FiltriGestioneParametriDto());
        int pagina = Utility.normalizzaPagina(paginaRichiesta);
        int elementiPerPagina = Utility.normalizzaElementiPerPagina(elementiPerPaginaRichiesti);
        List<WTBAccertamentiInteresseLegale> interessiLegali = gestioneInteressiLegaliDao.findInteressiLegali(filtriGestioneParametriDto, pagina, elementiPerPagina);
        Long totaleElementi = gestioneInteressiLegaliDao.countInteressiLegali(filtriGestioneParametriDto);
        return RisultatoPaginatoDto.of(customMapper.toInteressiLegaliDto(interessiLegali), totaleElementi, pagina, elementiPerPagina);
    }

    // ------------ DETTAGLIO INTERESSE LEGALE ------------

    // findById restituisce anche le righe cancellate (FLAG_CANCELLA 1 o 2): per il frontend una riga cancellata non esiste.
    // Errore di stato con ExceptionType (404): resta nel controller
    @Override
    public InteresseLegaleDto findInteresseLegaleById(Long idInteresse) {
        WTBAccertamentiInteresseLegale interesseLegale = gestioneInteressiLegaliDao.findInteresseLegaleById(idInteresse);
        if (null == interesseLegale || !Constants.GestioneParametri.FLAG_CANCELLA_NO.equals(interesseLegale.getFlagCancella())) {
            throw new BusinessException(ExceptionType.NOT_FOUND);
        } else {
            return customMapper.toInteresseLegaleDto(interesseLegale);
        }
    }

    // ------------ PRIMO ANNO MANCANTE ------------

    // anno: il primo "buco" negli anni con un tasso, altrimenti l'ultimo anno + 1.
    // tasso proposto: quello dell'anno corrente se c'è, altrimenti il default (0.2)
    @Override
    public PrimoAnnoMancanteDto findPrimoAnnoMancante() {
        WTBAccertamentiInteresseLegale interesseAnnoCorrente = gestioneInteressiLegaliDao.findInteresseLegaleByAnno(LocalDate.now().getYear());
        return new PrimoAnnoMancanteDto(findPrimoAnnoInteresseLegaleMancante(),
                null != interesseAnnoCorrente
                        ? interesseAnnoCorrente.getTassoInteresse()
                        : Constants.GestioneParametri.TASSO_INTERESSE_LEGALE_DEFAULT);
    }

    // scorre gli anni dal più vecchio: il primo anno che salta quello atteso (precedente + 1) rivela il buco.
    // Es. 2022, 2023, 2025 -> 2024; 2022, 2023, 2024 -> 2025; nessun tasso a sistema -> anno corrente
    // (analisi: "il primo anno per il quale non è ancora definito un tasso")
    private Integer findPrimoAnnoInteresseLegaleMancante() {
        Integer annoAtteso = null;
        Integer primoAnnoMancante = null;
        for (Integer anno : gestioneInteressiLegaliDao.findAnniInteressiLegaliCrescenti()) {
            // trovato il buco, gli anni successivi non contano più
            if (null == primoAnnoMancante) {
                if (null != annoAtteso && anno > annoAtteso) {
                    primoAnnoMancante = annoAtteso;
                } else {
                    annoAtteso = anno + 1;
                }
            }
        }
        // nessun buco: l'anno dopo l'ultimo; nessun anno (annoAtteso mai valorizzato): l'anno corrente
        return null != primoAnnoMancante
                ? primoAnnoMancante
                : null != annoAtteso
                ? annoAtteso
                : LocalDate.now().getYear();
    }

    // ------------ SALVATAGGIO INTERESSI LEGALI ------------

    // @Transactional: la scrittura avviene tutta o niente (un errore del DB annulla tutto)
    @Override
    @Transactional
    public void scriviInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, String cfUtente) {
        // body e tipo operazione già garantiti da Bean Validation (@Valid sul service).
        // Ogni fase di validazione lancia lei la BusinessException se trova errori: qui non serve una lista
        switch (modificaInteresseLegaleDto.getTipoOperazione()) {
            case INSERIMENTO -> inserisciInteresseLegale(modificaInteresseLegaleDto, cfUtente);
            case MODIFICA -> modificaInteresseLegale(modificaInteresseLegaleDto);
        }
    }

    // ------------ INSERIMENTO ------------

    private void inserisciInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, String cfUtente) {
        // id, cancellazione, anno, tasso: tutti gli errori insieme
        validator.validaInserimentoInteresseLegale(modificaInteresseLegaleDto);
        // un solo tasso per anno: l'anno (ora valido) non deve averne già uno
        boolean isInteressePresenteInAnno = gestioneInteressiLegaliDao.countInteressiLegaliByAnno(modificaInteresseLegaleDto.getAnno()) > 0;
        validator.validaAnnoLibero(modificaInteresseLegaleDto.getAnno(), isInteressePresenteInAnno);
        gestioneInteressiLegaliDao.insertInteresseLegale(creaInteresseLegale(modificaInteresseLegaleDto, cfUtente));
    }

    // nuovo tasso: il mapper copia anno e tasso, qui i campi di inserimento
    private WTBAccertamentiInteresseLegale creaInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto, String cfUtente) {
        WTBAccertamentiInteresseLegale nuovoInteresseLegale = customMapper.toInteresseLegale(modificaInteresseLegaleDto);
        nuovoInteresseLegale.setFlagCancella(Constants.GestioneParametri.FLAG_CANCELLA_NO);
        nuovoInteresseLegale.setDtInserimento(LocalDate.now());
        nuovoInteresseLegale.setCfInserimento(Utility.getOrDefault(cfUtente, Constants.GestioneParametri.CF_INSERIMENTO_SISTEMA));
        return nuovoInteresseLegale;
    }

    // ------------ MODIFICA ------------

    // MODIFICA di un interesse legale ancora modificabile, riconosciuto dal suo id:
    // aggiorna il tasso, oppure lo cancella (flagCancella). L'anno non si modifica,
    // quindi l'anno della richiesta è ignorato.
    private void modificaInteresseLegale(ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        // id dell'interesse presente
        validator.validaRichiestaModifica(modificaInteresseLegaleDto);
        WTBAccertamentiInteresseLegale interesseLegaleSalvato = gestioneInteressiLegaliDao.findInteresseLegaleById(modificaInteresseLegaleDto.getIdInteresse());
        // esiste, non è cancellato ed è ancora modificabile (anno futuro o inserito oggi)
        validator.validaInteresseLegaleModificabile(interesseLegaleSalvato);
        // tolto col cestino: niente da validare sui valori
        if (Boolean.TRUE.equals(modificaInteresseLegaleDto.getFlagCancella())) {
            cancellaInteresseLegale(interesseLegaleSalvato);
        } else {
            // solo il tasso
            validator.validaModificaInteresseLegale(modificaInteresseLegaleDto);
            aggiornaInteresseLegale(interesseLegaleSalvato, modificaInteresseLegaleDto);
        }
    }

    // nuovo tasso sulla riga salvata: l'anno resta quello salvato
    private void aggiornaInteresseLegale(WTBAccertamentiInteresseLegale interesseLegaleSalvato, ModificaInteresseLegaleDto modificaInteresseLegaleDto) {
        interesseLegaleSalvato.setTassoInteresse(modificaInteresseLegaleDto.getTassoInteresseLegale());
        interesseLegaleSalvato.setDataModifica(LocalDate.now());
        gestioneInteressiLegaliDao.updateInteresseLegale(interesseLegaleSalvato);
    }

    // cancellazione logica, come per aliquote e interessi: la riga resta con il flag
    private void cancellaInteresseLegale(WTBAccertamentiInteresseLegale interesseLegaleSalvato) {
        interesseLegaleSalvato.setFlagCancella(Constants.GestioneParametri.FLAG_CANCELLA_SI);
        interesseLegaleSalvato.setDataModifica(LocalDate.now());
        gestioneInteressiLegaliDao.updateInteresseLegale(interesseLegaleSalvato);
    }
}
