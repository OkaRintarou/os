package team.os.global

import team.os.fileSystem.FileSystem
import team.os.gui.GUI
import team.os.instruction.GlobalModules
import team.os.instruction.InsSetFactory
import team.os.interruption.InterruptionManagement
import team.os.io.IOFacilityManagement
import team.os.memoryManager.MemoryManager
import team.os.process.ProcessManagement


object Global {
    val fs: FileSystem = FileSystem()
    val gui: GUI = GUI()
    val im: InterruptionManagement = InterruptionManagement.getInstance()
    val io: IOFacilityManagement = IOFacilityManagement()
    val mm: MemoryManager = MemoryManager()
    val pm: ProcessManagement = ProcessManagement.getInstance()
    val insSetFactory: InsSetFactory = InsSetFactory(
        GlobalModules(
            hashMapOf(),
            hashMapOf(),
            hashMapOf(),
            pm,
            fs,
            mm,
            io
        )
    )

    fun createProcess(name: String, filePath: String) {
        pm.createProcess(name, filePath)
    }

    fun killProcess(pid: Int) {
        pm.terminateProcess(pid)
    }

    fun exec(cycle: Int, core: Int) {
        for (i in 0 until cycle)
            pm.singleRoundExecution(core)
    }
}
