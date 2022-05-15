package team.os.memoryManager;

/**
 * 保存变量信息
 * 与varID一一映射
 */
public class VarPartition {
    private final String type;
    private final int size;
    private String value;

    public VarPartition(int size, String varType) {
        this.size = size;
        type = varType;
        if (type.equals("Int"))
            value = "0";
        else
            value = "";
    }

    public String getType() {
        return type;
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
