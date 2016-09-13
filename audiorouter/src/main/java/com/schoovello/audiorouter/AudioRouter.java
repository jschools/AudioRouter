package com.schoovello.audiorouter;

import com.schoovello.audiorouter.source.FileSource;

import java.io.File;

public class AudioRouter {

    public static void main(String[] args) throws Throwable {
        File file = new File("E:\\Schooler\\Desktop\\some_audio.wav");
        FileSource fileSource = new FileSource(file);

        byte[] buffer = new byte[44_100 * 2 * 2 * 60];
        fileSource.readFrames(44_100, buffer, );
    }
}
