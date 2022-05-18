package team.os.gui;

//import com.sun.javafx.collections.ListListenerHelper;

import team.os.global.Global;

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

    int i = 1;

    String proName;//进程名
    String fileName;//文件名
    int pid_DEL;//删除进程的pid
    int count_cyc;//周期数
    int count_cor;//核心数

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
        list_data();
        setTitle("Windose20"); //窗口标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //设置开关隐藏
        setBounds(100, 100, 1000, 450); //设置初始大小和位置
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);
        contentPane.add(panel, BorderLayout.SOUTH);
        button_text();
        list_IMP();
    }

    public void brokerMethod() {
        JOptionPane.showInputDialog(null, "请输入：", "brokerMethod", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void createProcess(ActionEvent e) {
        DefaultTableModel tableModel = (DefaultTableModel) tableProcess.getModel();
        tableModel.setColumnIdentifiers(new Object[]{"进程ID", "进程名称", "进程状态"});
        createProcess_Win(tableModel);
        tableProcess.setRowHeight(30);
        tableProcess.setModel(tableModel);
    }//create button 触发器

    protected void deleteProcess(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) tableProcess.getModel();    //获得表格模型
        deleteProcess_Win(model);
        tableProcess.setModel(model);
    }//delete button 触发器

    protected void actionProcess(ActionEvent e) {
        JFrame frameACT = new JFrame("action");
        JTextField textFieldCyc = new JTextField(16);
        JTextField textFieldCor = new JTextField(16);
        JPanel jpCyc = new JPanel();
        JPanel jpCor = new JPanel();
        JPanel jpButton = new JPanel();
        frameACT.setSize(400, 200);
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

    public void createProcess_Win(DefaultTableModel model) {
        JFrame frameCRE = new JFrame("create");
        JTextField textFieldPro = new JTextField(16);
        JTextField textFieldFile = new JTextField(16);
        DefaultTableModel model1 = model;
        JPanel jpPro = new JPanel();
        JPanel jpFile = new JPanel();
        JPanel jpButton = new JPanel();
        frameCRE.setSize(400, 200);
        JLabel labelPro = new JLabel("进程名");
        JLabel labelFile = new JLabel("文件名");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                create_input(model1, frameCRE, textFieldPro, textFieldFile);
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

    public void deleteProcess_Win(DefaultTableModel model) {
        JFrame frameDEL = new JFrame("delete");
        JTextField textFieldPid = new JTextField(16);
        DefaultTableModel model1 = model;
        JPanel jpPid = new JPanel();
        JPanel jpButton = new JPanel();
        frameDEL.setSize(400, 200);
        JLabel labelPid = new JLabel("进程pid");
        JButton buttonOK = new JButton("确定");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                delete_input(model1, frameDEL, textFieldPid);
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
        textArea = new JTextArea("textArea", 7, 140); //设置文本框
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
        panel.add(scroll);
        panel.add(buttonCRE);
        panel.add(buttonDEL);
        panel.add(buttonACT);
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
        list_Memory.add(new KeyValuePair_Memory("1", "111", "510"));
        list_IO.add(new KeyValuePair_IO("鼠标", "true"));
    }

    void create_input(DefaultTableModel model, JFrame frame, JTextField text1, JTextField text2) {
        JFrame frameCRE = frame;
        DefaultTableModel tableModel = model;
        JTextField textFieldPro = text1;
        JTextField textFieldFile = text2;
        proName = textFieldPro.getText();
        fileName = textFieldFile.getText();
        if (proName.equals("") || fileName.equals("")) {
            JOptionPane.showMessageDialog(null, "输入错误！请重新输入。", "警告", 2);
        } else {
            Global.INSTANCE.createProcess(proName, fileName);
            tableModel.addRow(new Object[]{"进程" + i, "lxh", "TRUE"});
            list_Process.add(new KeyValuePair_Process(i, "lxh", "true"));
            i++;
            JOptionPane.showMessageDialog(null, "输入成功！");
            frameCRE.dispose();
        }
    }//创建进程窗口确定键触发器中函数

    void delete_input(DefaultTableModel model, JFrame frame, JTextField text1) {
        JFrame frameDEL = frame;
        DefaultTableModel tableModel = model;
        JTextField textFieldPid = text1;
        pid_DEL = Integer.parseInt(textFieldPid.getText());
        if (list_Process.size() > 0) {
            for (int j = 0; j < list_Process.size(); j++) {
                if (pid_DEL == list_Process.get(j).pid) {
                    Global.INSTANCE.killProcess(pid_DEL);
                    list_Process.remove(j);
                    model.removeRow(j);
                    JOptionPane.showMessageDialog(panel, "删除进程成功。");
                    break;
                } else if (j == list_Process.size() - 1) {
                    JOptionPane.showMessageDialog(panel, "未找到所输入pid所对应的进程！", "警告", 2);
                }
            }
        } else {
            JOptionPane.showMessageDialog(panel, "没有正在运行的进程！", "警告", 2);
        }
    }//删除进程窗口确定键触发器中函数

    void action_update(JFrame frame, JTextField text1, JTextField text2) {
        JFrame frameACT = frame;
        count_cyc = Integer.parseInt(text1.getText());
        count_cor = Integer.parseInt(text2.getText());
        Global.INSTANCE.exec(count_cyc, count_cor);
        //TODO: 后续应该更新表格
    }//执行窗口确定键触发器中函数
}
