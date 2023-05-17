package Manager;

import java.io.IOException;

public class ManagerSaveException extends IOException {

    public ManagerSaveException(String errorMessage) {
        super(errorMessage);
    }
}
