import com.mongodb.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;


public class Slave {
    private static Map m_parms = new HashMap();

    public static void main(String[] args) throws Exception {
        try {
            //在Windows系统中，可以实现Swing界面跟Windows的GUI界面相同
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            try {
                InetAddress addr = InetAddress.getLocalHost();
                String ip = addr.getHostAddress().toString();
//                    System.out.print("Do you want to start command line mode?(y/n)");
//                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//                    String command = new String(in.readLine());
//                    if (command.equals("y")) {
//                        m_parms = CommandMode();
//
//                        System.out.println("please intput command!");
//                        BufferedReader inRead = new BufferedReader(new InputStreamReader(System.in));
//                        String strShow = new String(inRead.readLine());
//                        while (!strShow.equals("stop")) {
//                            Operation(strShow);
//                            System.out.println("please intput command!");
//                            BufferedReader inCommand = new BufferedReader(new InputStreamReader(System.in));
//                            strShow = new String(inCommand.readLine());
//                        }
//                    } else if (command.equals("n")) {
                ParamerSet parmSet = new ParamerSet();
                parmSet.setStrIp(ip);
                parmSet.setFrame();
                // }
            } catch (Exception ex) {
            }
        } catch (Exception ex) {
        }

    }

    ////根据配置文件获取通讯信息
    private static Map CommandMode() {
        Map m1 = new HashMap();
        try {
            ParameterInit ini = new ParameterInit("Parameter.ini");

            String ip = ini.getValue("IpAddress", "IpAddress");
            m1.put("IpAddress", ip);
            String strPort = ini.getValue("Port", "Port");
            m1.put("Port", strPort);
            String strProName = ini.getValue("ProjectName", "ProjectName");
            m1.put("ProjectName", strProName);
            String strBaud = ini.getValue("BaudRate", "BaudRate");
            m1.put("BaudRate", strBaud);
            String strData = ini.getValue("DataLength", "DataLength");
            m1.put("DataLength", strData);
            String strCheck = ini.getValue("CheckCode", "CheckCode");
            m1.put("CheckCode", strCheck);
            String strStop = ini.getValue("StopBit", "StopBit");
            m1.put("StopBit", strStop);
            String strTimer = ini.getValue("TimerInterval", "TimerInterval");
            m1.put("TimerInterval", strTimer);
            String strHeart = ini.getValue("HeartBit", "HeartBit");
            m1.put("HeartBit", strHeart);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return m1;
    }

    ////显示配置信息
    private static void ShowStatus() {
        Set set1 = m_parms.entrySet();
        Iterator it1 = set1.iterator();
        while (it1.hasNext()) {
            Map.Entry<String, String> entry1 = (Map.Entry<String, String>) it1.next();
            String key = entry1.getKey().toString();
            String value = entry1.getValue().toString();
            System.out.println(key + ":" + value);
        }
    }

    ////操作命令
    private static void Operation(String strShow) {
        String[] arrCommand = strShow.split(" ");
        strShow = arrCommand[0];
        String strKey = "";
        String strValue = "";
        if (arrCommand.length == 3) {
            strKey = arrCommand[1];
            strValue = arrCommand[2];
        }
        if (strShow.equals("show")) {
            ShowStatus();
        }
        if (strShow.equals("start")) {
            Start();
        }
        if (strShow.equals("update")) {
            Update(strKey, strValue);
        }


    }

    /////start
    private static void Start() {
        String paramIp = m_parms.get("IpAddress").toString();
        String paramName = m_parms.get("ProjectName").toString();
        String paramNo = m_parms.get("Port").toString();
        int paramBote = Integer.parseInt(m_parms.get("BaudRate").toString());
        int parmLength = Integer.parseInt(m_parms.get("DataLength").toString());
        int parmParity = Integer.parseInt(m_parms.get("CheckCode").toString());
        int parmStopBit = Integer.parseInt(m_parms.get("StopBit").toString());
        int parmDelay = Integer.parseInt(m_parms.get("TimerInterval").toString());
        String parmHeart = m_parms.get("HeartBit").toString();

        ParamerSet parmSet = new ParamerSet();
        parmSet.receiveData(paramIp, paramName, paramNo, paramBote, parmLength, parmParity, parmStopBit, parmDelay, parmHeart);
    }

    ////update
    private static void Update(String strKey, String strValue) {
        if (strKey != "" && strValue != "") {
            if (m_parms.containsKey(strKey)) {
                m_parms.remove(strKey);
                m_parms.put(strKey, strValue);
            }
        } else {
            System.out.println("please intput(key&value):");
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                String strinput = new String(input.readLine());
                String[] arr_value = strinput.split(" ");
                strKey = arr_value[0];
                strValue = arr_value[1];
                if (m_parms.containsKey(strKey)) {
                    m_parms.remove(strKey);
                    m_parms.put(strKey, strValue);
                }
            } catch (Exception ex) {
            }
        }
        try {
            String file = "src/Parameter.ini";
            ParameterInit ini = new ParameterInit(file);
            ini.setValue(strKey, strKey, strValue);
        } catch (Exception ex) {
        }

        System.out.println("Ok!");
    }

    ///获取机器的mac地址
    private static String getMACAddress(InetAddress ia) throws Exception {
        //获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
        byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        //下面代码是把mac地址拼装成String
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //mac[i] & 0xFF 是为了把byte转化为正整数
            String s = Integer.toHexString(mac[i] & 0xFF);
            sb.append(s.length() == 1 ? 0 + s : s);
        }
        //把字符串所有小写字母改为大写成为正规的mac地址并返回
        return sb.toString().toUpperCase();
    }
}
