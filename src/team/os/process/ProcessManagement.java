package team.os.process;

import team.os.instruction.InstructionSet;

import java.util.*;


public final class ProcessManagement implements IProcessManagement {
    //TODO：不清楚是否需要进行进程间死锁处理？
    //TODO：不清楚是否需要维护cpu寄存器，如果不维护则中断管理模块的部分功能将无法体现，例如进程间切换需要由中断来完成？
    public static class Message {
        String message;int dst;
        public Message(String message,int dst){
            this.message=message;
            this.dst=dst;
        }
    } //格式化消息，用于进程间通信
    private int processNumber; //当前系统中进程数
    private final Map<Integer, Process.PCB> processTable; //系统进程表
    private Process.PCB runningProcess; //当前处于running状态进程
    private final List<Process.PCB> readyQueue; //当前就绪队列
    private final List<Process.PCB> blockQueue; //当前等待队列
    private final List<Message> mailBox; //邮箱，用于进程间通信

    private static final ProcessManagement instance = new ProcessManagement();

    private ProcessManagement() {
        processNumber = 0;
        processTable = new HashMap<>();
        runningProcess = null;
        readyQueue = new LinkedList<>();
        blockQueue = new LinkedList<>();
        mailBox = new LinkedList<>();
    }
    /**
     * 调用getInstance获取全局唯一ProcessManagement实例
     * */
    public static ProcessManagement getInstance() {
        return instance;
    }

    @Override
    public void createProcess(int pid, String processName, String filePath, int memorySize, InstructionSet instructionSet) {
        //TODO：打开文件读取内容 && 解析成指令序列
        //TODO：为进程分配内存
        Process process = new Process(pid, processName, filePath, memorySize, instructionSet);
        processNumber++;
        processTable.put(pid, process.getPCB());
        readyQueue.add(process.getPCB());
    }

    @Override
    public void terminateProcess(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            System.out.println("\u0001[33m" + "\tcan not find PCB from processTable whose pid is" + pid + "\u0001[0m");
        } else {
            Process.ProcessStates processStates = pcb.getState();
            if (processStates.equals(Process.ProcessStates.READY)) {
                readyQueue.remove(pcb);
            } else if (processStates.equals(Process.ProcessStates.BLOCK)) {
                blockQueue.remove(pcb);
            } else if (processStates.equals(Process.ProcessStates.RUNNING)) {
                sortByPriority(readyQueue);
                runningProcess = readyQueue.get(0);
                readyQueue.remove(0);
                runningProcess.getProcess().run();
            } else {
                runningProcess.getProcess().run();
            }
            //TODO：回收分配给该进程的内存
        }
    }

    @Override
    public void blockProcess() {
        sortByPriority(readyQueue);
        blockQueue.add(runningProcess);
        runningProcess.setState(Process.ProcessStates.BLOCK);
        runningProcess = readyQueue.get(0);
        runningProcess.setState(Process.ProcessStates.RUNNING);
        readyQueue.remove(0);
        runningProcess.getProcess().run();
    }

    @Override
    public void scheduleProcess() {
        readyQueue.add(runningProcess);
        runningProcess.setState(Process.ProcessStates.READY);
        sortByPriority(readyQueue);
        runningProcess = readyQueue.get(0);
        runningProcess.setState(Process.ProcessStates.RUNNING);
        readyQueue.remove(0);
        runningProcess.getProcess().run();
    }

    @Override
    public void wakeUpProcess(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            System.out.println("\u0001[33m" + "\tcan not find PCB from processTable whose pid is" + pid + "\u0001[0m");
        } else {
            pcb.setState(Process.ProcessStates.READY);
            readyQueue.add(pcb);
            blockQueue.remove(pcb);
        }
    }

    @Override
    public void sortByPriority(List<Process.PCB> readyQueue) {
        Collections.sort(readyQueue);
    }

    @Override
    public Process.PCB getPCB(int pid) {
        return processTable.get(pid);
    }

    @Override
    public void addLock(String lockName, int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            System.out.println("\u0001[33m" + "\tcan not find PCB from processTable whose pid is" + pid + "\u0001[0m");
        } else {
            if (pcb.getMutex().contains(lockName)) {
                System.out.println("\u0001[33m" + "\tlock already exist on process: " + pid + "\u0001[0m");
            } else {
                pcb.getMutex().add(lockName);
            }
        }
    }

    @Override
    public void removeLock(String lockName, int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            System.out.println("\u0001[33m" + "\tcan not find PCB from processTable whose pid is" + pid + "\u0001[0m");
        } else {
            pcb.getMutex().remove(lockName);
        }
    }

    @Override
    public List<Map<Integer, Process.ProcessStates>> getAllProcessState() {
        List<Map<Integer, Process.ProcessStates>> res = new LinkedList<>();
        for (Map.Entry<Integer, Process.PCB> entry: processTable.entrySet()) {
            Map<Integer, Process.ProcessStates> map = new HashMap<>();
            map.put(entry.getKey(), entry.getValue().getState());
            res.add(map);
        }
        return res;
    }

    @Override
    public void sendMessageToMail(String message, int dstPid) {
        Message newMessage = new Message(message, dstPid);
        mailBox.add(newMessage);
    }

    @Override
    public void getMessageFromMail(int pid) {
        Process.PCB pcb;
        if ((pcb = getPCB(pid)) == null) {
            System.out.println("\u0001[33m" + "\tcan not find PCB from processTable whose pid is" + pid + "\u0001[0m");
        } else {
            List<ProcessManagement.Message> messageList = pcb.getMessageList();
            for (Message m: mailBox) {
                if (m.dst == pid) {
                    messageList.add(m);
                }
            }
        }
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
}
