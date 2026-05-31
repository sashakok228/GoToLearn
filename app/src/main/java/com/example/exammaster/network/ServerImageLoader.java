package com.example.exammaster.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerImageLoader {

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final Map<String, Bitmap> memoryCache = new ConcurrentHashMap<>();

    public static void load(ImageView imageView,
                            String imageUrl,
                            int placeholderResId) {

        if (imageView == null) {
            return;
        }

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        String fullUrl = buildFullUrl(imageUrl);

        imageView.setTag(fullUrl);

        Bitmap cached = memoryCache.get(fullUrl);

        if (cached != null) {
            imageView.setImageBitmap(cached);
            return;
        }

        imageView.setImageResource(placeholderResId);

        executor.execute(() -> {
            Bitmap bitmap = downloadBitmap(fullUrl);

            if (bitmap == null) {
                return;
            }

            memoryCache.put(fullUrl, bitmap);

            mainHandler.post(() -> {
                Object tag = imageView.getTag();

                if (tag != null && tag.equals(fullUrl)) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    private static String buildFullUrl(String imageUrl) {
        String trimmed = imageUrl.trim();

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        if (trimmed.startsWith("/")) {
            return ApiConfig.BASE_URL + trimmed;
        }

        return ApiConfig.BASE_URL + "/" + trimmed;
    }

    private static Bitmap downloadBitmap(String fullUrl) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(7000);
            connection.setReadTimeout(7000);
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();

            if (code < 200 || code >= 300) {
                return null;
            }

            try (InputStream inputStream = connection.getInputStream()) {
                return BitmapFactory.decodeStream(inputStream);
            }

        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}