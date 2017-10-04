package com.example.phillipwitkin.disdetector;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;

    private EditText inputEditText;
    private TextView resultTextView;
    private Button searchButton;
    private ProgressBar searchProgressBar;
    private ImageButton speakButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputEditText = (EditText)findViewById(R.id.inputTextView);
        searchButton = (Button)findViewById(R.id.searchButton);
        resultTextView = (TextView)findViewById(R.id.resultTextView);
        searchProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        speakButton = (ImageButton)findViewById(R.id.recordButton);

        searchProgressBar.setVisibility(View.INVISIBLE);

        searchButton.setOnClickListener(searchAction);
        speakButton.setOnClickListener(recordSpeech);

    }

    private View.OnClickListener searchAction = new View.OnClickListener(){
        public void onClick(View v){
            Uri builtUri2 = Uri.parse("https://api.meaningcloud.com/sentiment-2.1").buildUpon()
                    .appendQueryParameter("key","5cefecfee765dde6c9c943db4c891c88")
                    .appendQueryParameter("txt", inputEditText.getText().toString())
                    .appendQueryParameter("lang", "en")
                    .build();
            new FindSentimentTask().execute(builtUri2.toString());
        }
    };

    public View.OnClickListener recordSpeech = new View.OnClickListener(){
        public void onClick(View v){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
            try {
                startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    inputEditText.setText(result.get(0));
                    searchAction.onClick(inputEditText);
                }
                break;
            }

        }
    }


    public static String getResponseFromHttpUrl(String url) throws IOException {
        URL theURL = new URL(url);
        HttpURLConnection urlConnection = (HttpURLConnection) theURL.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }

        } finally {
            urlConnection.disconnect();
        }
    }


    private class FindSentimentTask extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){
            searchProgressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String...url){
            //android.os.Debug.waitForDebugger();
            String toReturn= "DID NOT WORK";
            try
            {
                toReturn=getResponseFromHttpUrl(url[0]);

            }catch (Exception e)
            {
                Log.d("ErrorInApp","exception on get Response from HTTP call" + e.getMessage());
                return toReturn;
            }
            return toReturn;
        }

        protected void onProgressUpdate(){

        }

        protected void onPostExecute(String sentimentData) {
            searchProgressBar.setVisibility(View.INVISIBLE);
            int x = 5;
            x = x + 1;
            /*P+: strong positive
            P: positive
            NEU: neutral
            N: negative
            N+: strong negative
            NONE: without sentiment*/
            String response = null;
            try {
                JSONObject sentimentJSON = new JSONObject(sentimentData);
                String scoreTag = sentimentJSON.get("score_tag").toString();
                switch(scoreTag){
                    case "P+":
                        scoreTag="strong positive";
                        break;
                    case "P":
                        scoreTag="positive";
                        break;
                    case "NEU":
                        scoreTag="neutral";
                        break;
                    case "N":
                        scoreTag="negative";
                        break;
                    case "N+":
                        scoreTag="strong neutral";
                        break;
                    case "NONE":
                        scoreTag="without sentiment";
                        break;
                    default:
                        scoreTag="no score";
                }


                String confidence = sentimentJSON.get("confidence").toString();
                String irony = sentimentJSON.get("irony").toString();
                response = " Score: " + scoreTag + "\n Confidence: " + confidence + "\n Irony: " + irony;
            } catch (Exception e) {
                Log.d("sentimentJSON",e.toString());
            }
            if (response != null){
                resultTextView.setText(response);
            }else {
                resultTextView.setText("Problem getting response");
            }
        }

    }


}
