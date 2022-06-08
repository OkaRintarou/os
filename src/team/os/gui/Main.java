package team.os.gui;

import team.os.global.Global;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Frame frame = Global.INSTANCE.getGui();
        frame.setVisible(true);
    }
}
