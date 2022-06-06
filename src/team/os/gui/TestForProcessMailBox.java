package team.os.gui;

import team.os.process.Process;
import team.os.process.ProcessManagement;

import java.util.Arrays;

/**
 * 测试内容：
 * 1。 进程A可以发送消息到共享邮箱给B
 * 2。 进程C可以发送消息到共享邮箱给A
 * 3。 进程B可以从共享邮箱取到自己所有消息，而不会获取到C发送给A的消息
 * 4。 进程B可以正常解析从邮箱获取的消息，并返回消息数组
 * 5。 进程B可以给A发送消息到共享邮箱
 * 6。 进程A可以拿到共享邮箱中所有消息，并正确打印
 */
public class TestForProcessMailBox {
    public static void main(String[] args) {
        ProcessManagement instance = ProcessManagement.getInstance();
        Process processA = new Process(0, "A", "", 8);
        Process processB = new Process(1, "B", "", 8);
        Process processC = new Process(2, "C", "", 8);
        processC.sendMessage("A你好", 0);
        processA.sendMessage("B你好哇", 1);
        processA.sendMessage("再见B", 1);
        System.out.println("共享邮箱:" + instance.getMailBox());
        processB.getMessage();
        System.out.println("共享邮箱:" + instance.getMailBox());
        System.out.println("B的消息:" + Arrays.toString(processB.showAllMessage()));
        processB.sendMessage("A我是B", 0);
        System.out.println("共享邮箱:" + instance.getMailBox());
        processA.getMessage();
        System.out.println("A的消息:" + Arrays.toString(processA.showAllMessage()));
        System.out.println("共享邮箱:" + instance.getMailBox());
    }
}
