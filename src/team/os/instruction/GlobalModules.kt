package team.os.instruction

import team.os.fileSystem.IFileSystem
import team.os.interruption.InterruptionManagement
import team.os.io.IOFacilityManagement
import team.os.memoryManager.IMemoryManager
import team.os.process.ProcessManagement

/**
 * #  进程使用的全局模块实例及活动变量维护
 *
 * 用于存放全局共享的模块单例
 *
 * 维护进程的变量表和一些变量地址
 *
 * 该数据结构中进程活动产生部分是各进程独立维护的，
 * 因此传给指令集需要拷贝
 *
 * 设备管理为静态方法，因此在此处不存在实例
 * @property variables 变量名与标识符的映射
 * @property tmpIntMap 存储临时整形变量
 * @property tmpStrMap 存储临时字符串变量
 * @property pm 进程管理模块
 * @property fs 文件管理模块
 * @property mm 内存管理模块
 * @property io 设备管理模块
 * @property im 中断管理模块
 */
class GlobalModules(
    val variables: HashMap<String, Int>,
    val tmpIntMap: HashMap<String, Int>,
    val tmpStrMap: HashMap<String, String>,
    val pm: ProcessManagement,
    val fs: IFileSystem,
    val mm: IMemoryManager,
    val io: IOFacilityManagement,
    val im: InterruptionManagement
) {
    fun copy() = GlobalModules(
        HashMap(), HashMap(), HashMap(), pm, fs, mm, io, im
    )
}
