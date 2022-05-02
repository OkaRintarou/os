package team.os.memoryManager;

import java.util.*;

public class MemoryManager implements IMemoryManager {

    //todo 变量内存控制
    private static final int MEMORY_SIZE = 1 << 20;     // 内存大小，1MB
    private static final int HEAP_SIZE_MAX = 8 << 10;  // 堆最大尺寸，8KB
    private static final int VAR_ID_MAX = 1000;

    private int seqForNewPro;                       // 申请内存时找到的合适空闲分区编号
    private final List<Partition> proPartitions;    // 已被分配的内存分区表
    private final List<Partition> idlePartitions;   // 空闲内存分区表

    private int curHeapSize;                        // 当前堆大小
    private int varIdSeq;                           // 下一个被分配的varId，[1,1000]
    private final HashMap<Integer, ArrayList<Integer>> proVarMap;    // key:pid  value:varId列表
    private final HashMap<Integer, VarPartition> heap;              // 堆：变量标识与变量信息的一一映射
    private final Set<Integer> varIdSet;            // 活跃的变量表示集合

    public MemoryManager() {
        this.seqForNewPro = -1;
        this.proPartitions = new ArrayList<>();
        this.idlePartitions = new ArrayList<>();
        this.idlePartitions.add(new Partition(-1, 0, MEMORY_SIZE));// 程序内存总大小初始化为1MB

        curHeapSize = 0;
        varIdSeq = 0;
        this.proVarMap = new HashMap<>();
        this.heap = new HashMap<>();
        this.varIdSet = new HashSet<>();
    }

    @Override
    public boolean memoryRequest(int pSize) {
        int seq = 0;
        for (Partition part : idlePartitions) {
            if (part.getSize() > pSize) {
                seqForNewPro = seq;
                return true;
            }
            seq++;
        }
        // todo 进程替换
        return false;
    }

    @Override
    public void memoryAllocate(int pid, int proSize) {
        try {
            Partition parAllocated = idlePartitions.get(seqForNewPro);
            int parFir = parAllocated.getFirAddress();
            int parSize = parAllocated.getSize();
            proPartitions.add(new Partition(pid, parFir, proSize));
            idlePartitions.get(seqForNewPro).setFirAddress(parFir + proSize);
            idlePartitions.get(seqForNewPro).setSize(parSize - proSize);
            seqForNewPro = -1;
            System.out.println("Succeed in allocating memory for process " + pid + ".");
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("\u001B[33m" + "\tCannot allocate memory for a process which doesn't request!" + "\u001B[0m");
        }
    }

    @Override
    public void memoryFree(int pid) {
        if (proPartitions.isEmpty()) {
            System.out.println("There is nothing in the memory");
            return;
        }

        int seq = 0;
        for (Partition par : proPartitions) {
            if (pid == par.getPid()) break;
            seq++;
        }
        Partition parFreed = proPartitions.get(seq);
        proPartitions.remove(seq);          // 释放pid的进程内存
        if (proVarMap.containsKey(pid))     // 释放进程的所有变量
            varFree(pid);
        if (!mergeFragment(parFreed)) {     // 合并分区碎片
            parFreed.ProToIdle();
            idlePartitions.add(parFreed);
        }
        System.out.println("Succeed in freeing memory of process " + pid + ".");
    }

    @Override
    public int varDeclare(int pid, String varType, int size) {
        if (curHeapSize + size > HEAP_SIZE_MAX) {
            System.out.println("\u0001[31m" + "The rest heap space is not enough!!!" + "\u0001[0m");
            return -1;
        }

        // 获取varID
        varIdSeq++;
        if (varIdSeq > VAR_ID_MAX) {
            varIdSeq = 1;
            int i = 0;
            int setSize = varIdSet.size();
            for (; i < setSize; i++, varIdSeq++) {
                if (!varIdSet.contains(varIdSeq))
                    break;
            }
            // 当前活跃变量个数超过上限
            if (i == setSize) {
                System.out.println("\u0001[31m" + "The number of running variables is over!!!" + "\u0001[0m");
                return -1;
            }
        }

        varIdSet.add(varIdSeq);
        // 增加 进程id 与 变量id 的映射关系
        if (proVarMap.containsKey(pid)) proVarMap.get(pid).add(varIdSeq);
        else {
            ArrayList<Integer> vars = new ArrayList<>(varIdSeq);
            proVarMap.put(pid, vars);
        }
        // 保存变量基本信息
        VarPartition varPar = new VarPartition(0, size, varType);
        heap.put(varIdSeq, varPar);
        curHeapSize += size;
        System.out.println("Increase a variable for process " + pid + ". VarId is " + varIdSeq + ".");
        return varIdSeq;
    }

