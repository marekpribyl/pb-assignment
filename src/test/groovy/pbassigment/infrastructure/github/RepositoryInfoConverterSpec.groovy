package pbassigment.infrastructure.github


import spock.lang.Specification

import java.time.LocalDate

class RepositoryInfoConverterSpec extends Specification {

    def "Should convert from API response"() {
        when:
            def repositoryInfo = RepositoryInfoConverter.fromApiResponse('repository', ['java': 100L, 'ruby': 200L])
        then:
            repositoryInfo.name == 'repository'
            repositoryInfo.isStale(LocalDate.now()) == false
    }

    def "Should convert from API response with missing languages to bare repository"() {
        when:
            def repositoryInfo = RepositoryInfoConverter.fromApiResponse('repository', null)
        then:
            repositoryInfo.name == 'repository'
            repositoryInfo.isStale(LocalDate.now()) == true
    }

}
