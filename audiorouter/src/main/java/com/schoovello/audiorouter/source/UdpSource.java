package com.schoovello.audiorouter.source;

import com.schoovello.audiorouter.buffer.AudioBuffer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

public class UdpSource implements AudioSource {

	private DatagramSocket mSocket;
	private DatagramPacket mPacket;
	private AudioFormat mAudioFormat;

	@Override
	public void initBlocking() throws Exception {
		mSocket = new DatagramSocket(8008);
		mPacket = new DatagramPacket(new byte[65536], 65536);
		mAudioFormat = new AudioFormat(Encoding.PCM_SIGNED, 44_100, 16, 1, 2, 44_100, false);
	}

	@Override
	public AudioFormat getAudioFormat() {
		return mAudioFormat;
	}

	@Override
	public AudioBuffer read(int byteCount) throws IOException {
		mSocket.receive(mPacket);

		final int length = mPacket.getLength();
		AudioBuffer buffer = AudioBuffer.obtain(length);
		System.arraycopy(mPacket.getData(), 0, buffer.data, 0, length);
		buffer.setSize(length);

		return buffer;
	}
}
