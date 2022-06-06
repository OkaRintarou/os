package team.os.interruption;

public interface IInterruptionManagement {
    /**
     * 产生中断
     *
     * @param interruptId 中断向量标识符
     * @param processName 产生中断的进程名称
     * @param cycle       阻塞周期数
     */
    void generateInterruption(int interruptId, String processName, int cycle);

    /**
     * 开中断
     */
    void allowInterruption();

    /**
     * 关中断
     */
    void forbidInterruption();
}
