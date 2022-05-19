package team.os.io;

import team.os.global.Global;

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
    public void getFacility()
    {
        facilityTotalNumberChart.add("Printer1");
        Global.INSTANCE.getGui().addList_IO("Printer1","空闲");
        facilityTotalNumberChart.add("Printer2");
        Global.INSTANCE.getGui().addList_IO("Printer2","空闲");
        facilityTotalNumberChart.add("Printer3");
        Global.INSTANCE.getGui().addList_IO("Printer3","空闲");
        facilityTotalNumberChart.add("Printer4");
        Global.INSTANCE.getGui().addList_IO("Printer4","空闲");
        facilityTotalNumberChart.add("Printer5");
        Global.INSTANCE.getGui().addList_IO("Printer5","空闲");
        facilityTotalNumberChart.add("Printer6");
        Global.INSTANCE.getGui().addList_IO("Printer6","空闲");
        facilityTotalNumberChart.add("Keyboard1");
        Global.INSTANCE.getGui().addList_IO("Keyboard1","空闲");
        facilityTotalNumberChart.add("Keyboard2");
        Global.INSTANCE.getGui().addList_IO("Keyboard2","空闲");
        facilityTotalNumberChart.add("Keyboard3");
        Global.INSTANCE.getGui().addList_IO("Keyboard3","空闲");
    }
    //设备占用表用于记录设备被进程占用的情况
    List<Integer>facilityOccupiedChart=new ArrayList<>();
    public void addOccupied(int[] array)
    {
        int i;
        for (i = 0; i < array.length; i++) {
            facilityOccupiedChart.add(i);
            System.out.print(array[i] + " ");
        }
    }
    public void deleteOccupied(int a)
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
    public void initState()
    {
        for(int i=0;i<facilityTotalNumberChart.size();i++)
            stateChart.add(0);
    }
    public void changeState(int a)
    {
        stateChart.set(a,1);
    }
    //返回设备资源表到ui
    public List<Integer> facilityRest(int signal)
    {
        List<Integer>facilityOccupied=new ArrayList<>();
        for(int i=0;i<stateChart.size();i++)
        {
            if(stateChart.get(i)==1)
                facilityOccupied.add(i);
        }
        return facilityOccupied;
    }


}
