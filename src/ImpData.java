import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import javax.swing.*;
import java.io.File;
import java.net.UnknownHostException;


public class ImpData {
    public void StartImp(String strProName, String strIp) {
        if (!strProName.equals("")) {
            JTable jtable = new JTable();
            JFileChooser fileChooser = new JFileChooser("D:\\Java");
            fileChooser.addChoosableFileFilter(new DataFilter("txt"));
            fileChooser.addChoosableFileFilter(new DataFilter("csv"));
            fileChooser.addChoosableFileFilter(new DataFilter("xls"));
            fileChooser.addChoosableFileFilter(new DataFilter("xlsx"));
            int result = fileChooser.showOpenDialog(ParamerSet.f);
            File file;
            if (result == JFileChooser.APPROVE_OPTION) {
                Mongo m1 = null;
                DB dd = null;
                try {
                    m1 = new Mongo(strIp, 27017);
                    if (m1.getDatabaseNames().contains(strProName)) {
                        dd = m1.getDB(strProName);
                    } else {
                        dd = m1.getDB(strProName);
                        JOptionPane.showConfirmDialog(null, "该工程不存在");
                    }
                } catch (UnknownHostException ex) {
                    ex.printStackTrace();
                }


                file = fileChooser.getSelectedFile();
                String sType = file.getAbsolutePath();
                DataImport di = new DataImport();
                ///csv文件
                if (sType.toLowerCase().endsWith(".csv")) {
                    jtable = di.ReadCSV(file);
                }
                //xls文件
                if (sType.toLowerCase().endsWith(".xls")) {
                    jtable = di.ReadExcel(file);
                }
                //xlsx文件
                if (sType.toLowerCase().endsWith(".xlsx")) {
                    jtable = di.ReadExcel(file);
                }
                ///txt文件
                if (sType.toLowerCase().endsWith(".txt")) {
                    jtable = di.ReadTxt(file);
                }
                if (jtable.getRowCount() > 0) {
                    DBCollection meta = dd.getCollection("META");
                    meta.drop();

                    BasicDBObject doc = new BasicDBObject();
                    int row = jtable.getRowCount();
                    int col = jtable.getColumnCount();
                    String[] colName = new String[col];

                    for (int j0 = 0; j0 < col; j0++) {
                        colName[j0] = jtable.getModel().getValueAt(0, j0).toString();
                    }
                    String sValue = "";
                    for (int i = 1; i < row; i++) {
                        for (int j = 0; j < col; j++) {
                            sValue = jtable.getModel().getValueAt(i, j).toString();
                            doc.put(colName[j], sValue);
                        }
                        meta.insert(doc);
                    }

                    m1.close();

                    JOptionPane.showMessageDialog(null, "点表导入成功！");
                } else {
                    JOptionPane.showMessageDialog(null, "导入不成功,请检查点表文件！");
                }
            }
        } else {
            Mongo m1 = null;
            try {
                m1 = new Mongo(strIp, 27017);
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
            Object[] options = m1.getDatabaseNames().toArray();
            m1.close();

            strProName = (String) JOptionPane.showInputDialog(null, "请选择", "滤波", 1, null, options, options[0]);
            if (strProName != null) {
                StartImp(strProName, strIp);
            }
            strProName = "";
        }
    }
}
