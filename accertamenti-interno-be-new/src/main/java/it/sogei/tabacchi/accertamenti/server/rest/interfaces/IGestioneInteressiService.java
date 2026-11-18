package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@Tag(name = "Gestione interessi", description = "Consultazione, inserimento e modifica dei tassi di interesse sul tardato pagamento storicizzati")
public interface IGestioneInteressiService {

    @POST
    @Path(Constants.Endpoint.INTERESSI_LISTA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista paginata dei tassi di interesse sul tardato pagamento",
            description = "Decorrenze più recenti prima; i filtri non valorizzati vengono ignorati.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pagina di tassi trovati",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RisultatoPaginatoDto.class))),
            @APIResponse(responseCode = "400", description = "Filtri non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findInteressi(
            @Parameter(description = "Numero di pagina (da 1); se assente o < 1 restituisce la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) Integer pagina,
            @Parameter(description = "Elementi per pagina; se assente o < 1 usa il default (10), massimo 100", example = "10")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) Integer elementiPerPagina,
            @RequestBody(description = "Filtri; {} = nessun filtro", required = true)
            FiltriGestioneParametriDto filtriGestioneParametriDto);

    @GET
    @Path(Constants.Endpoint.INTERESSI_INSERIMENTO_ABILITATO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Indica se si può inserire un nuovo tasso di interesse",
            description = "false se esiste già un tasso futuro o inserito oggi (ancora modificabile).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "true se l'inserimento è abilitato"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response isInserimentoInteressiAbilitato();

    @POST
    @Path(Constants.Endpoint.INTERESSI_MODIFICA)
    @Consumes(MediaType.APPLICATION_JSON)
    // serve anche senza body di successo: gli errori ({"errorMessage": ...}) sono JSON
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Inserisce o modifica un tasso di interesse sul tardato pagamento",
            description = "INSERIMENTO: crea il tasso dalla data di decorrenza e chiude il tasso precedente. "
                    + "MODIFICA: aggiorna tasso e decorrenza del tasso indicato da idInteresse (ancora modificabile), "
                    + "oppure lo cancella (flagCancella) riaprendo il tasso precedente.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Tasso salvato"),
            @APIResponse(responseCode = "400", description = "Dati non validi (tutti gli errori in un unico messaggio)"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "403", description = "Esiste già un tasso di interesse modificabile")
    })
    Response scriviInteresse(
            @RequestBody(description = "Tipo operazione, id del tasso (solo in MODIFICA, come ricevuto dall'elenco), "
                    + "data di decorrenza, tasso e flag di cancellazione", required = true)
            ModificaInteresseDto modificaInteresseDto);
}
