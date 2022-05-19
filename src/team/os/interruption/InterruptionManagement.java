package team.os.interruption;

import java.util.HashMap;
import java.util.Map;

/**
 * 中断管理类
 * 维护中断向量表
 * 产生新中断
 * */
public class InterruptionManagement implements IInterruptionManagement {
    public final class InterruptId {
        public final static int BLOCK_INTERRUPTION = 1;
    }
    private final Map<Integer, Interruption> interruptionTable; //中断向量表
    private boolean isAllowInterrupt; //标识是否允许中断
    private static final InterruptionManagement instance = new InterruptionManagement();

    private InterruptionManagement() {
        interruptionTable = new HashMap<>();
        interruptionTable.put(1, new Interruption("interrrupt.txt", 1));
        isAllowInterrupt = true;
    }
    /**
     * 获取全局唯一InterruptionManagement实例
     * */
    public static InterruptionManagement getInstance() {
        return instance;
    }

    @Override
    public void generateInterruption(int interruptId, String processName, int cycle) {
        switch (interruptId) {
            case InterruptId.BLOCK_INTERRUPTION: {
                System.out.println("Process " + processName + " block for " + cycle + " cycle");
            }
            default: {
                //请放心吧。。不会走到这里的
            }
        }
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