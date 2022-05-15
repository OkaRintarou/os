package team.os.gui;

//import com.sun.javafx.collections.ListListenerHelper;
import team.os.global.Global;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class GUI extends JFrame {
    private JPanel contentPane;//主界面
    private JTable tableProcess;//进程表格
    private JTable tableIO;//IO设备表格
    private JTable tableMemory;//内存表格

    int i = 1;

    String file;//文件名
    String filePath;//文件路径

    JPanel panel = new JPanel();//包含按钮以及文本框等信息的面板容器

    public static class KeyValuePair_Process {
        public int pid;
        public String name;
        public String state;

        public KeyValuePair_Process(int first, String second, String third) {
            pid = first;
            name = second;
            state = third;
        }
    }//进程list的格式

    public static class KeyValuePair_Memory {
        public String id;
        public String address;
        public String size;

        public KeyValuePair_Memory(String first, String second, String third) {
            id = first;
            address = second;
            size = third;
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
        ArrayList<String> list=new ArrayList<>();
        list.add("VarPrint v1 String");
        list.add("VarPrint v1 String");
        list.add("VarPrint v1 String");
        return list;
    }
    public void print(String msg)  {
        // TODO: 2022/5/9
        System.out.println("From GUI: "+msg);
    }
    public GUI() {
        list_data();
        setTitle("Windose20"); //窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //设置开关隐藏
        setBounds(100, 100, 450, 200); //设置初始大小和位置
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        contentPane.add(panel, BorderLayout.SOUTH);
        button_text();
        list_IMP();
    }

    protected void createProcess(ActionEvent e) {
        DefaultTableModel tableModel = (DefaultTableModel) tableProcess.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"进程ID", "进程名称", "进程状态"});
        file = JOptionPane.showInputDialog(null, "请输入文件名：", "文件名", JOptionPane.INFORMATION_MESSAGE);
        filePath = JOptionPane.showInputDialog(null, "请输入路径：", "路径", JOptionPane.INFORMATION_MESSAGE);
        if ((null != file && !"".equals(file)) && (null != filePath && !"".equals(filePath))) {
            tableModel.addRow(new Object[]{"进程" + i, "lxh", "TRUE"});
            i++;
        } else {
            JOptionPane.showMessageDialog(panel, "输入错误！请重新输入。", "警告", 2);
        }
        tableProcess.setRowHeight(30);
        tableProcess.setModel(tableModel);
    }

    protected void deleteProcess(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) tableProcess.getModel();    //获得表格模型
        int[] selectedRows = tableProcess.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            model.removeRow(selectedRows[i]);
        }
        tableProcess.setModel(model);
    }

    void button_text() {
        JTextArea textArea = new JTextArea("textArea",7,140); //设置文本框
        textArea.setLineWrap(true);
        textArea.setForeground(Color.BLACK);
        textArea.setFont(new Font("宋体",Font.BOLD,16));
        textArea.setBackground(Color.WHITE);
        JButton buttonCRE = new JButton("CREATE");

        buttonCRE.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createProcess(e);
            }
        });
        JButton buttonDEL = new JButton("DELETE");
        buttonDEL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteProcess(e);
            }
        });
        panel.add(textArea);
        panel.add(buttonCRE);
        panel.add(buttonDEL);
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
        DefaultTableModel tableModel = (DefaultTableModel) tableProcess.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"进程ID", "进程名称", "进程状态"});
        for (KeyValuePair_Process p : list_Process) tableModel.addRow(new Object[]{p.pid, p.name, p.state});
        tableProcess.setRowHeight(30);
        tableProcess.setModel(tableModel);
    }

    void list_Memory() {
        DefaultTableModel tableModel = (DefaultTableModel) tableMemory.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"进程id", "块首地址", "块大小"});
        for (KeyValuePair_Memory p : list_Memory) tableModel.addRow(new Object[]{p.id, p.address, p.size});
        tableMemory.setRowHeight(30);
        tableMemory.setModel(tableModel);
    }

    void list_IO() {
        DefaultTableModel tableModel = (DefaultTableModel) tableIO.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"设备名称", "设备状态"});
        for (KeyValuePair_IO p : list_IO) tableModel.addRow(new Object[]{p.name, p.state});
        tableIO.setRowHeight(30);
        tableIO.setModel(tableModel);
    }

    void list_data() {
        list_Process.add(new KeyValuePair_Process(1, "lxh", "true"));
        list_Memory.add(new KeyValuePair_Memory("1", "111", "510"));
        list_IO.add(new KeyValuePair_IO("鼠标", "true"));
    }

    static class update extends TimerTask {
        @Override
        public void run() {
            System.out.println("1");
        }
    }
}
