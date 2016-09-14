package com.schoovello.audiorouter.sink;

import com.schoovello.audiorouter.buffer.AudioBuffer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSink implements AudioSink {

	private final String mDstAddress;
	private final int mPort;
	private DatagramSocket mSocket;
	private InetAddress mAddress;

	public UdpSink(String address, int port) {
		mDstAddress = address;
		mPort = port;
	}

	@Override
	public void initBlocking() throws Exception {
		mAddress = InetAddress.getByName(mDstAddress);
		mSocket = new DatagramSocket();
	}

	@Override
	public void write(AudioBuffer buffer) throws IOException {
		if (buffer.size() > 0) {
			DatagramPacket packet = new DatagramPacket(buffer.data, 0, buffer.size());
			packet.setAddress(mAddress);
			packet.setPort(mPort);
			mSocket.send(packet);
		}

		buffer.recycle();
	}
}
