package Tasks;

public class SubTask extends Task {
    private int epicId; // ID эпика, к которому относится подзадача

    public SubTask(String taskName, String taskDescription, String taskStatus, int epicId) {
        super(taskName, taskDescription, taskStatus);
        this.epicId = epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Sub" + super.toString() + "{EpicId='" + epicId + "'}";
    }
}
