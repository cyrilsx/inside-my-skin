package dev.healthhub.api;

import dev.healthhub.api.dto.Dtos.EventDto;
import dev.healthhub.ingestion.CanonicalEvent;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

@Component
public class SseEventSink {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;

    public SseEventSink(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter register() {
        var emitter = new SseEmitter(TIMEOUT_MS);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (Exception e) {
            emitters.remove(emitter);
            emitter.complete();
        }
        return emitter;
    }

    int emitterCount() {
        return emitters.size();
    }

    @EventListener
    public void onCanonicalEvent(CanonicalEvent event) {
        broadcast(event);
    }

    void broadcast(CanonicalEvent event) {
        var dto = new EventDto(event.id(), event.type().name(),
                OffsetDateTime.ofInstant(event.occurredAt(), ZoneOffset.UTC).toString(), event.payload());
        var data = objectMapper.writeValueAsString(dto);
        for (var emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().id(dto.id()).name(dto.type()).data(data, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }
}
