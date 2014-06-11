import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.Vector;

public class ExcelExporter {
    public ExcelExporter() {
    }

    public void exportTable(JTable table, File file) throws IOException {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        FileWriter out = new FileWriter(file);
        for (int i = 0; i < model.getColumnCount(); i++) {
            out.write(model.getColumnName(i) + "\t");
        }
        out.write("\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                out.write(model.getValueAt(i, j).toString() + "\t");
            }
            out.write("\n");
        }
        out.close();
        System.out.println("write out to: " + file);
    }

    static class GetInfo {
        private Vector<Vector<String>> stuInfo = null;
        private Vector<String> head = null;

        public GetInfo(File file) {

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String info = null;

            try {
                String[] str = br.readLine().split(",");
                head = new Vector<String>();
                for (int i = 0; i < str.length; i++) {
                    head.add(str[i]);
                }
                info = br.readLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            stuInfo = new Vector<Vector<String>>();
            while (info != null) {
                String[] param = info.split(",");
                Vector<String> temp = new Vector<String>();
                for (int i = 0; i < param.length; i++) {
                    temp.add(param[i]);
                }
                stuInfo.add(temp);
                try {
                    info = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        public Vector<Vector<String>> getStuInfo() {
            return stuInfo;
        }

        public Vector<String> getHead() {
            return head;
        }
    }
}
