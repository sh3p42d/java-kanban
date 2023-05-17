package Tasks;

public class Task {
    private int taskId;
    private String taskName;
    private String taskDescription;
    private StatusOfTask taskStatus;

    public Task(String taskName, String taskDescription, StatusOfTask taskStatus) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
    }

    public int getTaskId() {
        return taskId;
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
        return "Tasks.Task{" +
                "ID='" + taskId + '\'' +
                ", name='" + taskName + '\'' +
                ", description='" + taskDescription + '\'' +
                ", status='" + taskStatus + '\'' +
                '}';
    }
}
