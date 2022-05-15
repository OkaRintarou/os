package team.os.gui;

import team.os.global.Global;

import java.util.Timer;

public class Main {
    public static void main(String[] args) {
        GUI frame= Global.INSTANCE.getGui();
        Timer timer = new java.util.Timer();
        timer.schedule(new GUI.update(), 0, 1000);
        frame.setVisible(true);
    }
}
