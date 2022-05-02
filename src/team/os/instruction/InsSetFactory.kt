package team.os.instruction

import team.os.process.Process

/**
 * # 指令集工厂
 * 用于得到一个指令集的实例
 * @property gb 全局模块引用，此为共享实例，导入指令集应使用拷贝，该参数将会在main里传入
 */
class InsSetFactory(private val gb: GlobalModules) {
    /**
     * 获得一个指令集的实例，该接口由进程初始化时调用
     * @param fileName 文件名，为空串、空白串或null时表示从键盘得到指令
     * @return 指令集的实例
     */
    fun getInst(fileName: String?, pcb: Process.PCB): InstructionSet = InstructionSet(fileName, pcb, gb.copy())
}
