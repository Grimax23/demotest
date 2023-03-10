package com.example.demotest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ThrottlerImpl implements Throttler{
    private final Map<String, LinkedBlockingQueue> requestIpsToTimes = new ConcurrentHashMap<>();
    private final Long duration;
    private final Integer limit;
    private static final Logger log = LoggerFactory.getLogger(ThrottlerImpl.class);

    @Autowired
    public ThrottlerImpl(@Value("${throttling.durationInMinutes}") Integer durationInMinutes, @Value("${throttling.limit}") Integer limit) {
        this.duration = TimeUnit.MILLISECONDS.convert(durationInMinutes, TimeUnit.MINUTES);
        this.limit = limit;
    }

    public ThrottlerImpl(Long durationInMilli, Integer limit) {
        this.duration = durationInMilli;
        this.limit = limit;
    }

    public Boolean throttle(String ip) {
        final long now = System.currentTimeMillis();
        LinkedBlockingQueue times = requestIpsToTimes.get(ip);
        Boolean isAllowed;

        // Первый запрос с нового IP
        if (times == null) {
            times = new LinkedBlockingQueue<Long>(limit);//LinkedBlockingQueue т.к. ConcurrentLinkedQueue не хранит размер
            try {
                times.put(now);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            requestIpsToTimes.put(ip, times);
            isAllowed = true;

            if (log.isDebugEnabled()) {
                log.debug("Thread:" + Thread.currentThread().getName() + ", ip:" + ip + ", times:" + times + ", now: " + now);
            }

        } else {
            // Проверяем условия времени и количества
            isAllowed = checkTime(times, now);

            if (log.isDebugEnabled()) {
                log.debug("Thread:" + Thread.currentThread().getName() + ", ip:" + ip + ", times:" + times + ", now: " + now);
            }
        }
        return isAllowed;
    }

    public Boolean checkTime(LinkedBlockingQueue<Long> times, long now) {
        //Если очередь пустая еще до обхода то кладем запрос и выходим
        if (times.size() == 0) {
            try {
                times.put(now);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;

        }

        while (times.size() > 0) {
            // Eсли попадаем в интервал времени то считаем число запросов (размер очереди)
            // Здесь проверка на вхождение в интервал требует строгого неравенства т.к.
            // начало интервала (голова очереди) входит в интервал (нестрогое неравенство >=),
            // а окончание интервала (голова + интервал) не входит в интервал (строгое неравенстов <).
            if ((now - times.peek()) < duration) {
                // По условию задачи: "Если количество запросов больше, то должен возвращаться 502 код ошибки"
                // Здесь неравенство не строгое т.к. не учитывается текущий запрос (now) который не помещен в очередь
                if (times.size() >= limit) {
                    return false;
                } else {
                    // если лимит не превышен добавляем время текущего запроса в конец очереди
                    try {
                        times.put(now);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            } else {// если не попадаем в интервал то удаляем голову и проверяем следующую запись
                times.poll();
            }
        }
        return true;
    }
}