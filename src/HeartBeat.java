import com.mongodb.*;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HeartBeat {
    private ScheduledExecutorService timerRead;
    private String[] arrValueCheck;
    static Mongo m = null;
    static DB db = null;
    public static int parmDelay = 0;
    public static String parmHeart;
    private static Date d11;
    private ReentrantLock lock = new ReentrantLock();///线程锁
    private int count = 0;
    private String[] arrHeart;


    public HeartBeat() {
    }

    public HeartBeat(String ip, String paramName, int parmDelay, String parmHeart) {
        this.parmDelay = parmDelay;
        count = 3000 / parmDelay;
        arrValueCheck = new String[count];
        switch (count) {
            case 3:
                arrHeart = new String[]{"true", "false", "true"};
                break;
            case 6:
                arrHeart = new String[]{"true", "true", "false", "false", "true", "true"};
                break;
            case 12:
                arrHeart = new String[]{"true", "true", "true", "true", "false", "false", "false", "false", "true", "true", "true", "true"};
                break;
            default:
                break;
        }
        this.parmHeart = parmHeart;
        try {
            m = new Mongo(ip, 27017);
            db = m.getDB(paramName);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }

    public void startHeart() {
        timerRead = Executors.newScheduledThreadPool(10);
        timerRead.scheduleAtFixedRate(
                new Runnable() {
                    public void run() {

                        ///线程锁方法1
                        lock.lock();
                        try {
                            DataCheck();
                        } catch (Exception ex) {
                        } finally {
                            lock.unlock();
                        }
                    }
                },
                3000,
                parmDelay,
                TimeUnit.MILLISECONDS);
    }

    ////监测三个周期内心跳信号情况
    private boolean DataCheck() {
        boolean isucc = false;
        Calendar calener1 = Calendar.getInstance();
        d11 = calener1.getTime();
        Date d21 = new Date(calener1.getTime().getTime() - 5000);
        BasicDBObject b21 = new BasicDBObject();
        b21.put("$gte", d21);
        b21.put("$lte", d11);
        DBCollection coll = db.getCollection("DATAOUT");
        DBCursor cursor1 = coll.find(new BasicDBObject("_id", b21)).sort(new BasicDBObject("_id", 1)).limit(count);
        int j = 0;
        String value = null;
        while (cursor1.hasNext()) {
            DBObject dbo1 = cursor1.next();
            value = dbo1.get(parmHeart).toString();
            arrValueCheck[j] = value;
            j++;
        }
        boolean iCmp = false;
        int iRep=0;////记录数组移位个数
        int num=arrHeart.length/3;///数组总共移位个数
        while (!iCmp&&iRep<num) {
            for (int i0 = 0; i0 < arrValueCheck.length; i0++) {
                if (arrValueCheck[i0].equals(arrHeart[i0])) {
                    iCmp = true;
                } else {
                    arrHeart = setArr(arrHeart);
                    iRep++;
                    iCmp = false;
                    break;
                }
            }
        }
        if(!iCmp&&iRep<num){
            JOptionPane.showMessageDialog(null,"心跳信号异常！");
        }
        return isucc;
    }

    private String[] setArr(String[] arrHeart) {
        String sval = null;
        sval = arrHeart[0];
        if (sval.equals("true"))
            sval = "false";
        for (int j = 0; j < arrHeart.length - 1; j++) {
            arrHeart[j] = arrHeart[j + 1];
        }
        arrHeart[count - 1] = sval;
        return arrHeart;
    }
}
