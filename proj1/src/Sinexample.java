
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class Sinexample {
    private static int sampleRate = 8000;
    public static void main(String[] args) {
        final AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, true);
        try {
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af);
            line.start();
            //play Frequency = 200 Hz for 1 seconds
            play(line, generateSineWavefreq(200,1));
            line.drain();
            line.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static byte[] generateSineWavefreq(int frequencyOfSignal, int seconds) {
        // total samples = (duration in second) * (samples per second)
        byte[] sin = new byte[seconds * sampleRate];
        double samplingInterval = (double) (sampleRate / frequencyOfSignal);
        System.out.println("Sampling Frequency  : "+sampleRate);
        System.out.println("Frequency of Signal : "+frequencyOfSignal);
        System.out.println("Sampling Interval   : "+samplingInterval);
        for (int i = 0; i < sin.length; i++) {
            double angle = (2.0 * Math.PI * i) / samplingInterval;
            sin[i] = (byte) (Math.sin(angle) * 127);
            //System.out.println("" + sin[i]);
        }
        return sin;
    }

    private static void play(SourceDataLine line, byte[] array) {
        int length = sampleRate * array.length / 1000;
        line.write(array, 0, array.length);
    }
}