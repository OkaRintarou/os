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
     * 进程执行指令序列中的一条指令
     */
    boolean run();

    /**
     * 设置进程下属的指令集
     * @param instructionSet 指令集实例
     */
    void setInstructionSet(InstructionSet instructionSet);

    /**
     * 展示当前消息队列中的消息
     * @return 返回该进程所有消息
     */
    ProcessManagement.Message[] showAllMessage();

    /**
     * 发送消息到邮箱
     * @param message 消息
     * @param destPid 接收方进程标识符
     */
    void sendMessage(String message, int destPid);

    void getMessage();
}
