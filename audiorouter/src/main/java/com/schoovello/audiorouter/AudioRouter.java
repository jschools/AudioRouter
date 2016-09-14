package com.schoovello.audiorouter;

import com.schoovello.audiorouter.log.Log;
import com.schoovello.audiorouter.pipe.AudioPipe;
import com.schoovello.audiorouter.pipe.BufferPipe;
import com.schoovello.audiorouter.pipe.RealtimePipe;
import com.schoovello.audiorouter.pump.AudioPump;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.sink.SystemAudioSink;
import com.schoovello.audiorouter.source.AudioSource;
import com.schoovello.audiorouter.source.FileSource;

import java.io.File;
import java.nio.file.Paths;

public class AudioRouter {

	private static final int CHUNK_DURATION_MS_MAX = 20;
	private static final int BUFFER_DURATION_MS = CHUNK_DURATION_MS_MAX * 2;

	public static void main(String[] args) throws Throwable {
        File file = Paths.get(".", "audiorouter", "res", "audio_test.wav").toFile();
        AudioSource fileSource = new FileSource(file);
	    AudioSink systemAudioSink = new SystemAudioSink();

	    AudioPipe realtimePipe = new RealtimePipe(2 * 44_100);
	    AudioPipe bufferPipe = new BufferPipe((int) (2 * 44_100 * (BUFFER_DURATION_MS / 1000f)));

	    AudioPump inputPump = new AudioPump("Input->RealtimePipe", fileSource, realtimePipe, CHUNK_DURATION_MS_MAX);
	    inputPump.init(false);

	    AudioPump pipePump = new AudioPump("RealtimePipe->BufferPipe", realtimePipe, bufferPipe, CHUNK_DURATION_MS_MAX);
	    pipePump.init(false);

	    AudioPump outputPump = new AudioPump("BufferPipe->Output", bufferPipe, systemAudioSink, CHUNK_DURATION_MS_MAX);
	    outputPump.init(false);

	    Log.startNow();

	    inputPump.start();
	    pipePump.start();
	    outputPump.start();
    }
}
