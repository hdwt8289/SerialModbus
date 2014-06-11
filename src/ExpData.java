import com.mongodb.*;

import javax.swing.*;
import java.io.File;
import java.net.UnknownHostException;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: Thinkpad
 * Date: 13-9-23
 * Time: 下午3:06
 * To change this template use File | Settings | File Templates.
 */
public class ExpData {
    public void StartExp(String strProName, String strIp) {
        Mongo m1 = null;
        DB dd = null;
        try {
            m1 = new Mongo(strIp, 27017);
            if (!strProName.equals("")) {
                JFileChooser fileChooser = new JFileChooser("D:\\Java");
                fileChooser.addChoosableFileFilter(new DataFilter("xml"));
                fileChooser.addChoosableFileFilter(new DataFilter("csv"));
                fileChooser.addChoosableFileFilter(new DataFilter("xls"));
                int result = fileChooser.showSaveDialog(ParamerSet.f);
                File file;
                if (result == 0) {
                    if (m1.getDatabaseNames().contains(strProName)) {
                        dd = m1.getDB(strProName);
                    } else {
                        dd = m1.getDB(strProName);
                        JOptionPane.showConfirmDialog(null, "该工程不存在");
                    }
                    file = fileChooser.getSelectedFile();
                    ///判断文件名中是否包含文件后缀，如果没有，则需要自动添加
                    if (!file.toString().contains(".")) {
                        String tail = fileChooser.getFileFilter().getDescription();
                        String sa = "";
                        if (tail.contains("(")) {
                            int s1 = tail.indexOf("(");
                            int e1 = tail.indexOf(")");
                            sa = tail.substring(s1 + 2, e1);
                        } else {
                            sa = ".csv";
                        }
                        file = new File(file.toString() + sa);
                    }
                    DBCollection meta = dd.getCollection("META");
                    Vector V_Title = new Vector();
                    Vector V_Table = new Vector();
                    DBCursor cursor = meta.find();
                    DBObject dbo1 = cursor.next();
                    Object[] colName = dbo1.keySet().toArray();
                    for (int i0 = 0; i0 < colName.length; i0++) {
                        V_Title.add(colName[i0].toString().trim());
                    }
                    DBCursor cursor1 = meta.find();
                    while (cursor1.hasNext()) {
                        DBObject dbo = cursor1.next();
                        Vector V_Value = new Vector();
                        for (int i = 0; i < colName.length; i++) {
                            String value = colName[i].toString();
                            if (dbo.keySet().contains(value)) {
                                V_Value.add(dbo.get(value));
                            } else {
                                V_Value.add("");
                            }
                        }
                        V_Table.add(V_Value);
                    }

                    JTable jtable = new JTable(V_Table, V_Title);
                    String sType = file.getAbsolutePath();
                    DataExp de = new DataExp();
                    if (sType.toLowerCase().endsWith(".xls")) {
                        de.DataExportXls(file, jtable);
                    }
                    if (sType.toLowerCase().endsWith(".xml")) {
                        de.DataExportXml(sType, jtable);
                    }
                    if (sType.toLowerCase().endsWith(".csv")) {
                        de.DataExportCsv(sType, jtable);
                    }
                    JOptionPane.showMessageDialog(null, "点表导出成功！");
                }
            } else {
                Object[] options = m1.getDatabaseNames().toArray();
                strProName = (String) JOptionPane.showInputDialog(null, "请选择", "滤波", 1, null, options, options[0]);
                if (strProName != null)
                    StartExp(strProName, strIp);
                strProName = "";
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } finally {
            m1.close();
        }
    }
}
