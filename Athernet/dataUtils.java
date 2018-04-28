import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class dataUtils{

    static int sampleRate = 48000;
    static int samples_per_bit = 48;
    protected boolean Rx_run = true;
    int preamble_size = 132;
    int scale = 32;

    int num_pack = 100;


    String filename = "./input.txt";
    byte[] preamble;




    //
    public void generate_all(String filename,byte[] output){
      preamble_init();

    }

    public void generate_pack(String input,byte[] output,int outbegin,int outend){



    }


    public void preamble_init(){
      preamble = new byte [preamble_size*2];

      double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
      double frequencyOfSignal4 = 15000.0;

      double samplingInterval3 = (double) (sampleRate/frequencyOfSignal3);
      double samplingInterval4 = (double) (sampleRate/frequencyOfSignal4);

      for(int i =0;i<preamble.length;i=i+2){

          double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
          double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
          if (i>=preamble.length/3 && i < preamble.length/3*2){
              System.out.print(scale*Math.sin(angle3)+" ");
              preamble[i] = (byte)(scale*Math.sin(angle3));
              System.out.print(preamble[i]+"\n");

          }
          else {
            System.out.print(scale*Math.sin(angle4)+" ");
            preamble[i] = (byte)(scale*Math.sin(angle4));
            System.out.print(preamble[i]+"\n");
          }

      }

    }



    public static void main(String[] args) throws IOException,InterruptedException{
      dataUtils du = new dataUtils();
      System.out.println("---------Program Start---------");




    }



  }
