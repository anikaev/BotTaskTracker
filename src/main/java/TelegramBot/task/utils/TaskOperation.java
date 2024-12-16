package TelegramBot.task.utils;

public interface TaskOperation {
    String startOperation(Long chatId);
    String processInput(Long chatId, String input);
    void clearOperationData(Long chatId);
    boolean isInProgress(Long chatId); // Добавьте этот метод
}

