package team.os.process;

import team.os.global.Global;
import team.os.instruction.InstructionSet;
import team.os.io.IOStuff;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class Process implements IProcess {
    public static class PCB {
        private final int pid; //进程标识符
        private final String name; //进程名称
        private ProcessStates state; //进程状态
        private final Process process; //当前进程
        private String filePath; //进程对应执行程序（文件）路径
        private int memorySize; //进程占用内存大小
        private final List<IOStuff> resourceList; //系统资源清单
        private final List<ProcessManagement.Message> messageList; //进程的消息列表
        private int blockTime; //进程剩余阻塞时间，如果没有被阻塞则为0
        private boolean isExecuted; //进程执行标识位，用于保证一个时钟周期一个进程只执行一次

        public PCB(int pid, String name, ProcessStates state, String filePath, int memorySize, Process process) {
            this.pid = pid;
            this.name = name;
            this.state = state;
            this.filePath = filePath;
            this.memorySize = memorySize;
            this.resourceList = new LinkedList<>();
            this.process = process;
            this.messageList = new LinkedList<>();
            this.blockTime = 0;
            this.isExecuted = false;
        }

        public Process getProcess() {
            return process;
        }

        public int getPid() {
            return pid;
        }

        public String getName() {
            return name;
        }

        public ProcessStates getState() {
            return state;
        }

        public void setState(ProcessStates state) {
            this.state = state;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getMemorySize() {
            return memorySize;
        }

        public void setMemorySize(int memorySize) {
            this.memorySize = memorySize;
        }

        public List<IOStuff> getResourceList() {
            return resourceList;
        }

        public List<ProcessManagement.Message> getMessageList() {
            return messageList;
        }

        public int getBlockTime() {
            return blockTime;
        }

        public void setBlockTime(int blockTime) {
            this.blockTime = blockTime;
        }

        public void decreaseBlockTime() {
            this.blockTime--;
        }

        public boolean isExecuted() {
            return isExecuted;
        }

        public void setIsExecuted(boolean isExecuted) {
            this.isExecuted = isExecuted;
        }
    }

    public enum ProcessStates {
        READY, RUNNING, BLOCK, INTERRUPT, SUSPEND
    }

    private final PCB pcb;
    private InstructionSet instructionSet;

    public Process(int _pid, String _name, String _filePath, int _memorySize) {
        pcb = new PCB(_pid, _name, ProcessStates.READY, _filePath, _memorySize, this);
    }

    @Override
    public int getPid() {
        return pcb.pid;
    }

    @Override
    public InstructionSet getInstructionSet() {
        return instructionSet;
    }

    @Override
    public PCB getPCB() {
        return pcb;
    }

    @Override
    public boolean run() {
        InstructionSet instructionSet = getInstructionSet();
        return instructionSet.next();
    }

    @Override
    public void setInstructionSet(InstructionSet instructionSet) {
        this.instructionSet = instructionSet;
    }

    @Override
    public ProcessManagement.Message[] showAllMessage() {
        getMessage();
        List<ProcessManagement.Message> res = new ArrayList<>();
        while (!pcb.messageList.isEmpty()) {
            res.add(pcb.messageList.remove(0));
        }
        return res.toArray(new ProcessManagement.Message[0]);
    }

    @Override
    public void sendMessage(String message, int destPid) {
        ProcessManagement.Message newMessage = new ProcessManagement.Message(message, pcb.getPid(), destPid);
        Global.INSTANCE.getPm().getMailBox().add(newMessage);
    }

    @Override
    public void getMessage() {
        List<ProcessManagement.Message> temp = new ArrayList<>();
        for (ProcessManagement.Message message : Global.INSTANCE.getPm().getMailBox()) {
            if (message.dst == pcb.getPid()) {
                temp.add(message);
                pcb.messageList.add(message);
            }
        }

        for (ProcessManagement.Message message : temp) {
            Global.INSTANCE.getPm().getMailBox().remove(message);
        }
    }
}
