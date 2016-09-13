package com.schoovello.audiorouter.source;

import java.util.concurrent.CountDownLatch;

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

	private final AudioSource mInput;
	private final AudioSink mOutput;
	private final CountDownLatch mStartLatch;
	private boolean mStarted;
	private boolean mStopped;

	public AudioPump(AudioSource input, AudioSink output) {
		mInput = input;
		mOutput = output;
		mStartLatch = new CountDownLatch(3);
	}

	public void init(boolean startWhenReady) {
		if (startWhenReady) {
			start();
		}
		new Thread(() -> {
			try {
				mStartLatch.await();
				//noinspection InfiniteLoopStatement
				while (!mStopped) {
					pumpLoop();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
		new Thread(() -> {
			try {
				mInput.initBlocking();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mStartLatch.countDown();
		}).start();
		new Thread(() -> {
			try {
				mOutput.initBlocking();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mStartLatch.countDown();
		}).start();
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

	private void pumpLoop() {
		pump(mInput, mOutput);
		postPump(mInput, mOutput);
	}

	protected void pump(AudioSource source, AudioSink sink) {
		source.
	}

	protected void postPump(AudioSource source, AudioSink sink) {

	}
}
