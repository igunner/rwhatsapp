package ru.skywing.readwhatsapp.readwhatsapproot;

import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class RootUtil extends Thread {

    private static boolean bCanSu = false;

    private final static String TAG = "readwhatsapp";

    private final static int COMMAND_ANSWER_WAIT_TIME = 15; //seconds

    public static void FLOG(String s)
    {
        Log.v(TAG, s);
    }

    public RootUtil() {

    }

    public void CheckSU() {
        start();
    }

    public static boolean Can()
    {
        return bCanSu;
    }

    @Override
    public void run() {
        Looper.prepare();
        bCanSu = checkSU();
    }

    private boolean checkSU() {
        FLOG("CheckSU");
        String id = commandSUresult("id");
        if (id.indexOf("uid=0(root)") > -1) {
            FLOG("We have ROOT!");
            return true;
        }
        FLOG("We don't have root ^(");
        return false;
    }

    public static String commandSUresult(String command)
    {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;
        InputStreamReader isr = null;

        String ret = "";

        FLOG("su " + command);

        try {

            proc = runtime.exec("su");

            osw = new OutputStreamWriter(proc.getOutputStream());

            osw.write(command);

            osw.flush();

            osw.close();

            char buffer[] = new char[1024];
            int size = 0;
            isr = new InputStreamReader(proc.getInputStream());
            while ((size = isr.read(buffer)) > 0) {
                String red = new String(buffer);
                ret += red;
            }
        } catch (Exception ex) {
            ret = "";
        } finally {
            if (osw != null) {
                try {
                    osw.close();

                } catch (IOException e) {

                }
            }
            if (isr != null) {
                try {
                    isr.close();

                } catch (IOException e) {

                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();

        } catch (InterruptedException e) {
            ret = "";
        }
        return ret;
    }

    public static boolean commandSU(String command) {
        FLOG(command);

        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;

        boolean ret = false;

        try {
            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
            osw.write(command);
            osw.flush();
            osw.close();
            ret = true;
        } catch (Exception ex) {

        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {

                }
            }
        }

        try {
            if (proc != null) {
                int i = COMMAND_ANSWER_WAIT_TIME * 2;
                while (i-- > 0) {
                    try {
                        proc.exitValue();
                        break;
                    } catch (IllegalThreadStateException e)
                    {
                    }
                    Thread.sleep(500);
                }
                if (i <= 0) proc.destroy();
            }

        } catch (InterruptedException e) {
            ret = false;
        }
        return ret;
    }
}
