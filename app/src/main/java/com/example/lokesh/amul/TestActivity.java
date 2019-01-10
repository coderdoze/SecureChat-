package com.example.lokesh.amul;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TestActivity extends AppCompatActivity {
    byte[] desKey = {'1','2','3','4','5','6','7','8'};
   // byte[] aesKey = {'1','2','3','4','5','6','7','8','9','10','11','12','1'}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


            final String path = getFilesDir().getParent() + "/t.txt";
            RandomAccessFile newSparseFile = null;
            try {
                new File(path).delete();
                newSparseFile = new RandomAccessFile(path, "rw");
                // create a 1MB file:
                newSparseFile.setLength((long)Math.pow(10,8));
            } catch (final Exception e) {
                Log.e("DEBUG", "error while creating file:" + e);
            } finally {
                if (newSparseFile != null)
                    try {
                        newSparseFile.close();
                        Log.e("DEBUG", "length:" + new File(path).length());
                    } catch (final IOException e) {
                        Log.e("DEBUG", "error while closing file:" + e);
                    }
            }

            Log.e("File created", newSparseFile + "");
            String file = newSparseFile.toString();
            String time1 = "00:00:00";
            String time2 = "0:00:00";

            long t1 = Calendar.getInstance().getTimeInMillis();
            byte[] encrypted = encrypt(file, "AES", desKey);
            long t2 = Calendar.getInstance().getTimeInMillis();
            Log.e("Time taken", t2 - t1 + "");



    }
    public static byte[] encrypt(String value,String algo,byte[] key) {
        byte[] encrypted = null;
        try {
            Key skeySpec;
            skeySpec = new SecretKeySpec(key, algo);

//            if(algo.equals("AES"))
//                skeySpec = new SecretKeySpec(algoKeyAES, algo);
//            else
//                skeySpec = new SecretKeySpec(algoKey, algo);
            Cipher cipher = Cipher.getInstance(algo+"/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];

            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec,ivParams);
            encrypted  = cipher.doFinal(value.getBytes("UTF-8"));
            System.out.println("encrypted string:" + encrypted.length);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return encrypted;
    }
}
