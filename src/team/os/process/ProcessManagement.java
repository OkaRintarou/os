package team.os.process;

import team.os.global.Global;
import team.os.instruction.InstructionSet;

import java.util.*;

public final class ProcessManagement implements IProcessManagement {
    public static class Message {
        public String message;
        public int dst;
        public int src;

        public Message(String message, int src, int dst) {
            this.message = message;
            this.src = src;
            this.dst = dst;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "message='" + message + '\'' +
                    ", dst=" + dst +
                    ", src=" + src +
                    '}';
        }
    } //格式化消息，用于进程间通信

    private final class Lock {
        private final ArrayDeque<Process.PCB> lockBlockQueue;
        private int value;

        private Lock(int value) {
            lockBlockQueue = new ArrayDeque<>();
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void take() {
            if (value == 0) {
                lockBlockQueue.offer(runningProcess);
            } else {
                if (!lockBlockQueue.contains(runningProcess))
                    value--;
            }
        }

        public void put() {
            if (value == 0) {
                lockBlockQueue.poll();
            } else {
                value++;
            }
        }
    }

    private int processNumber; //当前系统中进程数,用于产生pid，因此是一个只增值，不允许减少
    private final Map<Integer, Process.PCB> processTable; //系统进程表
    private Process.PCB runningProcess; //当前处于running状态进程
    private final List<Process.PCB> readyQueue; //当前就绪队列
    private final List<Process.PCB> blockQueue; //当前阻塞队列
    private final List<Process.PCB> suspendQueue; //当前挂起队列
    private final List<Message> mailBox; //邮箱，用于进程间通信
    private final Lock lock;

    private static final ProcessManagement instance = new ProcessManagement();

    private ProcessManagement() {
        processNumber = 0;
        processTable = new HashMap<>();
        runningProcess = null;
        readyQueue = new LinkedList<>();
        blockQueue = new LinkedList<>();
        mailBox = new LinkedList<>();
        suspendQueue = new LinkedList<>();
        lock = new Lock(0);
    }

    /**
     * 调用getInstance获取全局唯一ProcessManagement实例
     */
    public static ProcessManagement getInstance() {
        return instance;
    }

    @Override
    public int createProcess(String processName, String filePath) {
        Process process = new Process(processNumber, processName, filePath, -1);
        InstructionSet instructionSet = Global.INSTANCE.getInsSetFactory().getInst(filePath, process.getPCB(), processName);
        if (!Global.INSTANCE.getMm().memoryRequest(instructionSet.getInsCount() * 8)) {
            process = null;
            throw new RuntimeException("\u0001[33m" + "\tcreate process failed!" + "\u0001[0m");
        } else {
            Global.INSTANCE.getMm().memoryAllocate(processNumber, instructionSet.getInsCount() * 8);
            process.getPCB().setMemorySize(instructionSet.getInsCount() * 8);
            process.setInstructionSet(instructionSet);
            processTable.put(processNumber, process.getPCB());
            processNumber++;
            readyQueue.add(process.getPCB());
            if (runningProcess == null) runningProcess = readyQueue.remove(0);
            Global.INSTANCE.getGui().addList_Process(
                    process.getPid(),
                    process.getPCB().getName(),
                    process.getPCB().getState(),
                    process.getInstructionSet().getPointer(),
                    process.getInstructionSet().getInsCount()
            );
            return process.getPid();
        }
    }

    @Override
    public void terminateProcess(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) { //判断进程表中是否包含此进程
            throw new RuntimeException("can not find PCB from processTable whose pid is " + pid);
        } else {
            if (readyQueue.size() == 0) { //如果就绪队列中没有进程，则将运行态进程置为null
                runningProcess = null;
            } else { //否则
                if (pcb.getState().equals(Process.ProcessStates.RUNNING)) {
                    runningProcess = readyQueue.size() == 0 ? null : readyQueue.remove(0);
                    if (runningProcess != null) runningProcess.setState(Process.ProcessStates.RUNNING);
                } else if (pcb.getState().equals(Process.ProcessStates.READY)) {
                    readyQueue.remove(pcb);
                } else if (pcb.getState().equals(Process.ProcessStates.BLOCK)) {
                    blockQueue.remove(pcb);
                } else if (pcb.getState().equals(Process.ProcessStates.SUSPEND)) {
                    suspendQueue.remove(pcb);
                }
            }
            //图形化用户接口增加信息
            Global.INSTANCE.getGui().subList_Process(pid);

            processTable.remove(pid);
            Global.INSTANCE.getMm().memoryFree(pid);
            if (suspendQueue.size() != 0 && Global.INSTANCE.getMm().memoryRequest(suspendQueue.get(0).getMemorySize())) {
                Process.PCB remove = suspendQueue.remove(0);
                Global.INSTANCE.getMm().memoryAllocate(remove.getPid(), remove.getMemorySize());
                remove.setState(Process.ProcessStates.BLOCK);
                blockQueue.add(remove);
            }
        }
    }

    @Override
    public void blockProcess(int blockTime) {
        // 如果运行态进程不存在，则直接返回
        if (runningProcess == null) {
            throw new RuntimeException("running process not exist, check it first");
        }

        blockQueue.add(runningProcess);
        runningProcess.setBlockTime(blockTime);
        runningProcess.setState(Process.ProcessStates.BLOCK);
        Global.INSTANCE.getGui().modList_Process(
                runningProcess.getPid(),
                runningProcess.getName(),
                runningProcess.getState(),
                runningProcess.getProcess().getInstructionSet().getPointer(),
                runningProcess.getProcess().getInstructionSet().getInsCount()
        );
    }

