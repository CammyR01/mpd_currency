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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;
    private String result;
    private String url1="";
    private String urlSource="https://www.fx-exchange.com/gbp/rss.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        // More Code goes here

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
    private class Task implements Runnable
    {
        private String url;
        public Task(String aurl){
            url = aurl;
        }
        @Override
        public void run(){
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
            try {
                XmlPullParserFactory factory =
                        XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput( new StringReader( result ) );

//                YOUR PARSING HERE!!!



            } catch (XmlPullParserException e) {
                Log.e("Parsing","EXCEPTION" + e);
                //throw new RuntimeException(e);
//            } catch (IOException e) {
//                Log.e("Parsing","I/O EXCEPTION" + e);
                //throw new RuntimeException(e);
            }


            // Now update the TextView to display raw XML data
            // Probably not the best way to update TextView
            // but we are just getting started !

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    rawDataDisplay.setText(result);
                }
            });
        }

    }

}