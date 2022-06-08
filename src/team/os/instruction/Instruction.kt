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
     * @property name 进程名
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
     */
    class KillProcess(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.terminateProcess(gb.tmpIntMap["p_$name"] ?: -1)
        }
    }

    /**
     * 获取信号量数值
     */
    class GetProductNum(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            val productNumber = gb.pm.productNumber
            Global.gui.print("Product count: $productNumber")
        }
    }

    /**
     * 增加信号量
     *
     */
    class AddProduct(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            Global.gui.print("Add product from pid: ${pcb.pid}")
            gb.pm.addProduct()
        }
    }

    /**
     * 减少信号量
     */
    class SubProduct(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.subProduct()
        }
    }

    /**
     * 向指定进程发送消息
     *
     * @property dName 目的进程名
     * @property msg 消息内容
     */
    class SendMsg(pcb: PCB, gb: GlobalModules, private val dName: String, private val msg: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            pcb.process.sendMessage(msg, gb.tmpIntMap["p_$dName"] ?: -1)
        }
    }

    /**
     * 获取当前进程的所有消息
     *
     */
    class GetMsg(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            val msgs = pcb.process.showAllMessage()
            for (i in msgs) {
                Global.gui.print("From pid ${i.src}: ${i.message}")
            }
        }
    }

    /**
     * 阻塞当前进程
     *
     * @property cycle 周期数
     */
    class Block(pcb: PCB, gb: GlobalModules, private val cycle: Int) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.blockProcess(cycle)
            gb.im.generateInterruption(InterruptionManagement.InterruptId.BLOCK_INTERRUPTION, pcb.name, cycle)
        }
    }

    /**
     * 申请外设
     *
     * @property type 外设类别
     * @property varName 保存传输内容的变量名
     * @property taskID 任务id，可随意指定
     */
    class HwAccess(
        pcb: PCB, gb: GlobalModules, private val type: String, private val varName: String, private val taskID: String
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            if (type == "keyboard") {
                val index = gb.io.IOFacilityRequest(type, 0, "")[0]
                if (index == -1) {
                    println("分配失败 taskID:$taskID")
                    return
                }
                gb.tmpIntMap[taskID] = index
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
            if (list.size == 2) gb.pm.blockProcess(list[1])
        }
    }

    /**
     * 释放外设
     *
     * @property type 外设类别
     * @property taskID 任务id
     */
    class HwRelease(pcb: PCB, gb: GlobalModules, private val type: String, private val taskID: String) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.io.IOFacilityRelease(type, gb.tmpIntMap[taskID] ?: -1)
        }
    }

    /**
     * 声明变量
     *
     * @property name 变量名
     * @property type 变量类型
     * @property size 变量大小
     */
    class VarDeclare(
        pcb: PCB, gb: GlobalModules, private val name: String, private val type: String, private val size: Int
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.mm.varDeclare(pcb.pid, name, type, size).let {
                gb.variables[name] = it
            }
        }
    }


    /**
     * 读取变量，默认打印
     *
     * @property name 变量名
     * @property type 变量类别
     * @property out 是否打印
     */
    class VarPrint(
        pcb: PCB, gb: GlobalModules, private val name: String, private val type: String, private val out: Boolean = true
    ) : Instruction(pcb, gb) {
        override fun invoke() {
            if (name[0] != '$') gb.mm.run {
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
     *
     * @property name 变量名
     * @property value 变量值
     * @property type 变量类型
     */
    class VarWrite(
        pcb: PCB, gb: GlobalModules, private val name: String, private val value: String, private val type: String
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
     *
     * @property o1 加数1
     * @property o2 加数2
     * @property o3 和
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
     * 字符串拼接
     *
     * @property o1 字符串1
     * @property o2 字符串2
     * @property o3 结果
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

    /**
     * 字符串转整形
     *
     * @property o1 源字符串
     * @property o2 目的整形
     */
    class StrToInt(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "String", false)()
            val r = gb.tmpStrMap[o1] ?: "0"
            VarWrite(pcb, gb, o2, r, "Int")()
        }
    }

    /**
     * 整形转字符串
     *
     * @property o1 整形
     * @property o2 目的字符串
     */
    class IntToStr(pcb: PCB, gb: GlobalModules, private val o1: String, private val o2: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "Int", false)()
            val r = gb.tmpIntMap[o1] ?: 0
            VarWrite(pcb, gb, o2, r.toString(), "String")()
        }
    }

    /**
     * 创建文件，不可同时创建文件夹
     *
     * @property name 文件名
     */
    class FileCreate(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileCreate(name)
        }
    }

    /**
     * 创建文件夹
     *
     * @property name 文件夹名
     */
    class FolderCreate(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.folderCreate(name)
        }
    }

    /**
     * 删除文件夹
     *
     * @property name 文件夹名
     */
    class FolderDelete(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.folderDelete(name)
        }
    }

    /**
     * 写入文件
     *
     * @property filename 文件名
     * @property varName 待写入变量
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
     *
     * @property name 文件名
     */
    class FileDelete(pcb: PCB, gb: GlobalModules, private val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileDelete(name)
        }
    }

    /**
     * 读文件
     *
     * @property filename 文件名
     * @property size 读出大小
     * @property varName 存入变量
     */
    class FileRead(
        pcb: PCB, gb: GlobalModules, private val filename: String, private val size: Int, private val varName: String
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
     * 外部注入指令，可多条
     *
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
     * 因在翻译指令时，Exit一定为进程指令列表的最后一条，
     * Exit实际不做任何操作
     */
    class Exit(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
        }
    }

}
