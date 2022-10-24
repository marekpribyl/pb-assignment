package pbassigment.domain;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbassigment.infrastructure.github.GithubApiClient;
import pbassigment.infrastructure.persistence.DataRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.micronaut.http.HttpStatus.NOT_FOUND;
import static pbassigment.infrastructure.github.RepositoryInfoConverter.fromApiResponse;

@Singleton
public class LanguageStatisticsUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(LanguageStatisticsUpdater.class);

    private final GithubApiClient apiClient;

    private final DataRepository dataRepository;

    private final RateLimitGuard rateLimitGuard;

    private final String organization;


    public LanguageStatisticsUpdater(
            GithubApiClient apiClient,
            DataRepository dataRepository,
            RateLimitGuard rateLimitGuard,
            @Value("${github.organization}") String organization
    ) {
        this.apiClient = apiClient;
        this.dataRepository = dataRepository;
        this.rateLimitGuard = rateLimitGuard;
        this.organization = organization;
    }

    @Scheduled(fixedDelay = "11s")
    public void updateLanguageStatistics() {
        LOG.debug("Loading languages...");
        if (LoadingContext.loadingLanguagesIsBlocked()) {
            LOG.debug("Loading languages is skipped");
        }

        Flux.fromIterable(dataRepository.getStaleRepositoryNames())
                .flatMap(repositoryName -> apiClient.getRepositoryLanguages(organization, repositoryName)
                        .map(response -> fromApiResponse(repositoryName, response))
                        .onErrorResume(HttpClientResponseException.class, rateLimitGuard::pauseRequestingApiIfNeededDueToReachedLimit)
                        .onErrorResume(HttpClientResponseException.class, e -> {
                            if (NOT_FOUND.equals(e.getStatus())) {
                                LOG.debug("Removing repository [{}] from the data store", repositoryName);
                                dataRepository.removeRepository(repositoryName);
                            }
                            return Mono.empty();
                        })
                )
                .doOnNext(dataRepository::addRepositoryWithLanguageInfo)
                .subscribe(
                        repositoryInfo -> LOG.debug("Repository [{}] languages updated", repositoryInfo.getName()),
                        throwable -> LOG.error("Subscription canceled due to [{}]", throwable.getMessage(), throwable)
                );
    }

}
