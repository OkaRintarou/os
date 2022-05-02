package team.os.memoryManager;

/**
 * 保存变量信息
 * 与varID一一映射
 */
public class VarPartition {
    private final int firAddress;
    private final int size;
    private String value = null;

    public VarPartition(int firAddress, int size, String varType) {
        this.firAddress = firAddress;
        this.size = size;
    }

    public int getFirAddress() {
        return firAddress;
    }

    public int getSize() {
        return size;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
