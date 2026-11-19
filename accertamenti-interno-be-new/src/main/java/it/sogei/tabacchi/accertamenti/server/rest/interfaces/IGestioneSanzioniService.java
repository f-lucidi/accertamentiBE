package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaSanzioniDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.SanzioneDto;
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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@Tag(name = "Gestione sanzioni", description = "Consultazione, inserimento e modifica delle sanzioni storicizzate")
public interface IGestioneSanzioniService {

    @POST
    @Path(Constants.Endpoint.SANZIONI_LISTA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista paginata delle sanzioni",
            description = "Gruppi più recenti prima; i filtri non valorizzati vengono ignorati.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pagina di sanzioni trovate",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RisultatoPaginatoDto.class))),
            @APIResponse(responseCode = "400", description = "Filtri non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findSanzioni(
            @Parameter(description = "Numero di pagina (da 1); se assente o < 1 restituisce la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) Integer pagina,
            @Parameter(description = "Elementi per pagina; se assente o < 1 usa il default (10), massimo 100", example = "10")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) Integer elementiPerPagina,
            @RequestBody(description = "Filtri; {} = nessun filtro", required = true)
            FiltriGestioneParametriDto filtriGestioneParametriDto);

    @GET
    @Path(Constants.Endpoint.SANZIONI_GRUPPO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Sanzioni di un gruppo",
            description = "Le sanzioni del gruppo indicato, in ordine di giorni di ritardo.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Sanzioni del gruppo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SanzioneDto.class, type = SchemaType.ARRAY))),
            @APIResponse(responseCode = "400", description = "idGruppo non è una data valida"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "404", description = "Gruppo non trovato")
    })
    Response findSanzioniByIdGruppo(
            @Parameter(description = "Id del gruppo come ricevuto dall'elenco: decorrenza yyyyMMdd", required = true, example = "20270101")
            @PathParam(Constants.Parametri.ID_GRUPPO) Integer idGruppo);

    @GET
    @Path(Constants.Endpoint.SANZIONI_INSERIMENTO_ABILITATO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Indica se si può inserire un nuovo gruppo di sanzioni",
            description = "false se esiste già un gruppo futuro o inserito oggi (ancora modificabile).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "true se l'inserimento è abilitato"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response isInserimentoSanzioniAbilitato();

    @POST
    @Path(Constants.Endpoint.SANZIONI_MODIFICA)
    @Consumes(MediaType.APPLICATION_JSON)
    // serve anche senza body di successo: gli errori ({"errorMessage": ...}) sono JSON
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Salva un gruppo di sanzioni",
            description = "INSERIMENTO: crea il gruppo dalla data di decorrenza e chiude il gruppo precedente. "
                    + "MODIFICA: aggiorna, aggiunge o cancella (DELETE) le righe del gruppo delle sanzioni indicate (ancora modificabile); "
                    + "se le cancella tutte riapre il gruppo precedente.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Gruppo salvato"),
            @APIResponse(responseCode = "400", description = "Dati non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "403", description = "Esiste già un gruppo di sanzioni modificabile")
    })
    Response salvaSanzioni(
            @RequestBody(description = "Tipo operazione, data di decorrenza e sanzioni (\"scaglioni\")", required = true)
            @Valid @NotNull(message = Constants.ErrorMessages.DATI_SANZIONI_OBBLIGATORI)
            ModificaSanzioniDto modificaSanzioniDto);
}
