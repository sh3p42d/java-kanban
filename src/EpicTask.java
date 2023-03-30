import java.util.ArrayList;

public class EpicTask extends Task {
    protected ArrayList<Integer> subIds = new ArrayList<Integer>(); // ID всех подзадач из эпика

    public EpicTask(String taskName, String taskDescription) {
        // Пустой эпик может быть только в статусе NEW, а при добавлении подзадач, статус будет обновляться
        super(taskName, taskDescription, "NEW");
    }

    public void setSubIds(ArrayList<Integer> subIds) {
        this.subIds = subIds;
    }

    public ArrayList<Integer> getSubIds() {
        return subIds;
    }

    @Override
    public String toString() {
        return "Epic" + super.toString() + "{SubTasksIds='" + subIds + "'}";
    }
}
