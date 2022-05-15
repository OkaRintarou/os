package team.os.io;

import java.util.ArrayList;
import java.util.List;

public class IOFacilityManagement {
    public int totalNumber;
    public String name;
    public String type;
    public int time;
    IOFacility test0=new IOFacility("test0");

    List<Integer> stateChart=new ArrayList<>();
    public void initState()
    {
        for(int i=0;i<test0.facilityTotalNumberChart.size();i++)
            stateChart.add(0);
    }
    public void changeState(int a)
    {
        stateChart.set(a,1);
    }
    //对IO设备请求应答,output表示需要向打印机输出的内容本身，或者该内容在内存中的地址
    //返回的list第一位表示设备编号，第二位表示阻塞时间，编号为0则表示没有可用设备
    public List<Integer> IOFacilityRequest(String type, int size,String output)
    {
        int condition=0,result=0;
        List<Integer>facilityRest=new ArrayList<>();
        for(int i=0;i<stateChart.size()&&condition==0;i++)
        {
            if(stateChart.get(i)==0)
            {
                facilityRest.add(i);
                condition=1;
                stateChart.set(i,0);//改变当前设备状态
            }
        }
        //遍历设备表后没有成功分配设备
        if(condition==0)
            facilityRest.add(0);
        else
            //调用假脱机先将打印内容存到磁盘中，防止进程阻塞
            result=Spooling(type,size);
        if(result==1)
            size/=2;
        facilityRest.add(size);
        return facilityRest;
    }
    //假脱机
    public int Spooling(String output,int size)
    {
        //将打印内容存入模拟的磁盘中
        String[] temp =new String[size];
        for(int i=0;i<temp.length;i++)
        {
            temp[i]=output;
        }
        return 1;
    }
    //进程阻塞时间函数，受到传输字节数量的影响，返回模拟的进程阻塞时间

    //对IO设备释放应答
    public int IOFacilityRelease(String type,int index)
    {
        if(stateChart.get(index)==0)
        {
            stateChart.set(index,0);//改变当前设备状态
            return 1;
        }
        else
            return 0;
    }
}
