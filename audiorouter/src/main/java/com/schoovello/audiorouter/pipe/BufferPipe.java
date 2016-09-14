package com.schoovello.audiorouter.pipe;

import com.schoovello.audiorouter.buffer.AudioBuffer;
import com.schoovello.audiorouter.error.ExceptionHandler;
import com.schoovello.audiorouter.log.Log;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sound.sampled.AudioFormat;

public class BufferPipe implements AudioPipe {

	private final AudioFormat mAudioFormat;
	private int mFrameSize;
	private int mInitialBufferFrames;
	private CountDownLatch mInitBufferLatch;
	private int mInitBufferSizeRemaining;
	private BlockingQueue<AudioBuffer> mQueue;

	public BufferPipe(AudioFormat audioFormat, int initialBufferDurationMs) {
		mAudioFormat = audioFormat;
		mInitialBufferFrames = (int) (mAudioFormat.getFrameSize() * (initialBufferDurationMs / 1000f));
	}

	@Override
	public void initBlocking() throws Exception {
		mQueue = new LinkedBlockingQueue<>();
		mInitBufferLatch = new CountDownLatch(1);
		mFrameSize = mAudioFormat.getFrameSize();
		mInitBufferSizeRemaining = mInitialBufferFrames * mFrameSize;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return mAudioFormat;
	}

	private int mTotalBytesWritten = 0;
	private int mTotalBytesRead = 0;

	@Override
	public void write(AudioBuffer buffer) throws IOException {
		try {
			if (buffer.size() > 0) {
				mQueue.put(buffer);
				mInitBufferSizeRemaining -= buffer.size();
				if (mInitBufferSizeRemaining <= 0) {
					mInitBufferLatch.countDown();
				} else {
					Log.d("Buffer pipe needs additional frames: " + mInitBufferSizeRemaining / mFrameSize);
				}
			} else {
				buffer.recycle();
			}
		} catch (InterruptedException e) {
			ExceptionHandler.handleException(e);
		}

		mTotalBytesWritten += buffer.size();
	}

	@Override
	public AudioBuffer read(int byteCount) throws IOException {
		try {
			if (mInitBufferSizeRemaining > 0) {
				Log.d("Buffer pipe waiting for frames: " + mInitBufferSizeRemaining / mFrameSize);
			}
			mInitBufferLatch.await();
			AudioBuffer buffer = mQueue.take();
			mTotalBytesRead += buffer.size();
//			Log.d("BufferPipe vended " + buffer.size() + " bytes for totalFramesRead=" + mTotalBytesRead / mFrameSize);
			return buffer;
		} catch (InterruptedException e) {
			ExceptionHandler.handleException(e);
		}
		return null;
	}

}
