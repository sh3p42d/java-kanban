package Tasks;

import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Integer> subIds = new ArrayList<>(); // ID всех подзадач из эпика

    public EpicTask(String taskName, String taskDescription) {
        // Пустой эпик может быть только в статусе NEW, а при добавлении подзадач, статус будет обновляться
        super(taskName, taskDescription, StatusOfTask.NEW);
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

    public void addSub(int subId) {
        subIds.add(subId);
    }

    public void removeSub(int subId) {
        if (subIds.contains(subId)) {
            int i = 0;
            for (Integer id : subIds) {
                if (id == subId) {
                    break;
                }
                i++;
            }
            subIds.remove(i);
        }
    }
}
