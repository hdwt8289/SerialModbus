import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterInit {
    protected HashMap sections = new HashMap();
    private transient String currentSecion;
    private transient Properties current;
    private String iniPath = "";

    public ParameterInit(String filename) throws IOException {
        iniPath = filename;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(
//                new FileInputStream(filename), "GBK"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(filename), "GBK"));

        read(reader);
        reader.close();
    }

    protected void read(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            parseLine(line);
        }

    }

    protected void parseLine(String line) {
        line = line.trim();
        if (line.matches("\\[.*\\]")) {
            currentSecion = line.replaceFirst("\\[(.*)\\]", "$1");
            current = new Properties();

        } else if (line.matches(".*=.*")) {
            int i = line.indexOf('=');
            String name = line.substring(0, i);
            String value = line.substring(i + 1);

            current.setProperty(name, value);
            sections.put(currentSecion, current);
        }
    }

    public String getValue(String section, String name) {
        Properties p = (Properties) sections.get(section);
        if (p == null) {
            return null;
        }
        String value = p.getProperty(name);
        return value;
    }

    ////修改ini文件
    public boolean setValue(String section, String variable, String value) throws IOException {
        String fileContent, allLine;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(iniPath));
        boolean isInSection = false;
        fileContent = "";
        try {
            while ((allLine = bufferedReader.readLine()) != null) {
                allLine = allLine.trim();
                Pattern p;
                Matcher m, m2;
                p = Pattern.compile("\\[\\s*" + section + "\\s*\\]");
                Pattern p2 = Pattern.compile("\\[.*\\]");
                m = p.matcher((allLine));
                m2 = p2.matcher((allLine));
                if (m2.find()) {
                    if (m.find()) {
                        isInSection = true;
                    } else {
                        isInSection = false;
                    }
                }
                if (isInSection == true) {
                    String[] strArray = allLine.split("=");
                    if (allLine.indexOf("=") > 0) {
                        String valueString = strArray[0].trim();
                        if (valueString.equalsIgnoreCase(variable)) {
                            String newLine = valueString + "=" + value;
                            fileContent += "    " + newLine + "\r\n";
                        } else {
                            fileContent += "    " + allLine + "\r\n";
                        }
                    } else {
                        fileContent += allLine + "\r\n";
                    }
                } else {
                    if (allLine.indexOf("=") > 0) {
                        fileContent += "    " + allLine + "\r\n";
                    } else {
                        fileContent += allLine + "\r\n";
                    }
                }
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(iniPath, false));
            bufferedWriter.write(fileContent);
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            bufferedReader.close();
        }
        return false;
    }
}
