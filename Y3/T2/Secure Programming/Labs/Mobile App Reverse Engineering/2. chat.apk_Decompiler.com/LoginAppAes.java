package com.selector.loginapp;

import android.os.Build;
import android.util.Log;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class LoginAppAes {
    public static String EncryptAES(String str, byte[] bArr, byte[] bArr2) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            instance.init(1, new SecretKeySpec(bArr, "AES"), new IvParameterSpec(bArr2));
            byte[] doFinal = instance.doFinal(str.getBytes());
            if (Build.VERSION.SDK_INT >= 26) {
                return Base64.getEncoder().encodeToString(doFinal);
            }
            Log.e("Error", "Application SDK error. You must work with SDK 26 or higher version.");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String DecryptAES(String str, byte[] bArr, byte[] bArr2) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            instance.init(2, new SecretKeySpec(bArr, "AES"), new IvParameterSpec(bArr2));
            if (Build.VERSION.SDK_INT >= 26) {
                return new String(instance.doFinal(Base64.getDecoder().decode(str)));
            }
            Log.e("Error", "Application SDK error. You must work with SDK 26 or higher version.");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
