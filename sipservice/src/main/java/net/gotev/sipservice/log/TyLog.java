package net.gotev.sipservice.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.logger.Logger;

/**
 * @author zhangli
 * @email zhanglihow@gmail.com
 * @time
 */
public class TyLog {
    public static boolean opLog = true;

    private TyLog() {
        throw new UnsupportedOperationException("can't instantiate");
    }

    public static void d(@Nullable Object object) {
        if(!opLog)return;
        Logger.d(object);
    }
    public static void d(@NonNull String message, @Nullable Object... args) {
        if(!opLog)return;
        Logger.d(args);
    }
    public static void i(@NonNull String message, @Nullable Object... args) {
        if(!opLog)return;
        Logger.i(message, args);
    }
    public static void e(@NonNull String message, @Nullable Object... args) {
        if(!opLog)return;
        Logger.e(message, args);
    }
    public static void json(@NonNull String message) {
        if(!opLog)return;
        Logger.json(message);
    }
    public static void httpLog(String tag, String msg){
        if(!opLog)return;
        Log.i(tag,msg);
    }

}
