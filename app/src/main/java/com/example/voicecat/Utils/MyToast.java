package com.example.voicecat.Utils;

import android.content.Context;
import android.widget.Toast;
//调用Toast
public class MyToast {
    private static Toast toast = null;
    public static void sendMsg(Context context,String str){
        if(toast == null){
            toast = Toast.makeText(context,str,Toast.LENGTH_SHORT);
        }else{
            toast.setText(str);
        }
        toast.show();
    }
}
