package pbassigment.infrastructure.github;

import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Simplistic declarative Client
 */
@Client(id="${github.api.url}", path = "/")
@Produces("application/vnd.github+json")
@Consumes("application/vnd.github+json")
@Header(name = "User-Agent", value = "micronaut/1.0")
public interface GithubApiClient {

    /**
     * We have hardcoded sort params to get recently added repos as quickly as possible
     */
    @Get("/orgs/{organizationName}/repos?page={page}&sort=created&direction=desc")
    Mono<List<GithubRepository>> getRepositories(String organizationName, int page);

    @Get("/repos/{organizationName}/{repositoryName}/languages")
    Mono<Map<String, Long>> getRepositoryLanguages(String organizationName, String repositoryName);

}
