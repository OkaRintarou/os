package team.os.memoryManager;

/**
 * 内存分区信息：属性包括占用分区的进程id、分区首地址、分区大小
 * <p>
 * 当分区空闲时，进程id为-1
 */
public class Partition implements Comparable<Partition> {
    private int pid;
    private int firAddress;
    private int size;

    public Partition(int pid, int firAddress, int size) {
        this.pid = pid;
        this.firAddress = firAddress;
        this.size = size;
    }

    public int getPid() {
        return pid;
    }

    /**
     * 在进程释放后，将分区pid设置为-1，表明分区变成空闲分区
     */
    public void ProToIdle() {
        this.pid = -1;
    }

    public int getFirAddress() {
        return firAddress;
    }

    /**
     * 更该分区首地址
     *
     * @param firAddress 新的分区首地址
     */
    public void setFirAddress(int firAddress) {
        this.firAddress = firAddress;
    }

    public int getSize() {
        return this.size;
    }

    /**
     * 更该分区大小
     *
     * @param size 新的分区大小
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * 分区按照首地址从低到高排序
     */
    @Override
    public int compareTo(Partition par) {
        return this.firAddress > par.getFirAddress() ? 1 : -1;
    }
}
