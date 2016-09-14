package com.schoovello.audiorouter.buffer;

import java.util.ArrayDeque;
import java.util.Deque;

public class AudioBufferSplicer {

	private static final int DEFAULT_CAN_APPEND_THRESHOLD_BYTES = 4096;

	private Deque<AudioBuffer> mQueue;
	private int mAvailableBytes;
	private int mHeadIndex;
	private int mCanAppendThresholdBytes;

	public AudioBufferSplicer(int canAppendThresholdBytes) {
		mQueue = new ArrayDeque<>();
		mAvailableBytes = 0;
		mHeadIndex = 0;
		mCanAppendThresholdBytes = canAppendThresholdBytes;
	}

	public AudioBufferSplicer() {
		this(DEFAULT_CAN_APPEND_THRESHOLD_BYTES);
	}

	public synchronized int getAvailableBytes() {
		return mAvailableBytes;
	}

	public boolean canAppend() {
		return getAvailableBytes() < mCanAppendThresholdBytes;
	}

	public synchronized void append(AudioBuffer buffer) {
		mQueue.add(buffer);
		mAvailableBytes += buffer.size();

		notify();
	}

	public synchronized AudioBuffer remove(int requestSize) {
		// the buffer to return
		AudioBuffer resultBuffer;
		int resultSize;

		// look at the first buffer
		AudioBuffer headBuffer = mQueue.peek();

		if (headBuffer != null && mHeadIndex == 0 && headBuffer.size() == requestSize) {
			// if the first buffer is exactly the size we need, we can return it immediately
			resultBuffer = headBuffer;
			resultSize = resultBuffer.size();
			mQueue.remove();
		} else {
			// combine buffers from the head of the queue until the request is satisfied
			int bytesRemaining = requestSize;

			// get a new buffer
			resultBuffer = AudioBuffer.obtain(Math.min(requestSize, mAvailableBytes));
			byte[] data = resultBuffer.data;
			int dataWriteIndex = 0;

			// copy from the front of the queue until we have filled up the new buffer
			while (bytesRemaining > 0 && headBuffer != null) {
				// determine the max number of bytes we can read from this head
				final int headBufferAvailableLength = headBuffer.size() - mHeadIndex;

				if (headBufferAvailableLength <= 0) {
					// there are no bytes left to read from this buffer, so get rid of it
					mQueue.remove();
					headBuffer.recycle();

					// move to the next buffer
					headBuffer = mQueue.peek();
					mHeadIndex = 0;
				} else {
					// we can copy from this buffer
					int copyLength = Math.min(bytesRemaining, headBufferAvailableLength);

					// copy the data
					System.arraycopy(headBuffer.data, mHeadIndex, data, dataWriteIndex, copyLength);
					dataWriteIndex += copyLength;
					mHeadIndex += copyLength;
					bytesRemaining -= copyLength;
				}
			}

			// dataWriteIndex is effectively the same as the result size
			resultSize = dataWriteIndex;
		}

		// keep track of the total size
		mAvailableBytes -= resultSize;

		// set the buffer size
		resultBuffer.setSize(resultSize);

		try {
			return resultBuffer;
		} finally {
			notify();
		}
	}

}
