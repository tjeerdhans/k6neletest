package nl.tjeerdhans.k6neletest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.URISyntaxException;

import ee.ioc.phon.netspeechapi.duplex.RecognitionEvent;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;

public class MainActivity extends AppCompatActivity implements RecognitionEventListener, OnRequestPermissionsResultCallback {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_INTERNET_PERMISSION = 201;

    private boolean _recording;
    private SpeechRecognizer _speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Audio record permission has not been granted.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            // Internet permission has not been granted.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    REQUEST_INTERNET_PERMISSION);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            _speechRecognizer = new SpeechRecognizer(this,
                    "ws://kaldith.westeurope.cloudapp.azure.com:80/client/ws/speech");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        _recording = false;
    }

    private void startRecognition() {
        Thread recognizerThread = new Thread(_speechRecognizer);
        recognizerThread.start();
        _speechRecognizer.setRecording(true);
        _recording = true;
    }

    public void ToggleRecording(View view) {
        _recording = !_recording;
        Button toggleButton = findViewById(R.id.buttonStartStop);
        if (_recording) {
            toggleButton.setText("STOP");
            startRecognition();
        } else {
            toggleButton.setText("START");
            _speechRecognizer.setRecording(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @param event from RecognitionEventListener
     */
    @Override
    public void onRecognitionEvent(RecognitionEvent event) {
        if (event.getStatus() == RecognitionEvent.STATUS_SUCCESS) {
            RecognitionEvent.Result result = event.getResult();

            if (result.isFinal()) {
                TextView textViewResult = findViewById(R.id.textViewResult);
                // String resultText="";
                result.getHypotheses().forEach(h -> textViewResult.append(h.getTranscript()));
                //textViewResult.append(resultText);
            }
        }
    }

    @Override
    public void onClose() {

    }
}
