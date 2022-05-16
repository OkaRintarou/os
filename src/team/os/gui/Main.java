package team.os.gui;

import team.os.global.Global;

import java.awt.*;
import java.util.Timer;

public class Main {
    public static void main(String[] args) {

        // For test
        Global.INSTANCE.createProcess("root","0.txt");
        // end

        Frame frame = Global.INSTANCE.getGui();
//        Timer timer = new Timer();
//        timer.schedule(new GUI.update(), 0, 1000);
        frame.setVisible(true);

        // For test
        for (int i=0;i<50;i++){
            Global.INSTANCE.getPm().singleRoundExecution(1);
        }
        // End
    }
}
