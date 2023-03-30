public class Task {
    protected int taskId;
    protected String taskName;
    protected String taskDescription;
    protected String taskStatus;

    public Task(String taskName, String taskDescription, String taskStatus) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
    }

    // getTaskName и getTaskDescription убрал, т.к. нигде не используются

    public int getTaskId() {
        return taskId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return "Task{" +
                "ID='" + taskId + '\'' +
                ", name='" + taskName + '\'' +
                ", description='" + taskDescription + '\'' +
                ", status='" + taskStatus + '\'' +
                '}';
    }
}
