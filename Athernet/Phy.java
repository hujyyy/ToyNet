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
  static int samples_per_bit = du.samples_per_bit;
  protected boolean Rx_running = true;
  int scale = du.scale;
  String pathname = "./INPUT.bin";
  //String pathname = "./input.txt";
  int data_size = du.total_data_size; // in bits
  final AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
  Node_status status = Node_status.A;

  ///
  static int num_pack = du.num_pack;
  int pack_data_size_raw = data_size /num_pack;
  int pack_data_size = pack_data_size_raw*samples_per_bit/du.num_freq;
  int pack_size =  pack_data_size + du.preamble_size;
  static int preamble_size = du.preamble_size;
  ///




  Runnable RxRunner = new Runnable() {

        RxBuf rxbuf = new RxBuf();

        //byte buffer[] = new byte[pack_size*2];
        byte zerobuffer[] = new byte[1];
        boolean data_arrive = false;
        byte[] out_array = new byte[data_size];

        long  sleeptime_init = pack_size*2/48;
        long  sleeptime_perWait = pack_size/48;

        boolean waiting = false;
        long timeout = 3000;
        long check_timeout_start = 0;


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

        public void writeOut_bin(byte[] writeData){
          byte[] out = new byte[writeData.length/8];

          for(int i=0;i<out.length;i++){
              byte tmp = 0;
              for(int j=0;j<8;j++){
                int index = 8*i+j;
                tmp += (byte)(writeData[index]<<(7-j)) ;
              }
              out[i] = tmp;
          }

          try{
            OutputStream output = new BufferedOutputStream(new FileOutputStream("OUTPUT.bin"));
            output.write(out);
            output.close();
          }catch (IOException e){e.printStackTrace();}



        }

        public void run() {
          System.out.print("------------------Rx starts--------------\n");
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          Rx_running = true;

          int decode_start = 0;
          int find_start = 0;
          int pack_id = -1;

          //final AudioFormat format = getFormat();
          rxbuf.start_read();

               try {
                    long time_offset = System.currentTimeMillis();
                    Thread.sleep(sleeptime_init);
                    //rxbuf.stop_read();
                  while (Rx_running) {
                    //System.out.print("\n");
                    long curret_running_time = System.currentTimeMillis()-time_offset;
                    //System.out.print("Current running time: "+curret_running_time+" ms\n");

                    decode_start = du.find_start(rxbuf.bufferlist,find_start,rxbuf.bufferlist.size());


                    if(decode_start<=0){
                      //System.out.print("Did not find the start\n");

                      ////check time out
                      if(!waiting){
                        check_timeout_start = curret_running_time;
                        waiting = true;
                        }else{
                        if(curret_running_time-check_timeout_start>=timeout){
                          System.out.print("%%%%%%%%TIME OUT: LINK ERROR POSSIBILY OCCURS!%%%%%%%\n");
                          break;
                        }

                      }
                      /////////

                      Thread.sleep(sleeptime_perWait);
                      continue;

                    }else waiting =false;

                      // if(!data_arrive){
                      //   line.read(zerobuffer,0,1);
                      //   if(Math.abs(zerobuffer[0])<10) continue;
                      //   else data_arrive = true;
                      // }
                    //
                    //   int count = line.read(buffer, read_start, buffer.length/2-read_start);
                    // //  if (count > 0) out.write(buffer, 0, count);
                    //   //for(int i=0;i<preamble_size;i++) System.out.print(buffer[i]+"\t");
                    //   System.out.print("\n");
                    //   read_start = du.find_start(buffer,0,buffer.length/2);
                    //
                    //   if(read_start<0){
                    //     //if read_start<0, then we should do a shifting and find the start point from scratch
                    //     read_start = -read_start;
                    //     int extra_read = read_start-preamble_size;
                    //     System.out.print("need to read this much data ahead "+extra_read+"/"+buffer.length/2+"\n");
                    //
                    //     count = line.read(buffer, buffer.length/2, extra_read);
                    //     //if (count > 0) out.write(buffer, buffer.length/2, count);
                    //
                    //
                    //   }
                    //   else if(read_start>preamble_size){
                    //     System.out.print("preamble is partialy found at "+read_start+"/"+buffer.length/2+" need to go further\n");
                    //      count = line.read(buffer, buffer.length/2, preamble_size*3/2);
                    //     //if (count > 0) out.write(buffer, buffer.length/2, count);
                    //     int nextstart = buffer.length/2+preamble_size*3/2;
                    //     int decode_start = du.find_start(buffer,read_start-preamble_size,nextstart);
                    //
                    //
                    //     if(decode_start<0){
                    //       read_start = -decode_start;
                    //       //int extra_read = read_start+pack_data_size-nextstart;
                    //       //System.out.print("need to read this much data ahead "+extra_read+"/"+buffer.length/2+"\n");
                    //
                    //       //count = line.read(buffer, nextstart, extra_read);
                    //
                    //     }else read_start = decode_start;
                    //
                    //
                    //     System.out.print(read_start+" "+pack_data_size+" "+nextstart+"\n");
                    //     int extra_read = read_start+pack_data_size-nextstart;
                    //     System.out.print("need to read this much data ahead "+extra_read+"/"+buffer.length/2+"\n");
                    //      count = line.read(buffer, nextstart, extra_read);
                    //
                    //     //if (count > 0) out.write(buffer, buffer.length/2, count);
                    //
                    //   }
                    //   if(read_start==0) {
                    //     System.out.print("Did not find the start\n");
                    //     continue;
                    //   }
                        //if read_start>0 we just find the start_index
                    System.out.print("find the start at "+decode_start+"\n");
                    while(decode_start+pack_data_size>rxbuf.bufferlist.size()) Thread.sleep(sleeptime_perWait);
                    pack_id++;
                    //System.out.print(pack_id*pack_data_size_raw+"\n");
                    int ret  = du.decode_pack(rxbuf.bufferlist,decode_start,out_array,pack_id*pack_data_size_raw);
                    System.out.print("pack "+pack_id+" process done\n");
                    if(du.num_pack==pack_id+1) break;



                    find_start = decode_start+pack_data_size-2;
                    decode_start = 0;
                    //if(pack_id==10)break;

                  System.out.print("packid "+pack_id+" \n\n");
                }
                rxbuf.stop_read();
                rxbuf.buffer_reset();
                  // byte tmpbuffer[] = out.toByteArray();
                  // out.close();
                  // // analysis
                  // System.out.print(tmpbuffer.length);
                  // ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
                  // AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
                  // AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));
                  //
                  // System.out.println("------------->");

              } catch (Exception e) {
                   System.err.println("Problems: " + e);
                   System.exit(-1);
               }
          System.out.print("-----------------Rx ends--------------\n");
          writeOut_bin(out_array);
        }
    };

  Runnable TxRunner = new Runnable(){
      int ctr=-1;

      public void run(){
        System.out.print("---------------Tx starts--------------\n");
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
          System.out.print("SOUNDARRAY LENGTH in bits: "+soundarray.length+"\n");
          //for(int i =0;i<preamble_size;i++) System.out.print(soundarray[i]+"\t");
        }
      }catch (Exception e) {e.printStackTrace();}
      System.out.print("---------------Tx ends--------------\n");

    }


  };











  public static void main(String[] args) throws IOException,InterruptedException{
    long time_offset = System.currentTimeMillis();
    byte[] buffer = new byte[0x7ffffff];
    Phy phy = new Phy();
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("---------Program Start---------");
    String a ="01010101";
    System.out.print(a.substring(0,2)+" ");
    Thread RxThread = new Thread(phy.RxRunner);
    Thread TxThread = new Thread(phy.TxRunner);
    RxThread.start();
    //br.read();
    TxThread.start();
    while(RxThread.isAlive()||TxThread.isAlive()){}
    System.out.print("TOTAL TIME: "+(System.currentTimeMillis()-time_offset)+" ms\n");


  }

}
