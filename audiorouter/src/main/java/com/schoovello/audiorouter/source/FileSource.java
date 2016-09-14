package com.schoovello.audiorouter.source;

import com.schoovello.audiorouter.buffer.AudioBuffer;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

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
public class FileSource implements AudioSource {

	private File mFile;
    private AudioInputStream mInputStream;
	private AudioFormat mFormat;

    public FileSource(File file) {
		mFile = file;
    }

	@Override
	public void initBlocking() throws Exception {
		mInputStream = AudioSystem.getAudioInputStream(mFile);
		mFormat = mInputStream.getFormat();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return mFormat;
	}

	@Override
	public AudioBuffer read(int byteCount) throws IOException {
		final AudioBuffer buffer = AudioBuffer.obtain(byteCount);

		int totalReadCount = 0;
		int singleReadCount;

		while (totalReadCount < byteCount) {
			singleReadCount = mInputStream.read(buffer.data, totalReadCount, byteCount - totalReadCount);
			if (singleReadCount < 0) {
				if (totalReadCount > 0) {
					break;
				} else {
					return null;
				}
			}
			totalReadCount += singleReadCount;
		}

		buffer.setSize(totalReadCount);

		return buffer;
	}
}
