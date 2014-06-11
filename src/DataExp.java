import com.csvreader.CsvWriter;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Vector;
import java.util.Iterator;


public class DataExp {

    /*导出为xls数据*/
    public void DataExportXls(File file, JTable jtable) {
        try {
            ExcelExporter exp = new ExcelExporter(); //获得实例对象，用来导出数据
            exp.exportTable(jtable, file);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /*导出为xml数据*/
    public void DataExportXml(String file, JTable jTable) {
        TableModel model = (TableModel) jTable.getModel();
        Document doc = DocumentHelper.createDocument();
        Element root = DocumentHelper.createElement("table");
        doc.setRootElement(root);
        Element columns = DocumentHelper.createElement("columns");
        for (int i = 0; i < model.getColumnCount(); i++) {
            Element column = DocumentHelper.createElement("column");
            column.addAttribute("name", model.getColumnName(i));
            column.addAttribute("type", model.getColumnClass(i).getName());
            columns.add(column);
        }
        root.add(columns);
        for (int i = 0; i < model.getRowCount(); i++) {
            Element tr = DocumentHelper.createElement("tr");
            for (int j = 0; j < model.getColumnCount(); j++) {
                Element td = DocumentHelper.createElement("td");
                td.setText(model.getValueAt(i, j).toString());
                tr.add(td);
            }
            root.add(tr);
        }
        saveDocument(doc, file);
    }

    ///xml保存数据函数
    private void saveDocument(Document doc, String name) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        try {
            XMLWriter writer = new XMLWriter(format);
            FileOutputStream fos = new FileOutputStream(name);
            writer.setOutputStream(fos);
            writer.write(doc);
            fos.close();
        } catch (UnsupportedEncodingException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*导出为csv数据*/
    public void DataExportCsv(String fileName, JTable jTable) {
        DefaultTableModel model = (DefaultTableModel) jTable.getModel();
        try {
            CsvWriter writer = new CsvWriter(fileName, ',', Charset.forName("UTF-8"));
            int cols = model.getColumnCount();
            for (int i = 0; i < cols; i++) {
                writer.write(model.getColumnName(i));
            }
            writer.endRecord();
            Vector rows = model.getDataVector();
            for (Iterator it = rows.iterator(); it.hasNext(); ) {
                Vector v = (Vector) it.next();

                for (int j = 0; j < cols; j++) {
                    writer.write(v.get(j).toString());
                }
                writer.endRecord();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
