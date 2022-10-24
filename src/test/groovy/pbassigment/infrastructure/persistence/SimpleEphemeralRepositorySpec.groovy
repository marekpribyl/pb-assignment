package pbassigment.infrastructure.persistence

import pbassigment.domain.LanguageInfo
import spock.lang.Specification
import spock.lang.Subject

class SimpleEphemeralRepositorySpec extends Specification {

    @Subject
    SimpleEphemeralRepository repository = new SimpleEphemeralRepository()

    def "Should return empty map if no data set"() {
        expect:
            repository.getLanguagesStatistics() == [:]
    }

    def "Should calculate statistics"() {
        given:
            repository.addRepositoryWithLanguageInfo("Repo_1", [
                    new LanguageInfo("Java", 100),
                    new LanguageInfo("Ruby", 150),
                    new LanguageInfo("Shell", 10),
            ])
            repository.addRepositoryWithLanguageInfo("Repo_2", [
                    new LanguageInfo("Ruby", 200),
                    new LanguageInfo("Shell", 50),
            ])
            repository.addRepositoryWithLanguageInfo("Repo_3", [
                    new LanguageInfo("Java", 80),
                    new LanguageInfo("Groovy", 20),
            ])
        when:
            def statistics = repository.getLanguagesStatistics()
        then:
            statistics.size() == 4
            statistics.get("Java") == 0.29
            statistics.get("Ruby") == 0.58
            statistics.get("Groovy") == 0.03
            statistics.get("Shell") == 0.1
    }

    def "Should calculate 1.00 for single language"() {
        given:
            repository.addRepositoryWithLanguageInfo("Repo_1", [
                    new LanguageInfo("Java", 1234)
            ])
            repository.addRepositoryWithLanguageInfo("Repo_2", [
                    new LanguageInfo("Java", 666)
            ])
        when:
            def statistics = repository.getLanguagesStatistics()
        then:
            statistics.size() == 1
            statistics.get("Java") == 1.00
    }

    def "Should return stale repositories if exist"() {
        expect: 'no stale repositories'
            repository.getStaleRepositoryNames() == [] as HashSet
        when:
            repository.addBareRepositoryIfMissing('staleRepository')
        then:
            repository.getStaleRepositoryNames() == ['staleRepository'] as HashSet
    }

    def "Should remove repository"() {
        given:
            repository.addRepositoryWithLanguageInfo("Repo_1", [])
        and:
            repository.getRepositoriesCount() == 1
        when:
            repository.removeRepository("Repo_1")
        then:
            repository.getRepositoriesCount() == 0
    }

}
