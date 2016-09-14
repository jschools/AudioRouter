package com.schoovello.audiorouter;

import com.schoovello.audiorouter.buffer.AudioBufferSplicer;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.sink.SystemAudioSink;
import com.schoovello.audiorouter.source.AudioSource;
import com.schoovello.audiorouter.source.FileSource;

import java.io.File;

public class Test {

	public static void main(String[] args) throws Throwable {
		File file = new File("/Users/jonathanschooler/Desktop/scratch/audio_test.wav");
		AudioSource fileSource = new FileSource(file);
		AudioSink systemAudioSink = new SystemAudioSink();

		AudioBufferSplicer smoother = new AudioBufferSplicer();

		fileSource.initBlocking();
		systemAudioSink.initBlocking();

		smoother.append(fileSource.read(100));
		smoother.append(fileSource.read(200));
		smoother.append(fileSource.read(300));

		systemAudioSink.write(smoother.remove(600));

		smoother.append(fileSource.read(400));
		systemAudioSink.write(smoother.remove(100));
		systemAudioSink.write(smoother.remove(100));
		systemAudioSink.write(smoother.remove(100));
		systemAudioSink.write(smoother.remove(100));
		systemAudioSink.write(smoother.remove(100));
		systemAudioSink.write(smoother.remove(1000));

		while (true) {}
	}

}
