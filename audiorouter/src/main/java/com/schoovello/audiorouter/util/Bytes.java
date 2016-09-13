package com.schoovello.audiorouter.util;

import java.io.IOException;
import java.io.InputStream;

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
public final class Bytes {

	private Bytes() {
		// do not instantiate
	}

	public static int readIntSigned(InputStream in) throws IOException {
		return readIntSigned(in.read(), in.read(), in.read(), in.read());
	}

	public static int readIntSigned(byte[] buffer, int position) {
		return readIntSigned(buffer[position], buffer[position + 1],
				buffer[position + 2], buffer[position + 3]);
	}

	public static int readIntSigned(int a, int b, int c, int d) {
		int result;
		result  = (a << 24) & 0xff000000;
		result |= (b << 16) & 0x00ff0000;
		result |= (c << 8)  & 0x0000ff00;
		result |=  d        & 0x000000ff;
		return result;
	}

}
