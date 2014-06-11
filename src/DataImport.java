/*import jxl.*;
import jxl.biff.EmptyCell;
import jxl.read.biff.BiffException;*/

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.*;
import java.util.Vector;

public class DataImport {
    /*读取csv文件*/
    public JTable ReadCSV(File file) {
        TableModel dataInfo = getFileStats(file);
        JTable jt = new JTable(dataInfo);
        jt.setPreferredScrollableViewportSize(new Dimension(400, 80));
        return jt;
    }

    private TableModel getFileStats(File a) {
        String data;
        Object[] object = null;
        int columnCount = 0;//文件中最大行的列数
        DefaultTableModel dt = new DefaultTableModel();
        try {
            BufferedReader br = new BufferedReader(new FileReader(a));
            //不是文件尾一直读
            while ((data = br.readLine()) != null) {
                object = data.split(",");
                //如果这行的列数大于最大的，那么再增加一列
                if (object.length > columnCount) {
                    for (int i = 0; i < object.length - columnCount; i++) {
                        dt.addColumn("column".concat(String.valueOf(i)));
                    }
                    columnCount = object.length;
                }
                //添加一行
                dt.addRow(object);
            }
            ;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dt;
    }

    /*读取excel文件*/
    // public JTable ReadExcel(File file) {
    /*  Workbook rwb = null;
    // DefaultTableModel tableModel = new DefaultTableModel();
    JTable jTable;
    try {
        FileInputStream is = new FileInputStream(file);
        //创建一个workbook类读取excel文件
        rwb = Workbook.getWorkbook(is);
        //得到第i个工作薄
        Sheet st = rwb.getSheet(0);//这里有两种方法获取sheet表,1为名字，而为下标，从0开始
        int rowCount = st.getRows();
        int colCount = st.getColumns();
        Vector<Vector<String>> V_2 = new Vector<Vector<String>>();
        Vector<String> V_3 = new Vector<String>();
        for (int i0 = 0; i0 < colCount; i0++) {
            V_3.add("column" + i0);
        }
        for (int i = 0; i < rowCount; i++) {
            //列循环
            Object[] object = new Object[colCount];
            Vector<String> V_1 = new Vector<String>();
            for (int j = 0; j < colCount; j++) {
                //		得到第j列第i行的数据
                Cell c00 = st.getCell(j, i);
                if (c00.getType() == CellType.LABEL) {
                    LabelCell labelc00 = (LabelCell) c00;
                    //object[j] = labelc00.getString();
                    V_1.add(labelc00.getString());
                } else if (c00.getType() == CellType.NUMBER) {
                    NumberCell numc10 = (NumberCell) c00;
                    Double numd = new Double(numc10.getValue());
                    //object[j] = numd;
                    V_1.add(numd.toString());
                } else if (c00.getType() == CellType.DATE) {
                    DateCell dc = (DateCell) c00;
                    //object[j] = dc.getDate();
                    V_1.add(dc.getContents().toString());
                } else if (c00.getType() == CellType.EMPTY) {
                    EmptyCell ec = (EmptyCell) c00;
                    //object[j]="null";
                    V_1.add("null");
                }
            }
            V_2.add(V_1);
        }
        DefaultTableModel tableModel = new DefaultTableModel(V_2, V_3);
        jTable = new JTable(tableModel);

    } catch (Exception e) {
        System.out.println(e.toString());
        jTable = new JTable();
    } finally {
        rwb.close();
    }*/
    //  JTable jTable=new JTable();
    //  return jTable;
    // }

    /*读取Excel文件*/
    public JTable ReadExcel(File file) {
        JTable jTable;
        Workbook wb = null;
        String strPath = file.getPath();
        if (strPath.toLowerCase().endsWith(".xls")) {
            FileInputStream is = null;
            POIFSFileSystem fs = null;
            try {
                is = new FileInputStream(file);
                fs = new POIFSFileSystem(is);
                wb = new HSSFWorkbook(fs);
                is.close();
            } catch (IOException e) {
                System.out.println("读取文件出错");
                e.printStackTrace();
            }
        } else if (strPath.toLowerCase().endsWith(".xlsx")) {
            try {
                wb = new XSSFWorkbook(strPath);
            } catch (IOException e) {
                System.out.println("读取文件出错");
                e.printStackTrace();
            }
        }
        Vector<Vector<String>> V_2 = new Vector<Vector<String>>();
        Vector<String> V_3 = new Vector<String>();

        try {
            for (int k = 0; k < wb.getNumberOfSheets(); k++) {
                //sheet
                Sheet sheet = wb.getSheetAt(k);
//                /for(int i0=0;i0<sheet.)
                int rows = sheet.getPhysicalNumberOfRows();
                for (int r = 0; r < rows; r++) {
                    // 定义 row
                    Row row = sheet.getRow(r);
                    Vector<String> V_1 = new Vector<String>();
                    if (row != null) {
                        int cells = row.getPhysicalNumberOfCells();
                        for (short c = 0; c < cells; c++) {
                            Cell cell = row.getCell(c);
                            if (cell != null) {
                                String value = null;
                                ///正常使用
                                /* switch (cell.getCellType()) {
                                                                    case Cell.CELL_TYPE_FORMULA:
                                                                        value = cell.getCellFormula();
                                                                        break;
                                                                    case Cell.CELL_TYPE_NUMERIC:
                                                                        if(HSSFDateUtil.isCellDateFormatted(cell)){
                                                                            value = cell.getDateCellValue().toString();
                                                                        }else{
                                                                            value = String.valueOf(cell.getNumericCellValue());
                                                                        }
                                                                        break;
                                                                    case Cell.CELL_TYPE_STRING:
                                                                        value =  cell.getStringCellValue();

                                                                        break;
                                                                    case Cell.CELL_TYPE_BOOLEAN:
                                                                        value = String.valueOf(cell.getBooleanCellValue());
                                                                        break;
                                                                    default:
                                                                }
                                                                V_1.add(value);
                                */                              ///测试专用
                                value = cell.getStringCellValue();
                                String[] arr = value.split("  ");
                                for (int j = 0; j < arr.length; j++) {
                                    String temp = arr[j];
                                    if ((!temp.isEmpty()) && (temp != null)) {
                                        V_1.add(temp);
                                    }
                                }
                            }
                        }
                    }
                    V_2.add(V_1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i0 = 0; i0 < V_2.get(0).size(); i0++) {
            V_3.add("column" + i0);
        }
        DefaultTableModel tableModel = new DefaultTableModel(V_2, V_3);
        jTable = new JTable(tableModel);
        return jTable;
    }


    /*读取txt文件*/
    public JTable ReadTxt(File file) {
        ExcelExporter.GetInfo getTask = new ExcelExporter.GetInfo(file);
        Vector<Vector<String>> stuInfo = getTask.getStuInfo();
        Vector<String> head = getTask.getHead();
        DefaultTableModel tableModel = new DefaultTableModel(stuInfo, head);
        JTable jTable = new JTable(tableModel);
        return jTable;
    }

}
