package com.schoovello.audiorouter;

import com.schoovello.audiorouter.log.Log;
import com.schoovello.audiorouter.pipe.AudioPipe;
import com.schoovello.audiorouter.pipe.BufferPipe;
import com.schoovello.audiorouter.pump.AudioPump;
import com.schoovello.audiorouter.pump.AudioPump.AudioPumpListener;
import com.schoovello.audiorouter.pump.RealtimeAudioPump;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.sink.SystemAudioSink;
import com.schoovello.audiorouter.source.AudioSource;
import com.schoovello.audiorouter.source.FileSource;

import java.io.File;

public class AudioRouter {

	private static final int BUFFER_DURATION_MS = 20;

    public static void main(String[] args) throws Throwable {
        File file = new File("/Users/jonathanschooler/Desktop/scratch/audio_test.wav");
        AudioSource fileSource = new FileSource(file);
	    AudioSink systemAudioSink = new SystemAudioSink();

	    AudioPipe bufferPipe = new BufferPipe((int) (2 * 44_100 * (BUFFER_DURATION_MS / 1000f)));

	    AudioPump inputPump = new RealtimeAudioPump("Realtime", fileSource, bufferPipe, BUFFER_DURATION_MS);
	    inputPump.setListener(new AudioPumpListener() {
		    @Override
		    public void onStart() {
			    Log.d("Input pump started");
		    }

		    @Override
		    public void onStop() {
			    Log.d("Input pump stopped");
		    }
	    });
	    inputPump.init(false);

	    AudioPump outputPump = new AudioPump("Immediate", bufferPipe, systemAudioSink, BUFFER_DURATION_MS);
	    outputPump.setListener(new AudioPumpListener() {
		    @Override
		    public void onStart() {
			    Log.d("Output pump started");
		    }

		    @Override
		    public void onStop() {
			    Log.d("Output pump stopped");
			    System.exit(0);
		    }
	    });
	    outputPump.init(false);

	    Log.startNow();

	    outputPump.start();
	    inputPump.start();
    }
}
