package team.os.global

import team.os.fileSystem.FileSystem
import team.os.global.Global.fs
import team.os.global.Global.gui
import team.os.global.Global.im
import team.os.global.Global.insSetFactory
import team.os.global.Global.io
import team.os.global.Global.mm
import team.os.global.Global.pm
import team.os.gui.GUI
import team.os.instruction.GlobalModules
import team.os.instruction.InsSetFactory
import team.os.interruption.InterruptionManagement
import team.os.io.IOFacilityManagement
import team.os.memoryManager.MemoryManager
import team.os.process.ProcessManagement

/**
 * # 全局共享静态模块引用
 *
 * @property fs 文件系统模块
 * @property gui 用户图形界面
 * @property im 中断管理模块
 * @property io 设备管理模块
 * @property mm 内存管理模块
 * @property pm 进程管理模块
 * @property insSetFactory 指令集工厂
 *
 */
object Global {
    val fs: FileSystem = FileSystem()
    val gui: GUI = GUI()
    val im: InterruptionManagement = InterruptionManagement.getInstance()
    val io: IOFacilityManagement = IOFacilityManagement()
    val mm: MemoryManager = MemoryManager()
    val pm: ProcessManagement = ProcessManagement.getInstance()
    val insSetFactory: InsSetFactory = InsSetFactory(
        GlobalModules(
            hashMapOf(), hashMapOf(), hashMapOf(), pm, fs, mm, io, im
        )
    )

    /**
     * # 新建进程
     *
     * @param name 进程名
     * @param filePath 文件名
     */
    fun createProcess(name: String, filePath: String) {
        pm.createProcess(name, filePath)
    }

    /**
     * # 删除进程
     *
     * @param pid 进程pid
     */
    fun killProcess(pid: Int) {
        pm.terminateProcess(pid)
    }

    /**
     * 执行指定周期数
     *
     * @param cycle 周期
     * @param core 并发数
     */
    fun exec(cycle: Int, core: Int) {
        for (i in 0 until cycle) pm.singleRoundExecution(core)
    }
}
