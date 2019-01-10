package com.example.lokesh.amul;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


class details implements Serializable {
    public byte[] key,msg;
    public String algo;
    public byte[] sign;
    public PublicKey pubKey;
    public int flag;

    details(byte[] msg,byte[] key,String algo,byte[] sign,PublicKey pubKey,int flag){
        this.key=key;
        this.msg=msg;
        this.flag = flag;
        this.algo=algo;
        this.sign = sign;
        this.pubKey = pubKey;
    }
}

public class ChatActivity extends AppCompatActivity {
    public static String deviceName;
    public static String deviceIp;
    private static  PublicKey publicKey;
    private static PrivateKey privateKey;
    private static String myIp;
    EditText messageView;
    private String macAdress;
    private static byte[] algoKey = new byte[8];
    private  static  byte[] algoKeyAES=new byte[16];
    public static MessageAdapter messageAdapter;
    public static ListView messagesListView;
    public static String ALGO;
    private byte[] encryptedByte;
    private byte[] encryptedKey;
    private byte[] imageByte;
    Key publicKeys;
    static Key privateKeys;
    String message,decrypted;
    KeyPairGenerator kpg;
    KeyPair kp;
    Cipher cipher;
    static Cipher cipher1;
    byte[] myencryptedByte;
    byte[] enc_msg;
    static byte[] decryptedBytes;
    byte[] encryptedBytes;
    byte[] signature;
    private String encryptedMessage;
    private static String rsaPublicKey;
    private static String rsaPrivateKey;
    int mode;
    static String keyForRSA;
    ImageButton exc;
    Handler handler = new Handler();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent chatIntent =getIntent();
        deviceName = chatIntent.getExtras().getString("device");
        deviceIp = chatIntent.getExtras().getString("ip");
        macAdress = chatIntent.getExtras().getString("mac");
        ALGO = chatIntent.getExtras().getString("algo");
        myIp = chatIntent.getExtras().getString("myIp");
        //setting server for connection
        Thread myThread=new Thread(new ChatActivity.MyServer(getApplicationContext()));
        myThread.start();


        final rsa rsaObj = new rsa();
        try {
            rsaObj.initKey();
            rsaPublicKey = rsaObj.getPublicKey();
            rsaPrivateKey = rsaObj.getPrivateKey();


        } catch (Exception e) {
            e.printStackTrace();
        }
        final ProgressDialog dialog = new ProgressDialog(ChatActivity.this);
        dialog.setTitle("Pairing with"+deviceIp+"...");
        dialog.setMessage("Please wait....");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();

