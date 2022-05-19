package team.os.gui

import team.os.global.Global


fun main() {
    Global.pm.createProcess("ioTest","io.txt");
    for (i in 0 until  50){
        Global.exec(1,1)
    }
}
