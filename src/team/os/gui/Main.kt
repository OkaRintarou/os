package team.os.gui

import team.os.global.Global

fun main() {
    Global.pm.createProcess("root","0.txt")
    for (i in 0..50){
        Global.pm.singleRoundExecution(2)
    }
}
