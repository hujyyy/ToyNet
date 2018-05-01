import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class RxBuf{

    static int sampleRate = 48000;

    byte[] buffer = new byte[1];
    protected boolean running = false;

    final AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
    ArrayList<Byte> bufferlist = new ArrayList<Byte>();

    Runnable readRunner = new Runnable(){

      public void run(){

      running = true;
      try {
          DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
          final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
          line.open(format);
          line.start();
          while(running){

              int count = line.read(buffer, 0, 1);
              bufferlist.add(buffer[0]);

          }
          System.out.print("READING STOP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
          line.drain();
          line.close();

        }catch (Exception e) {
             System.err.println("I/O problems: " + e+"\n");
             System.exit(-1);
         }
      }


    };
    Thread readThread;


    //
    //
    public void buffer_reset(){
      bufferlist.clear();
    }
    //
    public void start_read(){
        readThread = new Thread(readRunner);
        readThread.start();
      }

    public void stop_read(){
      try{
        readThread.sleep(1);
        running = false;
      }catch(Exception e){e.printStackTrace();}
    }//must be called after start






    // public static void main(String[] args) {
    //   RxBuf rxbuf = new RxBuf();
    //   rxbuf.start_read();
    //   int tmp=0;
    //
    //   for(int i=0;i<10000;i++){
    //     while(rxbuf.bufferlist.size()<=i) {
    //       try {
    //           Thread.sleep(1);
    //       } catch (InterruptedException e) {
    //           e.printStackTrace();
    //       }
    //     }
    //
    //     System.out.print(rxbuf.bufferlist.get(i)+" \n");
    //
    //   }
    //   rxbuf.stop_read();
    //     //System.out.print(index+" "+end+" hhh\n");
    //
    //
    // }


  }
