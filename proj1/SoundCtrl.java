import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class SoundCtrl{

  protected boolean running;
  ByteArrayOutputStream out;
  ByteArrayOutputStream sinout;
  public void recordAudio(){
    try {
      final AudioFormat format = getFormat();
      DataLine.Info info = new DataLine.Info(
              TargetDataLine.class, format);
      final TargetDataLine line = (TargetDataLine)
              AudioSystem.getLine(info);
      line.open(format);
      line.start();
      Runnable runner = new Runnable() {
        int bufferSize = (int)format.getSampleRate()
                * format.getFrameSize();
        byte buffer[] = new byte[bufferSize];

        public void run() {
          out = new ByteArrayOutputStream();
          running = true;
          try {
            while (running) {
              int count =
                      line.read(buffer, 0, buffer.length);
              if (count > 0) {
                out.write(buffer, 0, count);
              }
            }
            out.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };
      Thread captureThread = new Thread(runner);
      captureThread.start();
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
    }

  }

  public  void playAudio(ByteArrayOutputStream out0) {
    try {
      byte audio[] = out0.toByteArray();
      // InputStream input =
      //   new ByteArrayInputStream(audio);
      final AudioFormat format = getFormat();
      // final AudioInputStream ais =
      //   new AudioInputStream(input, format,
      //   audio.length / format.getFrameSize());
      DataLine.Info info = new DataLine.Info(
              SourceDataLine.class, format);
      final SourceDataLine line = (SourceDataLine)
              AudioSystem.getLine(info);
      line.open(format);
      line.start();

      Runnable runner = new Runnable() {

        public void run() {
          line.write(audio, 0, audio.length);
          line.drain();
          line.close();

        }
      };
      Thread playThread = new Thread(runner);
      playThread.start();
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-4);
    }
  }


  //modify all the parameters of the signal
  private AudioFormat getFormat() {
    float sampleRate = 8000;
    int sampleSizeInBits = 8;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = true;
    return new AudioFormat(sampleRate,
            sampleSizeInBits, channels, signed, bigEndian);
  }

  public void playSinAudio() {
    final AudioFormat sinformat = new AudioFormat(44000, 8, 1, true, true);
    try {
      SourceDataLine line = AudioSystem.getSourceDataLine(sinformat);
      line.open(sinformat);
      line.start();
      Runnable runner = new Runnable() {
        int bufferSize = 100;
        //int bufferSize = (int) sinformat.getSampleRate()/1000;
        byte buffer[] = new byte[bufferSize];
        public void run() {
          //sinout = new ByteArrayOutputStream();
          running = true; // running = flag
          try {
            while (running) {
              double frequencyOfSignal1 = 1000.0;
              double frequencyOfSignal2 = 10000.0;
              double samplingInterval1 = (double) (sinformat.getSampleRate()/frequencyOfSignal1);
              double samplingInterval2 = (double) (sinformat.getSampleRate()/frequencyOfSignal2);
              System.out.println("Frequency of Signal 1 : "+ frequencyOfSignal1);
              System.out.println("Frequency of Signal 2 : "+ frequencyOfSignal2);
              for(int i=0;i<buffer.length;i++){
                //double time = i/sinformat.getSampleRate();
                //double sinValue = Math.sin(2*)
                double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
                double angle2 = (2.0 * Math.PI * i) / samplingInterval2;
                buffer[i] = (byte) (Math.sin(angle1) + Math.sin(angle2));
                line.write(buffer,0,buffer.length);
              }
              //line.drain();

            }
            line.close();
            System.out.println("hhhh ");

          } catch (Exception e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };

      Thread sinplayThread = new Thread(runner);
      sinplayThread.start();

    }catch (Exception e) {
      e.printStackTrace();
    }
  }

//  private static byte[] generateSineWavefreq(int frequencyOfSignal, int seconds) {
//    // total samples = (duration in second) * (samples per second)
//
//    byte[] sin = new byte[seconds * 8000];
//    double samplingInterval = (double) (8000 / frequencyOfSignal);
//    System.out.println("Sampling Frequency  : "+8000);
//    System.out.println("Frequency of Signal : "+frequencyOfSignal);
//    System.out.println("Sampling Interval   : "+samplingInterval);
//    for (int i = 0; i < sin.length; i++) {
//      double angle = (2.0 * Math.PI * i) / samplingInterval;
//      sin[i] = (byte) (Math.sin(angle) * 127);
//      //System.out.println("" + sin[i]);
//    }
//    return sin;
//  }

  public static void main(String[] args) throws IOException,InterruptedException{
    System.out.println("---------Program Start---------");
    System.out.println("Press Enter to start recording...");
    BufferedReader br = new BufferedReader(new
            InputStreamReader(System.in));
//      int temp = br.read();
    SoundCtrl sc = new SoundCtrl();
//      System.out.println("---------Record Start---------");
//      sc.recordAudio();
//      Thread.sleep(1000*5);
//      sc.running = false;
    System.out.println("Press Enter to start playing...");
    br.read();
    System.out.println("---------Playing Start---------");
    sc.playSinAudio();


  }

}
