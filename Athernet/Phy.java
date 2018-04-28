import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;

public class Phy{
  static int sampleRate = 48000;
  static int samples_per_bit = 40;
  protected boolean Rx_run = true;
  int preamble_size = 132;
  int scale = 32;

  byte[] preamble;
  Runnable RxRunner;
  Runnable TxRunner;

  void phy_init(){
    RxRunner = new Runnable() {
        int breaktime = 5;
        //int packlength = num_bit*samples_per_bit/num_pack/2;
        //byte buffer[] = new byte[preamble.length+packlength+breaktime];
        byte zerobuffer[] = new byte[1];
        boolean data_arrive = false;
        int patternlength = 6*samples_per_bit;

        public void run() {
        //     out = new ByteArrayOutputStream();
        //     running = true;
        //
        //     int scale = 50;
        //     int read_start = 0;
        //     int pack_id = -1;
        //     try {
        //         while (running) {
        //             if(!data_arrive){
        //               line.read(zerobuffer,0,1);
        //               if(Math.abs(zerobuffer[0])<50) continue;
        //               else data_arrive = true;
        //             }
        //             int count = line.read(buffer, read_start, buffer.length-read_start);
        //             if (count > 0) out.write(buffer, 0, count);
        //
        //             read_start = 265;//find_start(buffer,preamble,packlength,scale);
        //
        //             if(read_start<0){
        //               //if read_start<0, then we should do a shifting and find the start point from scratch
        //               read_start = -read_start;
        //               System.out.print("Need to shift backwards, padding from "+read_start+"/"+buffer.length+"\n");
        //               continue;
        //             }else if(read_start>0){
        //               //if read_start>0 we just find the start_index
        //                 System.out.print("find the start at "+read_start+"\n");
        //                 decoding(packlength,buffer,read_start);
        //                 pack_id++;
        //                 System.out.print("pack "+pack_id+" process done\n");
        //                 if(num_pack==pack_id+1) break;
        //
        //           }
        //           if(read_start==0) System.out.print("Did not find the start\n");
        //
        //           read_start = 0;
        //         }
        //         byte tmpbuffer[] = out.toByteArray();
        //         out.close();
        //         // analysis
        //         System.out.print(tmpbuffer.length);
        //         ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
        //         AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
        //         AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));
        //
        //         System.out.println("------------->");
        //
        //         //for(int i=0;i<tmpbuffer.length;i++) if(tmpbuffer[i]>1) System.out.print(10*tmpbuffer[i]+"\n");
        //         //decoding(num_pack,num_bit,samples_per_bit,tmpbuffer);
        //
        //
        //
        //     //     // end of analysis
        //     } cagdtch (IOException e) {
        //          System.err.println("I/O problems: " + e);
        //          System.exit(-1);
        //      }
         }
    };
    TxRunner = new Runnable(){



        public void run(){





        }
    };


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




  public int correlation(byte[] array0,byte[] array1,int begin0,int begin1,int length){
    int result = 0;
    for (int i = 0; i<length;i++){
      result+=array0[begin0+i]*array1[begin1+i];
    }
    return result;
  }





  public static void main(String[] args) throws IOException,InterruptedException{
    Phy phy = new Phy();
    System.out.println("---------Program Start---------");
    phy.preamble_init();
    String a ="01010101";
    System.out.print(a.substring(0,2));



  }

}
