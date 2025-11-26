// Name                 Cameron Reid
// Student ID           S2455533
// Programme of Study   Software Development

package com.example.reid_cameron_s2455533;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

//Summary page - shows Euro, USD, and JPY
public class SummaryActivity extends AppCompatActivity {

    private String urlSource = "https://www.fx-exchange.com/gbp/rss.xml";
    private TextView txtLastUpdated;
    private TextView txtUsd;
    private TextView txtEur;
    private TextView txtJpy;
    private Button btnViewAll;

    private String result;
    private List<CurrencyItem> currencyItems = new ArrayList<>();
    private String lastBuildDate = "-";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        txtLastUpdated = findViewById(R.id.txtLastUpdated);
        txtUsd = findViewById(R.id.txtUsd);
        txtEur = findViewById(R.id.txtEur);
        txtJpy = findViewById(R.id.txtJpy);
        btnViewAll = findViewById(R.id.btnViewAll);

        //Sends user to MainActivity (all currencies page)
        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(SummaryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        //Automatically fetches exchange rates when app opened
        startProgress();
    }

    private void startProgress() {
        new Thread(new Task(urlSource)).start();
    }

    //Runnable class used to fetch the XML data from the RSS URL on a background thread
    private class Task implements Runnable {
        private final String url;

        public Task(String aurl) {
            url = aurl;
        }

        @Override
        public void run() {
            result = ""; //Reset before concatenating
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine;

            try {
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    result = result + inputLine;
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                Log.e("SummaryTask", "Error downloading data", e);
            }

            int i = result.indexOf("<?");
            if (i != -1) {
                result = result.substring(i);
            }
            i = result.indexOf("</rss>");
            if (i != -1) {
                result = result.substring(0, i + 6);
            }

            //Now that you have the xml data into result, you can parse it
            parseXML(result);

            runOnUiThread(() -> {
                //Updating label to show date and time exchange rates were last updated on RSS feed
                txtLastUpdated.setText("Last updated: " + lastBuildDate);

                //Fetching summary currencies
                CurrencyItem usd = findByCode("USD");
                CurrencyItem eur = findByCode("EUR");
                CurrencyItem jpy = findByCode("JPY");

                if (usd != null) {
                    txtUsd.setText("GBP → USD: " + usd.getRate());
                    txtUsd.setOnClickListener(v -> openConversion(usd));
                }
                if (eur != null) {
                    txtEur.setText("GBP → EUR: " + eur.getRate());
                    txtEur.setOnClickListener(v -> openConversion(eur));
                }
                if (jpy != null) {
                    txtJpy.setText("GBP → JPY: " + jpy.getRate());
                    txtJpy.setOnClickListener(v -> openConversion(jpy));
                }
            });
        }
    }

    //Parse the XML string into a list of CurrencyItem objects
    private void parseXML(String xml) {

        //Starting fresh each time
        currencyItems.clear();

        //Try catch for PullParser
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            boolean insideItem = false;
            CurrencyItem currentItem = null;
            String text = "";

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    //Start tag
                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            insideItem = true;
                            currentItem = new CurrencyItem();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                        //End tag
                    case XmlPullParser.END_TAG:
                        if ("lastBuildDate".equalsIgnoreCase(tagName)) {
                            lastBuildDate = text.trim();
                        }

                        if (insideItem && currentItem != null) {

                            if ("link".equalsIgnoreCase(tagName)) {
                                String link = text.trim();
                                int lastSlash = link.lastIndexOf('/');
                                int dot = link.lastIndexOf('.');
                                if (lastSlash != -1 && dot != -1 && dot > lastSlash) {
                                    String code = link.substring(lastSlash + 1, dot);
                                    currentItem.setCode(code.toUpperCase());
                                }

                            } else if ("title".equalsIgnoreCase(tagName)) {
                                currentItem.setTitle(text.trim());

                            } else if ("description".equalsIgnoreCase(tagName)) {
                                String desc = text.trim();
                                int eqPos = desc.indexOf('=');
                                if (eqPos != -1) {
                                    String rightSide = desc.substring(eqPos + 1).trim();
                                    String[] parts = rightSide.split(" ");
                                    if (parts.length > 0) {
                                        try {
                                            double rate = Double.parseDouble(parts[0]);
                                            currentItem.setRate(rate);
                                        } catch (NumberFormatException e) {
                                            Log.e("SummaryParser", "Could not parse rate from: " + desc);
                                        }
                                    }
                                }

                            } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                currentItem.setPublishDate(text.trim());

                            } else if ("item".equalsIgnoreCase(tagName)) {
                                //Finished one item
                                currencyItems.add(currentItem);
                                insideItem = false;
                                currentItem = null;
                            }
                        }
                        break;
                }

                eventType = parser.next();
            }
            //Error handling
        } catch (Exception e) {
            Log.e("SummaryParser", "Error parsing XML", e);
        }
    }

    private CurrencyItem findByCode(String code) {
        for (CurrencyItem item : currencyItems) {
            if (code.equalsIgnoreCase(item.getCode())) {
                return item;
            }
        }
        return null;
    }

    //Enables user to select a currency on summary page for conversion
    private void openConversion(CurrencyItem item) {
        Intent intent = new Intent(SummaryActivity.this, ConversionActivity.class);
        intent.putExtra("code", item.getCode());
        intent.putExtra("rate", item.getRate());
        intent.putExtra("title", item.getTitle());
        startActivity(intent);
    }
}
