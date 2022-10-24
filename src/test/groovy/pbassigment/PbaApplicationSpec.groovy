package pbassigment

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import jakarta.inject.Inject

@MicronautTest
class PbaApplicationSpec extends Specification {

    @Inject
    EmbeddedApplication application

    void 'It just works...'() {
        expect:
            application.running
    }

}
