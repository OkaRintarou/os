package team.os.instruction

import team.os.global.Global
import team.os.instruction.Instruction.*
import team.os.process.Process.PCB
import java.io.File


/**
 * # 指令集
 * 将内部维护一个指令列表，
 * 每次调用next将执行一条指令并指向下一条指令
 * @property fileName 文件名，空串、空白串和null表示从键盘得到指令
 * @property pcb 所属进程的PCB
 * @property gb 全局模块引用，需传入拷贝以维护专属活动变量
 */
class InstructionSet(private val fileName: String?, private val pcb: PCB, private val gb: GlobalModules, pName:String) {
    /**
     * 指令列表
     */
    private val list: ArrayList<Instruction> = ArrayList()

    /**
     * 指令数
     */
    val insCount get() = list.size

    init {
        gb.tmpIntMap["p_$pName"]=pcb.pid
        val instructions: ArrayList<String> = if ((fileName == null) || fileName.isBlank()) {
            Global.gui.insString
        } else {
            val lines = File(fileName).readLines()
            ArrayList(lines)
        }
        if (instructions.size == 0) instructions.add("Exit")
        for (i in 0 until instructions.size) {
            val tmp = getIns(instructions[i], this.pcb, this.gb)
            list.add(tmp)
            if (tmp is Exit) break
        }
    }


    companion object {
        /**
         * 解析单条指令
         *
         * @param insString 指令串
         * @param pcb 默认使用私有属性pcb
         * @param gb 默认使用私有属性gb
         * @return 指令类
         */
        fun getIns(insString: String, pcb: PCB, gb: GlobalModules): Instruction =
            insString.split(" ").let {
                when (it[0]) {
                    "CreateProcess" -> CreateProcess(pcb, gb, it[1], it[2])
                    "KillProcess" -> KillProcess(pcb, gb, it[1])
                    "SendMsg" -> SendMsg(pcb, gb, it[1], it[2])
                    "GetMsg" -> GetMsg(pcb, gb)
                    "Block" -> Block(pcb, gb, it[1].toInt())
                    "HwAccess" -> HwAccess(pcb, gb, it[1], it[2],it[3])
                    "HwRelease" -> HwRelease(pcb, gb, it[1],it[2])
                    "VarDeclare" -> VarDeclare(pcb, gb, it[1], it[2], it[3].toInt())
                    "VarPrint" -> VarPrint(pcb, gb, it[1], it[2])
                    "VarWrite" -> VarWrite(pcb, gb, it[1], it[2], it[3])
                    "Add" -> Add(pcb, gb, it[1], it[2], it[3])
                    "StrCat" -> StrCat(pcb, gb, it[1], it[2], it[3])
                    "StrToInt"->StrToInt(pcb,gb,it[1],it[2])
                    "IntToStr"->IntToStr(pcb,gb,it[1],it[2])
                    "FileCreate" -> FileCreate(pcb, gb, it[1])
                    "FileWrite" -> FileWrite(pcb, gb, it[1], it[2])
                    "FileDelete" -> FileDelete(pcb, gb, it[1])
                    "FileRead" -> FileRead(pcb, gb, it[1], it[2].toInt(), it[3])
                    "Broker" -> Broker(pcb, gb)
                    else -> Exit(pcb, gb)
                }
            }
    }


    /**
     * 指令执行到的位置指针
     */
    private var pointer: Int = 0

    /**
     * 执行当前指令并指向下一条指令
     *
     * 初始化时保证了至少有一条指令，为Exit
     * @return 若下一条指令存在，true
     */
    fun next(): Boolean {
        list[pointer]()
        pointer++
        return pointer < list.size
    }
}
