package team.os.instruction

import team.os.global.Global
import team.os.interruption.InterruptionManagement
import team.os.process.Process.PCB

/**
 * # 指令原型
 * @property pcb 指令集所在进程的PCB
 * @property gb 指令集所在进程的GlobalModules拷贝
 */
sealed class Instruction(protected val pcb: PCB, protected val gb: GlobalModules) {
    /**
     * 指令执行
     */
    abstract operator fun invoke()

    /**
     * 创建进程
     * @property filePath 文件路径，非物理路径
     */
    class CreateProcess(pcb: PCB, gb: GlobalModules, private val name: String, private val filePath: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            val nPid = gb.pm.createProcess(name, filePath)
            gb.tmpIntMap["p_$name"] = nPid
        }
    }


    /**
     * 终止指定进程
     * @property name 被终止的进程名
     *
     * @param pcb PCB
     */
    class KillProcess(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.terminateProcess(gb.tmpIntMap["p_$name"] ?: -1)
        }
    }

    /**
     * Send msg
     *
     * @property dName
     * @property msg
     * @constructor
     *
     * @param pcb
     * @param gb
     */
    class SendMsg(pcb: PCB, gb: GlobalModules, private val dName: String, private val msg: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            pcb.process.sendMessage(msg, gb.tmpIntMap["p_$dName"] ?: -1)
        }
    }

    class GetMsg(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            val msgs = pcb.process.showAllMessage()
            for (i in msgs) {
                Global.gui.print("From pid ${i.src}: ${i.message}")
            }
        }
    }

    class Block(pcb: PCB, gb: GlobalModules, private val cycle: Int) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.blockProcess(cycle)
            gb.im.generateInterruption(InterruptionManagement.InterruptId.BLOCK_INTERRUPTION, pcb.name, cycle)
        }
    }

    /**
     * 申请外设
     * @property type 外设类型
     *
     * @param pcb PCB
     */
    class HwAccess(
        pcb: PCB,
        gb: GlobalModules,
        private val type: String,
        private val varName: String,
        private val taskID: String
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            if(type=="keyboard"){
                val index=gb.io.IOFacilityRequest(type,0,"")[0]
                if (index==-1){
                    println("分配失败 taskID:$taskID")
                    return
                }
                gb.tmpIntMap[taskID]=index
                return
            }
            VarPrint(pcb, gb, varName, "String", false)()
            val content = gb.tmpStrMap[varName] ?: ""
            val list = gb.io.IOFacilityRequest(type, content.length, content)
            if (list[0] == -1) {
                println("分配失败 taskID:$taskID")
                return
            }
            gb.tmpIntMap[taskID] = list[0]
            if (list.size == 2)
                gb.pm.blockProcess(list[1])
        }
    }

    /**
     * 释放外设
     *
     * @param pcb PCB
     */
    class HwRelease(pcb: PCB, gb: GlobalModules, private val type: String, private val taskID: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.io.IOFacilityRelease(type, gb.tmpIntMap[taskID] ?: -1)
        }
    }

    /**
     * 声明变量
     * 隐式转换为内存申请
     * @property name 变量名
     * @property size 变量大小
     *
     * @param pcb PCB
     */
    class VarDeclare(
        pcb: PCB,
        gb: GlobalModules,
        private val name: String,
        private val type: String,
        private val size: Int
    ) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.mm.varDeclare(pcb.pid, name, type, size).let {
                gb.variables[name] = it
            }
        }
    }


    /**
     * 打印变量
     * 内存访问会隐式转换为地址
     * 同时可作为读取使用
     * @property name 变量名称
     *
     * @param pcb PCB
     */
    class VarPrint(
        pcb: PCB,
        gb: GlobalModules,
        private val name: String,
        private val type: String,
        private val out: Boolean = true
    ) :
        Instruction(pcb, gb) {
        override fun invoke() {
            if (name[0] != '$')
                gb.mm.run {
                    when (type) {
                        "String" -> gb.tmpStrMap[name] = varReadString(pcb.pid, name)
                        "Int" -> gb.tmpIntMap[name] = varReadInt(pcb.pid, name)
                        else -> throw Exception("Invalid varPrint type!")
                    }
                }
            if (out) Global.gui.print(
                when (type) {
                    "String" -> gb.tmpStrMap[name]
                    "Int" -> gb.tmpIntMap[name].toString()
                    else -> "Never happened in VarPrint!!!"
                }
            )
        }
    }

    /**
     * 写入变量
     * @property name 变量名
     * @property value 值，正常为字符串，若以$开头则为整形
     *
     * @param pcb PCB
     */
    class VarWrite(
        pcb: PCB,
        gb: GlobalModules,
        private val name: String,
        private val value: String,
        private val type: String
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            when (type) {
                "String" -> gb.mm.varWriteString(pcb.pid, name, value)
                "Int" -> gb.mm.varWriteInt(pcb.pid, name, value.toInt())
            }
        }
    }


    /**
     * 整形加法
     * @property o1 变量1
     * @property o2 变量2
     *
     * @param pcb PCB
     */
    class Add(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String, private val o3: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "Int", false)()
            VarPrint(pcb, gb, o2, "Int", false)()
            val result = (gb.tmpIntMap[o1] ?: -1) + (gb.tmpIntMap[o2] ?: -1)
            VarWrite(pcb, gb, o3, result.toString(), "Int")()
        }
    }

    /**
     * 字符串连接
     * 只能对已有变量添加常量字符串
     * @property o1 变量名
     *
     * @param pcb PCB
     */
    class StrCat(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String, private val o3: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "String", false)()
            VarPrint(pcb, gb, o2, "String", false)()
            val r = (gb.tmpStrMap[o1] ?: "") + (gb.tmpStrMap[o2] ?: "")
            VarWrite(pcb, gb, o3, r, "String")()
        }
    }

    class StrToInt(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "String", false)()
            val r = gb.tmpStrMap[o1] ?: "0"
            VarWrite(pcb, gb, o2, r, "Int")()
        }
    }

    class IntToStr(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "Int", false)()
            val r = gb.tmpIntMap[o1] ?: 0
            VarWrite(pcb, gb, o2, r.toString(), "String")()
        }
    }

    /**
     * 创建文件
     *
     * @param pcb PCB
     * @property name 文件名
     */
    class FileCreate(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileCreate(name)
        }
    }

    class FolderCreate(pcb:PCB,gb:GlobalModules,private val name:String):Instruction(pcb,gb){
        override fun invoke() {
            gb.fs.folderCreate(name)
        }
    }
    class FolderDelete(pcb:PCB,gb:GlobalModules,private val name:String):Instruction(pcb,gb){
        override fun invoke() {
            gb.fs.folderDelete(name)
        }
    }

    /**
     * 写入文件，直接覆盖
     * @property filename 文件名
     *
     * @param pcb PCB
     */
    class FileWrite(pcb: PCB, gb: GlobalModules, private val filename: String, private val varName: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.run {
                val h = fileOpen(filename)
                val size: Int
                VarPrint(pcb, gb, varName, "String", false)()
                val value = gb.tmpStrMap[varName] ?: ""
                fileWrite(h, value.toByteArray().also { size = it.size }, size)
                fileClose(h)
            }
        }
    }

    /**
     * 删除文件
     * @property name 文件名
     *
     * @param pcb PCB
     */
    class FileDelete(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileDelete(name)
        }
    }

    /**
     * 读文件
     * @property filename 文件名
     * @property size 读取大小
     *
     * @param pcb PCB
     */
    class FileRead(
        pcb: PCB,
        gb: GlobalModules,
        private val filename: String,
        private val size: Int,
        private val varName: String
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.run {
                val h = fileOpen(filename)
                val r = ByteArray(size + 1)
                val s = fileRead(h, r, size)
                fileClose(h)
                val sb = StringBuilder()
                for (i in 0 until s) {
                    val t = r[i].toInt()
                    if (t == 0) break
                    sb.append(t.toChar())
                }
                VarWrite(pcb, gb, varName, sb.toString(), "String")()
            }
        }
    }

    /**
     * 调试用指令
     * 该周期执行从控制台获取的指令
     *
     * @param pcb PCB
     */
    class Broker(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            for (str in Global.gui.insString) {
                InstructionSet.getIns(str, pcb, gb)()
            }
        }
    }

    /**
     * 当前进程退出
     * 任何非法指令均会翻译为该指令
     * @param pcb PCB
     */
    class Exit(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
        }
    }

}
