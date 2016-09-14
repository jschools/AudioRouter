package com.schoovello.audiorouter.pipe;

import com.schoovello.audiorouter.buffer.AudioBuffer;
import com.schoovello.audiorouter.buffer.AudioBufferSplicer;
import com.schoovello.audiorouter.log.Log;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public class RealtimePipe implements AudioPipe {

	private final AudioFormat mAudioFormat;
	private double mByteRate;
	private AudioBufferSplicer mSplicer;
	private long mFirstWriteTimeNanos = -1L;
	private int mBytesRead = 0;

	private int mFrameSize;

	public RealtimePipe(AudioFormat audioFormat) {
		mAudioFormat = audioFormat;
	}

	@Override
	public void initBlocking() throws Exception {
		mSplicer = new AudioBufferSplicer();
		mByteRate = mAudioFormat.getFrameRate() * mAudioFormat.getFrameSize();
		mFrameSize = mAudioFormat.getFrameSize();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return mAudioFormat;
	}

	@Override
	public void write(AudioBuffer buffer) throws IOException {
		if (mFirstWriteTimeNanos < 0) {
			mFirstWriteTimeNanos = System.nanoTime();
		}

		try {
			//noinspection SynchronizeOnNonFinalField
			synchronized (mSplicer) {
				while (!mSplicer.canAppend()) {
					mSplicer.wait();
				}
				mSplicer.append(buffer);
			}
		} catch (InterruptedException e) {
			// don't care
		}
	}

	@Override
	public AudioBuffer read(int byteCount) throws IOException {
		if (mFirstWriteTimeNanos < 0) {
			return AudioBuffer.obtain(0);
		}

		final long nowNanos = System.nanoTime();
		final long elapsedTimeNanos = nowNanos - mFirstWriteTimeNanos;

		final int liveBytePosition = (int) ((elapsedTimeNanos * mByteRate) / 1_000_000_000d);
		final int bytesBehind = liveBytePosition - mBytesRead;
		final int wholeFramesBehind = bytesBehind / mFrameSize;

		if (wholeFramesBehind < 512) {
			return AudioBuffer.obtain(0);
		}

		AudioBuffer buffer = mSplicer.remove(wholeFramesBehind * mFrameSize);
		final int readSize = buffer.size();
		mBytesRead += readSize;

		if (readSize > 0) {
			Log.d("RealtimePipe: vend " + readSize + " bytes" + "; bytesRead=" + mBytesRead + "; liveBytePosition=" + liveBytePosition);
		}

		return buffer;
	}
}
