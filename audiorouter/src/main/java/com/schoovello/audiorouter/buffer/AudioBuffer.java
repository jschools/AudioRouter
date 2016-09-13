package com.schoovello.audiorouter.buffer;

import com.schoovello.audiorouter.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
public class AudioBuffer implements Comparable<AudioBuffer> {

	/**
	 * The backing buffer
	 */
	public final byte[] data;

	/**
	 * The number of valid elements in the buffer, starting with index 0
	 */
	private int mSize;

	public AudioBuffer(int length) {
		Log.d("Allocating buffer of length " + length);
		data = new byte[length];
	}

	public void setSize(int size) {
		this.mSize = size;
	}

	/**
	 * @return The number of elements in the buffer. This may be smaller than data.length.
	 */
	public int size() {
		return mSize;
	}

	public void recycle() {
		recycle(this);
	}

	@Override
	public int compareTo(AudioBuffer o) {
		return data.length - o.data.length;
	}

	private static final List<AudioBuffer> sSortedBuffers = new ArrayList<>(10);

	public static AudioBuffer obtain(int minSize) {
		synchronized (sSortedBuffers) {
			Iterator<AudioBuffer> it = sSortedBuffers.iterator();
			while (it.hasNext()) {
				AudioBuffer buffer = it.next();
				if (buffer.data.length >= minSize) {
					it.remove();
					return buffer;
				}
			}
			return new AudioBuffer(minSize);
		}
	}

	public static void recycle(AudioBuffer buffer) {
		synchronized (sSortedBuffers) {
			sSortedBuffers.add(buffer);
			Collections.sort(sSortedBuffers);
		}
	}
}
