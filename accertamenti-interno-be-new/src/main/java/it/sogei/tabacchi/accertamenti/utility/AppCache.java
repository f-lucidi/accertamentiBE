package it.sogei.tabacchi.accertamenti.utility;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * AppCache is a thread-safe application-scoped cache utility.
 * It uses {@link ConcurrentHashMap} internally to manage cached objects.
 */
@ApplicationScoped
@Slf4j
public class AppCache {

    private final Map<String, Object> cache;

    public AppCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrInit(String key, Supplier<T> supplier) {
        return (T) cache.computeIfAbsent(key, k -> {
            log.info("Inizializzazione oggetto in cache per la seguente chiave: {}", k);
            return supplier.get();
        });
    }


}
