package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.enums.TipoParametro;
import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.GESTIONE_PARAMETRI)
@Tag(name = "Gestione parametri", description = "Dati comuni ai parametri storicizzati (aliquote, sanzioni, interessi)")
public interface IGestioneParametriService {

    @GET
    @Path(Constants.Endpoint.ANNI + "/{" + Constants.Parametri.TIPO_PARAMETRO + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Anni per il box di ricerca di un parametro",
            description = "Tutti gli anni dal più alto al più basso trovati, intermedi compresi; le righe cancellate non contano.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Anni trovati (lista vuota se nessuno)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = Integer.class))),
            @APIResponse(responseCode = "401", description = "Utente non autenticato"),
            @APIResponse(responseCode = "404", description = "Tipo parametro non valido")
    })
    Response findAnni(
            @Parameter(description = "Parametro di cui leggere gli anni", required = true, example = "ALIQUOTE_MORA")
            @PathParam(Constants.Parametri.TIPO_PARAMETRO) TipoParametro tipoParametro);
}
