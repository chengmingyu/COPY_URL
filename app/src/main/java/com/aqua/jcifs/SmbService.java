package com.aqua.jcifs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.aqua.copy_url.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class SmbService extends Service {
    // debug tag
    String tag = "COPY_URL_SMB_SERVICE";
    String url;
    String USER;
    String PWD;
    String proKey;
    String FILENAME;
    String localPath;
    File LocalFile;

    public SmbService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }




    @Override
    public void onStart(Intent intent,int startid){
        url = intent.getStringExtra("URL");
        USER = intent.getStringExtra("USER");
        PWD = intent.getStringExtra("PWD");
        proKey = intent.getStringExtra("proKey");
        FILENAME = intent.getStringExtra("FILENAME");
        localPath = intent.getStringExtra("localPath");
        connectingWithSmbServer();

    }

    public void connectingWithSmbServer() {
        final SmbFile smbFile;
        try {
            String yourPeerPassword = PWD;
            String yourPeerName = USER;
            String yourPeerIP = url;
            String path = "smb://" + yourPeerIP+FILENAME;
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(
                    null, yourPeerName, yourPeerPassword);
            Log.d("Connected", "Yes");
            smbFile = new SmbFile(path, auth);
            /** Printing Information about SMB file which belong to your Peer **/
            String nameoffile = smbFile.getName();
            String pathoffile = smbFile.getPath();
            LocalFile = new File(localPath + File.separator + FILENAME);
            if (LocalFile.exists()){
                Log.d(tag,"delete old file!!!");
                LocalFile.delete();
            }

            Thread thread=new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    downloadFileFromPeerToSdcard(LocalFile,smbFile);
                }
            });
            thread.start();
//            downloadFileFromPeerToSdcard(LocalFile,smbFile);
            Log.d(tag,nameoffile + ":" +pathoffile);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Connected", e.getMessage());
        }
    }

    public void downloadFileFromPeerToSdcard(File mLocalFile, SmbFile mFile) {
        final Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_UPDATEUI);

        try {
            SmbFileInputStream mFStream = new SmbFileInputStream(mFile);
//            mLocalFile = new File(Environment.getExternalStorageDirectory(),
//                  mFile.getName());
            FileOutputStream mFileOutputStream = new FileOutputStream(
                    mLocalFile);
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = mFStream.read(buffer)) > 0) {
                mFileOutputStream.write(buffer, 0, len1);
            }
            mFileOutputStream.close();
            mFStream.close();
            intent.putExtra("copy_result", "OK");
            sendBroadcast(intent);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("MalformURL", e.getMessage());
        } catch (SmbException e) {
            e.printStackTrace();
            Log.e("SMBException", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
