package me.merhlim;

import javax.sound.sampled.*;

public class ToneGenerator {

    public static int SAMPLE_RATE = 44100;
    private static double BYTE_LIMIT = 127.0;

    public static void playTone(double freq, double duration, double volume, Mixer.Info mixInfo) {
        double[] sData = generateSoundData(freq, duration, volume);
        byte[] frequencyList = new byte[sData.length];
        for (int i = 0; i < sData.length; i++) {
            frequencyList[i] = (byte) sData[i];
        }
        try {
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            SourceDataLine line = AudioSystem.getSourceDataLine(af,mixInfo);
            line.open(af, SAMPLE_RATE);
            line.start();
            line.write(frequencyList, 0, frequencyList.length);
            line.drain();
            line.close();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static double[] generateSoundData(double freq, double duration, double volume) {
        double[] dataList = new double[(int) (duration * SAMPLE_RATE)];
        for (int i = 0; i < dataList.length; i++) {
            dataList[i] = (Math.sin(2.0 * Math.PI * i / (double) (SAMPLE_RATE / freq))) * BYTE_LIMIT * (int) (duration * SAMPLE_RATE) * volume;
        }
        return dataList;
    }

}
