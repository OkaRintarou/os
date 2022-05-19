package team.os.memoryManager;

import java.util.*;

public class TestMemory {
    public static void main(String[] args) {
        MemoryManager memManage = new MemoryManager();
        int pid;
        int pSize;
        String varName;
        String varType;
        int varSize;
        String varValue;
        List<Partition> memList;
        int code = 0;
        Scanner scanner = new Scanner(System.in);
        while (code != -1) {
            System.out.println("Input code: -1<exit> 1<MemoryAllocate> 2<MemoryFree> 3<VarDeclare> 4<ReadStr> " +
                    "5<WriteStr> 6<ReadInt> 7<WriteInt> 8<GetMemoryAllocation>");
            code = scanner.nextInt();
            switch (code) {
                // 退出
                case -1:
                    System.out.print("End testMemory...");
                    break;
                // 为进程申请内存
                case 1:
                    System.out.print("Input pid & pSize: ");
                    pid = scanner.nextInt();
                    pSize = scanner.nextInt();
                    if (memManage.memoryRequest(pid))
                        memManage.memoryAllocate(pid, pSize);
                    break;
                // 释放进程内存
                case 2:
                    System.out.print("Input pid: ");
                    pid = scanner.nextInt();
                    memManage.memoryFree(pid);
                    break;
                // 声明变量
                case 3:
                    System.out.print("Input pid & varName & varType <& varSize>: ");
                    pid = scanner.nextInt();
                    varName = scanner.next();
                    varType = scanner.next();
                    if (varType.equals("Int"))
                        memManage.varDeclare(pid, varName, varType);
                    else {
                        varSize = scanner.nextInt();
                        memManage.varDeclare(pid, varName, varType, varSize);
                    }
                    break;
                // 读String变量
                case 4:
                    System.out.print("Input pid & varName: ");
                    pid = scanner.nextInt();
                    varName = scanner.next();
                    memManage.varReadString(pid, varName);
                    break;
                // 写String变量
                case 5:
                    System.out.print("Input pid & varName & varValue: ");
                    pid = scanner.nextInt();
                    varName = scanner.next();
                    varValue = scanner.next();
                    memManage.varWriteString(pid, varName, varValue);
                    break;
                // 读Int变量
                case 6:
                    System.out.println("Input pid & varName: ");
                    pid = scanner.nextInt();
                    varName = scanner.next();
                    memManage.varReadInt(pid, varName);
                    break;
                // 写Int变量
                case 7:
                    System.out.print("Input pid & varName & varValue: ");
                    pid = scanner.nextInt();
                    varName = scanner.next();
                    varValue = scanner.next();
                    memManage.varWriteInt(pid, varName, Integer.parseInt(varValue));
                    break;
                // 获取整个内存
                case 8:
                    memList = memManage.getMemoryAllocation();
                    System.out.format("%-15s%-15s%-15s\n", "pid", "firAddress", "size");
                    for (Partition par : memList)
                        System.out.format("%-15d%-15d%-15d\n", par.getPid(), par.getFirAddress(), par.getSize());
                    break;
                default:
                    System.out.println("Meaningless code...");
                    break;
            }
            System.out.println();
        }
    }
}
