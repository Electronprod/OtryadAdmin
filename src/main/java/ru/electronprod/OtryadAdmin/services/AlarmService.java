package ru.electronprod.OtryadAdmin.services;

import java.time.LocalTime;
import java.util.concurrent.locks.LockSupport;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.electronprod.OtryadAdmin.data.AlarmRepository;

@Slf4j
@Component
public class AlarmService implements InitializingBean {
	@Autowired
	private AlarmRepository alarmRep;

	@Override
	public void afterPropertiesSet() throws Exception {
		Thread engineThread = new AlarmEngine();
		engineThread.setPriority(Thread.MIN_PRIORITY);
		engineThread.setName("AlarmEngineThread");
		engineThread.start();
	}

	// Once per day
	@Scheduled(fixedRate = 36000, initialDelay = 0)
	public void checkScheludeUpdates() throws InterruptedException {
		alarmRep.findAll();
	}

}

@Slf4j
class AlarmEngine extends Thread {
	public void run() {
		log.info(Thread.currentThread().getName() + " launched successfully.");
		while (true) {
			LockSupport.parkNanos(100000);
		}
	}
}
