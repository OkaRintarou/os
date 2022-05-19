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
    public void getFacility()
    {
        facilityTotalNumberChart.add("Printer1");
        facilityTotalNumberChart.add("Printer2");
        facilityTotalNumberChart.add("Printer3");
        facilityTotalNumberChart.add("Printer4");
        facilityTotalNumberChart.add("Printer5");
        facilityTotalNumberChart.add("Printer6");
        facilityTotalNumberChart.add("Keyboard7");
        facilityTotalNumberChart.add("Keyboard8");
        facilityTotalNumberChart.add("Keyboard9");
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
