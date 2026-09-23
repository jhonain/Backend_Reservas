package com.vasquez.reservas_backend.reserva.Event;

import com.vasquez.reservas_backend.config.CacheConfig;
import com.vasquez.reservas_backend.reserva.dto.DisponibilidadCambiadaEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReservaCambiadaListener {

    private static final Logger logger =
            LoggerFactory.getLogger(ReservaCambiadaListener.class);

    private final CacheManager cacheManager;
    private final SimpMessagingTemplate messagingTemplate;

    public ReservaCambiadaListener(
            CacheManager cacheManager,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.cacheManager = cacheManager;
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void alCambiarReserva(
            ReservaCambiadaEvent event
    ) {
        limpiarCache(
                CacheConfig.DISPONIBILIDADES_POR_SERVICIO
        );

        limpiarCache(
                CacheConfig.DISPONIBILIDADES_POR_SERVICIO_Y_DIA
        );

        DisponibilidadCambiadaEvent eventoWebSocket =
                new DisponibilidadCambiadaEvent(
                        "DISPONIBILIDAD_CAMBIADA",
                        event.servicioId(),
                        event.fecha()
                );

        messagingTemplate.convertAndSend(
                "/topic/disponibilidad/" + event.servicioId(),
                eventoWebSocket
        );

        logger.info(
                "Reserva procesada; Redis limpiado y WebSocket publicado. "
                        + "reservaId={}, servicioId={}, fecha={}, evento={}",
                event.reservaId(),
                event.servicioId(),
                event.fecha(),
                event.tipo()
        );
    }

    private void limpiarCache(String nombreCache) {
        Cache cache = cacheManager.getCache(nombreCache);

        if (cache != null) {
            cache.clear();
        }
    }
}