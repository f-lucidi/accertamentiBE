package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.utility.Constants;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path(Constants.Endpoint.EMPTY)
@Tag(name = "Servizi rest utente")
public interface IUserService {

    @GET
    @Path(Constants.Endpoint.USER_INFO)
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Recupera le informazione dell'utente autenticato")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Utente trovato", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "500", description = "Errore interno del server"),
            @APIResponse(responseCode = "504", description = "Utente non trovato", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    })
    Response getUserInfo();

    @GET
    @Path(Constants.Endpoint.LOGOUT)
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Effettua il logout")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Logout effettuato"),
            @APIResponse(responseCode = "500", description = "Errore interno del server")
    })
    Response logout();

    @GET
    @Path(Constants.Endpoint.REACTIVATE_SESSION)
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(summary = "Riattiva la sessione")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Session reactivate"),
            @APIResponse(responseCode = "500", description = "Errore interno del server")
    })
    Response reactivateSession();
}
