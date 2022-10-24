package pbassigment.domain;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Objects.isNull;

public class RepositoryInfo {

    private final String name;

    private LocalDate timestamp;

    private List<LanguageInfo> languages;

    private RepositoryInfo(String name, LocalDate timestamp, List<LanguageInfo> languages) {
        this.name = name;
        this.timestamp = timestamp;
        this.languages = languages;
    }

    public static RepositoryInfo bareRepository(String name) {
        return new RepositoryInfo(name, null, List.of());
    }

    public static RepositoryInfo withLanguages(String name, List<LanguageInfo> languages) {
        //assertTrue(CollectionUtils.isNotEmpty(languages))
        return new RepositoryInfo(name, now(), languages);
    }

    public RepositoryInfo updateLanguages(List<LanguageInfo> languages) {
        this.languages = languages;
        this.timestamp = LocalDate.now();

        return this;
    }

    public boolean isStale(LocalDate when) {
        return isNull(timestamp) || timestamp.isBefore(when);
    }

    public List<LanguageInfo> getLanguages() {
        return languages;
    }

    public String getName() {
        return name;
    }

}
