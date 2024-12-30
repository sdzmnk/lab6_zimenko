package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.*;

@SpringBootApplication
@EnableScheduling
public class Task implements CommandLineRunner {

	private static final int MAX_ATTEMPTS = 3;
	private static final int MAX_TASKS = 1; // Кількість завдань, які потрібно виконати (можна використовувати для завершення)
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private int successfulAttempts = 0;

	public static void main(String[] args) {
		SpringApplication.run(Task.class, args);
	}

	@Override
	public void run(String... args) {
		// Завдання з 3 спробами виконання та інтервалом у 5 секунд
		Runnable taskWithRetries = new Runnable() {
			private int attempt = 0;

			@Override
			public void run() {
				attempt++;
				try {
					System.out.println("Спроба " + attempt + ": Виконання завдання...");
					performTask();
					successfulAttempts++;

					if (successfulAttempts >= MAX_TASKS) {
						// Завершення роботи після того, як все було виконано
						executorService.shutdown();
						System.out.println("Завдання виконано успішно після " + attempt + " спроб.");
					}
				} catch (Exception e) {
					System.out.println("Спроба " + attempt + " не вдалася: " + e.getMessage());
					if (attempt < MAX_ATTEMPTS) {
						executorService.schedule(this, 5, TimeUnit.SECONDS);
					} else {
						System.out.println("Завдання провалене після 3 спроб.");
						executorService.shutdown();
					}
				}
			}

			private void performTask() throws Exception {
				if (Math.random() > 0.7) { // 70% ймовірність невдачі
					throw new Exception("Симульована помилка");
				}
				System.out.println("Завдання виконано успішно!");
			}
		};

		// Запланувати перше виконання завдання
		executorService.schedule(taskWithRetries, 0, TimeUnit.SECONDS);

		executorService.schedule(() -> executorService.shutdown(), 17, TimeUnit.SECONDS);
	}

	@Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 15000)
	public void delayedTask() {
		System.out.println("15 секунд від запуску програми.");
		System.exit(0);
	}
}
