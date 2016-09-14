package com.schoovello.audiorouter.source;

import com.schoovello.audiorouter.buffer.AudioBuffer;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

public class SystemAudioSource implements AudioSource {

	private AudioFormat mAudioFormat;
	private int mFrameSize;
	private TargetDataLine mInput;

	public SystemAudioSource() {
		mAudioFormat = new AudioFormat(Encoding.PCM_SIGNED, 44_100, 16, 1, 2, 44_100, false);
	}

	@Override
	public void initBlocking() throws Exception {
		mFrameSize = mAudioFormat.getFrameSize();

		mInput = AudioSystem.getTargetDataLine(mAudioFormat);
		mInput.open(mAudioFormat);
		mInput.start();

		//noinspection StatementWithEmptyBody
		while (!mInput.isOpen()) {
			// spin
		}
	}

	@Override
	public AudioFormat getAudioFormat() {
		return mAudioFormat;
	}

	@Override
	public AudioBuffer read(int byteCount) throws IOException {
		AudioBuffer buffer = AudioBuffer.obtain(byteCount);

		int integralFramesRequested = byteCount / mFrameSize;
		int bytesToRead = integralFramesRequested * mFrameSize;

		int readCount = mInput.read(buffer.data, 0, bytesToRead);

		buffer.setSize(readCount);
		return buffer;
	}
}
