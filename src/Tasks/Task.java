package Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Task {
    private int taskId;
    private String taskName;
    private String taskDescription;
    private StatusOfTask taskStatus;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(String taskName, String taskDescription, StatusOfTask taskStatus) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
    }

    public Task(String taskName, String taskDescription, StatusOfTask taskStatus,
                String startTime, long duration) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.startTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        this.duration = Duration.ofMinutes(duration);
    }

    public int getTaskId() {
        return taskId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public StatusOfTask getTaskStatus() {
        return taskStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setTaskStatus(StatusOfTask taskStatus) {
        this.taskStatus = taskStatus;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    @Override
    public String toString() {
        String timeStr = "";
        String durStr = "";

        if (startTime != null) {
            timeStr = startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        if (duration != null) {
            durStr = String.valueOf(duration.toMinutes());
        }

        return "Tasks.Task{" +
                "ID='" + taskId + '\'' +
                ", name='" + taskName + '\'' +
                ", description='" + taskDescription + '\'' +
                ", status='" + taskStatus + '\'' +
                ", start='" + timeStr + '\'' +
                " duration='" + durStr + '\'' +
                '}';
    }
}
