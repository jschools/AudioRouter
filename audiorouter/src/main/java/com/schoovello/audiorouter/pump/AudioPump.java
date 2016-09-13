package com.schoovello.audiorouter.pump;

import com.schoovello.audiorouter.buffer.AudioBuffer;
import com.schoovello.audiorouter.error.ExceptionHandler;
import com.schoovello.audiorouter.log.Log;
import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.source.AudioSource;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioFormat;

/**
 * Copyright 2016 Jonathan
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class AudioPump {

	private final String mName;
	private final AudioSource mInput;
	private final AudioSink mOutput;
	private final CountDownLatch mStartLatch;

	private AudioPumpListener mListener;

	private boolean mStarted;
	private boolean mStopped;

	private int mBufferDurationMs;
	private int mBufferFrameCount;
	private float mInputFrameRate;
	private int mInputFrameSize;

	public AudioPump(String name, AudioSource input, AudioSink output, int bufferDurationMs) {
		mName = name;
		mInput = input;
		mOutput = output;
		mStartLatch = new CountDownLatch(3);
		mBufferDurationMs = bufferDurationMs;
	}

	public void setListener(AudioPumpListener listener) {
		mListener = listener;
	}

	public void init(boolean startWhenReady) {
		if (startWhenReady) {
			start();
		}

		// pumping thread
		new Thread(() -> {
			try {
				mStartLatch.await();

				if (mListener != null) {
					mListener.onStart();
				}

				//noinspection InfiniteLoopStatement
				while (!mStopped) {
					pumpLoop();
				}

				if (mListener != null) {
					mListener.onStop();
				}
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
			}
		}, mName + ".main").start();

		// input initialization thread
		new Thread(() -> {
			try {
				mInput.initBlocking();

				AudioFormat audioFormat = mInput.getAudioFormat();
				mInputFrameRate = audioFormat.getFrameRate();
				mInputFrameSize = audioFormat.getFrameSize();
				mBufferFrameCount = (int) (mInputFrameRate * (mBufferDurationMs / 1000f) + 0.5);

				Log.d(mName + " Input initialized, bufferFrameCount:" + mBufferFrameCount);
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
			}
			mStartLatch.countDown();
		}, mName + ".input.init").start();

		// output initialization thread
		new Thread(() -> {
			try {
				mOutput.initBlocking();

				Log.d(mName + " Output initialized");
			} catch (Exception e) {
				ExceptionHandler.handleException(e);
			}
			mStartLatch.countDown();
		}, mName + ".output.init").start();
	}

	public void start() {
		if (!mStarted) {
			mStarted = true;
			mStartLatch.countDown();
		}
	}

	public void stop() {
		mStopped = true;
	}

	private void pumpLoop() throws IOException {
		int framesPumped = pump(mInput, mOutput, mBufferFrameCount);

		postPump(mInput, mOutput, framesPumped);

		if (framesPumped < 0) {
			stop();
		}
	}

	protected float getInputFrameRate() {
		return mInputFrameRate;
	}

	protected int getInputFrameSize() {
		return mInputFrameSize;
	}

	protected int pump(AudioSource source, AudioSink sink, int frameCount) throws IOException {
		long startTime = System.currentTimeMillis();
		AudioBuffer buffer = source.read(frameCount);
		long readDurationMs = System.currentTimeMillis() - startTime;
		Log.d("Read took " + readDurationMs + "ms");
		if (buffer == null) {
			return -1;
		}
		sink.write(buffer);
		return buffer.size() / mInputFrameSize;
	}

	protected void postPump(AudioSource source, AudioSink sink, int framesPumped) {
		Log.d(mName + " Pumped frames: " + framesPumped);
	}

	public interface AudioPumpListener {
		void onStart();
		void onStop();
	}
}
