package pbassigment.infrastructure.persistence;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Singleton;
import pbassigment.domain.LanguageInfo;
import pbassigment.domain.RepositoryInfo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static java.time.LocalDate.now;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toSet;
import static pbassigment.domain.RepositoryInfo.bareRepository;

/**
 * Domain specific simplistic in-memory data store
 */
@Singleton
public class SimpleEphemeralRepository implements DataRepository {

    private final Map<String, RepositoryInfo> data = new HashMap<>();


    @VisibleForTesting
    @Override
    public void addRepositoryWithLanguageInfo(String name, List<LanguageInfo> languages) {
        data.put(name, RepositoryInfo.withLanguages(name, languages));
    }

    @Override
    public void addRepositoryWithLanguageInfo(RepositoryInfo repositoryInfo) {
        data.put(repositoryInfo.getName(), repositoryInfo);
    }

    @Override
    public void addBareRepositoryIfMissing(String name) {
        data.putIfAbsent(name, bareRepository(name));
    }

    @Override
    public void removeRepository(String name) {
        data.remove(name);
    }

    @Override
    public long getRepositoriesCount() {
        return data.size();
    }

    @Override
    public Set<String> getStaleRepositoryNames() {
        final LocalDate today = now();
        return data.entrySet().stream()
                .filter(entry -> entry.getValue().isStale(today))
                .map(Map.Entry::getKey)
                .collect(toSet());
    }


    @Override
    public Map<String, BigDecimal> getLanguagesStatistics() {
        final Long total = data.values().stream()
                .map(RepositoryInfo::getLanguages)
                .flatMap(Collection::stream)
                .map(LanguageInfo::numberOfBytes)
                .reduce(0L, Long::sum);

        return data.values().stream()
                .map(RepositoryInfo::getLanguages)
                .flatMap(Collection::stream)
                .collect(
                        groupingBy(LanguageInfo::language, reducing(
                                ZERO,
                                languageInfo -> calculateRatio(languageInfo.numberOfBytes(), total),
                                BigDecimal::add)
                        )
                );
    }

    private static BigDecimal calculateRatio(Long value, Long total) {
        final BigDecimal totalAsDecimal = valueOf(requireNonNullElse(total, 1L));
        return valueOf(requireNonNullElse(value, 0L)).divide(totalAsDecimal, 2, HALF_UP);
    }

}
