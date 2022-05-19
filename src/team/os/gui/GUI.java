package team.os.gui;

//import com.sun.javafx.collections.ListListenerHelper;

import team.os.global.Global;
import team.os.process.Process;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GUI extends JFrame {
    private JPanel contentPane;//主界面
    private JTable tableProcess;//进程表格
    private JTable tableIO;//IO设备表格
    private JTable tableMemory;//内存表格
    DefaultTableModel tableModel_Pro;
    DefaultTableModel tableModel_Mem;
    DefaultTableModel tableModel_IO;

    int iPro = 1;
    int iMem = 1;
    int iIO = 1;

    String proName;//进程名
    String fileName;//文件名
    int pid_DEL;//删除进程的pid
    int count_cyc;//周期数
    int count_cor;//核心数

    JPanel panel = new JPanel();//包含按钮以及文本框等信息的面板容器

    public static class KeyValuePair_Process {
        public int pid;
        public String name;
        public Process.ProcessStates state;
        public int ID_now;
        public int all_ins;

        public KeyValuePair_Process(int first, String second, Process.ProcessStates third,int fourth,int fifth) {
            pid = first;
            name = second;
            state = third;
            ID_now = fourth;
            all_ins = fifth;
        }
    }//进程list的格式

    public static class KeyValuePair_Memory {
        public String name;
        public String type;
        public int size;
        public String count;
        public int pid;
        public int uid;

        public KeyValuePair_Memory(String first, String second, int third, String fourth, int fifth, int sixth) {
            name = first;
            type = second;
            size = third;
            count = fourth;
            pid = fifth;
            uid = sixth;
        }
    }//内存list的格式

    public static class KeyValuePair_IO {
        public String name;
        public String state;

        public KeyValuePair_IO(String first, String second) {
            name = first;
            state = second;
        }
    }//IO设备list的格式

    ArrayList<KeyValuePair_Process> list_Process = new ArrayList<KeyValuePair_Process>();//创建进程列表list_Process
    ArrayList<KeyValuePair_Memory> list_Memory = new ArrayList<KeyValuePair_Memory>();//创建内存列表list_Memory
    ArrayList<KeyValuePair_IO> list_IO = new ArrayList<KeyValuePair_IO>();//创建IO设备列表list_IO


    public ArrayList<String> getInsString() {
        // TODO: 2022/5/9
        ArrayList<String> list = new ArrayList<>();
        list.add("VarPrint v1 String");
        list.add("VarPrint v1 String");
        list.add("VarPrint v1 String");
        return list;
    }

    public void print(String msg) {
        textArea.append(msg);
        textArea.append("\n");
    }

    public GUI() {
        //list_data();
        setTitle("Windose20"); //窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //设置开关隐藏
        setBounds(500, 300, 1000, 450); //设置初始大小和位置
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        contentPane.add(panel, BorderLayout.SOUTH);
        button_text();
        list_IMP();
    }

    public void brokerMethod() {
        JFrame frameBro = new JFrame("delete");
        JTextArea textAreaIns = new JTextArea(null,5,16);
        JPanel jpIns = new JPanel();
        JPanel jpButton = new JPanel();
        frameBro.setBounds(750, 400, 400, 250);
        JLabel labelIns = new JLabel("指令");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                brokerMethodInput(frameBro,textAreaIns);
            }
        });
        JButton buttonCan = new JButton("取消");
        buttonCan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frameBro.dispose();
            }
        });
        jpIns.add(labelIns);
        jpIns.add(textAreaIns);
        jpButton.add(buttonOK);
        jpButton.add(buttonCan);
        frameBro.add(jpIns, BorderLayout.NORTH);
        frameBro.add(jpButton, BorderLayout.SOUTH);
        frameBro.setVisible(true);
        frameBro.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    protected void createProcess(ActionEvent e) {
        tableModel_Pro= (DefaultTableModel) tableProcess.getModel();
        tableModel_Pro.setColumnIdentifiers(new Object[]{"进程ID", "进程名称", "进程状态","当前指令序号","总指令条数"});
        createProcess_Win();
        tableProcess.setRowHeight(30);
        tableProcess.setModel(tableModel_Pro);
    }//create button 触发器

    protected void deleteProcess(ActionEvent e) {
        tableModel_Pro= (DefaultTableModel) tableProcess.getModel();    //获得表格模型
        deleteProcess_Win();
        tableProcess.setModel(tableModel_Pro);
    }//delete button 触发器

    protected void actionProcess(ActionEvent e) {
        JFrame frameACT = new JFrame("action");
        JTextField textFieldCyc = new JTextField(16);
        JTextField textFieldCor = new JTextField(16);
        JPanel jpCyc = new JPanel();
        JPanel jpCor = new JPanel();
        JPanel jpButton = new JPanel();
        frameACT.setBounds(750, 400, 400, 250);
        JLabel labelCyc = new JLabel("周期数");
        JLabel labelCor = new JLabel("核心数");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                action_update(frameACT, textFieldCyc, textFieldCor);
            }
        });
        JButton buttonCan = new JButton("取消");
        buttonCan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frameACT.dispose();
            }
        });
        jpCyc.add(labelCyc);
        jpCyc.add(textFieldCyc);
        jpCor.add(labelCor);
        jpCor.add(textFieldCor);
        jpButton.add(buttonOK);
        jpButton.add(buttonCan);
        frameACT.add(jpCyc, BorderLayout.NORTH);
        frameACT.add(jpCor, BorderLayout.CENTER);
        frameACT.add(jpButton, BorderLayout.SOUTH);
        frameACT.setVisible(true);
        frameACT.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }//action button 触发器

    public void createProcess_Win() {
        JFrame frameCRE = new JFrame("create");
        JTextField textFieldPro = new JTextField(16);
        JTextField textFieldFile = new JTextField(16);

        JPanel jpPro = new JPanel();
        JPanel jpFile = new JPanel();
        JPanel jpButton = new JPanel();
        frameCRE.setBounds(750, 400, 400, 250);
        JLabel labelPro = new JLabel("进程名");
        JLabel labelFile = new JLabel("文件名");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                create_input(frameCRE, textFieldPro, textFieldFile);
            }
        });
        JButton buttonCan = new JButton("取消");
        buttonCan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frameCRE.dispose();
            }
        });
        jpPro.add(labelPro);
        jpPro.add(textFieldPro);
        jpFile.add(labelFile);
        jpFile.add(textFieldFile);
        jpButton.add(buttonOK);
        jpButton.add(buttonCan);
        frameCRE.add(jpPro, BorderLayout.NORTH);
        frameCRE.add(jpFile, BorderLayout.CENTER);
        frameCRE.add(jpButton, BorderLayout.SOUTH);
        frameCRE.setVisible(true);
        frameCRE.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }//创建进程窗口

    public void deleteProcess_Win() {
        JFrame frameDEL = new JFrame("delete");
        JTextField textFieldPid = new JTextField(16);
        JPanel jpPid = new JPanel();
        JPanel jpButton = new JPanel();
        frameDEL.setBounds(750, 400, 400, 250);
        JLabel labelPid = new JLabel("进程pid");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delete_input(frameDEL, textFieldPid);
            }
        });
        JButton buttonCan = new JButton("取消");
        buttonCan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frameDEL.dispose();
            }
        });
        jpPid.add(labelPid);
        jpPid.add(textFieldPid);
        jpButton.add(buttonOK);
        jpButton.add(buttonCan);
        frameDEL.add(jpPid, BorderLayout.NORTH);
        frameDEL.add(jpButton, BorderLayout.SOUTH);
        frameDEL.setVisible(true);
        frameDEL.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }//删除进程窗口

    public JButton buttonCRE;
    public JButton buttonDEL;
    public JButton buttonACT;
    public JTextArea textArea;
    public JScrollPane scroll;

    void button_text() {
        textArea = new JTextArea("textArea", 7, 100); //设置文本框
        scroll = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setForeground(Color.BLACK);
        textArea.setFont(new Font("宋体", Font.BOLD, 16));
        textArea.setBackground(Color.WHITE);
        textArea.setText("");
        textArea.setEditable(false);

        buttonCRE = new JButton("CREATE");
        buttonCRE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createProcess(e);
            }
        });

        buttonDEL = new JButton("DELETE");
        buttonDEL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteProcess(e);
            }
        });

        buttonACT = new JButton(("ACTION"));
        buttonACT.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actionProcess(e);
            }
        });
        panel.add(scroll,BorderLayout.NORTH);
        panel.add(buttonCRE,BorderLayout.SOUTH);
        panel.add(buttonDEL,BorderLayout.SOUTH);
        panel.add(buttonACT,BorderLayout.SOUTH);
    }

    void list_IMP() {
        JScrollPane scrollPane = new JScrollPane();
        JScrollPane scrollPane1 = new JScrollPane();
        JScrollPane scrollPane2 = new JScrollPane();
        contentPane.add(scrollPane, BorderLayout.EAST);
        contentPane.add(scrollPane1, BorderLayout.WEST);
        contentPane.add(scrollPane2, BorderLayout.CENTER);
        tableProcess = new JTable();
        tableIO = new JTable();
        tableMemory = new JTable();
        tableProcess.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableIO.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        tableMemory.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        scrollPane.setViewportView(tableProcess);
        scrollPane1.setViewportView(tableIO);
        scrollPane2.setViewportView(tableMemory);
        list_Process();
        list_Memory();
        list_IO();
    }

    private void list_Process() {
        tableModel_Pro = (DefaultTableModel) tableProcess.getModel();
        tableModel_Pro.setColumnIdentifiers(new Object[]{"进程ID", "进程名称", "进程状态","当前指令序号","总指令条数"});
        for (KeyValuePair_Process p : list_Process) tableModel_Pro.addRow(new Object[]{p.pid, p.name, p.state,p.ID_now,p.all_ins});
        tableProcess.setRowHeight(30);
        tableProcess.setModel(tableModel_Pro);
    }

    void list_Memory() {
        tableModel_Mem = (DefaultTableModel) tableMemory.getModel();
        tableModel_Mem.setColumnIdentifiers(new Object[]{"变量名","变量类型","大小","值","pid","变量标识符"});
        for (KeyValuePair_Memory p : list_Memory) tableModel_Mem.addRow(new Object[]{p.name,p.type,p.size,p.count,p.pid,p.uid});
        tableMemory.setRowHeight(30);
        tableMemory.setModel(tableModel_Mem);
    }

    void list_IO() {
        tableModel_IO = (DefaultTableModel) tableIO.getModel();
        tableModel_IO.setColumnIdentifiers(new Object[]{"设备名称", "设备状态"});
        for (KeyValuePair_IO p : list_IO) tableModel_IO.addRow(new Object[]{p.name, p.state});
        tableIO.setRowHeight(30);
        tableIO.setModel(tableModel_IO);
    }

    void list_data() {

    }

    void create_input(JFrame frame, JTextField text1, JTextField text2) {
        JFrame frameCRE = frame;
        JTextField textFieldPro = text1;
        JTextField textFieldFile = text2;
        proName = textFieldPro.getText();
        fileName = textFieldFile.getText();
        if (proName.equals("") || fileName.equals("")) {
            JOptionPane.showMessageDialog(null, "输入错误！请重新输入。", "警告", 2);
        } else {
            Global.INSTANCE.createProcess(proName, fileName);
            JOptionPane.showMessageDialog(null, "输入成功！");
            frameCRE.dispose();
        }
    }//创建进程窗口确定键触发器中函数

    void delete_input(JFrame frame, JTextField text1) {
        JFrame frameDEL = frame;
        JTextField textFieldPid = text1;
        pid_DEL = Integer.parseInt(textFieldPid.getText());
        if (list_Process.size() > 0) {
            for (int j = 0; j < list_Process.size(); j++) {
                if (pid_DEL == list_Process.get(j).pid) {
                    Global.INSTANCE.killProcess(pid_DEL);
                    JOptionPane.showMessageDialog(null, "删除进程成功。");
                    break;
                } else if (j == list_Process.size() - 1) {
                    JOptionPane.showMessageDialog(null, "未找到所输入pid所对应的进程！", "警告", 2);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "没有正在运行的进程！", "警告", 2);
        }
    }//删除进程窗口确定键触发器中函数

    public void brokerMethodInput(JFrame frame,JTextArea Area){
        JFrame frameBro = frame;
        JTextArea textArea = Area;
        textArea.getText();
        //TODO:后续添加具体操作
    }

    public void addList_Process(int pidPro, String namePro, Process.ProcessStates statePro, int ID_nowPro, int all_insPro){
        list_Process.add(new KeyValuePair_Process(pidPro,namePro,statePro,ID_nowPro,all_insPro));
        tableModel_Pro.addRow(new Object[]{list_Process.get(iPro -1).pid,list_Process.get(iPro -1).name,list_Process.get(iPro -1).state,list_Process.get(iPro -1).ID_now,list_Process.get(iPro -1).all_ins});
        iPro++;
    }

    public void subList_Process(int pidPro) {
        for (int j = 0; j < list_Process.size(); j++) {
            if(pidPro ==list_Process.get(j).pid){
                list_Process.remove(j);
                tableModel_Pro.removeRow(j);
                break;
            }
        }
    }

    public void modList_Process(int pidPro, String namePro, Process.ProcessStates statePro, int ID_nowPro, int all_insPro){
        for (int j = 0; j < list_Process.size(); j++) {
            if(pidPro == list_Process.get(j).pid){
                list_Process.get(j).name = namePro;
                list_Process.get(j).state = statePro;
                list_Process.get(j).ID_now = ID_nowPro;
                list_Process.get(j).all_ins = all_insPro;
                break;
            }
        }
        tableModel_Pro.setRowCount(0);
        list_Process();
    }

    public void addList_Memory(String nameMem, String typeMem, int sizeMem, String countMem, int pidMem, int uidMem){
        list_Memory.add(new KeyValuePair_Memory(nameMem,typeMem,sizeMem,countMem,pidMem,uidMem));
        tableModel_Mem.addRow(new Object[]{list_Memory.get(iMem -1).name,list_Memory.get(iMem -1).type,list_Memory.get(iMem -1).size,list_Memory.get(iMem -1).count,list_Memory.get(iMem -1).pid,list_Memory.get(iMem -1).uid});
        iMem++;
    }

    public void subList_Memory(int uidMem){
        for (int j = 0; j < list_Memory.size(); j++) {
            if(uidMem ==list_Memory.get(j).uid){
                list_Memory.remove(j);
                tableModel_Mem.removeRow(j);
                break;
            }
        }
    }

    public void modList_Memory(String countMem, int uidMem){
        for (int j = 0; j < list_Memory.size(); j++) {
            if(uidMem == list_Memory.get(j).uid){
                list_Memory.get(j).count = countMem;
                break;
            }
        }
        tableModel_Mem.setRowCount(0);
        list_Memory();
    }

    public void addList_IO(String nameIO, String stateIO){
        list_IO.add(new KeyValuePair_IO(nameIO,stateIO));
        tableModel_IO.addRow(new Object[]{list_IO.get(iIO -1).name,list_IO.get(iIO -1).state});
    }

    public void subList_IO(String nameIO){
        for (int j = 0; j < list_IO.size(); j++) {
            if(nameIO.equals(list_IO.get(j).name)){
                list_IO.remove(j);
                tableModel_IO.removeRow(j);
                break;
            }
        }
    }

    public void modList_IO(String nameIO, String stateIO){
        for (int j = 0; j < list_IO.size(); j++) {
            if(nameIO.equals(list_IO.get(j).name)){
                list_IO.get(j).state = stateIO;
                break;
            }
        }
        tableModel_IO.setRowCount(0);
        list_IO();
    }

    void action_update(JFrame frame, JTextField text1, JTextField text2) {
        JFrame frameACT = frame;
        count_cyc = Integer.parseInt(text1.getText());
        count_cor = Integer.parseInt(text2.getText());
        Global.INSTANCE.exec(count_cyc, count_cor);
        //TODO: 后续应该更新表格
    }//执行窗口确定键触发器中函数
}
