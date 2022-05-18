package team.os.gui;

import team.os.global.Global;
import team.os.process.ProcessManagement;

public class TestForProcess {
    public static void main(String[] args) {
        ProcessManagement pm = Global.INSTANCE.getPm();
        pm.createProcess("p0", "0.txt");
        for (int i = 0; i < 100; ++i) {
            pm.singleRoundExecution(50);
        }
    }
}
