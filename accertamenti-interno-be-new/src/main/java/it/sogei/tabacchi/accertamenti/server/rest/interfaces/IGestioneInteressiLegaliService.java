package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.InteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaInteresseLegaleDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.PrimoAnnoMancanteDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
@Tag(name = "Gestione interessi legali", description = "Consultazione, inserimento e modifica dei tassi di interesse legale (uno per anno)")
public interface IGestioneInteressiLegaliService {

    @POST
    @Path(Constants.Endpoint.INTERESSI_LEGALI_LISTA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista paginata dei tassi di interesse legale",
            description = "Anni più recenti prima; unico filtro usato: anno (gli altri campi sono ignorati).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pagina di tassi trovati",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RisultatoPaginatoDto.class))),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findInteressiLegali(
            @Parameter(description = "Numero di pagina (da 1); se assente o < 1 restituisce la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) Integer pagina,
            @Parameter(description = "Elementi per pagina; se assente o < 1 usa il default (10), massimo 100", example = "10")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) Integer elementiPerPagina,
            @RequestBody(description = "Filtri; {} = nessun filtro", required = true)
            FiltriGestioneParametriDto filtriGestioneParametriDto);

    @GET
    @Path(Constants.Endpoint.INTERESSI_LEGALI_DETTAGLIO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Dettaglio di un tasso di interesse legale",
            description = "Il tasso con l'id indicato; un tasso cancellato non viene restituito.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Tasso trovato",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = InteresseLegaleDto.class))),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "404", description = "Tasso non trovato o cancellato")
    })
    Response findInteresseLegaleById(
            @Parameter(description = "Id del tasso, come ricevuto dall'elenco", required = true, example = "1")
            @PathParam(Constants.Parametri.ID_INTERESSE) Long idInteresse);

    @GET
    @Path(Constants.Endpoint.INTERESSI_LEGALI_PRIMO_ANNO_MANCANTE)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Valori per precompilare l'inserimento di un tasso di interesse legale",
            description = "Primo anno senza tasso (o l'ultimo anno + 1) e tasso proposto: quello dell'anno corrente se c'è, altrimenti 0.2.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Anno e tasso proposti",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = PrimoAnnoMancanteDto.class))),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findPrimoAnnoMancante();

    @POST
    @Path(Constants.Endpoint.INTERESSI_LEGALI_MODIFICA)
    @Consumes(MediaType.APPLICATION_JSON)
    // serve anche senza body di successo: gli errori ({"errorMessage": ...}) sono JSON
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Inserisce o modifica un tasso di interesse legale",
            description = "INSERIMENTO: crea il tasso per l'anno indicato (che non deve averne già uno). "
                    + "MODIFICA: aggiorna il valore del tasso indicato da idInteresse (ancora modificabile; l'anno non si modifica e viene ignorato), "
                    + "oppure lo cancella (flagCancella).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Tasso salvato"),
            @APIResponse(responseCode = "400", description = "Dati non validi (tutti gli errori in un unico messaggio)"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response scriviInteresseLegale(
            @RequestBody(description = "Tipo operazione, id del tasso (solo in MODIFICA, come ricevuto dall'elenco), "
                    + "anno, tasso e flag di cancellazione", required = true)
            @Valid @NotNull(message = Constants.ErrorMessages.DATI_INTERESSE_LEGALE_OBBLIGATORI)
            ModificaInteresseLegaleDto modificaInteresseLegaleDto);
}
