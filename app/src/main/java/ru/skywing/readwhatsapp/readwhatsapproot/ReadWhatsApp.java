package ru.skywing.readwhatsapp.readwhatsapproot;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ASUS on 25.04.2016.
 */
public class ReadWhatsApp
{
    private final static String TAG = "readwhatsapp";
    private final static long DEFAULT_BLOCK_SIZE = 100;
    private final static int MAGIC_UTF = 0xD800;
    private static String sqlite = null;
    private long last_id = 0;
    private Context context  = null;

    ReadWhatsApp(Context ref) throws NoSuchFieldException
    {
        context = ref;
        if(!RootUtil.Can())
        {
            throw new NoSuchFieldException("No ROOT detected");
        }
        AssetManager assetManager = context.getAssets();
        FLOG("assetManager = " + assetManager);
        InputStream is = null;
        FileOutputStream fos = null;
        try
        {
            if(assetManager == null)
            {
                FLOG("assetManager is null, ret");
                throw new NoSuchFieldException("Assets Not Found");
            }

            String arch = System.getProperty("os.arch");

            if(arch.indexOf("armv7") > -1)
            {
                sqlite = "sqlite3.armv7-pie";
            }
            else
            if(arch.indexOf("armv6") > -1)
            {
                sqlite = "sqlite3.armv6";
            }
            else
                throw new NoSuchFieldException("Unknown processor type" + arch);

            is = assetManager.open(sqlite);

            FLOG(sqlite + " = " + is);

            if(is == null)
            {
                FLOG("No sqlite asset found");
                throw new NoSuchFieldException("Asset file " + sqlite + " Not Found");
            }

            fos = context.openFileOutput(sqlite, Context.MODE_PRIVATE);

            if(fos != null)
            {
                byte [] buffer = new byte[1024];
                int read;
                while((read = is.read(buffer)) != -1)
                {
                    fos.write(buffer, 0, read);
                }
                fos.close();
            }
            else
                throw new NoSuchFieldException("cannot open output stream");
            String localPath = "/data/data/"+context.getPackageName()+"/files/"+sqlite;
            RootUtil.commandSU("chmod 777 " + localPath);
        }
        catch(IOException e)
        {
            FLOG("Prepare sqlite failed");
        }
        finally
        {
            try {
                if (is != null)
                    is.close();
            }
            catch(IOException eio)
            {
                throw new NoSuchFieldException("Error processing input asset file");
            }
            try {
                if (fos != null)
                    fos.close();
            }
            catch(IOException eio)
            {
                throw new NoSuchFieldException("Error processing output file");
            }
        }
    }
    public static void FLOG(String s)
    {
        Log.v(TAG, s);
    }
    public String ReadNextBlock()
    {
        return ReadNextBlock(DEFAULT_BLOCK_SIZE);
    }
    public String ReadNextBlock(long how_much)
    {
        return ReadNextBlock(last_id, how_much);
    }
    public String ReadNextBlock(long start_id, long how_much)
    {
        FLOG("ReadNextBlock(), last="+start_id);
        String retData = "";
        try
        {
            String filesFolder = "files/";
            String appFolderPath = "/data/data/" + context.getPackageName() + "/";
            String sqlite3 = appFolderPath + filesFolder + sqlite;
            String whatsappPath = "/data/data/com.whatsapp/databases/msgstore.db";
            String dumpCommand = sqlite3 + " " + whatsappPath + " -csv \"SELECT _id, key_remote_jid,key_from_me,timestamp/1000,media_url,latitude,longitude,data FROM messages WHERE _id > " + start_id + " LIMIT "+how_much+"\"";
            String suData = RootUtil.commandSUresult(dumpCommand);

            while(true)//once
            {
                if(suData == null && suData.length() < 3)
                {
                    FLOG("Answer is empty");
                    break;
                }
                String [] lines = suData.split("\\r?\\n");
                int lc = lines.length;
                lc--;
                int field_count = 8;
                for(int i = 0; i < lc; i++)
                {
                    String [] fields = lines[i].split("\\,", field_count);
                    if(fields == null || fields.length != field_count)
                    {
                        FLOG("Fields parse error");
                        break;
                    }
                    if(fields[field_count-1].length() > 0 && fields[field_count-1].charAt(0) == '"')
                    {
                        while (i < lc && fields[field_count - 1].charAt(fields[field_count - 1].length() - 1) != '"') {
                            fields[field_count-1] += "\n" + lines[i + 1];
                            i++;
                        }
                        int end = fields[field_count - 1].length();
                        if(end > 1)
                        {
                            fields[field_count - 1] = fields[field_count - 1].substring(1, end - 1);
                        }
                    }

                    int id = Integer.parseInt(fields[0]);
                    String address = fields[1];
                    String list[] = address.split("@");
                    if(list.length > 0)
                        address = list[0];

                    retData += fields[0] + "|";
                    retData += address + "|";
                    retData += fields[2] + "|";
                    retData += fields[3] + "|";
                    retData += fields[4] + "|";
                    retData += fields[5] + "|";
                    retData += fields[6] + "|";

                    String data = "";
                    String cdata = fields[7];
                    if(cdata != null && cdata.length() > 0)
                    {
                        char [] chars = cdata.toCharArray();
                        for(int j = 0; j < chars.length; j++)
                        {
                            if((int)chars[j] >= MAGIC_UTF)
                            {
                                chars[j] = ' ';
                            }
                        }
                        data = new String(chars);
                    }
                    retData += data + "\n";
                    last_id = id;

                }
                break;
            }
        }
        catch(Exception e)
        {
            FLOG("Error processing whats app");
            e.printStackTrace();
        }
        return retData;
    }
    public long getLastID()
    {
        return last_id;
    }
}
