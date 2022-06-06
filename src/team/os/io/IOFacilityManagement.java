package team.os.io;

import team.os.global.Global;

import java.util.ArrayList;
import java.util.List;

public class IOFacilityManagement {
    public int totalNumber;
    public String name;
    public String type;
    public int time;
    IOFacility test0 = new IOFacility("test0");

    public IOFacilityManagement() {
        test0.getFacility();
        test0.initState();
    }

    //对IO设备请求应答,output表示需要向打印机输出的内容本身，或者该内容在内存中的地址
    //返回的list第一位表示设备编号，第二位表示阻塞时间，编号为-1则表示没有可用设备
    public List<Integer> IOFacilityRequest(String type, int size, String output) {
        int condition = 0, result = 0, temp = 0;
        int head = 0, tail = test0.stateChart.size();
        if (type.equals("printer")) {
            head = 0;
            tail = 6;
        } else if (type.equals("keyboard")) {
            head = 6;
            tail = test0.stateChart.size();
        }
        List<Integer> facilityRest = new ArrayList<>();
        for (int i = head; i < tail && condition == 0; i++) {
            if (test0.stateChart.get(i) == 0) {
                facilityRest.add(i + 1);
                temp = i;
                condition = 1;
                test0.stateChart.set(i, 1);//改变当前设备状态
            }
        }
        //遍历设备表后没有成功分配设备
        //System.out.println(condition );
        if (condition == 0) facilityRest.add(-1);
        else if (type.equals("printer")) {
            //调用假脱机先将打印内容存到磁盘中，防止进程阻塞
            result = Spooling(output, size);
            if (result == 1) size /= 2;
            facilityRest.add(size);
            //System.out.println(temp );
            //IOFacilityRelease("printer",temp);
        }
        System.out.println("From io: Request " + facilityRest);
        if (facilityRest.get(0) != -1)
            Global.INSTANCE.getGui().modList_IO(test0.facilityTotalNumberChart.get(facilityRest.get(0) - 1), "占用");
        return facilityRest;
    }

    //假脱机
    public int Spooling(String output, int size) {
        //将打印内容存入模拟的磁盘中
        String[] temp = new String[size];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = output;
        }
        return 1;
    }
    //进程阻塞时间函数，受到传输字节数量的影响，返回模拟的进程阻塞时间

    //对IO设备释放应答
    public int IOFacilityRelease(String type, int index) {
        if (index == -1 || index > test0.stateChart.size()) {
            System.out.println("Release Fault " + index);
            return 0;
        }
        if (test0.stateChart.get(index - 1) == 1) {
            test0.stateChart.set(index - 1, 0);//改变当前设备状态
            Global.INSTANCE.getGui().modList_IO(test0.facilityTotalNumberChart.get(index - 1), "空闲");
            System.out.println("From io: release " + index);
            return 1;
        } else {
            System.out.println("Release Fault " + index);
            return 0;
        }

    }

}
