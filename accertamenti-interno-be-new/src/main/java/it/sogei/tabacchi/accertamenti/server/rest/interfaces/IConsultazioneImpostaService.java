package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
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

@Path(Constants.Endpoint.CONSULTAZIONE_IMPOSTE)
// documentazione: raggruppa tutti i metodi di questa interfaccia sotto un tag "Consultazione imposte" in Swagger
@Tag(name = "Consultazione imposte", description = "Ricerca ed esportazione delle posizioni d'imposta")
public interface IConsultazioneImpostaService {

    @GET
    @Path(Constants.Endpoint.CATEGORIE_PRODOTTO)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Elenco delle categorie prodotto")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Categorie prodotto disponibili",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = CategoriaProdottoDto.class))),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response getCategorieProdotti();

    @GET
    @Path("/{sigla}/descrizione")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Recupera la descrizione di una categoria prodotto")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Descrizione della categoria"),
            @APIResponse(responseCode = "400", description = "Sigla della categoria mancante"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response getDescrizioneCategoria(
            @Parameter(description = "Sigla della categoria prodotto", required = true, example = "PLI")
            @PathParam("sigla") String siglaCategoria);

    @GET
    @Path(Constants.Endpoint.FILTRI_RICERCA + "/{" + Constants.Parametri.CATEGORIA_PRODOTTO + "}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Liste per i filtri di ricerca di una categoria prodotto",
            description = "Restituisce soggetti (ragione sociale e P.IVA) e tipi di pagamento.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Liste per i filtri di ricerca",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ListeFiltriRicercaDto.class))),
            @APIResponse(responseCode = "400", description = "Categoria prodotto mancante"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response getFiltriRicerca(
            @Parameter(description = "Sigla della categoria prodotto", required = true, example = "PLI")
            @PathParam(Constants.Parametri.CATEGORIA_PRODOTTO) String categoriaProdotto);
}