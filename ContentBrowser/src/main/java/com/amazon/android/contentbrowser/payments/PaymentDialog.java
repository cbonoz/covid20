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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.amazon.android.contentbrowser.payments.PaymentHelper.HTTP_CLIENT;

public class PaymentDialog {

    public static final Map<String, Integer> PRICE_MAP = new HashMap<>();
    public static Gson GSON = new Gson();

    public static final String TEST_ADDR = "XXX";


    public static void createPaymentDialog(Activity context,
                                           Content content,
                                           DialogInterface.OnClickListener onClickListener)
            throws Exception {
        final double price;
        if (PRICE_MAP.containsKey(content.getId())) {
            String string = context.getString(PRICE_MAP.get(content.getId()));
            price = Double.parseDouble(string.substring(1)); // remove $.
        } else {
            throw new Exception("Could not find price for item: " + content.getId() + ". This needs to be added to the PRICE_MAP.");
        }

        ViewGroup subView = (ViewGroup) context.getLayoutInflater().// inflater view
                inflate(R.layout.payment_dialog, null, false);

        TextView purchaseText = (TextView) subView.findViewById(R.id.pay_id_text);
        purchaseText.setText(String.format(Locale.US, "Scan with your mobile wallet to complete purchase of %s.", content.getTitle()));

        Picasso picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(HTTP_CLIENT)).build();
        picasso.setLoggingEnabled(true);
        new Handler(Looper.getMainLooper()).post(() -> {
            String url = createBitcoinQRCode(TEST_ADDR);
            ImageView v = (ImageView) subView.findViewById(R.id.btcImage);
            picasso.load(url).into(v);


            new AlertDialog.Builder(context)
                    .setView(subView)
                    .setTitle("Scan address to complete purchase")
                    .setPositiveButton("Done", onClickListener)
                    .show();

        });


    }

    public static String createBitcoinQRCode(String addr) {
        return "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + addr;
    }
}
