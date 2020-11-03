package com.amazon.android.contentbrowser.payments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazon.android.contentbrowser.R;
import com.amazon.android.model.content.Content;
import com.google.gson.Gson;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class PaymentHelper {

    public static final OkHttpClient HTTP_CLIENT = getUnsafeOkHttpClient();
    private static final String QR_URL = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=";

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final Map<String, Integer> PRICE_MAP = new HashMap<>();
    public static Gson GSON = new Gson();

    public static final String TEST_ADDR = "n4VQ5YdHf7hLQ2gWQYYrcxoE5B7nWuDFNF";

    public static void createPaymentDialog(Activity context,
                                           Content content,
                                           DialogInterface.OnClickListener onClickListener)
            throws Exception {
        final double price;
        if (PRICE_MAP.containsKey(content.getId())) {
            String string = context.getString(PRICE_MAP.get(content.getId()));
            price = Double.parseDouble(string.substring(1)); // remove $.
        } else {
            // TODO: pull from product backend.
            price = 100.0;
//            throw new Exception("Could not find price for item: " + content.getId() + ". This needs to be added to the PRICE_MAP.");
        }

        ViewGroup subView = (ViewGroup) context.getLayoutInflater().// inflater view
                inflate(R.layout.payment_dialog, null, false);

        TextView purchaseTextView = (TextView) subView.findViewById(R.id.pay_id_text);
        final String purchaseText = String.format(Locale.US,
                "Scan with your mobile wallet to send funds to %s.",
                content.getTitle());
        purchaseTextView.setText(purchaseText);

        final String description = content.getDescription().isEmpty() ?
                content.getTitle() :
                content.getDescription();

        TextView conversionTextView = (TextView) subView.findViewById(R.id.conversion_text);
        final String conversionText = String.format(Locale.US,
                "Amount: $%.2f\nDescription: %s\n\nSend payment with Bitcoin:",
                price, description);
        conversionTextView.setText(conversionText);

        Picasso picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(HTTP_CLIENT)).build();
        picasso.setLoggingEnabled(true);
        new Handler(Looper.getMainLooper()).post(() -> {
            String url = createBitcoinQRCodeUrl(TEST_ADDR);
            ImageView v = (ImageView) subView.findViewById(R.id.btcImage);
            picasso.load(url).into(v);

            new AlertDialog.Builder(context)
                    .setView(subView)
                    .setTitle(R.string.scan_to_pay)
                    .setPositiveButton(R.string.done, onClickListener)
                    .show();

        });
    }

    public static String createBitcoinQRCodeUrl(String addr) {
        return String.format(Locale.US, "%s%s", QR_URL, addr);
    }

}
