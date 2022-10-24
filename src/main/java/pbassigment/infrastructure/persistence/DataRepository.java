package pbassigment.infrastructure.persistence;

import pbassigment.domain.LanguageInfo;
import pbassigment.domain.RepositoryInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Domain specific data store for language info for repositories
 */
public interface DataRepository {

    void addRepositoryWithLanguageInfo(String name, List<LanguageInfo> languages);

    void addRepositoryWithLanguageInfo(RepositoryInfo repositoryInfo);

    void addBareRepositoryIfMissing(String name);

    void removeRepository(String name);

    long getRepositoriesCount();

    Set<String> getStaleRepositoryNames();

    Map<String, BigDecimal> getLanguagesStatistics();

}
