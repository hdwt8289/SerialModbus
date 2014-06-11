import com.mongodb.*;
import com.serotonin.io.serial.SerialParameters;
import com.serotonin.modbus4j.BasicProcessImage;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusSlaveSet;
import com.serotonin.modbus4j.ProcessImageListener;
import com.serotonin.modbus4j.exception.ModbusInitException;
import gnu.io.CommPortIdentifier;


import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class ParamerSet extends JPanel {
    private JLabel lblIP;
    private JTextField txtIp;
    private JLabel lblName;
    private JComboBox txtProjectName;
    private JLabel lblNo;
    private JComboBox cmbNo;
    private JLabel lblBote;
    private JComboBox cmbBote;
    private JLabel lblLength;
    private JComboBox cmbLength;
    private JLabel lblParity;
    private JComboBox cmbParity;
    private JLabel lblStopBit;
    private JComboBox cmbStopBit;
    private JLabel lblDelay;
    private JTextField txtDelay;
    private JButton btnOk;
    private JButton btnCancel;
    private JLabel lblHeart;
    private JComboBox cmbHeart;
    private static String paramIp;
    private static String paramName;
    private static String paramNo;
    private static int paramBote;
    private static int parmLength;
    private static int parmParity;
    private static int parmStopBit;
    private static int parmDelay = 0;
    private static String parmHeart;
    private static long nowTime = 0;
    static Mongo m = null;
    static DB db = null;
    static ModbusSlaveSet slave = null;
    public static JFrame f;
    private ScheduledExecutorService timerRead;
    private ScheduledExecutorService timerWrite;
    private JMenu menu;
    private static ReentrantLock lockR = new ReentrantLock();///线程锁
    private static ReentrantLock lockW = new ReentrantLock();///线程锁

    public void setStrIp(String strIp) {
        ParamerSet.strIp = strIp;
    }

    private static String strIp;    ///记录IP地址
    private static String strProName = "";
    private static Map m_Analog = new HashMap();
    private static Map m_Coils = new HashMap();

    ///构造窗体
    public void setFrame() {
        f = new JFrame("数据通讯参数设置");
        //获取屏幕分辨率的工具集
        Toolkit tool = Toolkit.getDefaultToolkit();
        //利用工具集获取屏幕的分辨率
        Dimension dim = tool.getScreenSize();
        //获取屏幕分辨率的高度
        int height = (int) dim.getHeight();
        //获取屏幕分辨率的宽度
        int width = (int) dim.getWidth();
        //设置位置
        f.setLocation((width - 300) / 2, (height - 400) / 2);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        f.setContentPane(this);
        f.setSize(320, 300);
        f.setResizable(false);

        JMenuBar menubar = new JMenuBar();
        menu = new JMenu("点表操作");
        menu.setMnemonic('F');
        menubar.add(menu);
        JMenuItem item0 = new JMenuItem("导入点表");
        item0.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImpData imp = new ImpData();
                imp.StartImp(strProName, strIp);

            }
        });
        menu.add(item0);
        menu.addSeparator();
        JMenuItem item1 = new JMenuItem("导出点表");
        item1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExpData exp = new ExpData();
                exp.StartExp(strProName, strIp);
            }
        });
        menu.add(item1);
        f.setJMenuBar(menubar);
        JPanel p1 = new JPanel();
        f.add(addPanel(p1));
        f.validate();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (slave != null)
                    slave.stop();
            }
        });
    }

    ///添加控件
    public JPanel addPanel(JPanel p1) {

        lblIP = new JLabel("主机名");
        txtIp = new JTextField(20);
        txtIp.setText(strIp);


        lblNo = new JLabel("端口号");
        cmbNo = new JComboBox();
        cmbNo.setEditable(true);
        cmbNo.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cmbNo.removeAllItems();
                CommPortIdentifier portId = null;
                Enumeration portList;
                portList = CommPortIdentifier.getPortIdentifiers();
                while (portList.hasMoreElements()) {
                    portId = (CommPortIdentifier) portList.nextElement();
                    cmbNo.addItem(portId.getName());
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        lblName = new JLabel("工程名");
        txtProjectName = new JComboBox();
        txtProjectName.setEditable(true);
        txtProjectName.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                txtProjectName.removeAllItems();
                Mongo m1 = null;
                try {
                    m1 = new Mongo(txtIp.getText().toString(), 27017);
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
                for (String name : m1.getDatabaseNames()) {
                    txtProjectName.addItem(name);
                }
                m1.close();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        lblBote = new JLabel("波特率");
        cmbBote = new JComboBox();
        cmbBote.addItem(9600);
        cmbBote.addItem(19200);
        cmbBote.addItem(57600);
        cmbBote.addItem(115200);

        lblLength = new JLabel("数据长度");
        cmbLength = new JComboBox();
        cmbLength.addItem(8);
        cmbLength.addItem(7);

        lblParity = new JLabel("校验");
        cmbParity = new JComboBox();
        cmbParity.addItem("None");
        cmbParity.addItem("Odd");
        cmbParity.addItem("Even");

        lblStopBit = new JLabel("停止位");
        cmbStopBit = new JComboBox();
        cmbStopBit.addItem(1);
        cmbStopBit.addItem(2);

        lblDelay = new JLabel("刷新");
        txtDelay = new JTextField(20);

        lblHeart = new JLabel("心跳信号");
        cmbHeart = new JComboBox();
        cmbHeart.setEditable(true);
        cmbHeart.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                cmbHeart.removeAllItems();
                Mongo m1 = null;
                DB dd = null;
                try {
                    m1 = new Mongo(txtIp.getText().toString(), 27017);
                    dd = m1.getDB(txtProjectName.getSelectedItem().toString());
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }
                DBCollection meta = dd.getCollection("META");
                DBCursor cursor = meta.find();
                while (cursor.hasNext()) {
                    //记录数据类型
                    DBObject dbo = cursor.next();
                    String name = dbo.get("_id").toString();
                    String type = dbo.get("type").toString();
                    if (type.equals("DO")) {
                        cmbHeart.addItem(name);
                    }
                }

                m1.close();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        btnOk = new JButton("确定");
        btnOk.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnOk.setEnabled(false);
                paramIp = txtIp.getText().toString();
                paramName = txtProjectName.getSelectedItem().toString();
                paramNo = cmbNo.getSelectedItem().toString();
                paramBote = Integer.parseInt(cmbBote.getSelectedItem().toString());
                parmLength = Integer.parseInt(cmbLength.getSelectedItem().toString());
                parmParity = cmbParity.getSelectedIndex();
                parmStopBit = Integer.parseInt(cmbStopBit.getSelectedItem().toString());
                parmDelay = Integer.parseInt(txtDelay.getText().toString());
                parmHeart = cmbHeart.getSelectedItem().toString();

                if (!paramName.equals("") && !paramNo.equals("") && !parmHeart.equals("")) {
                    menu.setEnabled(false);
                    receiveData(paramIp, paramName, paramNo, paramBote, parmLength, parmParity, parmStopBit, parmDelay, parmHeart);
                }
            }
        });
        btnCancel = new JButton("取消");
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });


        //JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(10, 2));

        p1.add(lblIP);
        p1.add(txtIp);

        p1.add(lblNo);
        p1.add(cmbNo);

        p1.add(lblName);
        p1.add(txtProjectName);

        p1.add(lblBote);
        p1.add(cmbBote);

        p1.add(lblLength);
        p1.add(cmbLength);

        p1.add(lblParity);
        p1.add(cmbParity);

        p1.add(lblStopBit);
        p1.add(cmbStopBit);

        p1.add(lblDelay);
        p1.add(txtDelay);
        txtDelay.setText("500");

        p1.add(lblHeart);
        p1.add(cmbHeart);

        p1.add(btnOk);
        p1.add(btnCancel);


        p1.validate();

        return p1;
    }

    ///数据接收函数
    public void receiveData(String ip, String paramName, String paramNo, int paramBote, int paramLength, int parmParity, int parmStopBit, int parmDelay, String parmHeart) {
        try {
            try {
                m = new Mongo(ip, 27017);
                db = m.getDB(paramName);
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            } catch (MongoException e) {
                e.printStackTrace();
            }
            final DBCollection coll = db.getCollection("DATAIN");
            final DBCollection collout = db.getCollection("DATAOUT");
            DBCollection meta = db.getCollection("META");

            //记录数据字段
            final Map map00 = new HashMap();
            final Map map01 = new HashMap();
            final Map map02 = new HashMap();
            final Map map03 = new HashMap();

            final Map m_ai_max = new HashMap();
            final Map m_ai_min = new HashMap();
            final Map m_ao_max = new HashMap();
            final Map m_ao_min = new HashMap();

            DBCursor cursor = meta.find();
            while (cursor.hasNext()) {
                //记录数据类型
                DBObject dbo = cursor.next();
                String name = dbo.get("_id").toString();
                String type = dbo.get("type").toString();
                String addr = dbo.get("addr").toString();
                if (type.equals("AI")) {
                    Double max = Double.parseDouble(dbo.get("max").toString());
                    Double min = Double.parseDouble(dbo.get("min").toString());
                    map00.put(name, addr);
                    m_ai_max.put(name, max);
                    m_ai_min.put(name, min);
                }
                if (type.equals("DI")) {
                    map01.put(name, addr);
                }
                if (type.equals("AO")) {
                    Double max = Double.parseDouble(dbo.get("max").toString());
                    Double min = Double.parseDouble(dbo.get("min").toString());
                    map02.put(name, addr);
                    m_ao_max.put(name, max);
                    m_ao_min.put(name, min);
                }
                if (type.equals("DO")) {
                    map03.put(name, addr);
                }
            }
            SerialParameters params = new SerialParameters();
            params.setCommPortId(paramNo);
            params.setBaudRate(paramBote);
            params.setDataBits(paramLength);
            params.setParity(parmParity);
            params.setStopBits(parmStopBit);
            ModbusFactory modbusFactory = new ModbusFactory();
            slave = modbusFactory.createRtuSlave(params);
            slave.addProcessImage(getModscanProcessImage(1));
            slave.addProcessImage(getModscanProcessImage(2));
            new Thread(new Runnable() {
                public void run() {
                    try {
                        slave.start();
                    } catch (ModbusInitException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


            timerRead.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            lockR.lock();
                            try {
                                TaskReadCoil(map01, slave);
                                TaskReadAnalog(map00, m_ai_max, m_ai_min, slave);
                                DataInput(coll);
                            } catch (Exception ex) {
                            } finally {
                                lockR.unlock();
                            }
                        }
                    },
                    0,
                    parmDelay,
                    TimeUnit.MILLISECONDS);

            timerWrite = Executors.newScheduledThreadPool(10);
            timerWrite.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            lockW.lock();
                            try {
                                TaskWriteCoil(map03, collout, slave);
                                TaskWriteAnalog(map02, m_ao_max, m_ao_min, collout, slave);
                            } catch (Exception ex) {
                            } finally {
                                lockW.unlock();
                            }
                        }
                    },
                    0,
                    parmDelay,
                    TimeUnit.MILLISECONDS);

            HeartBeat heat = new HeartBeat(paramIp, paramName, parmDelay, parmHeart);
            heat.startHeart();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    ////Image监听事件
    private class BasicProcessImageListener implements ProcessImageListener {
        public void coilWrite(int offset, boolean oldValue, boolean newValue) {
//            System.out.println("Coil at " + offset + " was set from " + oldValue + " to " + newValue);
        }

        public void holdingRegisterWrite(int offset, short oldValue, short newValue) {
//            System.out.println("HR at " + offset + " was set from " + oldValue + " to " + newValue);
        }


    }

    private BasicProcessImage getModscanProcessImage(int slaveId) {
        BasicProcessImage processImage = new BasicProcessImage(slaveId);
        processImage.setAllowInvalidAddress(true);
        processImage.setInvalidAddressValue(Short.valueOf("0"));
        // Add an image listener.
        //processImage.addListener(new BasicProcessImageListener());
        return processImage;
    }

    ////数据开关量读取
    public static void TaskReadCoil(Map map1, ModbusSlaveSet slave) {
        try {
            Set set1 = map1.entrySet();
            Iterator it1 = set1.iterator();
            while (it1.hasNext()) {
                Map.Entry<String, String> entry2 = (Map.Entry<String, String>) it1.next();
                String name = entry2.getKey().toString();
                String paramAddr = entry2.getValue().toString();
                int fun = (int) (Double.parseDouble(paramAddr));

                if (paramAddr.substring(0, 1).equals("0")) {
                    ///Boolean a = slave.getProcessImage(1).getCoil(fun % 10000);
                    Boolean a = slave.getProcessImage(1).getInput(fun % 10000);
                    m_Coils.put(name, a);
                }

                if (paramAddr.substring(0, 1).equals("1")) {
                    ////Boolean a = slave.getProcessImage(1).getInput(fun % 10000);
                    Boolean a = slave.getProcessImage(1).getCoil(fun % 10000);
                    m_Coils.put(name, a);
                }
            }
        } catch (Exception ex) {
            System.out.print("Read Coils");
            ex.printStackTrace();
        }
    }

    ////读取模拟量
    public static void TaskReadAnalog(Map map1, Map m_max, Map m_min, ModbusSlaveSet slave) {
        try {
            Set set1 = map1.entrySet();
            Iterator it1 = set1.iterator();
            while (it1.hasNext()) {
                Map.Entry<String, String> entry2 = (Map.Entry<String, String>) it1.next();
                String name = entry2.getKey().toString();
                String paramAddr = entry2.getValue().toString();
                int fun = (int) (Double.parseDouble(paramAddr));
                if (paramAddr.substring(0, 1).equals("4")) {
                    double d3 = slave.getProcessImage(1).getHoldingRegister(fun % 10000);
                    double dmax = (Double) m_max.get(name);
                    double dmin = (Double) m_min.get(name);
                    double dValue = 0;
                    if (d3 >= 0) {
                        dValue = dmax * d3 / 32000;
                    } else {
                        dValue = -1 * dmin * d3 / 32000;
                    }
                    m_Analog.put(name, dValue);
                }

                if (paramAddr.substring(0, 1).equals("3")) {
                    double d3 = slave.getProcessImage(1).getInputRegister(fun % 10000);
                    double dmax = (Double) m_max.get(name);
                    double dmin = (Double) m_min.get(name);
                    double dValue = 0;
                    if (d3 >= 0) {
                        dValue = dmax * d3 / 32000;
                    } else {
                        dValue = -1 * dmin * d3 / 32000;
                    }
                    m_Analog.put(name, dValue);
                }
            }
        } catch (Exception ex) {
            System.out.print("Read Analog");
            ex.printStackTrace();
        }
    }

    ////保存数据
    private static void DataInput(DBCollection coll) {
        Calendar calendar = Calendar.getInstance();
        Date dd = calendar.getTime();
        BasicDBObject doc = new BasicDBObject();
        doc.put("_id", dd);

        Set set = m_Analog.entrySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) it.next();
            doc.put(entry1.getKey(), entry1.getValue());
        }
        Set set1 = m_Coils.entrySet();
        Iterator it1 = set1.iterator();
        while (it1.hasNext()) {
            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) it1.next();
            doc.put(entry1.getKey(), entry1.getValue());
        }
        coll.insert(doc);
    }

    ////开关量数据回写
    public static void TaskWriteCoil(Map map1, DBCollection coll, ModbusSlaveSet slave) {
        try {
            Calendar calener = Calendar.getInstance();
            Date d1 = calener.getTime();
            Date d2 = new Date(calener.getTime().getTime() - 1000);
            BasicDBObject b2 = new BasicDBObject();
            b2.put("$gte", d2);
            b2.put("$lte", d1);
            DBCursor cursor = coll.find(new BasicDBObject("_id", b2)).sort(new BasicDBObject("_id", -1)).limit(1);
            while (cursor.hasNext()) {
                DBObject dbo = cursor.next();
                Set set1 = map1.entrySet();
                Iterator it1 = set1.iterator();
                while (it1.hasNext()) {
                    Map.Entry<String, String> entry2 = (Map.Entry<String, String>) it1.next();
                    String name = entry2.getKey().toString();
                    String paramAddr = entry2.getValue().toString();
                    int fun = (int) (Double.parseDouble(paramAddr));
                    if (paramAddr.substring(0, 1).equals("0")) {
                        if (dbo.keySet().contains(name)) {
                            String value = dbo.get(name).toString();
                            ///slave.getProcessImage(1).setCoil(fun % 10000, Boolean.valueOf(value));       ////实际对应
                            slave.getProcessImage(1).setInput(fun % 10000, Boolean.valueOf(value));      ///临时
                        }
                    }

                    if (paramAddr.substring(0, 1).equals("1")) {
                        if (dbo.keySet().contains(name)) {
                            String value = dbo.get(name).toString();
                            //slave.getProcessImage(1).setInput(fun % 10000, Boolean.valueOf(value));      ///实际
                            slave.getProcessImage(1).setCoil(fun % 10000, Boolean.valueOf(value));            ////临时
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.print("Write Coil");
            System.out.println(ex.getMessage());
        }
    }

    ////模拟量数据回写
    public static void TaskWriteAnalog(Map map1, Map m_max, Map m_min, DBCollection coll, ModbusSlaveSet slave) {
        try {
            Calendar calener = Calendar.getInstance();
            Date d1 = calener.getTime();
            Date d2 = new Date(calener.getTime().getTime() - 2000);
            BasicDBObject b2 = new BasicDBObject();
            b2.put("$gte", d2);
            b2.put("$lte", d1);
            DBCursor cursor = coll.find(new BasicDBObject("_id", b2)).sort(new BasicDBObject("_id", -1)).limit(1);
            while (cursor.hasNext()) {
                DBObject dbo = cursor.next();
                Set set1 = map1.entrySet();
                Iterator it1 = set1.iterator();
                while (it1.hasNext()) {
                    Map.Entry<String, String> entry2 = (Map.Entry<String, String>) it1.next();
                    String name = entry2.getKey().toString();
                    String paramAddr = entry2.getValue().toString();
                    int fun = (int) (Double.parseDouble(paramAddr));
                    if (paramAddr.substring(0, 1).equals("4")) {
                        if (dbo.keySet().contains(name)) {
                            String s1 = dbo.get(name).toString();
                            double value = Double.parseDouble(s1);
                            double dmax = (Double) m_max.get(name);
                            double dmin = (Double) m_min.get(name);
                            double dValue = 0;
                            if (value >= 0) {
                                dValue = 32000 * (value / dmax);
                            }
                            if (value < 0) {
                                dValue = -32000 * (value / dmin);
                            }
                            BigDecimal b1 = new BigDecimal(dValue).setScale(0, BigDecimal.ROUND_HALF_UP);     ////四舍五入保留整数，
                            dValue = b1.doubleValue();
                            slave.getProcessImage(1).setHoldingRegister(fun % 10000, (short) dValue);
                        }
                    }

                    if (paramAddr.substring(0, 1).equals("3")) {
                        if (dbo.keySet().contains(name)) {
                            String s1 = dbo.get(name).toString();
                            double value = Double.parseDouble(s1);
                            double dmax = (Double) m_max.get(name);
                            double dmin = (Double) m_min.get(name);
                            double dValue = 0;
                            if (value >= 0) {
                                dValue = 32000 * (value / dmax);
                            }
                            if (value < 0) {
                                dValue = -32000 * (value / dmin);
                            }
                            BigDecimal b1 = new BigDecimal(dValue).setScale(0, BigDecimal.ROUND_HALF_UP);     ////四舍五入保留整数，
                            dValue = b1.doubleValue();
                            slave.getProcessImage(1).setInputRegister(fun % 10000, (short) dValue);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.print("Write Analog");
            System.out.println(ex.getMessage());
        }
    }


}

