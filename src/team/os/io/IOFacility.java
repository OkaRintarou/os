package team.os.io;

import java.util.List;
import java.util.ArrayList;


public class IOFacility
{
    public int totalNumber;
    public String name;
    public String type;
    public int time;
    public IOFacility(String name)
    {
        this.name = name;
    }
    //设备资源表
    List<String>facilityTotalNumberChart=new ArrayList<>();
    void getFacility()
    {
        facilityTotalNumberChart.add("Printer1");
        facilityTotalNumberChart.add("Printer2");
        facilityTotalNumberChart.add("Printer3");
        facilityTotalNumberChart.add("Printer4");
        facilityTotalNumberChart.add("Printer5");
        facilityTotalNumberChart.add("Printer6");
    }
    //设备占用表用于记录设备被进程占用的情况
    List<Integer>facilityOccupiedChart=new ArrayList<>();
    void addOccupied(int[] array)
    {
        int i;
        for (i = 0; i < array.length; i++) {
            facilityOccupiedChart.add(i);
            System.out.print(array[i] + " ");
        }
    }
    void deleteOccupied(int a)
    {
        int i,temp;
        for (i = 0; i < facilityOccupiedChart.size(); i++) {
            temp=facilityOccupiedChart.get(i);
            if(temp==a)
                facilityOccupiedChart.remove(i);
        }
    }
    //设备状态表
    List<Integer>stateChart=new ArrayList<>();
    void initState()
    {
        for(int i=0;i<facilityTotalNumberChart.size();i++)
            stateChart.add(0);
    }
    void changeState(int a)
    {
        stateChart.set(a,1);
    }
    //返回设备资源表到ui
    List<Integer> facilityRest(int signal)
    {
        List<Integer>facilityOccupied=new ArrayList<>();
        for(int i=0;i<stateChart.size();i++)
        {
            if(stateChart.get(i)==1)
                facilityOccupied.add(i);
        }
        return facilityOccupied;
    }

    //对IO设备请求应答
    //返回的list第一位表示设备编号，第二位表示阻塞时间，编号为0则表示没有可用设备
    public List<Integer> IOFacilityRequest(String type, int size)
    {
        int condition=0;
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
        if(condition==0)
            facilityRest.add(0);
        size/=2;
        facilityRest.add(size);
        return facilityRest;
    }
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
