package pbassigment.domain

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import spock.lang.Specification
import spock.lang.Subject

import static java.time.OffsetDateTime.now

class RateLimitGuardSpec extends Specification {

    @Subject
    def rateLimitGuard = new RateLimitGuard(100L)

    def "Should set `blockUntil` and re-throw the exception "() {
        given:
            HttpHeaders httpHeaders = Stub() { it -> it.get('x-ratelimit-remaining') >> 0 }
            HttpResponse httpResponse = Stub() { it ->
                it.getStatus() >> HttpStatus.FORBIDDEN
                it.getHeaders() >> httpHeaders
            }
            HttpClientResponseException ex = new HttpClientResponseException('', httpResponse)
        when:
            rateLimitGuard.pauseRequestingApiIfNeededDueToReachedLimit(ex).block()
        then:
            LoadingContext.blockedUntil.isBefore(now().plusSeconds(100L)) == true
        and:
            thrown(HttpClientException)
    }

}
