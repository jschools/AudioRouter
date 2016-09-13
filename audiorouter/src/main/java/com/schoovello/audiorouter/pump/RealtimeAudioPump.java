package com.schoovello.audiorouter.pump;

import com.schoovello.audiorouter.sink.AudioSink;
import com.schoovello.audiorouter.source.AudioSource;

import java.io.IOException;

public class RealtimeAudioPump extends AudioPump {

	private long mFirstPumpTimeNanos = -1;
	private long mFramesPumped = 0;
	private long mBufferDurationMs;

	public RealtimeAudioPump(String name, AudioSource input, AudioSink output, int bufferDurationMs) {
		super(name, input, output, bufferDurationMs);

		mBufferDurationMs = bufferDurationMs;
	}

	@Override
	protected int pump(AudioSource source, AudioSink sink, int frameCount) throws IOException {
		final long currentTimeNanos = System.nanoTime();
		if (mFirstPumpTimeNanos < 0) {
			mFirstPumpTimeNanos = currentTimeNanos;
		}

		final long streamPositionNanos = currentTimeNanos - mFirstPumpTimeNanos;
		final long streamPositionFrame = (long) (streamPositionNanos / getInputFrameRate());
		final int framesBehind = (int) (streamPositionFrame - mFramesPumped);

		return super.pump(source, sink, framesBehind);
	}

	@Override
	protected void postPump(AudioSource source, AudioSink sink, int framesPumped) {
		super.postPump(source, sink, framesPumped);

		mFramesPumped += framesPumped;

		try {
			Thread.sleep(mBufferDurationMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
