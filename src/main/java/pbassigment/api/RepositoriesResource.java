package pbassigment.api;


import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import pbassigment.domain.LoadingContext;
import pbassigment.infrastructure.persistence.DataRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Controller("/api/repositories")
public class RepositoriesResource {

    private final DataRepository repository;


    public RepositoriesResource(DataRepository repository) {
        this.repository = repository;
    }

    @Get("/languages")
    public Mono<Map<String, BigDecimal>> getLanguagesStat() {
        return Mono.just(repository.getLanguagesStatistics());
    }

    /**
     * This is just service resource for debugging
     *
     * @return Status of the loading
     */
    @Get("/_status")
    public Mono<Map<String, Object>> getLoadingStatus() {
        return Mono.just(LoadingContext.asMap());
    }

}
