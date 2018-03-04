import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;


public class SoundCtrl{

    protected boolean running;
    ByteArrayOutputStream out;

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

    public  void playAudio() {
      try {
        byte audio[] = out.toByteArray();
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

    public static void main(String[] args) throws IOException,InterruptedException{
      System.out.println("---------Program Start---------");
      System.out.println("Press Enter to start recording...");
      BufferedReader br = new BufferedReader(new
                      InputStreamReader(System.in));
      int temp = br.read();
      SoundCtrl sc = new SoundCtrl();
      System.out.println("---------Record Start---------");
      sc.recordAudio();
      Thread.sleep(1000*5);
      sc.running = false;
      System.out.println("Press Enter to start playing...");
      br.read();
      System.out.println("---------Playing Start---------");
      sc.playAudio();
    }

}
