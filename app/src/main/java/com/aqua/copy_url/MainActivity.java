package com.aqua.copy_url;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.aqua.jcifs.SmbService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // debug tag
    String tag = "COPY_URL_SMB_SERVICE";
    public static String FILENAME = "URL.txt";
    public static String localpath = "/sdcard/copy_url/";
    EditText txtIp;
    ListView ls;
    public String TargetURL;
    public int POSITION;

//    List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    List<Project> ProList=new ArrayList<>();
    public static final String ACTION_UPDATEUI = "action.updateUI";
    UpdateUIBroadcastReceiver broadcastReceiver;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtIp = (EditText)findViewById(R.id.txtIP);
        ls = (ListView)findViewById(R.id.showList);
//        /*为ListView添加点击事件*/
//        ls.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                Log.d(tag, "你点击了ListView条目" + position);
//                Log.d(tag, "URL:" + adapter.getItem(position).getUrl());
//                TargetURL = adapter.getItem(position).getUrl();
//                POSITION = position;
//            }
//        });
        // 添加长按点击弹出选择菜单
        ls.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("select please");
                menu.add(0, 0, 0, "open in browser");
                menu.add(0, 1, 0, "copy to clipboard");
            }
        });

        Button btnOpen = (Button) findViewById(R.id.btnOpen);


        System.setProperty("jcifs.smb.client.dfs.disabled", "true");
        System.setProperty("jcifs.smb.client.soTimeout", "1000000");
        System.setProperty("jcifs.smb.client.responseTimeout", "30000");


        // 动态注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATEUI);
        broadcastReceiver = new UpdateUIBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);

    }

    //给菜单项添加事件
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        //info.id得到listview中选择的条目绑定的id
//        String id = String.valueOf(info.id);
        TargetURL = adapter.getItem(info.position).getUrl();
        switch (item.getItemId()) {
            case 0:
                Log.d(tag,"OPON" + TargetURL);
                Intent intent = new Intent(Intent.ACTION_VIEW);    //为Intent设置Action属性
                intent.setData(Uri.parse(TargetURL)); //为Intent设置DATA属性
//                intent.setData(Uri.parse("http://www.baidu.com"));
                startActivity(intent);

                return true;
            case 1:
                Log.d(tag,"COPY" + TargetURL);
                //获取剪贴板管理器：
                 ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                 ClipData mClipData = ClipData.newPlainText("Label", TargetURL);
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // copy net file to local and set it to viewlist
    void showFile(){
        ContextWrapper c = new ContextWrapper(this);
        File Dir;

        try {

            Dir = new File(localpath);
            if (!Dir.exists()){
                Dir.mkdir();
            }

            Intent regIntent = new Intent(this, SmbService.class);
            regIntent.putExtra("URL", txtIp.getText().toString());
            regIntent.putExtra("USER", "guest");
            regIntent.putExtra("PWD", "guest");
            regIntent.putExtra("proKey", "test");
            regIntent.putExtra("FILENAME", FILENAME);
            regIntent.putExtra("localPath", localpath);

            startService(regIntent);


        }catch (Exception e){
            showMsg("ERROR",e.getMessage());
        }
    }

    void readResultFile(){
        File ReadFile;
        BufferedReader reader;
        try {
            ReadFile = new File(localpath + File.separator + FILENAME);
            reader = new BufferedReader(new FileReader(ReadFile));
            String line;
            clearList();
            while ((line = reader.readLine()) != null) {
                String[] RowData = line.split(",");
                setList(RowData[0], RowData[1]);
                Log.d("READ_NEW:", RowData[0]);
                Log.d("READ_NEW:", RowData[1]);
            }
            reader.close();
            showList();
        }catch (Exception e){
            showMsg("ERROR",e.getMessage());
        }
    }

    void showMsg(String _style,String _msg){
        if (_style == "ERROR") {
            Toast.makeText(this, "ERROR: "+_msg, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, _msg, Toast.LENGTH_SHORT).show();
        }
    }

    void clearList()
    {
        ls.setAdapter(null);
        ProList.clear();
    }

    void setList(String title, String url){
        Project Pro=new Project();
        Pro.setTitle(title);
        Pro.setUrl(url);
        Pro.setPhoto(R.drawable.dev);
        ProList.add(Pro);
    }
    void showList(){
//        adapter = new SimpleAdapter(this,list,R.layout.vlist, new String[]{"title","url"},new int[]{R.id.title,R.id.url});
//        ls.setAdapter(adapter);
        adapter=new MyAdapter(ProList,MainActivity.this);
        ls.setAdapter(adapter);
//        adapter.setSelectedPosition(POSITION);
//        adapter.notifyDataSetInvalidated();

    }

    public void btnOpenClick(View view) {
        showFile();
    }


    private class UpdateUIBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(tag,"get broad cast:"+ String.valueOf(intent.getExtras().getString("copy_result")));
            readResultFile();
        }

    }
    @Override
    protected void onDestroy() {
        System.out.println("onDestroy");
        super.onDestroy();
        // 注销广播
        unregisterReceiver(broadcastReceiver);
    }

}
