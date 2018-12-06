package nl.tjeerdhans.k6neletest;

import java.io.IOException;
import java.net.URISyntaxException;

import ee.ioc.phon.android.speechutils.ContinuousRawAudioRecorder;
import ee.ioc.phon.netspeechapi.duplex.DuplexRecognitionSession;
import ee.ioc.phon.netspeechapi.duplex.RecognitionEventListener;
import ee.ioc.phon.netspeechapi.duplex.WsDuplexRecognitionSession;

/**
 * SpeechRecognizer thread
 */
class SpeechRecognizer implements Runnable {
    private volatile boolean _isRecording;
    private final Object _mutex = new Object();
    private ContinuousRawAudioRecorder _audioRecorder;
    private DuplexRecognitionSession _recognitionSession;

    SpeechRecognizer(RecognitionEventListener recognitionEventListener, String serverUrl) throws IOException, URISyntaxException {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        _audioRecorder = new ContinuousRawAudioRecorder();//(MediaRecorder.AudioSource.VOICE_RECOGNITION, 16000); // default audio source and sample rate (16k)

        _recognitionSession = new WsDuplexRecognitionSession(serverUrl);
        _recognitionSession.addRecognitionEventListener(recognitionEventListener);
    }

    @Override
    public void run() {
        synchronized (_mutex) {
            while (!_isRecording) {
                try {
                    _mutex.wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException("Wait() interrupted!", e);
                }
            }
        }
        try {
            _recognitionSession.connect();
            _audioRecorder.start();
            while (_isRecording) {
                Thread.sleep(200);
                byte[] readBuffer = _audioRecorder.consumeRecording();
                if (readBuffer != null)
                    _recognitionSession.sendChunk(readBuffer, false);
            }
            _recognitionSession.sendChunk(new byte[]{}, true);
            _audioRecorder.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    void setRecording(boolean isRecording) {
        synchronized (_mutex) {
            _isRecording = isRecording;
            if (_isRecording) {
                _mutex.notify();
            }
        }
    }
}
