package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.AliquotaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.FiltriGestioneParametriDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ModificaAliquoteDto;
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
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@Tag(name = "Gestione aliquote", description = "Consultazione, inserimento e modifica delle aliquote di mora storicizzate")
public interface IGestioneAliquoteService {

    @POST
    @Path(Constants.Endpoint.ALIQUOTE_LISTA)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista paginata delle aliquote di mora",
            description = "Gruppi più recenti prima; i filtri non valorizzati vengono ignorati.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pagina di aliquote trovate",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RisultatoPaginatoDto.class))),
            @APIResponse(responseCode = "400", description = "Filtri non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findAliquote(
            @Parameter(description = "Numero di pagina (da 1); se assente o < 1 restituisce la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) Integer pagina,
            @Parameter(description = "Elementi per pagina; se assente o < 1 usa il default (10), massimo 100", example = "10")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) Integer elementiPerPagina,
            @RequestBody(description = "Filtri; {} = nessun filtro", required = true)
            FiltriGestioneParametriDto filtriGestioneParametriDto);

    @GET
    @Path(Constants.Endpoint.ALIQUOTE_GRUPPO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Aliquote di un gruppo",
            description = "Le aliquote non cancellate del gruppo indicato, in ordine di giorni di ritardo.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Aliquote del gruppo",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AliquotaDto.class, type = SchemaType.ARRAY))),
            @APIResponse(responseCode = "400", description = "idGruppo non è una data valida"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "404", description = "Gruppo non trovato")
    })
    Response findAliquoteByIdGruppo(
            @Parameter(description = "Id del gruppo come ricevuto dall'elenco: decorrenza yyyyMMdd", required = true, example = "20270101")
            @PathParam(Constants.Parametri.ID_GRUPPO) Integer idGruppo);

    @GET
    @Path(Constants.Endpoint.ALIQUOTE_INSERIMENTO_ABILITATO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Indica se si può inserire un nuovo gruppo di aliquote",
            description = "false se esiste già un gruppo futuro o inserito oggi (ancora modificabile).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "true se l'inserimento è abilitato"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response isInserimentoAliquoteAbilitato();

    @POST
    @Path(Constants.Endpoint.ALIQUOTE_MODIFICA)
    @Consumes(MediaType.APPLICATION_JSON)
    // serve anche senza body di successo: gli errori ({"errorMessage": ...}) sono JSON
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Salva un gruppo di aliquote di mora",
            description = "INSERIMENTO: crea il gruppo dalla data di decorrenza e chiude il gruppo precedente. "
                    + "MODIFICA: aggiorna, aggiunge o cancella le righe del gruppo indicato da idGruppo (ancora modificabile); "
                    + "se le cancella tutte riapre il gruppo precedente.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Gruppo salvato"),
            @APIResponse(responseCode = "400", description = "Dati non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "403", description = "Esiste già un gruppo di aliquote modificabile")
    })
    Response salvaAliquote(
            @RequestBody(description = "Tipo operazione, id del gruppo (solo in MODIFICA, come ricevuto dall'elenco), data di decorrenza e aliquote (\"scaglioni\")", required = true)
            @Valid @NotNull(message = Constants.ErrorMessages.DATI_ALIQUOTE_OBBLIGATORI)
            ModificaAliquoteDto modificaAliquote);
}
