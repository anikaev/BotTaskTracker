package TelegramBot.model;

import TelegramBot.task.TaskController;
import TelegramBot.task.TaskData;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class TaskBuilder {
    private final Map<Long, TaskData> taskDataMap = new HashMap<>();
    private final TaskController taskController;

    public TaskBuilder(TaskController taskController) {
        this.taskController = taskController;
    }

    public String startTaskCreation(long chatId) {
        taskDataMap.put(chatId, new TaskData());
        return "Please provide a description for the task.";
    }

    public String processInput(long chatId, String input) {
        TaskData taskData = taskDataMap.get(chatId);
        if (taskData == null) {
            return "Task creation process has not started. Please initiate by clicking 'Add Task'.";
        }

        switch (taskData.getStep()) {
            case 0:
                taskData.setDescription(input);
                taskData.nextStep();
                return "Please provide a deadline for the task (YYYY-MM-DD HH:MM:SS).";

            case 1:
                try {
                    taskData.setDeadline(Timestamp.valueOf(input));
                    taskData.nextStep();
                    return "Please provide a priority for the task (e.g., High, Medium, Low).";
                } catch (IllegalArgumentException e) {
                    return "Invalid date format. Please use YYYY-MM-DD HH:MM:SS.";
                }

            case 2:
                taskData.setPriority(input);
                taskData.setChatId(chatId);
                taskData.setStatus("Pending");
                taskData.setCreationDate(new Timestamp(System.currentTimeMillis()));

                taskController.addTaskCommand(taskData);

                taskDataMap.remove(chatId);
                return "Task has been added successfully!";

            default:
                return "Unexpected error. Please try again.";
        }
    }

    public boolean isInProgress(long chatId) {
        return taskDataMap.containsKey(chatId);
    }
}
