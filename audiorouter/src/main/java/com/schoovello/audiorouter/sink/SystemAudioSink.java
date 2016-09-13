package com.schoovello.audiorouter.sink;

import com.schoovello.audiorouter.buffer.AudioBuffer;
import com.schoovello.audiorouter.log.Log;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SystemAudioSink implements AudioSink {

	private SourceDataLine mSourceDataLine;

	@Override
	public void initBlocking() throws Exception {
		AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 44_100, 16, 1, 2, 44_100, false);
		mSourceDataLine = AudioSystem.getSourceDataLine(null);
		mSourceDataLine.open(format);

		while (!mSourceDataLine.isOpen()) {
			// wait
		}

		new Thread(() -> {
			try {
				//noinspection InfiniteLoopStatement
				while (true) {
					Log.d("OUTPUT framePosition:" + mSourceDataLine.getLongFramePosition());
					Thread.sleep(5);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();

		mSourceDataLine.start();
	}

	@Override
	public void write(AudioBuffer buffer) throws IOException {
		int written = 0;
		int singleWrite;
		while (written < buffer.size()) {
			singleWrite = mSourceDataLine.write(buffer.data, written, buffer.size() - written);
			written += singleWrite;
		}

		buffer.recycle();
	}

}
