package pbassigment.domain;

import com.google.common.annotations.VisibleForTesting;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;

/**
 * Global loading state holder
 */
public final class LoadingContext {

    public static AtomicBoolean repositoriesLoading = new AtomicBoolean(false);

    //keep list of loaded pages in the current batch
    public static final AtomicReference<Set<Integer>> loadedPages = new AtomicReference<>(initialPages());

    public static LocalDate repositoriesLoadedOn = LocalDate.now().minusDays(1);

    public static OffsetDateTime blockedUntil = now();


    public static void pauseLoading(OffsetDateTime value) {
        requireNonNull(value, "Pause loading value cannot be null");
        blockedUntil = value;
    }

    public static Optional<Integer> repositoriesPageToLoadMaybe() {
        final OffsetDateTime now = now();
        if (now.isBefore(blockedUntil) || !now.toLocalDate().isAfter(repositoriesLoadedOn)) {
            return empty();
        }

        repositoriesLoading.set(true);

        //FIXME manage correctly gaps in the sequence...
        return loadedPages.getAcquire().stream()
                .max(Integer::compareTo)
                .map(page -> page + 1);
    }

    public static void pageLoaded(int page) {
        loadedPages.updateAndGet(pages -> {
            pages.add(page);
            return pages;
        });
    }

    public static boolean loadingLanguagesIsBlocked() {
        return now().isBefore(blockedUntil) || repositoriesLoading.getAcquire();
    }

    public static void repositoriesLoaded() {
        repositoriesLoading.set(false);
        loadedPages.set(initialPages());
        repositoriesLoadedOn = now().toLocalDate();
    }

    private static Set<Integer> initialPages() {
        final TreeSet<Integer> initialSet = new TreeSet<>();
        initialSet.add(0);

        return initialSet;
    }

    public static Map<String, Object> asMap() {
        return Map.of(
            "repositoriesLoading", repositoriesLoading.get(),
            "repositoriesLoadedOn", repositoriesLoadedOn.format(ISO_LOCAL_DATE),
            "blockedUntil", blockedUntil.format(ISO_DATE_TIME)
        );
    }

    @VisibleForTesting
    protected static void resetContext() {
        repositoriesLoading.set(false);
        loadedPages.set(initialPages());
        repositoriesLoadedOn = LocalDate.now().minusDays(1);
        blockedUntil = now();
    }

}
