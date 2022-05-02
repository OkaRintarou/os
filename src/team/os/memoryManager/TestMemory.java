package team.os.memoryManager;

import java.util.*;

public class TestMemory {
    public static void main(String[] args) {
        MemoryManager memManage = new MemoryManager();
        int pid;
        int pSize;
        String varType;
        int varSize;
        int varId;
        String varValue;
        List<Partition> memList = new ArrayList<>();

        int code = 0;
        Scanner scanner = new Scanner(System.in);
        while (code != -1) {
            System.out.println("Input code: -1<exit> 1<MemoryAllocate> 2<MemoryFree> 3<VarDeclare> 4<varReadStr> 5<varWriteStr> 6<GetMemoryAllocation>");
            code = scanner.nextInt();
            switch (code) {
                // 退出
                case -1 : System.out.println("End testMemory...");break;
                // 为进程申请内存
                case 1 :
                    System.out.print("Input pid & pSize: ");
                    pid = scanner.nextInt();
                    pSize = scanner.nextInt();
                    if (memManage.memoryRequest(pid))
                        memManage.memoryAllocate(pid, pSize);
                    break;

                // 释放进程内存
                case 2 :
                    System.out.print("Input pid: ");
                    pid = scanner.nextInt();
                    memManage.memoryFree(pid);
                    break;
                // 声明变量
                case 3 :
                    System.out.print("Input pid & varType & varSize: ");
                    pid = scanner.nextInt();
                    varType = scanner.next();
                    varSize = scanner.nextInt();
                    memManage.varDeclare(pid, varType, varSize);
                    break;
                // 读String变量
                case 4 :
                    System.out.print("Input varID: ");
                    varId = scanner.nextInt();
                    System.out.println("The value is \"" + memManage.varReadString(varId) + "\"");
                    break;
                // 写String变量
                case 5 :
                    System.out.print("Input varId & varValue: ");
                    varId = scanner.nextInt();
                    varValue = scanner.next();
                    memManage.varWriteString(varId, varValue);
                    break;
                // 获取整个内存
                case 6 :
                    memList = memManage.getMemoryAllocation();
                    System.out.format("%-15s%-15s%-15s\n", "pid", "firAddress", "size");
                    for (Partition par : memList)
                        System.out.format("%-15d%-15d%-15d\n", par.getPid(), par.getFirAddress(), par.getSize());
                    break;
                default :
                    System.out.println("Meaningless code...");
                    break;
            }
            System.out.println();
        }
    }
}
