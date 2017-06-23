package com.aqua.jcifs;

/**
 * Created by TEI on 2017/05/02.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbSession;

import static android.content.ContentValues.TAG;


public class SMB {

    public String NetFileFullPath;
    public String LocalFileFullPath;
    public String FILENAME;
    public String USER;
    public String PWD;
    public NtlmPasswordAuthentication mAuthentication;

    public String remoteUrl;
    public String localDir;
    public boolean Init(String strUser, String strPwd, String strURL, String strLocalPath, String strFileName){
        boolean ReFlg = true;
        if ((strURL.length() == 0) || (strLocalPath.length() == 0) || (strFileName.length() == 0)){
            return false;
        }

        if (strUser.length() != 0){
            USER = strUser;
        }else{
            USER = "GUEST";
        }
        if (strPwd.length() != 0){
            PWD = strPwd;
        }else{
            PWD = "GUEST";
        }
        FILENAME = strFileName;
        NetFileFullPath = "smb://" + USER + ":" + PWD +"@" +strURL + "/" + FILENAME;
//        NetFileFullPath = "smb://" +strURL + "/" + FILENAME;
//        LocalFileFullPath = strLocalPath + "/" +FILENAME;
        LocalFileFullPath = strLocalPath + "/";

        remoteUrl = NetFileFullPath;
        localDir = LocalFileFullPath;

        Log.d("AQUA",FILENAME);
        Log.d("AQUA",NetFileFullPath);
        Log.d("AQUA",LocalFileFullPath);
        try {
//            UniAddress mDomain = UniAddress.getByName("172.18.3.211");
//            mAuthentication = new NtlmPasswordAuthentication("172.18.3.211", USER, PWD);
            UniAddress mDomain = UniAddress.getByName("192.168.0.50");
            mAuthentication = new NtlmPasswordAuthentication("192.168.0.50", USER, PWD);
            SmbSession.logon(mDomain, mAuthentication);
        }catch (Exception e){
            Log.e("AQUA","init:" + e.getMessage());
            ReFlg = false;
        }finally {
            return ReFlg;
        }

    }


    public  void smbGet(){
        InputStream in = null;
        FileOutputStream out = null;
        try {
            SmbFile smbFile = new SmbFile(remoteUrl);
            String fileName = smbFile.getName();
            File localFile = new File(localDir+File.separator+fileName);
//            in = new BufferedInputStream(new SmbFileInputStream(smbFile));
            in = new BufferedInputStream(new SmbFileInputStream(remoteUrl));
            out = new FileOutputStream(localFile);
            byte []buffer = new byte[1024];
            while((in.read(buffer)) != -1){
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (out != null){
                out.close();
                }
                if (in != null){
                in.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadShareFile() {

        SmbFile dir;
        String host = "192.168.0.50";
        String user = "administrator";
        String password = "Cheng1007";

        try {
            //最後にバックスラッシュが必要
            String path = "smb://" + user + ":" + password + "@" + host + "/D/";
            dir = new SmbFile(path);

            //ファイル一覧セット
            SmbFile[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {

                //ファイルパスを取得
                String filePath = files[i].getPath();
                Log.i(TAG, "File name: " + filePath);

                //ファイル取得
                SmbFileInputStream sfis = new SmbFileInputStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(sfis, "sjis"));

                String str;
                while ((str = br.readLine()) != null) {
                    Log.i(TAG, "data: " + str);
                }

                br.close();
                sfis.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }



    public File readFromSmb(){
        File localfile=null;
        InputStream bis=null;
        OutputStream bos=null;
        List<File> files = new ArrayList<>();
        try {
            SmbFile rmifile = new SmbFile(NetFileFullPath);
            String filename=rmifile.getName();
            bis=new BufferedInputStream(new SmbFileInputStream(rmifile));
            localfile=new File(LocalFileFullPath+File.separator+filename);
            if (localfile.exists()){
                localfile.delete();
            }
            bos=new BufferedOutputStream(new FileOutputStream(localfile));
            int length=rmifile.getContentLength();
            byte[] buffer=new byte[length];
            bis.read(buffer);
            bos.write(buffer);
            try {
                bos.close();
                bis.close();
            } catch (IOException e) {
//                e.printStackTrace();
                Log.e("AQUA","CLOSE ERROR: " + e.getMessage());
            }
            files.add(localfile);
        } catch (Exception e) {
//            e.printStackTrace();
            Log.e("AQUA","readFromSmb:" + e.getMessage());
        }
        return localfile;
    }

}
