package team.os.instruction

import team.os.io.IOFacility
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
    class CreateProcess(pcb: PCB, gb: GlobalModules, private val filePath: String) : Instruction(pcb, gb) {
        override fun invoke() {
            TODO()
        }
    }


    /**
     * 终止指定进程
     * @property pid 被终止进程的PID
     *
     * @param pcb PCB
     */
    class KillProcess(pcb: PCB, gb: GlobalModules, val pid: Int) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.terminateProcess(pid)
        }
    }

    /**
     * 创建互斥锁
     * 互斥锁为全局共享的变量
     * @property name  互斥锁名称
     *
     * @param pcb PCB
     */
    class CreateMutex(pcb: PCB, gb: GlobalModules, val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.addLock(name, pcb.pid)
        }
    }

    /**
     * 释放互斥锁
     * @property name 互斥锁名称
     * @param pcb PCB
     */
    class ReleaseMutex(pcb: PCB, gb: GlobalModules, val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.removeLock(name, pcb.pid)
        }
    }

    /**
     * 申请外设
     * @property name 外设名称
     * @property size 申请的传输字节数
     *
     * @param pcb PCB
     */
    class HwAccess(pcb: PCB, gb: GlobalModules, val name: String, val size: Int) : Instruction(pcb, gb) {
        override fun invoke() {
            val result = gb.io.IOFacilityRequest(name, size)
            val num=result[0]
            val time=result[1]
            gb.tmpIntMap[name]=num
            TODO("阻塞进程，时间为time个周期")
        }
    }

    /**
     * 释放外设
     * @property name 外设名称
     *
     * @param pcb PCB
     */
    class HwRelease(pcb: PCB, gb: GlobalModules, val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.io.IOFacilityRelease(name,gb.tmpIntMap[name]?:-1)
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
    class VarDeclare(pcb: PCB, gb: GlobalModules, val name: String, val type: String, val size: Int) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.mm.varDeclare(pcb.pid, type, size).let {
                gb.variables[name]=it
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
    class VarPrint(pcb: PCB, gb: GlobalModules, val name: String, val type: String, val out: Boolean = true) :
        Instruction(pcb, gb) {
        override fun invoke() {
            gb.mm.run {
                when (type) {
                    "String" -> {
                        gb.tmpStrMap[name]=varReadString(gb.variables[name]?:-1)
                    }
                    "Int" -> {
                        gb.tmpIntMap[name]=varReadInt(gb.variables[name]?:-1)
                    }
                    else -> {
                        throw Exception("Invalid varPrint type!")
                    }
                }
            }
            if (out) TODO("GUI PRINT")
        }
    }

    /**
     * 写入变量
     * @property name 变量名
     * @property value 值，正常为字符串，若以$开头则为整形
     *
     * @param pcb PCB
     */
    class VarWrite(pcb: PCB, gb: GlobalModules, val name: String, val value: String) : Instruction(pcb, gb) {
        override fun invoke() {
            if(value[0]=='$'){
                gb.mm.varWriteInt(gb.variables[name]?:-1,value.slice(1 until value.length).toInt())
            }else{
                gb.mm.varWriteString(gb.variables[name]?:-1,value)
            }
        }
    }

    /**
     * 整形加法，运算结果写入寄存器$result
     * @property o1 变量1
     * @property o2 变量2
     *
     * @param pcb PCB
     */
    class Add(pcb: PCB, gb: GlobalModules, val o1: String, val o2: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb,gb,o1,"Int",false)()
            VarPrint(pcb,gb,o2,"Int",false)()
            gb.tmpIntMap["\$result"]=(gb.tmpIntMap[o1]?:-1)+(gb.tmpIntMap[o2]?:-1)
        }
    }

    /**
     * 字符串连接
     * 只能对已有变量添加常量字符串
     * @property o1 变量名
     * @property addition 常量字符串
     *
     * @param pcb PCB
     */
    class StrCat(pcb: PCB, gb: GlobalModules, val o1: String, val addition: String) : Instruction(pcb, gb) {
        override fun invoke() {
            VarPrint(pcb, gb, o1, "String", false)()
            val r = (gb.tmpStrMap[o1]?:"") + addition
            VarWrite(pcb, gb, o1, r)()
        }
    }

    /**
     * 创建文件
     *
     * @param pcb PCB
     * @property name 文件名
     * @property size 文件大小
     */
    class FileCreate(pcb: PCB, gb: GlobalModules, val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileCreate(name)
        }
    }

    /**
     * 写入文件，直接覆盖
     * @property name 文件名
     * @property value 写入内容
     *
     * @param pcb PCB
     */
    class FileWrite(pcb: PCB, gb: GlobalModules, val name: String, val value: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.run {
                val h = fileOpen(name)
                val size: Int
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
    class FileDelete(pcb: PCB, gb: GlobalModules, val name: String) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.fileDelete(name)
        }
    }

    /**
     * 读文件
     * @property name 文件名
     * @property size 读取大小
     *
     * @param pcb PCB
     */
    class FileRead(pcb: PCB, gb: GlobalModules, val name: String, val size: Int) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.fs.run {
                val h = fileOpen(name)
                val r=ByteArray(size+1)
                val s=fileRead(h, r,size)
                fileClose(h)
                val sb=StringBuilder()
                for(i in 0 until s){
                    sb.append(r[i])
                }
                gb.tmpStrMap["\$result"]=sb.toString()
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
            TODO("GUI")
        }
    }

    /**
     * 当前进程退出
     * 任何非法指令均会翻译为该指令
     * @param pcb PCB
     */
    class Exit(pcb: PCB, gb: GlobalModules) : Instruction(pcb, gb) {
        override fun invoke() {
            gb.pm.terminateProcess(pcb.pid)
        }
    }

}
