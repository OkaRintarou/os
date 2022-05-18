package team.os.interruption;

/**
 * 中断类
 * 维护每个中断向量对应的执行程序，作为中断向量表中的value存在
 * */
public class Interruption {
    /**
     * 中断向量对应执行程序（文件）路径
     * */
    public String filePath;
    public int interruptId;

    public Interruption(String filePath, int interruptId) {
        this.filePath = filePath;
        this.interruptId = interruptId;
    }
}

