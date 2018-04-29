import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


enum Node_status{
  A,
  B

};


public class Phy{


  static dataUtils du = new dataUtils();

  static int sampleRate = 48000;
  static int samples_per_bit = 45;
  protected boolean Rx_running = true;
  int scale = 32;
  String pathname = "./input.txt";
  int data_size = 10000; // in bits
  final AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
  Node_status status = Node_status.A;

  ///
  static int num_pack = du.num_pack;
  int pack_data_size = data_size /num_pack *samples_per_bit / du.num_freq;
  int pack_size =  pack_data_size + du.preamble_size;
  static int preamble_size = du.preamble_size;
  ///

  static Runnable RxRunner;
  static Runnable TxRunner;

  public void init(){
    RxRunner = new Runnable() {
        int breaktime = 0;
        //int packlength = num_bit*samples_per_bit/num_pack/2;
        byte buffer[] = new byte[pack_size];
        byte zerobuffer[] = new byte[1];
        boolean data_arrive = false;
        int patternlength = 6*samples_per_bit;

        byte[] out_array = new byte[data_size];

        public void writeOut (byte[] writeData){
          try{
            Writer output = new BufferedWriter(new FileWriter("test.txt", false));
            for(int i=0;i<writeData.length;i++){
              if(writeData[i]==1) output.append("1");
              else output.append("0");
            }
            output.flush();
          }catch (IOException e){e.printStackTrace();}
        }


        public void run() {
          System.out.print("Rx starts--------------\n");
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          Rx_running = true;

          int read_start = 0;
          int pack_id = -1;

          //final AudioFormat format = getFormat();

              try {
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                  while (Rx_running) {
                      if(!data_arrive){
                        line.read(zerobuffer,0,1);
                        if(Math.abs(zerobuffer[0])<50) continue;
                        else data_arrive = true;
                      }
                      int count = line.read(buffer, read_start, buffer.length-read_start);
                      if (count > 0) out.write(buffer, 0, count);
                      read_start = preamble_size;//du.find_start(buffer);
                      System.out.print("read start"+read_start+"\n");
                      if(read_start<0){
                        //if read_start<0, then we should do a shifting and find the start point from scratch
                        read_start = -read_start;
                        System.out.print("Need to shift backwards, padding from "+read_start+"/"+buffer.length+"\n");
                        continue;
                      }else if(read_start>0){
                        //if read_start>0 we just find the start_index
                          System.out.print("find the start at "+read_start+"\n");
                          pack_id++;
                          int ret  = du.decode_pack(buffer,read_start,out_array,pack_id*data_size/num_pack);
                          System.out.print("pack "+pack_id+" process done\n");
                          if(du.num_pack==pack_id+1) break;

                    }
                    if(read_start==0) System.out.print("Did not find the start\n");

                    read_start = 0;
                  }
                  System.out.print("packid "+pack_id+" \n");
                  byte tmpbuffer[] = out.toByteArray();
                  out.close();
                  // analysis
                  System.out.print(tmpbuffer.length);
                  ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
                  AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
                  AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));

                  System.out.println("------------->");

              } catch (Exception e) {
                   System.err.println("I/O problems: " + e);
                   System.exit(-1);
               }
          System.out.print("Rx ends--------------\n");
          writeOut(out_array);
        }
    };
    TxRunner = new Runnable(){
      int ctr=-1;

      public void run(){
        System.out.print("Tx starts--------------");
        try{
          if(ctr==-1){
          byte[] soundarray = du.generate_all(pathname);
          final AudioFormat txtformat = new AudioFormat(sampleRate, 8, 1, true, true);
          SourceDataLine txtline = AudioSystem.getSourceDataLine(txtformat);
          txtline.open(txtformat);
          txtline.start();
          txtline.write(soundarray,0,soundarray.length);
          txtline.drain();
          txtline.close();
        }
      }catch (Exception e) {e.printStackTrace();}
      System.out.print("Tx ends--------------");

    }
  };
}










  public static void main(String[] args) throws IOException,InterruptedException{
    Phy phy = new Phy();
    System.out.println("---------Program Start---------");
    phy.init();
    String a ="01010101";
    System.out.print(a.substring(0,2));
    Thread RxThread = new Thread(phy.RxRunner);
    Thread TxThread = new Thread(phy.TxRunner);
    RxThread.start();
    TxThread.start();




  }

}