        long delayInMillis = 5000;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                BackgroundTask b= new BackgroundTask(deviceIp,rsaPublicKey,0);
                b.execute();
                dialog.dismiss();


            }
        }, delayInMillis);

        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey= keyPair.getPrivate();
        try {
            signature = createSignature(myIp,privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        messageView = (EditText) findViewById(R.id.editText);
        messageAdapter = new MessageAdapter(this);
        messagesListView = (ListView) findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);

        if(deviceName.isEmpty())
            deviceName = "Anonymous";
        Log.e("Chat",deviceIp+" "+deviceName);
        int i;
        for( i=0;i<8;i++){
            algoKey[i] = (byte)macAdress.charAt(i);
        }
        for( i=0;i<8;i++){
            algoKeyAES[i] = (byte)macAdress.charAt(i);
        }
        for(i=8;i<16;i++) {
            algoKeyAES[i] = algoKey[i - 8];
        }


        Log.e("AlgoKey mac DES",algoKey+"");

    }
    @SuppressLint("LongLogTag")
    public void sendMessage(View view) throws Exception {
        String message = messageView.getText().toString();

        encryptedByte = encrypt(message,ALGO);
        Log.e("Encrypted Byte",encryptedByte+"");
        if(ALGO.equals("DES"))
            myencryptedByte = rsa.encryptByPublicKey(algoKey,keyForRSA);
        else
            myencryptedByte = rsa.encryptByPublicKey(algoKeyAES,keyForRSA);
        Log.e("Algo codeKey & RSAencKey",Base64.encodeToString(algoKey,Base64.DEFAULT)+" "+myencryptedByte);
        MemberData memberData = new MemberData(deviceName,getRandomColor());
        messageAdapter.add(new Message(message,deviceName,memberData,true));
        messagesListView.setSelection(messagesListView.getCount() - 1);

       if(ALGO.equals("DES")) {
           BackgroundTask b = new BackgroundTask(deviceIp, encryptedByte, myencryptedByte, ALGO,signature,publicKey,1,1);
           b.execute();
       }
        else {
            BackgroundTask b = new BackgroundTask(deviceIp, encryptedByte, myencryptedByte, ALGO,signature,publicKey,1,1);
            b.execute();
        }


    }
    public void sendFile(View view) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");

        startActivityForResult(i,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1){
            if(resultCode==RESULT_OK) {

                        try {
                            Uri imageUri = data.getData();

                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                            Log.e("Send Image", imageBitmap + "");

                            MemberData memberData = new MemberData(deviceName, getRandomColor());
                            messageAdapter.add(new Message(imageBitmap, deviceName, memberData, true));
                            messagesListView.setSelection(messagesListView.getCount() - 1);

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            imageByte = bos.toByteArray();

                            imageByte = encryptFile(imageByte, ALGO);

                            if (ALGO.equals("DES")) {
                                myencryptedByte = rsa.encryptByPublicKey(algoKey, keyForRSA);

                                BackgroundTask b = new BackgroundTask(deviceIp, imageByte, myencryptedByte, ALGO, signature, publicKey, 0, 1);
                                b.execute();
                            } else {
                                BackgroundTask b = new BackgroundTask(deviceIp, imageByte, myencryptedByte, ALGO, signature, publicKey, 0, 1);
                                b.execute();
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }


        }
    public static byte[] encryptFile(byte[] value,String algo) {
        byte[] encrypted = null;
        try {
            Key skeySpec;
            if(algo.equals("AES"))
                skeySpec = new SecretKeySpec(algoKeyAES, algo);
            else
                skeySpec = new SecretKeySpec(algoKey, algo);
            Cipher cipher = Cipher.getInstance(algo+"/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];

            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec,ivParams);
            encrypted  = cipher.doFinal(value);
            System.out.println("encrypted string:" + encrypted.length);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return encrypted;
    }


    public static byte[] createSignature(String message, PrivateKey key) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException, SignatureException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");

        byte[] signBytes = messageDigest.digest(message.getBytes("UTF-8"));
        Signature signature = Signature.getInstance("NONEwithRSA");
        signature.initSign(key);
        signature.update(signBytes);
        return signature.sign();

    }

    public static boolean verifySignature(String message,PublicKey key,byte[] sign) throws NoSuchAlgorithmException, UnsupportedEncodingException, NoSuchProviderException, InvalidKeyException, SignatureException, SignatureException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        byte[] signBytes = messageDigest.digest(message.getBytes("UTF-8"));
        Signature signature = Signature.getInstance("NONEwithRSA");
        signature.initVerify(key);
        signature.update(signBytes);
         return signature.verify(sign);

    }

    public static byte[] encrypt(String value,String algo) {
        byte[] encrypted = null;
        try {
            Key skeySpec;
            if(algo.equals("AES"))
            skeySpec = new SecretKeySpec(algoKeyAES, algo);
            else
                skeySpec = new SecretKeySpec(algoKey, algo);
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

    public static byte[]  decrypt(byte[] encrypted,byte[] algoKey,String algo) {
        byte[] original = null;
        Cipher cipher = null;
        try {

            Key key = new SecretKeySpec(algoKey, algo);
            cipher = Cipher.getInstance(algo+"/CBC/PKCS5Padding");
            //the block size (in bytes), or 0 if the underlying algorithm is not a block cipher
            byte[] ivByte = new byte[cipher.getBlockSize()];
            //This class specifies an initialization vector (IV). Examples which use
            //IVs are ciphers in feedback mode, e.g., DES in CBC mode and RSA ciphers with OAEP encoding operation.
            IvParameterSpec ivParamsSpec = new IvParameterSpec(ivByte);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParamsSpec);
            original= cipher.doFinal(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return original;
    }



    class BackgroundTask extends AsyncTask<String,Void,String>
    {

        Socket s;
        DataOutputStream dos;
        String ip,msg;
        byte[] encMessage;
        byte[] encKey;
        String algo;
        Context mContext=getApplicationContext();
        details detail;
        byte[] sign;
        PublicKey pubKey;
        int flag;
        int mode;
        String senderKey;

        public BackgroundTask(String deviceIp ,String senderKey,int mode){
            super();
            this.senderKey = senderKey;
            ip = deviceIp;
            this.mode = mode;
            detail = new details(null,null,senderKey,null,null,2);
        }
        public BackgroundTask(String deviceIp,byte[] encMessage,byte[] encKey,
                              String algo,byte[] sign,PublicKey pubKey,int flag,int mode) {
            super();
            ip = deviceIp;
            this.encMessage = encMessage;
            this.encKey = encKey;
            this.algo = algo;
            this.mode = mode;
            this.sign = sign;
            this.pubKey=pubKey;
            this.flag = flag;
            detail=new details(this.encMessage,this.encKey,this.algo,this.sign,this.pubKey,this.flag);
        }

        @Override
        protected String doInBackground(String... strings) {

            Log.e("backg",ip);


            try{
                s=new Socket(ip,9700);

                ObjectOutputStream os=new ObjectOutputStream(s.getOutputStream());
                os.writeObject(detail);



                s.close();

            }catch (IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }


    }



    public static  String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    /**
     * Created by lokesh on 26/10/18.
     */
    public static class MyServer implements Runnable {

        ServerSocket ss;
        Socket mysocket;
        Context mContext;
        DataInputStream dis;
        byte[] msg;
        byte[] key = new byte[256];
        byte[] algoKeyDES = new byte[8];
        byte[] algoKeyAES = new byte[16];
        String algo;
        private byte[] decKey;
        private byte[] sign;
        private byte[] decMsgBytes;
        private String  decMsg;
        details detail = null;
        String senderKey;
        PublicKey pubKey;


        android.os.Handler handler=new android.os.Handler();

        MyServer(Context context)
        {
            mContext=context;
        }
        @Override
        public void run() {


                try {
                    ss = new ServerSocket(9700);


                    do {

                        mysocket = ss.accept();

                        ObjectInputStream oi = new ObjectInputStream(mysocket.getInputStream());
                        detail = (details) oi.readObject();


                        if (detail.flag != 2) {
                            try {

                                msg = detail.msg;
                                Log.e("Rec Mesg", msg + "");
                                key = detail.key;
                                algo = detail.algo;
                                final int flags = detail.flag;
                                boolean flag = false;
                                try {
                                    flag = verifySignature(deviceIp, detail.pubKey, detail.sign);

                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (NoSuchProviderException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeyException e) {
                                    e.printStackTrace();
                                } catch (SignatureException e) {
                                    e.printStackTrace();
                                }


                                Log.e("Rec Key", detail.key + "");

                                try {

                                    if (algo.equals("DES")) {
                                        algoKeyDES = rsa.decryptByPrivateKey(key, rsaPrivateKey);
                                        decMsgBytes = decrypt(msg, algoKeyDES, algo);
                                    } else {
                                        algoKeyAES = rsa.decryptByPrivateKey(key, rsaPrivateKey);
                                        decMsgBytes = decrypt(msg, algoKeyAES, algo);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                final boolean finalFlag = flag;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MemberData memberData = new MemberData(deviceName, getRandomColor());
                                        if (finalFlag) {
                                            if (flags == 1) {
                                                try {
                                                    decMsg = new String(decMsgBytes, "UTF-8");
                                                } catch (UnsupportedEncodingException e) {
                                                    e.printStackTrace();
                                                }
                                                Log.e("Algo dec msg", decMsg);
                                                messageAdapter.add(new Message(decMsg, deviceName, memberData, false));
                                            } else {

                                                Bitmap bitmap = BitmapFactory.decodeByteArray(decMsgBytes, 0, decMsgBytes.length);
                                                Log.e("Msg", msg + "");
                                                Log.e("Rev Image", bitmap + "");
                                                messageAdapter.add(new Message(bitmap, deviceName, memberData, false));
                                                saveImage(bitmap, mContext);
                                            }
                                            messagesListView.setSelection(messagesListView.getCount() - 1);
                                        } else {
                                            Toast.makeText(mContext, "Hacked ", Toast.LENGTH_LONG).show();
                                        }


                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                            senderKey = detail.algo;
                            ChatActivity.keyForRSA = senderKey;


                        }


                    } while (true);


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

    public static void saveImage(Bitmap image,Context context){
        String root = Environment.getExternalStorageDirectory().toString();
        File myFile = new File(root+"/saved_images");
        myFile.mkdir();
        Random genrator = new Random();
        int n=1000;
        n=genrator.nextInt();
        String fname = "Image-"+n+".jpg";
        File file = new File(myFile,fname);
        if(file.exists())file.delete();
        try{
            FileOutputStream out = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,90,out);
            out.flush();
            out.close();
            Toast.makeText(context,"Image Saved",Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

