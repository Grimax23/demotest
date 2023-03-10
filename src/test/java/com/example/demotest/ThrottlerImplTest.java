package com.example.demotest;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class ThrottlerImplTest {

    @Test
    void checkTimeTest() {
        int durationInMinutes = 1;
        long durationInMilli = durationInMinutes * 60 * 1000;
        int limit = 2;
        ThrottlerImpl ipThrottler = new ThrottlerImpl(durationInMinutes, limit);
        LinkedBlockingQueue times = new LinkedBlockingQueue<Long>(limit);
        final long startOfTodayInMilli = LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        times.add(startOfTodayInMilli);//00.00.00
        long secondTime = startOfTodayInMilli + durationInMilli / 2;
        times.add(secondTime);//00.00.30
        final long nowInDuration = startOfTodayInMilli + durationInMilli - 1;
        final long nowOutOfDuration = secondTime + durationInMilli;

        // Test cases:
        // 1. попадание в интервал с превышением лимита на первой итерации
        assertFalse(ipThrottler.checkTime(times, nowInDuration));

        // 2. попадание в интервал без превышения лимита на второй или последующих итерациях
        assertTrue(ipThrottler.checkTime(times, nowOutOfDuration));//очередь чистится перебрав весь limit

        // Превышение лимита на второй и последующих итерациях исключается за счет ограничения размера очереди равному ограничению количества запросов (limit) за заданный интервал.
        // Вывод: обязательное условие конфигурации - ограничение количества запросов должно быть меньше заданного интервала в миллисекундах (limit < durationInMilli)!
        // Например если интервал 1 минута лимит запросов не должен быть больше 60000.
        // Таким образом ограничение не будет срабатывать а лишь впустую тратить ресурсы тем больше чем больше очередь на этот случай можно сделать оптимизацию алгоритма
        // добавив проверку если ограничение количества запросов больше заданного интервала в миллисекундах всегда пропускать запрос: if(limit > durationInMilli) return true;
        // либо сделать  duration типом long, а limit int и при создании конвертировать минуты в миллисекунды.
    }

    @Test
    void throttleFalseTest() throws InterruptedException {
        long durationInMilli = 20;
        int limit = 2;
        final String IP = "0.0.0.1";
        ThrottlerImpl ipThrottler = new ThrottlerImpl(durationInMilli, limit);

        assertTrue(ipThrottler.throttle(IP));
        assertTrue(ipThrottler.throttle(IP));

        // 1. попадание в интервал с превышением лимита на первой итерации
        assertFalse(ipThrottler.throttle(IP));
    }

    @Test
    void throttleTrueTest() throws InterruptedException {
        long durationInMilli = 20;
        int limit = 2;
        final String IP = "0.0.0.1";
        ThrottlerImpl ipThrottler = new ThrottlerImpl(durationInMilli, limit);

        assertTrue(ipThrottler.throttle(IP));
        Thread.sleep(durationInMilli - 10);

        // 2. попадание в интервал без превышения лимита на второй или последующих итерациях
        assertTrue(ipThrottler.throttle(IP));

    }
}