package TelegramBot.task;

import TelegramBot.service.MessageSender;
import TelegramBot.task.utils.TaskBuilder;
import TelegramBot.task.utils.TaskOperation;
import TelegramBot.task.utils.TaskRemover;
import TelegramBot.task.utils.TaskUpdater;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TaskController {
    private final Map<String, TaskOperation> operations = new HashMap<>();
    private final MessageSender messageSender;
    private final TaskService taskService;

    public TaskController(TaskService taskService, MessageSender messageSender) {
        this.messageSender = messageSender;
        this.taskService = taskService;

        // Регистрация операций
        operations.put("create", new TaskBuilder(taskService));
        operations.put("update", new TaskUpdater(taskService));
        operations.put("delete", new TaskRemover(taskService));
    }

    // Инициализация процесса добавления задачи
    public String addTaskCommand(Long chatId) {
        TaskOperation taskBuilder = operations.get("create");
        return taskBuilder.startOperation(chatId);
    }

    // Инициализация процесса обновления задачи
    public String updateTaskCommand(Long chatId) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.startOperation(chatId);
    }

    // Инициализация процесса удаления задачи
    public String deleteTaskCommand(Long chatId) {
        TaskOperation taskRemover = operations.get("delete");
        return taskRemover.startOperation(chatId);
    }

    // Отображение всех задач с фильтрацией по статусу
    public void viewTasksCommand(Long chatId, String status) {
        String tasks = taskService.getTasksByStatus(chatId, status);
        messageSender.sendMessage(chatId, tasks);
    }

    // Обработка пошагового ввода для добавления задачи
    public String handleTaskInput(Long chatId, String input) {
        TaskOperation taskBuilder = operations.get("create");
        return taskBuilder.processInput(chatId, input);
    }

    // Обработка пошагового ввода для обновления задачи
    public String handleUpdateInput(Long chatId, String input) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.processInput(chatId, input);
    }

    // Обработка пошагового ввода для удаления задачи
    public String handleDeleteInput(Long chatId, String input) {
        TaskOperation taskRemover = operations.get("delete");
        return taskRemover.processInput(chatId, input);
    }

    // Проверка, находится ли пользователь в процессе добавления задачи
    public boolean isTaskInProgress(Long chatId) {
        TaskOperation taskBuilder = operations.get("create");
        return ((TaskBuilder) taskBuilder).isInProgress(chatId);
    }

    // Проверка, находится ли пользователь в процессе обновления задачи
    public boolean isUpdateInProgress(Long chatId) {
        TaskOperation taskUpdater = operations.get("update");
        return taskUpdater.isInProgress(chatId);
    }

    // Проверка, находится ли пользователь в процессе удаления задачи
    public boolean isDeleteInProgress(Long chatId) {
        TaskOperation taskRemover = operations.get("delete");
        return taskRemover.isInProgress(chatId);
    }

    // Проверка наличия задач у пользователя
    public boolean hasTasks(Long chatId) {
        List<TaskData> tasks = taskService.getTasks(chatId, "allTasks");
        return !tasks.isEmpty();
    }
}
