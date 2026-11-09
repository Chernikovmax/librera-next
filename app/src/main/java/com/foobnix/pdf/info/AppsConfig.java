package com.foobnix.pdf.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingWorkPolicy;

import com.bumptech.glide.load.DecodeFormat;
import com.foobnix.LibreraBuildConfig;
import com.foobnix.android.utils.Apps;
import com.foobnix.android.utils.Dips;
import com.foobnix.android.utils.LOG;
import com.foobnix.model.AppProfile;
import com.foobnix.model.AppState;

import org.ebookdroid.droids.mupdf.codec.MuPdfDocument;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppsConfig {

    public static final DecodeFormat CURRENT_BITMAP = DecodeFormat.PREFER_ARGB_8888;
    public static final Bitmap.Config CURRENT_BITMAP_ARGB = Bitmap.Config.ARGB_8888;
    public static final String PRO_LIBRERA_READER = "com.foobnix.pro.pdf.reader";
    public static final String LIBRERA_READER = "com.foobnix.pdf.reader";
    /**
     * How much of the tint colour the floating chrome keeps, 0-255. Two things bound it: the
     * page has to be seen moving under the bars at all, and the white the labels are set in
     * has to hold against the brightest thing that can pass beneath them - a white cover,
     * which lightens the bar by whatever is let through.
     */
    public static final int APP_TRANSPARENCY = 240;
    /**
     * The flavours that are not published on Google Play and are built without Google Play
     * services: the F-Droid one and the MuPDF-free build of it, fdroidJS.
     */
    public static final boolean
            IS_FDROID =
            LibreraBuildConfig.FLAVOR.startsWith("fdroid") || LibreraBuildConfig.FLAVOR.equals("huawei");
    public static final boolean IS_WRITE_LOGS = IS_FDROID;
    public static final String FLAVOR = LibreraBuildConfig.FLAVOR;
    public static final boolean IS_ENABLE_1_PAGE_SEARCH = true;

    public static final String SEARCH_FRAGMENT_WORKER_NAME = "search";
    public static final String SYNC_DRIVE_WORKER_NAME = "sync-drive";
    public static final String SYNC_DROPBOX_WORKER_NAME = "sync-dropbox";
    public static final ExistingWorkPolicy WORKER_POLICY = ExistingWorkPolicy.REPLACE;

    static int cpuCores = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
//    public final static ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
//        Thread t = new Thread(r);
//        t.setPriority(Thread.MAX_PRIORITY);
//        //t.setDaemon(true);
//        return t;
//    });

    public final static ExecutorService executorService = Executors.newFixedThreadPool(2);
    public final static ExecutorService executorServiceSingle = Executors.newSingleThreadExecutor();

    public static boolean
            IS_LOG =
            Build.DEVICE.toLowerCase().contains("emu") || Build.MODEL.toLowerCase().contains("sdk");
    public static boolean
            IS_EMULATOR =
            Build.DEVICE.toLowerCase().contains("emu") || Build.MODEL.toLowerCase().contains("sdk");
    public static String MUPDF_FZ_VERSION = "";
    public static String MUPDF_1_11 = "1.11";
    public static boolean isDOCXSupported = Build.VERSION.SDK_INT >= 26;
    public static boolean isCloudsEnable = false;



    static {
        // libmupdf.so where the build has it - without it MUPDF_FZ_VERSION stays empty, as every
        // reader of it expects (they only ever ask whether it is MuPDF 1.11) - and the libraries
        // every build has
        if (Engines.IS_MUPDF) {
            System.loadLibrary(Engines.MUPDF_LIBRARY);
            AppsConfig.MUPDF_FZ_VERSION = MuPdfDocument.getFzVersion();
        }
        for (final String library : Engines.NATIVE_LIBRARIES) {
            System.loadLibrary(library);
        }
        Log.d("Librera", "native " + java.util.Arrays.toString(Engines.NATIVE_LIBRARIES)
                + (Engines.IS_MUPDF ? " + MuPDF " + MUPDF_FZ_VERSION : ""));
    }

    public static boolean isPDF_DRAW_ENABLE() {
        return true;
    }

}
