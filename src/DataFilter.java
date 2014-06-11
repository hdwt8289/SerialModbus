import java.io.File;
import java.io.FileFilter;

/*设置文档类型*/
public class DataFilter extends javax.swing.filechooser.FileFilter {
    String ext;

    public DataFilter(String ext) {
        this.ext = ext;
    }

    /*在accept()方法中,当程序所抓到的是一个目录而不是文件时,我们返回true值,表示将此目录显示出来.*/
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(ext)) {
            return true;
        }
        return false;
    }

    //实现getDescription()方法,返回描述文件的说明字符串!!!
    public String getDescription() {
        if (ext.equals("txt"))
            return "TXT File(*.txt)";
        if (ext.equals("csv"))
            return "CSV File(*.csv)";
        if (ext.equals("xls"))
            return "XLS File(*.xls)";
        if (ext.equals("xlsx"))
            return "XLSX File(*.xlsx)";
        if (ext.equals("xml"))
            return "XML(*.xml)";
        return "";
    }
}
