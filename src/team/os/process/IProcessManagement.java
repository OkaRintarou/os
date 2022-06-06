package team.os.process;

import java.util.List;
import java.util.Map;

public interface IProcessManagement {
    /**
     * 创建进程
     *
     * @param filePath    程序（文件）路径
     * @param processName 进程名称 未来可能从文件系统获取
     */
    int createProcess(String processName, String filePath);

    /**
     * 终止进程
     *
     * @param pid 进程标识符
     */
    void terminateProcess(int pid);

    /**
     * @param blockTime 阻塞时间，用于唤醒
     *                  阻塞当前运行态进程
     */
    void blockProcess(int blockTime);

    /**
     * 在就绪进程和运行进程之间调度
     */
    void scheduleProcess();

    /**
     * 唤醒被阻塞但已经获得除cpu外的所有资源的进程
     */
    void wakeUpProcess(int pid);

    /**
     * 根据进程标识符获取进程控制块
     *
     * @param pid 进程标识符
     * @return 进程控制块
     */
    Process.PCB getPCB(int pid);

    /**
     * 获取所有进程的状态
     *
     * @return 包含所有进程的状态列表
     */
    List<Map<Integer, Process.ProcessStates>> getAllProcessState();

    /**
     * 将最近加入阻塞态的进程换入到挂起队列
     *
     * @param pid 要换出的进程标识符
     */
    void swapOutProcess(int pid);

    /**
     * 暴露给GUI的单周期执行操作
     *
     * @param coreNumber 核心数
     */
    void singleRoundExecution(int coreNumber);

    /**
     * 获取仓库中产品数量
     *
     * @return 仓库中产品数量
     */
    int getProductNumber();

    /**
     * 向仓库中添加产品
     */
    void addProduct();

    /**
     * 从仓库中拿走产品
     */
    void subProduct();
}
