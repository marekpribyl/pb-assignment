package pbassigment.domain;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static io.micronaut.http.HttpStatus.FORBIDDEN;
import static java.lang.Integer.parseInt;
import static java.time.OffsetDateTime.now;
import static java.util.Objects.requireNonNullElse;

@Singleton
public final class RateLimitGuard {

    private static final Logger LOG = LoggerFactory.getLogger(RateLimitGuard.class);

    private static final String X_RATELIMIT_REMAINING = "x-ratelimit-remaining";

    private final Long rateLimitWindowSeconds;


    public RateLimitGuard(@Value("${github.api.rateLimitWindowSeconds}") Long rateLimitWindowSeconds) {
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    public <T> Mono<T> pauseRequestingApiIfNeededDueToReachedLimit(HttpClientResponseException e) {
        if (FORBIDDEN.equals(e.getStatus()) && isLimitReached(e)) {
            final OffsetDateTime until = now().plusSeconds(rateLimitWindowSeconds);
            LOG.debug("Pausing loading due to rate limit until [{}]", until);
            LoadingContext.pauseLoading(until);
        }

        return Mono.error(e);
    }

    public boolean isLimitReached(HttpClientResponseException e) {
        boolean limitReached = false;
        try {
            final String headerValue = requireNonNullElse(e.getResponse().getHeaders().get(X_RATELIMIT_REMAINING), "1");
            limitReached = (0 == parseInt(headerValue));
        } catch (NumberFormatException nfe) {
            LOG.error("Header x-ratelimit-remaining doesn't contain valid value");
        }

        return limitReached;
    }

}
