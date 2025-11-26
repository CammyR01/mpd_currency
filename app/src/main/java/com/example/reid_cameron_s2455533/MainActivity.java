/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 Cameron Reid
// Student ID           S2455533
// Programme of Study   Software Development
//

package com.example.reid_cameron_s2455533;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;


import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button startButton;
    private String result;
    private String url1="";
    private String urlSource="https://www.fx-exchange.com/gbp/rss.xml";
    private List<CurrencyItem> currencyItems = new ArrayList<>();
    private List<CurrencyItem> allCurrencyItems = new ArrayList<>();
    private ListView currencyListView;
    private CurrencyAdapter currencyAdapter;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.startButton);
        currencyListView = findViewById(R.id.currencyListView);
        searchEditText = findViewById(R.id.edtSearch);

        startButton.setOnClickListener(this);

        //Automatically fetches exchange rates when app opened
        startProgress();

        //Create the adapter and attach it to the ListView
        currencyAdapter = new CurrencyAdapter(this, currencyItems);
        currencyListView.setAdapter(currencyAdapter);

        currencyListView.setOnItemClickListener((parent, view, position, id) -> {
            CurrencyItem item = currencyItems.get(position);   // clicked item

            Intent intent = new Intent(MainActivity.this, ConversionActivity.class);
            intent.putExtra("code", item.getCode());
            intent.putExtra("rate", item.getRate());
            intent.putExtra("title", item.getTitle());

            startActivity(intent);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCurrencies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        // Run network access on a separate thread;
        new Thread(new Task(urlSource)).start();
    } //

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.

    //Parse the XML string into a list of CurrencyItem objects
    private void parseXML(String xml) {

        //Starting fresh each time
        currencyItems.clear();
        allCurrencyItems.clear();

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

                    case XmlPullParser.START_TAG:
                        if ("item".equalsIgnoreCase(tagName)) {
                            insideItem = true;
                            currentItem = new CurrencyItem();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:
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
                                String title = text.trim();

                                //Removes "British Pound Sterling(GBP)/"
                                if (title.contains("/")) {
                                    title = title.substring(title.indexOf("/") + 1).trim();
                                }

                                currentItem.setTitle(title);

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
                                            Log.e("Parser", "Could not parse rate from: " + desc);
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

            //Filling additional list
            allCurrencyItems.addAll(currencyItems);

        } catch (Exception e) {
            Log.e("Parser", "Error parsing XML", e);
        }
    }

    private void filterCurrencies(String query) {
        currencyItems.clear();

        if (query == null || query.trim().isEmpty()) {
            //No search text shows all items
            currencyItems.addAll(allCurrencyItems);
        } else {
            String lower = query.toLowerCase();

            for (CurrencyItem item : allCurrencyItems) {
                //Matches on rate code OR title
                if (item.getCode().toLowerCase().contains(lower) ||
                        (item.getTitle() != null && item.getTitle().toLowerCase().contains(lower))) {
                    currencyItems.add(item);
                }
            }
        }

        currencyAdapter.notifyDataSetChanged();
    }


    private class Task implements Runnable
    {
        private String url;
        public Task(String aurl){
            url = aurl;
        }
        @Override
        public void run(){
            result = ""; //Reset before concatenating
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";


            Log.d("MyTask","in run");

            try
            {
                Log.d("MyTask","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine()) != null){
                    result = result + inputLine;
                }
                in.close();
            }
            catch (IOException ae) {
                Log.e("MyTask", "ioexception");
            }

            //Clean up any leading garbage characters
            int i = result.indexOf("<?"); //initial tag
            result = result.substring(i);

            //Clean up any trailing garbage at the end of the file
            i = result.indexOf("</rss>"); //final tag
            result = result.substring(0, i + 6);

            // Now that you have the xml data into result, you can parse it
            parseXML(result);

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    currencyAdapter.notifyDataSetChanged();

                }
            });
        }

    }

}