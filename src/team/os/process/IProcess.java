package team.os.process;

import team.os.instruction.InstructionSet;

public interface IProcess {
    /**
     * 获取当前进程pid
     * @return 进程标识符pid
     */
    int getPid();

    /**
     * 获取当前进程指令序列
     * @return 进程指令序列InstructionSet
     */
    InstructionSet getInstructionSet();

    /**
     * 获取当前进程的进程控制块
     * @return 当前进程的进程控制快PCB
     */
    Process.PCB getPCB();

    /**
     * 进程执行指令序列
     */
    void run();

    //TODO:进程读取信箱中消息，以及消息的作用？

    //TODO：进程需要对IO设备进行的维护操作有哪些？
}
