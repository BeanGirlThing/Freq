package me.merhlim;

import javax.sound.sampled.*;

public class ToneGenerator {
	
	protected static final int SAMPLE_RATE = 44100;

	public void playTone(long freq, int volume, long duration, Mixer.Info mixinfo) throws LineUnavailableException {
       final AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
       SourceDataLine line = AudioSystem.getSourceDataLine(af,mixinfo);
       line.open(af, SAMPLE_RATE);
       line.start();
	   
	   
	   byte[] toneBuffer;
	   int count;
	   
	   setVolume(line, volume);
	   toneBuffer = createSinWaveBuffer(freq, duration);
	   count = line.write(toneBuffer, 0, toneBuffer.length);


       line.drain();
       line.close();
    }
	
	public byte[] createSinWaveBuffer(double freq, long ms) {
       int samples = (int)((ms * SAMPLE_RATE) / 1000);
       byte[] output = new byte[samples];
           //
       double period = (double)SAMPLE_RATE / freq;
       for (int i = 0; i < output.length; i++) {
           double angle = 2.0 * Math.PI * i / period;
           output[i] = (byte)(Math.sin(angle) * 127f);  
		}

       return output;
   }
   
   private static void setVolume(SourceDataLine source,int volume){
	  try {
		FloatControl gainControl=(FloatControl)source.getControl(FloatControl.Type.MASTER_GAIN);
		BooleanControl muteControl=(BooleanControl)source.getControl(BooleanControl.Type.MUTE);
		if (volume == 0) {
		  muteControl.setValue(true);
		} else {
		  muteControl.setValue(false);
		  gainControl.setValue((float)(Math.log(volume / 100d) / Math.log(10.0) * 20.0));
		}
	  }
	 catch (Exception e) {}
}
 

}
