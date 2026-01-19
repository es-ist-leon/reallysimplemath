package events;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * EventBus - Zentraler Message Broker für Service-Kommunikation
 * Implementiert das Publish-Subscribe Pattern
 */
public class EventBus {
    private static EventBus instance;
    private final Map<String, List<Consumer<GameEvent>>> subscribers;
    private final ExecutorService executor;
    
    private EventBus() {
        subscribers = new ConcurrentHashMap<>();
        executor = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    public void subscribe(String eventType, Consumer<GameEvent> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
    }
    
    public void publish(GameEvent event) {
        List<Consumer<GameEvent>> handlers = subscribers.get(event.getType());
        if (handlers != null) {
            for (Consumer<GameEvent> handler : handlers) {
                executor.submit(() -> handler.accept(event));
            }
        }
    }
    
    public void publishSync(GameEvent event) {
        List<Consumer<GameEvent>> handlers = subscribers.get(event.getType());
        if (handlers != null) {
            for (Consumer<GameEvent> handler : handlers) {
                handler.accept(event);
            }
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