    @Override
    public void scheduleProcess() {
        if (runningProcess == null) { //初始状态，即就绪队列和运行态均为空的情况
            runningProcess = readyQueue.size() == 0 ? null : readyQueue.remove(0);
            if (runningProcess != null) {
                runningProcess.setState(Process.ProcessStates.RUNNING);
                Global.INSTANCE.getGui().modList_Process(
                        runningProcess.getPid(),
                        runningProcess.getName(),
                        runningProcess.getState(),
                        runningProcess.getProcess().getInstructionSet().getPointer(),
                        runningProcess.getProcess().getInstructionSet().getInsCount()
                );
            }
        } else { //途中状态，即就绪队列不为空的情况
            //更新running -> ready
            readyQueue.add(runningProcess);
            runningProcess.setState(Process.ProcessStates.READY);
            Global.INSTANCE.getGui().modList_Process(
                    runningProcess.getPid(),
                    runningProcess.getName(),
                    runningProcess.getState(),
                    runningProcess.getProcess().getInstructionSet().getPointer(),
                    runningProcess.getProcess().getInstructionSet().getInsCount()
            );
            //更新ready -> running
            runningProcess = readyQueue.remove(0);
            runningProcess.setState(Process.ProcessStates.RUNNING);
            Global.INSTANCE.getGui().modList_Process(
                    runningProcess.getPid(),
                    runningProcess.getName(),
                    runningProcess.getState(),
                    runningProcess.getProcess().getInstructionSet().getPointer(),
                    runningProcess.getProcess().getInstructionSet().getInsCount()
            );
        }
    }

    @Override
    public void wakeUpProcess(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) { //判断进程表中是否包含此进程
            throw new RuntimeException("can not find PCB from processTable whose pid is " + pid);
        } else {
            if (blockQueue.contains(pcb)) { //判断阻塞队列中是否存在该进程
                pcb.setState(Process.ProcessStates.READY);
                readyQueue.add(pcb);
                blockQueue.remove(pcb);
                Global.INSTANCE.getGui().modList_Process(
                        pid,
                        pcb.getName(),
                        pcb.getState(),
                        pcb.getProcess().getInstructionSet().getPointer(),
                        pcb.getProcess().getInstructionSet().getInsCount()
                );
            } else {
                throw new RuntimeException("process " + pid + "not exist in blockQueue, check it first");
            }
        }
    }

    @Override
    public Process.PCB getPCB(int pid) {
        return processTable.get(pid);
    }

    @Override
    public List<Map<Integer, Process.ProcessStates>> getAllProcessState() {
        List<Map<Integer, Process.ProcessStates>> res = new LinkedList<>();
        for (Map.Entry<Integer, Process.PCB> entry : processTable.entrySet()) {
            Map<Integer, Process.ProcessStates> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue().getState());
            res.add(map);
        }
        return res;
    }

    @Override
    public void swapOutProcess(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            throw new RuntimeException("can not find PCB from processTable whose pid is " + pid);
        } else {
            blockQueue.remove(pcb);
            pcb.setState(Process.ProcessStates.SUSPEND);
            suspendQueue.add(pcb);
            Global.INSTANCE.getGui().modList_Process(
                    pid,
                    pcb.getName(),
                    pcb.getState(),
                    pcb.getProcess().getInstructionSet().getPointer(),
                    pcb.getProcess().getInstructionSet().getInsCount()
            );
        }
    }

    @Override
    public void singleRoundExecution(int coreNumber) {
        //在给定时间范围内执行就绪队列中的就绪进程
        for (int i = 0; i < coreNumber; ++i) {
            // 如果当前运行态没有进程，那么说明就绪队列一定为空
            if (runningProcess == null || runningProcess.isExecuted()) break;
            boolean res = runningProcess.getProcess().run();
            runningProcess.setIsExecuted(true);
            if (res) { //如果当前运行态进程还有指令未执行，则执行就绪队列和运行态切换
                if (runningProcess != null && runningProcess.getState().equals(Process.ProcessStates.BLOCK)) {
                    runningProcess.setIsExecuted(false);
                    runningProcess = null;
                }
                scheduleProcess();
            } else { //否则终止当前进程
                terminateProcess(runningProcess.getPid());
                if (runningProcess != null && runningProcess.getState().equals(Process.ProcessStates.BLOCK)) {
                    runningProcess = null;
                    scheduleProcess();
                }
            }
        }
        //每个时钟周期检查是否有阻塞进程需要被唤醒，如果有则唤醒（唤醒进程放到下一轮时钟周期执行）
        int i = 0;
        while (i < blockQueue.size()) {
            blockQueue.get(i).decreaseBlockTime();
            if (blockQueue.get(i).getBlockTime() == 0) {
                wakeUpProcess(blockQueue.get(0).getPid());
                scheduleProcess();
            } else {
                ++i;
            }
        }

        //重置进程执行标识位
        if (runningProcess != null && runningProcess.isExecuted()) {
            runningProcess.setIsExecuted(false);
        }
        for (Process.PCB pcb : readyQueue) {
            pcb.setIsExecuted(false);
        }
    }

    @Override
    public int getProductNumber() {
        return lock.getValue();
    }

    @Override
    public void addProduct() {
        lock.put();
    }

    @Override
    public void subProduct() {
        lock.take();
    }

    public int getProcessNumber() {
        return processNumber;
    }

    public Map<Integer, Process.PCB> getProcessTable() {
        return processTable;
    }

    public Process.PCB getRunningProcess() {
        return runningProcess;
    }

    public List<Process.PCB> getReadyQueue() {
        return readyQueue;
    }

    public List<Process.PCB> getBlockQueue() {
        return blockQueue;
    }

    public List<Process.PCB> getSuspendQueue() {
        return suspendQueue;
    }

    public List<Message> getMailBox() {
        return mailBox;
    }
}
