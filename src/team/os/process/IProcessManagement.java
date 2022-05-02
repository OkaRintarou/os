package team.os.process;

import team.os.instruction.InstructionSet;
import java.util.List;
import java.util.Map;

public interface IProcessManagement {
    /**
     * 创建进程
     * @param filePath 程序（文件）路径
     * */
    void createProcess(int pid, String processName, String filePath, int memorySize, InstructionSet instructionSet);

    /**
     * 终止进程
     * @param pid 进程标识符
     */
    void terminateProcess(int pid);

    /**
     * 阻塞进程
     */
    void blockProcess();

    /**
     * 在就绪进程和运行进程之间调度
     */
    void scheduleProcess();

    /**
     * 唤醒被阻塞但已经获得除cpu外的所有资源的进程
     */
    void wakeUpProcess(int pid);
    /**
     * 把就绪队列中的进程按照优先级进行排序
     * @param readyQueue 就绪队列
     */
    void sortByPriority(List<Process.PCB> readyQueue);

    /**
     * 根据进程标识符获取进程控制块
     * @param pid 进程标识符
     * @return 进程控制块
     */
    Process.PCB getPCB(int pid);

    /**
     * 根据进程标识符为进程加锁
     * @param lockName 锁名称
     * @param pid 进程标识符
     */
    void addLock(String lockName, int pid);

    /**
     * 根据进程标识符移除锁
     * @param lockName 锁名称
     * @param pid 进程标识符
     */
    void removeLock(String lockName, int pid);

    /**
     * 获取所有进程的状态
     * @return 包含所有进程的状态列表
     */
    List<Map<Integer, Process.ProcessStates>> getAllProcessState();

    /**
     * 发送消息到邮箱
     * @param message 消息
     * @param dstPid 目的进程标识符
     */
    void sendMessageToMail(String message, int dstPid);

    /**
     * 从邮箱取回当前进程消息
     */
    void getMessageFromMail(int pid);

    //TODO：目前不清楚进程间同步互斥应该如何实现，以及应该如何处理
}
