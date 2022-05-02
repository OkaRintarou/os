package team.os.process;

import team.os.instruction.InstructionSet;
import team.os.io.IOStuff;

import java.util.LinkedList;
import java.util.List;

public final class Process implements IProcess {
    public static class PCB implements Comparable<PCB> {
        private final int pid; //进程标识符
        private final String name; //进程名称
        private ProcessStates state; //进程状态
        private final Process process; //当前进程
        private int priority; //进程优先级
        private String filePath; //进程对应执行程序（文件）路径
        private int memorySize; //进程占用内存大小
        private final List<IOStuff> resourceList; //系统资源清单
        private final List<String> mutex; //进程添加的互斥锁列表
        private final List<ProcessManagement.Message> messageList; //进程的消息列表

        public PCB(int pid, String name, ProcessStates state, int priority, String filePath, int memorySize, Process process) {
            this.pid = pid;
            this.name = name;
            this.state = state;
            this.priority = priority;
            this.filePath = filePath;
            this.memorySize = memorySize;
            this.mutex = new LinkedList<>();
            this.resourceList = new LinkedList<>();
            this.process = process;
            this.messageList = new LinkedList<>();
        }

        @Override
        public int compareTo(PCB o) {
            return Integer.compare(this.priority, o.priority);
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

        public void setPriority(int priority) {
            this.priority = priority;
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

        public List<String> getMutex() {
            return mutex;
        }

        public List<ProcessManagement.Message> getMessageList() {
            return messageList;
        }
    }

    public enum ProcessStates {
        READY, RUNNING, BLOCK, INTERRUPT
    }

    private final PCB pcb;
    private final InstructionSet instructionSet;
    //TODO：不太确定是否需要数据段？如果需要数据段需要存放什么内容？

    public Process(int _pid, String _name, String _filePath, int _memorySize, InstructionSet _instructionSet) {
        pcb = new PCB(_pid, _name, ProcessStates.READY, 16, _filePath, _memorySize, this);
        instructionSet = _instructionSet;
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
    public void run() {
        while (instructionSet.next());
    }
}
