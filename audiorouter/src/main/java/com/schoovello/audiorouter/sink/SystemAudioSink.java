package com.schoovello.audiorouter.sink;

import com.schoovello.audiorouter.buffer.AudioBuffer;
import com.schoovello.audiorouter.log.Log;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SystemAudioSink implements AudioSink {

	private AudioFormat mAudioFormat;
	private SourceDataLine mSourceDataLine;

	public SystemAudioSink(AudioFormat audioFormat) {
		mAudioFormat = audioFormat;
	}

	@Override
	public void initBlocking() throws Exception {
		mSourceDataLine = AudioSystem.getSourceDataLine(null);
		mSourceDataLine.open(mAudioFormat);

		while (!mSourceDataLine.isOpen()) {
			// wait
		}

		new Thread(() -> {
			try {
				//noinspection InfiniteLoopStatement
				while (true) {
					long framePosition = mSourceDataLine.getLongFramePosition();
					Log.d("OUTPUT framePosition:" + framePosition + "; bytePosition:" + framePosition * mAudioFormat.getFrameSize());
					Thread.sleep(1000);
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
