package org.example.gc_coffee.order.scheduler;


import lombok.RequiredArgsConstructor;
import org.example.gc_coffee.order.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class OrderStatusScheduler {

    private final OrderService orderService;

    // 매일 14시에 실행
    @Scheduled(cron = "0 0 14 * * *")
    public void updateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.minusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(14).withMinute(0).withSecond(0).withNano(0);

        orderService.updateOrderStatus(startOfDay, endOfDay);
    }

}
