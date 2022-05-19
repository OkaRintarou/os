package team.os.memoryManager;

import java.util.*;

import team.os.global.Global;
import team.os.process.Process;
import team.os.process.ProcessManagement;

public class MemoryManager implements IMemoryManager {

    private static final int MEMORY_SIZE = 1 << 20;     // 内存大小，1MB
    private static final int HEAP_SIZE_MAX = 8 << 10;  // 堆最大尺寸，8KB
    private static final int VAR_ID_MAX = 1000;

    private int seqForNewPro;                       // 申请内存时找到的合适空闲分区编号
    private final List<Partition> proPartitions;    // 已被分配的内存分区表
    private final List<Partition> idlePartitions;   // 空闲内存分区表

    private int curHeapSize;                        // 当前堆大小
    private int varIdSeq;                           // 下一个被分配的varId，[1,1000]

    /**
     * key:pid  value:varName与varId的映射
     */
    private final HashMap<Integer, HashMap<String, Integer>> proVarMap;
    private final HashMap<Integer, VarPartition> heap;      // 堆：变量标识与变量信息的一一映射
    private final Set<Integer> varIdSet;                    // 活跃的变量表示集合

    // 替换进程时方便定位被替换进程的空闲分区编号
    private int SeqFreed;

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

        SeqFreed = -1;
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
        // 寻找可替换进程
        if (!haveReplaceablePro(pSize))
            return false;
        seqForNewPro = SeqFreed;
        return true;
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
            System.out.println("\u001B[31m" + "\tCannot allocate memory for a process which doesn't request!" + "\u001B[0m");
        }
    }

    @Override
    public void memoryFree(int pid) {
        int seq = 0;
        for (Partition par : proPartitions) {
            if (pid == par.getPid()) break;
            seq++;
        }

        // 检测数据的合法性
        if (proPartitions.isEmpty() || seq >= proPartitions.size()) {
            System.out.println("\33[31m" + "Error : Process " + pid + " is not in the memory!" + "\33[0m");
            return;
        }

        Partition parFreed = proPartitions.get(seq);
        proPartitions.remove(seq);          // 释放pid的进程内存
        if (proVarMap.containsKey(pid))     // 释放进程的所有变量
            varFree(pid);
        if (!mergeFragment(parFreed)) {     // 合并分区碎片
            parFreed.ProToIdle();
            idlePartitions.add(parFreed);
            SeqFreed = idlePartitions.size() - 1;
        }
        System.out.println("Succeed in freeing memory of process " + pid + ".");
    }

    @Override
    public int varDeclare(int pid, String varName, String varType, int... size) {
        int varSize = isVarLegal(varType, size);
        if (varSize == -1)
            return -1;

        // 获取varID
        if (!generateNextVarId()) return -1;
        varIdSet.add(varIdSeq);

        // 增加 进程id 与 变量名 的映射关系
        if (!proVarMap.containsKey(pid))
            proVarMap.put(pid, new HashMap<>());
        proVarMap.get(pid).put(varName, varIdSeq);

        // 保存变量基本信息
        VarPartition varPar;
        varPar = new VarPartition(varSize, varType);
        heap.put(varIdSeq, varPar);
        curHeapSize += varSize;

        // 更新gui中变量信息
        Global.INSTANCE.getGui().addList_Memory(varName, varType, varSize, varPar.getValue(), pid, varIdSeq);
        System.out.println("Increase a variable for process " + pid + ". VarId is " + varIdSeq + ".");
        return varIdSeq;
    }

    @Override
    public int varReadInt(int pid, String varName) {
        try {
            int varID = proVarMap.get(pid).get(varName);
            if (heap.get(varID).getType().equals("Int")) {
                String val = heap.get(varID).getValue();
                return Integer.parseInt(val);
            } else
                System.out.println("\33[31m" + "Error: Variable " + varID + " is not int!" + "\33[0m");
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\t" + "\33[31;4m" + "Cannot read a variable which doesn't exist!" + "\33[0m");
        }
        return 0;
    }

    @Override
    public String varReadString(int pid, String varName) {
        try {
            int varID = proVarMap.get(pid).get(varName);
            if (heap.get(varID).getType().equals("String"))
                return heap.get(varID).getValue();
            else
                System.out.println("\33[31m" + "Error: Variable " + varID + " is not String!" + "\33[0m");
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\t" + "\33[31;4m" + "Cannot read a variable which doesn't exist!" + "\33[0m");
        }
        return null;
    }

    @Override
    public void varWriteInt(int pid, String varName, int varValue) {
        try {
            int varID = proVarMap.get(pid).get(varName);
            if (heap.get(varID).getType().equals("Int")) {
                heap.get(varID).setValue(Integer.toString(varValue));
                System.out.println("Write successfully for varId:" + varID + " ,value is " + varValue);
            } else
                System.out.println("\33[31m" + "Error: Variable " + varID + " is not int!" + "\33[0m");
            // todo
            Global.INSTANCE.getGui().modList_Memory(varName, "Int", 4, Integer.toString(varValue), pid, varID);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\33[31;4m" + "\tCannot write a variable which doesn't exist!" + "\33[0m");
        }
    }

    @Override
    public void varWriteString(int pid, String varName, String varValue) {
        try {
            int varID = proVarMap.get(pid).get(varName);
            VarPartition var = heap.get(varID);
            if (var.getType().equals("String")) {
                String realValue = varValue;
                // 字符串太长
                if (varValue.length() > var.getSize()) {
                    System.out.println("\33[33m" + "Warning: Variable " + varID + " is too long" + "\33[0m");
                    realValue = varValue.substring(0, var.getSize());
                }
                heap.get(varID).setValue(realValue);
                System.out.println("Write successfully for varId:" + varID + " ,value is " + realValue);
            } else
                System.out.println("\33[31m" + "Error: Variable " + varID + " is not String!" + "\33[0m");
            // todo
            Global.INSTANCE.getGui().modList_Memory(varName, "String", 4, varValue, pid, varID);
        } catch (NullPointerException e) {
            e.printStackTrace();
            System.out.println("\33[31;4m" + "\tCannot write a variable which doesn't exist!" + "\33[0m");
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
     * 检查声明变量传入的各参数是否合法以及对是否够分配
     *
     * @param varType 变量类型
     * @param size    变量大小
     * @return 合法返回变量大小，否则返回-1
     */
    private int isVarLegal(String varType, int... size) {
        // 检查数据的合法性
        int varSize = 4;
        if (varType.equals("String")) {
            if (size == null) {
                System.out.println("\33[31m" + "Error: A string var must have a size" + "\33[0m");
                return -1;
            } else
                varSize = size[0];
        } else if (!varType.equals("Int")) {
            System.out.println("\33[31m" + "Error in the type of variable" + "\33[0m");
            return -1;
        }
        // 检查堆内存是否足够分配
        if (curHeapSize + varSize > HEAP_SIZE_MAX) {
            System.out.println("\33[31m" + "Error: The rest heap space is not enough!!!" + "\33[0m");
            return -1;
        }
        return varSize;
    }

    /**
     * 内存不能满足新进程大小要求时，寻找能被替换的阻塞态进程
     *
     * @param pSize 新进程需要的内存大小
     * @return 是否存在可替换进程
     */
    private boolean haveReplaceablePro(int pSize) {
        // 获取全局进程管理实例
        ProcessManagement pm = Global.INSTANCE.getPm();
        List<Process.PCB> blockQueue = pm.getBlockQueue();
        int pidReplaced = -1;
        // 寻找内存大小满足新进程要求的阻塞态进程
        for (Process.PCB pcb : blockQueue) {
            if (pcb.getMemorySize() >= pSize) {
                pidReplaced = pcb.getPid();
                break;
            }
        }
        // 没找到
        if (pidReplaced == -1) return false;
        // 找到，释放被替换进程内存，并转移到就绪态
        memoryFree(pidReplaced);
        pm.swapOutProcess(pidReplaced);
        return true;
    }

    /**
     * 生成下一个可用的变量标识
     *
     * @return 成功生成返回true，否则返回false
     */
    private boolean generateNextVarId() {
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
                System.out.println("\33[31m" + "Error: The number of running variables is over!!!" + "\33[0m");
                return false;
            }
        }
        return true;
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
                    SeqFreed = curSeq;
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
                    SeqFreed = curSeq;
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
        for (Integer varId : proVarMap.get(pid).values()) {
            curHeapSize -= heap.get(varId).getSize();
            heap.remove(varId);
            varIdSet.remove(varId);
            Global.INSTANCE.getGui().subList_Memory(varId);
        }
        proVarMap.remove(pid);
    }
}
