package it.sogei.tabacchi.accertamenti.server.rest.interfaces;

import it.sogei.tabacchi.accertamenti.business.model.dto.ListeFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.ParametriFiltriRicercaDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.RisultatoPaginatoDto;
import it.sogei.tabacchi.accertamenti.business.model.dto.tipologiche.CategoriaProdottoDto;
import it.sogei.tabacchi.accertamenti.utility.Constants;
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
    @Path("/{" + Constants.Parametri.CATEGORIA_PRODOTTO + "}/descrizione")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Recupera la descrizione di una categoria prodotto")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Descrizione della categoria"),
            @APIResponse(responseCode = "400", description = "Sigla della categoria mancante"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response getDescrizioneCategoria(
            @Parameter(description = "Sigla della categoria prodotto", required = true, example = "PLI")
            @PathParam(Constants.Parametri.CATEGORIA_PRODOTTO) String categoriaProdotto);

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
            @PathParam(Constants.Parametri.CATEGORIA_PRODOTTO) String categoriaProdotto,
            @Parameter(description = "Anno contabile: se indicato restituisce solo i soggetti attivi in quell'anno", example = "2025")
            @QueryParam(Constants.Parametri.ANNO) String anno);

    @POST
    @Path(Constants.Endpoint.LISTA_POSIZIONI + "/{" + Constants.Parametri.CATEGORIA_PRODOTTO + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista paginata delle posizioni d'imposta",
            description = "Applica i filtri di ricerca indicati nel body; i filtri non valorizzati (anche \"\") vengono ignorati. "
                    + "La categoria prodotto è obbligatoria e fa parte del path.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Pagina di posizioni trovate",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = RisultatoPaginatoDto.class))),
            @APIResponse(responseCode = "400", description = "Parametri non validi"),
            @APIResponse(responseCode = "401", description = "Utente non autenticato")
    })
    Response findAllElencoPosizioni(
            @Parameter(description = "Sigla della categoria prodotto", required = true, example = "PLI")
            @PathParam(Constants.Parametri.CATEGORIA_PRODOTTO) String categoriaProdotto,
            @Parameter(description = "Numero di pagina (da 1); se assente o < 1 restituisce la prima", example = "1")
            @QueryParam(Constants.Parametri.PAGINA) Integer pagina,
            @Parameter(description = "Elementi per pagina; se assente o < 1 usa il default (10), massimo 100", example = "10")
            @QueryParam(Constants.Parametri.ELEMENTI_PER_PAGINA) Integer elementiPerPagina,
            @RequestBody(description = "Filtri di ricerca; i campi non valorizzati vengono ignorati", required = true)
            ParametriFiltriRicercaDto filtriRicerca);
}

