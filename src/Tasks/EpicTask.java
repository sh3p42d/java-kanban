package Tasks;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicTask extends Task {
    private ArrayList<Integer> subIds = new ArrayList<>(); // ID всех подзадач из эпика
    private LocalDateTime endTime;

    public EpicTask(String taskName, String taskDescription) {
        super(taskName, taskDescription, StatusOfTask.NEW);
    }

    public void setEndTime() {
        this.endTime = getStartTime().plusMinutes(getDuration().toMinutes());
    }

    public void setSubIds(ArrayList<Integer> subIds) {
        this.subIds = subIds;
    }

    public ArrayList<Integer> getSubIds() {
        return subIds;
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

    @Override
    public String toString() {
        return "Epic" + super.toString() + "{SubTasksIds='" + subIds + "'}";
    }
}
