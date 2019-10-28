
package com.vuongductuanktmt.telpos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.nfc.Nfc;
import com.telpo.tps550.api.util.RSReader;
import com.telpo.tps550.api.util.ReaderUtils;
import com.telpo.tps550.api.util.StringUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RNTelposModule extends ReactContextBaseJavaModule implements ActivityEventListener, LifecycleEventListener {

    ReaderUtils readerUtils;
    RSReader rsReader;
    byte[] bytes = null;
    List<byte[]> qrData;
    private Nfc nfc;
    byte[] nfcData = null;

    Thread threadQr;
    Thread threadNfc;


    private final byte B_CPU = 3;
    private final byte A_CPU = 1;
    private final byte A_M1 = 2;
    private final ReactApplicationContext reactContext;

    public RNTelposModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        this.reactContext.addActivityEventListener(this);
        this.reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "RNTelpos";
    }

    @ReactMethod
    public void closeQr() {
        if (threadQr != null) {
            threadQr.interrupt();
        }
        rsReader.closeReader();
    }

    @ReactMethod
    public void closeNfc() {
        if (threadNfc != null) {
            threadNfc.interrupt();
        }
        if (nfc != null) {
            try {
                nfc.close();
            } catch (TelpoException e) {
                e.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void qrReader(final ReadableMap options) {
        Context mContext = getReactApplicationContext();
        final int timeout = options.getInt("timeout");
        readerUtils = new ReaderUtils();
        rsReader = new RSReader();
        try {
            bytes = rsReader.RsConfig(mContext, 115200, (byte) 0x08, (byte) 0x02, (byte) 0x01, (byte) 0x00);
            threadQr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        qrData = rsReader.rs_read(getReactApplicationContext(), 1024 * 1000, timeout);
                        if (qrData != null && qrData.size() > 0) {
                            String string1 = "";
                            for (int j = 0; j < qrData.get(qrData.size() - 1).length; j++) {
                                string1 = string1 + qrData.get(qrData.size() - 1)[j];
                            }
                            reactContext
                                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                    .emit("onQrDetected", convertQr(qrData.get(qrData.size() - 1)));
                        }
                    } catch (TelpoException e) {
                        e.printStackTrace();
                    }
                }
            });
            ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(threadQr, 0, 1, TimeUnit.SECONDS);
        } catch (TelpoException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void nfcReader(final ReadableMap options) {
        Context context = getReactApplicationContext();
        final int timeout = options.getInt("timeout");
        nfc = new Nfc(context);
        threadNfc = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    nfc.close();
                    nfc.open();
                    nfcData = nfc.activate(timeout);
                    if (nfcData != null && nfcData.length > 0) {
                        reactContext
                                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit("onNfcDetected", convertNfc(nfcData));
                    } else {
                        nfc.open();
                    }
                } catch (TelpoException e) {
                    e.printStackTrace();
                }
            }
        });

        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(threadNfc, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        closeNfc();
        closeQr();
    }

    public WritableMap convertNfc(byte[] uid_data) {
        WritableMap readData = Arguments.createMap();
        if (uid_data[0] == 0x42) {
            // TYPE B
            byte[] atqb = new byte[uid_data[16]];
            byte[] pupi = new byte[4];
            String type = null;

            System.arraycopy(uid_data, 17, atqb, 0, uid_data[16]);
            System.arraycopy(uid_data, 29, pupi, 0, 4);

            if (uid_data[1] == B_CPU) {
                type = "CPU";
            } else {
                type = "unknow";
            }
            readData.putString("type", type);
            readData.putString("atqb", StringUtil.toHexString(atqb));
            readData.putString("pupi", StringUtil.toHexString(pupi));

        } else if (uid_data[0] == 0x41) {
            // TYPE A（CPU, M1）
            byte[] atqa = new byte[2];
            byte[] sak = new byte[1];
            byte[] uid = new byte[uid_data[5]];
            String type = null;

            System.arraycopy(uid_data, 2, atqa, 0, 2);
            System.arraycopy(uid_data, 4, sak, 0, 1);
            System.arraycopy(uid_data, 6, uid, 0, uid_data[5]);

            if (uid_data[1] == A_CPU) {
                type = "CPU";
            } else if (uid_data[1] == A_M1) {
                type = "M1";
            } else {
                type = "unknow";
            }

            readData.putString("type", type);
            readData.putString("atqa", StringUtil.toHexString(atqa));
            readData.putString("sak", StringUtil.toHexString(sak));
            readData.putString("uid", StringUtil.toHexString(uid));
        }
        return readData;
    }

    public String convertQr(byte[] rs_data) {
        return new String(rs_data);
    }
}