    @Override
    public int varReadInt(int varID) {
        try {
            String val = heap.get(varID).getValue();
            return Integer.parseInt(val);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\u0001[33m" + "\tCannot read a variable which doesn't exist!" + "\u0001[0m");
        }
        return 0;
    }

    @Override
    public String varReadString(int varID) {
        try {
            return heap.get(varID).getValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\u0001[33m" + "\tCannot read a variable which doesn't exist!" + "\u0001[0m");
        }
        return null;
    }

    @Override
    public void varWriteInt(int varID, int varValue) {
        try {
            heap.get(varID).setValue(Integer.toString(varValue));
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\u0001[33m" + "\tCannot write a variable which doesn't exist!" + "\u0001[0m");
        }
    }

    @Override
    public void varWriteString(int varID, String varValue) {
        try {
            heap.get(varID).setValue(varValue);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\u0001[33m" + "\tCannot write a variable which doesn't exist!" + "\u0001[0m");
        }
    }

    @Override
    public List<Partition> getMemoryAllocation() {
        List<Partition> memory = new ArrayList<>();
        memory.addAll(idlePartitions);
        memory.addAll(proPartitions);
        memory.sort(Partition::compareTo);  // 按照分区首地址从小到大排序
        return memory;
    }

    /**
     * 释放进程回收分区后，合并可合并的分区
     * 避免出现外部碎片
     *
     * @param parFreed 即将释放的进程内存分区
     * @return 返回是否合并了分区
     */
    private boolean mergeFragment(Partition parFreed) {
        /*
          若新分区首尾都有分区可合并
          isMerge用来标记之前是否已经合并过其他分区
          lastSeq用于保存先合并的空闲分区编号
         */
        boolean isMerge = false;
        int lastSeq = 0;
        int idleSize = idlePartitions.size();

        for (int curSeq = 0; curSeq < idleSize; curSeq++) {
            // 依次为 当前分区首地址、当前分区大小、新分区首地址、新分区大小
            int curFir = idlePartitions.get(curSeq).getFirAddress();
            int curSize = idlePartitions.get(curSeq).getSize();
            int firFreed = parFreed.getFirAddress();
            int sizeFreed = parFreed.getSize();

            // 新分区首部有空闲分区可合并
            if (curFir + curSize == firFreed) {
                if (isMerge) {
                    int lastSize = idlePartitions.get(lastSeq).getSize();
                    idlePartitions.get(lastSeq).setFirAddress(curFir);
                    idlePartitions.get(lastSeq).setSize(lastSize + curSize);
                    idlePartitions.remove(curSeq);
                    break;
                } else {
                    idlePartitions.get(curSeq).setSize(curSize + sizeFreed);
                    isMerge = true;
                }
            }

            // 新分区尾部有空闲分区可合并
            else if (firFreed + sizeFreed == curFir) {
                if (isMerge) {
                    int lastSize = idlePartitions.get(lastSeq).getSize();
                    idlePartitions.get(lastSeq).setSize(lastSize + curSize);
                    idlePartitions.remove(curSeq);
                    break;
                } else {
                    idlePartitions.get(curSeq).setFirAddress(firFreed);
                    idlePartitions.get(curSeq).setSize(curSize + sizeFreed);
                    isMerge = true;
                }

            } else
                lastSeq++;
        }
        return isMerge;
    }

    /**
     * 释放进程的所有变量内存
     *
     * @param pid 进程id
     */
    private void varFree(int pid) {
        for (Integer varId : proVarMap.get(pid)) {
            curHeapSize -= heap.get(varId).getSize();
            heap.remove(varId);
            varIdSet.remove(varId);
        }
        proVarMap.remove(pid);
    }
}
