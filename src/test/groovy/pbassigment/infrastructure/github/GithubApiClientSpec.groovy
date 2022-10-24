package pbassigment.infrastructure.github

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.mockserver.client.MockServerClient
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
@MicronautTest
@Testcontainers
class GithubApiClientSpec extends Specification {

    @Shared
    public MockServerContainer mockServer = new MockServerContainer(
            MOCKSERVER_IMAGE.withTag("mockserver-" + MockServerClient.class.getPackage().getImplementationVersion())
    );

    @Inject
    GithubApiClient client

    def "Should retrieve repositories of organization"() {
        when:
            def repos = client.getRepositories('productboard', 2).block()
        then:
            repos != null
    }

    def "Should retrieve languages of repository"() {
        given:
            new MockServerClient(mockServer.getHost(), mockServer.getServerPort())
                    .when(request()
                            .withPath("/person")
                            .withQueryStringParameter("name", "peter"))
                    .respond(response()
                            .withBody("Peter the person!"));
        when:
            def repos = client.getRepositoryLanguages('productboard', 'webpack-deploy').block()
        then:
            repos != null
    }

}
