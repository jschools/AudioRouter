package com.schoovello.audiorouter;

import com.schoovello.audiorouter.pipe.AudioPipe;
import com.schoovello.audiorouter.pipe.BufferPipe;
import com.schoovello.audiorouter.pump.AudioPump;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.sink.SystemAudioSink;
import com.schoovello.audiorouter.sink.UdpSink;
import com.schoovello.audiorouter.source.AudioSource;
import com.schoovello.audiorouter.source.SystemAudioSource;
import com.schoovello.audiorouter.source.UdpSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class AudioRouter {

	private static final int CHUNK_DURATION_MS_MAX = 1;
	private static final int BUFFER_DURATION_MS = 1;

	private static final AudioFormat FORMAT = new AudioFormat(Encoding.PCM_SIGNED, 96_000, 16, 2, 4, 96_000, false);

	public static void main(String[] args) throws Throwable {
		startReceiver();
		startSender();
    }

	private static void startReceiver() {
		new Thread(() -> {
			AudioSource networkSource = new UdpSource(FORMAT);
			AudioPipe bufferPipe = new BufferPipe(FORMAT, BUFFER_DURATION_MS);
			AudioSink output = new SystemAudioSink(FORMAT);

			AudioPump networkPump = new AudioPump("Network->BufferPipe", networkSource, output, CHUNK_DURATION_MS_MAX);
			AudioPump outputPump = new AudioPump("BufferPipe->Speakers", bufferPipe, output, CHUNK_DURATION_MS_MAX);

			networkPump.init(true);
			outputPump.init(true);
		}).start();
	}

	private static void startSender() {
		new Thread(() -> {
			AudioSource microphoneSource = new SystemAudioSource(FORMAT);
			AudioSink networkSink = new UdpSink("127.0.0.1", 8008);

			AudioPump networkPump = new AudioPump("RealtimePipe->Network", microphoneSource, networkSink, CHUNK_DURATION_MS_MAX);

			networkPump.init(true);
		}).start();
	}

}
