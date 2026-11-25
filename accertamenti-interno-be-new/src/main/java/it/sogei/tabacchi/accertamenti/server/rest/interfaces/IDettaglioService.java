package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.dettaglio.DettaglioDto;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.DETTAGLIO)
@Tag(name = "Dettaglio posizione", description = "Dettaglio di una posizione d'imposta")
public interface IDettaglioService {

    @GET
    @Path(Constants.Endpoint.PARAMETRO_ID_POSIZIONE)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Dettaglio completo di una posizione",
            description = "Dati della posizione, imposta dovuta paginata e, se presenti, mora, interesse differito, "
                    + "interessi legali e sanzioni. Le posizioni cancellate sono incluse.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Dettaglio della posizione",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = DettaglioDto.class))),
            @APIResponse(responseCode = "400", description = "Id mancante o posizione non trovata"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response getDettaglio(
            @Parameter(description = "Id della posizione", required = true, example = "1")
            @PathParam(Constants.Parametri.ID_POSIZIONE) Long idPosizione,
            @Parameter(description = "Pagina della tabella imposta dovuta (da 1); se assente la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) @DefaultValue(Constants.Parametri.DEFAULT_PAGINA) Integer pagina,
            @Parameter(description = "Righe per pagina della tabella imposta dovuta; se assente il default (5), massimo 100", example = "5")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) @DefaultValue(Constants.Parametri.DEFAULT_ELEMENTI_PER_PAGINA) Integer elementiPerPagina);
}
