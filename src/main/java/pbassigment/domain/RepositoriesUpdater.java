package pbassigment.domain;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pbassigment.infrastructure.github.GithubApiClient;
import pbassigment.infrastructure.persistence.DataRepository;

import java.util.Optional;

@Singleton
public class RepositoriesUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoriesUpdater.class);

    private final GithubApiClient apiClient;

    private final DataRepository dataRepository;

    private final RateLimitGuard rateLimitGuard;

    private final String organization;


    public RepositoriesUpdater(
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

    @Scheduled(fixedDelay = "7s")
    public void updateRepositories() {
        LOG.debug("Loading repositories...");

        final Optional<Integer> pageToLoad = LoadingContext.repositoriesPageToLoadMaybe();
        if (pageToLoad.isEmpty()) {
            LOG.debug("Loading repositories is skipped");
            return;
        }

        final int page = pageToLoad.get();
        LOG.debug("Loading page [{}]...", page);
        apiClient.getRepositories(organization, page)
                .onErrorResume(HttpClientResponseException.class, rateLimitGuard::pauseRequestingApiIfNeededDueToReachedLimit)
                .doOnNext(repos -> repos.forEach(repo -> dataRepository.addBareRepositoryIfMissing(repo.name())))
                .subscribe(repos -> {
                            if (repos.isEmpty()) {
                                LOG.debug("Empty result received, all repositories loaded");
                                LoadingContext.repositoriesLoaded();
                            } else {
                                LOG.debug("Page [{}] marked as loaded", page);
                                LoadingContext.pageLoaded(page);
                            }
                        },
                        throwable -> LOG.error("Subscription canceled due to [{}]", throwable.getMessage(), throwable)
                );
    }

}
