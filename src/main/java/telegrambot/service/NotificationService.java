package telegrambot.service;

import telegrambot.model.BotUtils;
import telegrambot.model.User;
import telegrambot.model.UserRepository;
import telegrambot.task.TaskData;
import telegrambot.task.TaskService;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class NotificationService {
    private final TaskService taskService;
    private final MessageSender messageSender;
    private final UserRepository userRepository;

    public NotificationService(TaskService taskService, BotUtils botUtils) {
        this.taskService = taskService;
        this.messageSender = botUtils.getMessageSender();
        this.userRepository = botUtils.getUserRepository();

        startNotificationScheduler();
    }

    private void startNotificationScheduler() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendNotifications();
            }
        }, 0, 60 * 1000);
        log.info("Планировщик уведомлений запущен.");
    }

    private void sendNotifications() {
        List<User> users = userRepository.getAllUsers();
        Instant now = Instant.now();

        for (User user : users) {
            Long chatId = user.getChatId();
            if (chatId == null) continue;

            try {
                List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
                for (TaskData task : tasks) {
                    if (task.getDeadline() == null) continue;

                    long timeLeft = task.getDeadline().toInstant().toEpochMilli() - now.toEpochMilli();

                    if (timeLeft > 0 && timeLeft <= 10 * 60 * 1000) {
                        messageSender.setCurrentChatId(task.getChatId());
                        messageSender.sendMessage("⏰ Напоминание! До дедлайна задачи '" + task.getDescription() + "' осталось менее 10 минут!");
                        log.info("Отправлено напоминание о задаче {} пользователю {}.", task.getDescription(), chatId);
                    } else if (timeLeft <= 0) {
                        if (task.getDeadlineNotificationCount() < 3) {
                            messageSender.setCurrentChatId(task.getChatId());
                            messageSender.sendMessage("❗️ Дедлайн задачи '" + task.getDescription() + "' уже прошёл!");
                            task.setDeadlineNotificationCount(task.getDeadlineNotificationCount() + 1);

                            boolean updated = taskService.updateTaskNotificationCount(task.getDbID(), task.getDeadlineNotificationCount());
                            if (updated) {
                                log.info("Обновлён счетчик уведомлений для задачи {} пользователя {}.", task.getDbID(), chatId);
                            } else {
                                log.warn("Не удалось обновить счетчик уведомлений для задачи {} пользователя {}.", task.getDbID(), chatId);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Ошибка при отправке уведомлений для chatId: {}", chatId, e);
            }
        }
    }
}
