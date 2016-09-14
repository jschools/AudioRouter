package com.schoovello.audiorouter;

import com.schoovello.audiorouter.pipe.AudioPipe;
import com.schoovello.audiorouter.pipe.BufferPipe;
import com.schoovello.audiorouter.pipe.RealtimePipe;
import com.schoovello.audiorouter.pump.AudioPump;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.sink.SystemAudioSink;
import com.schoovello.audiorouter.sink.UdpSink;
import com.schoovello.audiorouter.source.AudioSource;
import com.schoovello.audiorouter.source.FileSource;
import com.schoovello.audiorouter.source.UdpSource;

import java.io.File;
import java.nio.file.Paths;

public class AudioRouter {

	private static final int CHUNK_DURATION_MS_MAX = 20;
	private static final int BUFFER_DURATION_MS = CHUNK_DURATION_MS_MAX * 2;

	public static void main(String[] args) throws Throwable {
        startReceiver();
		startSender();
    }

	private static void startReceiver() {
		new Thread(() -> {
			AudioSource networkSource = new UdpSource();
			AudioPipe bufferPipe = new BufferPipe((int) (2 * 44_100 * (BUFFER_DURATION_MS / 1000f)));
			AudioSink output = new SystemAudioSink();

			AudioPump networkPump = new AudioPump("Network->BufferPipe", networkSource, bufferPipe, CHUNK_DURATION_MS_MAX);
			AudioPump outputPump = new AudioPump("BufferPipe->Speakers", bufferPipe, output, CHUNK_DURATION_MS_MAX);

			networkPump.init(true);
			outputPump.init(true);
		}).start();
	}

	private static void startSender() {
		new Thread(() -> {
			File file = Paths.get(".", "audiorouter", "res", "audio_test.wav").toFile();
			AudioSource fileSource = new FileSource(file);
			AudioPipe realtimePipe = new RealtimePipe(2 * 44_100);
			AudioSink networkSink = new UdpSink("127.0.0.1", 8008);

			AudioPump filePump = new AudioPump("File->RealtimePipe", fileSource, realtimePipe, CHUNK_DURATION_MS_MAX);
			AudioPump networkPump = new AudioPump("RealtimePipe->Network", realtimePipe, networkSink, CHUNK_DURATION_MS_MAX);

			filePump.init(true);
			networkPump.init(true);
		}).start();
	}

}
