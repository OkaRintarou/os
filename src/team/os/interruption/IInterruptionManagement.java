package team.os.interruption;

public interface IInterruptionManagement {
    /**
     * 产生中断
     * @param interruptId 中断向量标识符
     */
    void generateInterruption(int interruptId);

    /**
     * 开中断
     */
    void allowInterruption();

    /**
     * 关中断
     */
    void forbidInterruption();
}
