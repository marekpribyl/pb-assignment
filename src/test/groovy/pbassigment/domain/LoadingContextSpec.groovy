package pbassigment.domain

import spock.lang.Specification

import java.time.OffsetDateTime


class LoadingContextSpec extends Specification {

    def "Should return pages to load"() {
        given: 'initial state'
            LoadingContext.resetContext()
            LoadingContext.repositoriesLoading.get() == false
            LoadingContext.loadingLanguagesIsBlocked() == false
            LoadingContext.loadedPages.get().size() == 1
            LoadingContext.loadedPages.get().contains(0) == true
        when: 'page is requested'
            def pageToLoadMaybe = LoadingContext.repositoriesPageToLoadMaybe()
        then:
            pageToLoadMaybe.get() == 1
        when: 'page 1 is marked as loaded'
            LoadingContext.pageLoaded(1)
        and: 'get page again'
            pageToLoadMaybe = LoadingContext.repositoriesPageToLoadMaybe()
        then:
            pageToLoadMaybe.get() == 2
        when: 'pause loading'
            LoadingContext.pauseLoading(OffsetDateTime.now().plusSeconds(1L))
        then: 'no page is returned'
            LoadingContext.repositoriesPageToLoadMaybe().isEmpty() == true
    }

    def "Should block language loading if repositories loading is ongoing or loading is paused"() {
        given:
            LoadingContext.resetContext()
            LoadingContext.loadingLanguagesIsBlocked() == false
        when:
            def pageToLoadMaybe = LoadingContext.repositoriesPageToLoadMaybe()
        then:
            LoadingContext.loadingLanguagesIsBlocked() == true
        when:
            LoadingContext.repositoriesLoaded()
        then:
            LoadingContext.loadingLanguagesIsBlocked() == false
        when:
            LoadingContext.pauseLoading(OffsetDateTime.now().plusSeconds(1L))
        then:
            LoadingContext.loadingLanguagesIsBlocked() == true
    }

    def "Should record loaded pages"() {
        given:
            LoadingContext.resetContext()
        when:
            LoadingContext.repositoriesPageToLoadMaybe()
            LoadingContext.pageLoaded(1)
            LoadingContext.repositoriesPageToLoadMaybe()
            LoadingContext.pageLoaded(2)
        then:
            LoadingContext.loadedPages.get() == [0,1,2] as TreeSet<Integer>
        when:
            LoadingContext.repositoriesLoaded()
        then: 'reset loaded pages'
            LoadingContext.loadedPages.get() == [0] as TreeSet<Integer>
    }

}
