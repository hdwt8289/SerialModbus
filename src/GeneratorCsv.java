import com.mongodb.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GeneratorCsv {
    static Mongo m = null;
    static DB db = null;
    static DBCollection meta = null;
    static DBCollection coll = null;
    static String header = "_id,";
    static String strIp = "";
    static String strName = "";
    static Vector v_colName = new Vector();

    public GeneratorCsv(String ip, String parName) {
        strIp = ip;
        strName = parName;
    }

    ///开始执行
    public void Start() {
        try {
            m = new Mongo(strIp, 27017);
            db = m.getDB(strName);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
        coll = db.getCollection("DATAOUT");
        meta = db.getCollection("META");
        DBCursor cursor = meta.find();
        v_colName.add("_id");
        while (cursor.hasNext()) {
            DBObject dbo = cursor.next();
            String name = dbo.get("_id").toString();
            v_colName.add(name);
            header += name + ",";
        }
        header = header.substring(0, header.length() - 1);
        header += "\r\n";
        System.out.print(header);
        //GeneratorNow();
        ///开始定时执行
        showTimer();

    }

    ////开始循环
    private void showTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                Generator();
            }
        };


        //设置执行时间
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
        //定制每天的24:00:00执行，
        calendar.set(year, month, day, 24, 00, 00);
        Date date = calendar.getTime();
        Timer timer = new Timer();
        System.out.println(date);

        int period = 2 * 1000;
        //每天的date时刻执行task，每隔2秒重复执行
        //timer.schedule(task, date, period);
        //每天的date时刻执行task, 仅执行一次
        timer.schedule(task, date);
    }

    ////每天生成
    private void Generator() {
        try {
            //设置执行时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
            String csvName = "" + year + "" + month + "" + day;
            final FileWriter fw = new FileWriter("D:/csv/" + csvName + ".csv");
            fw.write(header);
            final ScheduledExecutorService timerRead = Executors.newScheduledThreadPool(1);
            timerRead.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            try {
                                StringBuffer strbuffer = TaskRead(v_colName);
                                fw.write(strbuffer.toString());
                                fw.flush();
                            } catch (IOException ex) {
                            }
                        }
                    },
                    0,
                    1000,
                    TimeUnit.MILLISECONDS);

            timerRead.schedule(new Runnable() {
                public void run() {
                    timerRead.shutdownNow();
                }
            }, 3600 * 24, TimeUnit.SECONDS);
            if (timerRead.isShutdown()) {
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    ///从系统开始时间生成
    private void GeneratorNow() {
        try {
            //设置执行时间
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
            int hour = calendar.get(Calendar.HOUR_OF_DAY);//每天


            // int seconds=calendar.
            String csvName = "" + year + "" + month + "" + day + "" + hour;
            final FileWriter fw = new FileWriter("D:/csv/" + csvName + ".csv");
            fw.write(header);
            final ScheduledExecutorService timerRead = Executors.newScheduledThreadPool(1);
            timerRead.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            try {
                                StringBuffer strbuffer = TaskRead(v_colName);
                                fw.write(strbuffer.toString());
                                fw.flush();
                            } catch (IOException ex) {
                            }
                        }
                    },
                    0,
                    1000,
                    TimeUnit.MILLISECONDS);

            timerRead.schedule(new Runnable() {
                public void run() {
                    timerRead.shutdownNow();
                    try {
                        fw.close();
                    } catch (IOException ex) {
                    }
                }
            }, 30, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ////定时读取数据库中数据
    private StringBuffer TaskRead(Vector v_col) {
        StringBuffer strBuffer = new StringBuffer();
        try {

            Calendar calener = Calendar.getInstance();
            Date d1 = calener.getTime();
            Date d2 = new Date(calener.getTime().getTime() - 1000);
            BasicDBObject b2 = new BasicDBObject();
            b2.put("$gte", d2);
            b2.put("$lte", d1);
            DBCursor cursor = coll.find(new BasicDBObject("_id", b2)).sort(new BasicDBObject("_id", -1));
            String sValue = "";
            while (cursor.hasNext()) {
                DBObject dbo = cursor.next();
                int count = v_col.size();
                for (int i = 0; i < count; i++) {
                    String name = v_col.get(i).toString();
                    String value = "";
                    if (dbo.keySet().contains(name)) {
                        value = dbo.get(name).toString();
                    } else {
                        value = "0";
                    }
                    sValue += value + ",";
                }
                sValue = sValue.substring(0, sValue.length() - 1) + "\r\n";
                strBuffer.append(sValue);
            }
            return strBuffer;

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}

