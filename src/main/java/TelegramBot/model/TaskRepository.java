package TelegramBot.model;

import TelegramBot.task.TaskData;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {
    private final DatabaseConnector database;

    public TaskRepository(DatabaseConnector database) {
        this.database = database;
    }

    public boolean addTask(Task task) throws SQLException {
        String query = "INSERT INTO tasks (chat_id, description, deadline, priority, status, creation_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, task.getChatId());
            statement.setString(2, task.getDescription());
            statement.setTimestamp(3, task.getDeadline() != null ? task.getDeadline() : null);
            statement.setString(4, task.getPriority());
            statement.setString(5, task.getStatus());
            statement.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            return statement.executeUpdate() > 0;
        }
    }

    public List<TaskData> getTasks(Long chatId, String key) {
        List<TaskData> tasks = new ArrayList<>();
        String query;

        switch (key) {
            case "waitingTasks":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Waiting'";
                break;
            case "activeTasks":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Active'";
                break;
            case "completedTasks":
                query = "SELECT * FROM tasks WHERE chat_id = ? AND status = 'Completed'";
                break;
            case "allTasks":
            default:
                query = "SELECT * FROM tasks WHERE chat_id = ?";
                break;
        }

        try (Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, chatId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int dbID = resultSet.getInt("id");
                    Long chatid = resultSet.getLong("chat_id");
                    String description = resultSet.getString("description");
                    Timestamp deadline = resultSet.getTimestamp("deadline");
                    String priority = resultSet.getString("priority");
                    String status = resultSet.getString("status");
                    Timestamp creationDate = resultSet.getTimestamp("creation_date");

                    TaskData task = new TaskData(dbID, chatid, description, deadline, priority, status, creationDate);
                    tasks.add(task);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public <T> String updateTaskField(long id, String fieldName, T newValue) {
        String query = "UPDATE tasks SET " + fieldName + " = ? WHERE id = ?";

        try (Connection connection = database.connect();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (newValue instanceof String) {
                statement.setString(1, (String) newValue);
            } else if (newValue instanceof Timestamp) {
                statement.setTimestamp(1, (Timestamp) newValue);
            } else {
                return "Unsupported data type for field update.";
            }

            statement.setLong(2, id);
            statement.executeUpdate();
            return fieldName + " updated successfully.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error updating " + fieldName + ".";
        }
    }
}
