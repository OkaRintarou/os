package team.os.interruption;

import java.util.HashMap;
import java.util.Map;

/**
 * 中断管理类
 * 维护中断向量表
 * 产生新中断
 * */
public class InterruptionManagement implements IInterruptionManagement {
    private final Map<Integer, Interruption> interruptionTable; //中断向量表
    private boolean isAllowInterrupt; //标识是否允许中断
    //TODO: 需要时钟管理模块来配合中断管理模块，例如每个时间片产生中断进行进程调度
    private static final InterruptionManagement instance = new InterruptionManagement();

    private InterruptionManagement() {
        interruptionTable = new HashMap<>();
        isAllowInterrupt = true;
    }
    /**
     * 获取全局唯一InterruptionManagement实例
     * */
    public static InterruptionManagement getInstance() {
        return instance;
    }

    @Override
    public void generateInterruption(int interruptId) {
        //TODO：需要中断执行程序的支持
        //目前的思路是将中断执行程序初始化成一个状态为Interrupt的进程，并且设置关中断
        //即不允许多层中断产生，直到中断处理程序执行完毕
    }

    @Override
    public void allowInterruption() {
        isAllowInterrupt = true;
    }

    @Override
    public void forbidInterruption() {
        isAllowInterrupt = false;
    }

    public boolean getIsAllowInterrupt() {
        return isAllowInterrupt;
    }
}