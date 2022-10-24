package pbassigment.infrastructure.github;

import pbassigment.domain.LanguageInfo;
import pbassigment.domain.RepositoryInfo;

import java.util.List;
import java.util.Map;

import static io.micronaut.core.util.CollectionUtils.isEmpty;

public final class RepositoryInfoConverter {

    public static RepositoryInfo fromApiResponse(String repositoryName, Map<String, Long> languages) {
        return isEmpty(languages)
                ? RepositoryInfo.bareRepository(repositoryName)
                : RepositoryInfo.withLanguages(repositoryName, mapToList(languages));
    }

    private static List<LanguageInfo> mapToList(Map<String, Long> source) {
        return source.entrySet().stream()
                .map(entry -> new LanguageInfo(entry.getKey(), entry.getValue()))
                .toList();
    }

}
