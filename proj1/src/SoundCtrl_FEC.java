import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class SoundCtrl_FEC{

  protected boolean running;
  ByteArrayOutputStream out;

  String tri_result="";






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
            byte tmpbuffer[] = out.toByteArray();
            out.close();
            // analysis


            // end of analysis
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
    float sampleRate = 44000;
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
            System.out.println("Frequency of Signal 1 : "+ 1000);
            System.out.println("Frequency of Signal 2 : "+ 10000);
            while (running) {
              double frequencyOfSignal1 = 1000.0;
              double frequencyOfSignal2 = 10000.0;
              double samplingInterval1 = (double) (sinformat.getSampleRate()/frequencyOfSignal1);
              double samplingInterval2 = (double) (sinformat.getSampleRate()/frequencyOfSignal2);
              for(int i=0;i<buffer.length;i++){
                double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
                double angle2 = (2.0 * Math.PI * i) / samplingInterval2;
                buffer[i] = (byte) (Math.sin(angle1) + Math.sin(angle2));

              }
                line.write(buffer,0,buffer.length);
              //line.drain();

            }
            line.drain();
            line.close();

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };

      Thread sinplayThread = new Thread(runner);
      sinplayThread.start();

    }catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void playpredefined(){

    try {
      AudioInputStream bgm = AudioSystem.getAudioInputStream(new File("bgm.wav"));
      AudioFormat format = bgm.getFormat();
      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
      SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);
      line.start();
      int nBytesRead = 0;
      byte[] buffer = new byte[512];
      while (true) {
        nBytesRead = bgm.read(buffer, 0, buffer.length);
        if (nBytesRead <= 0)
          break;
        line.write(buffer, 0, nBytesRead);
      }
      line.drain();
      line.close();
      running = false;
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  public void processoffline(int num_bit,int samples_per_bit){
    try{
      File file = new File("out-90.wav");
      InputStream fis = new FileInputStream(file);
      int nBytesRead = 0;


      byte[] buffer = new byte[88044];

      while (true) {
        nBytesRead = fis.read(buffer, 0, buffer.length);
        if (nBytesRead <= 0)
          break;
        //line.write(buffer,0,nBytesRead);
      }

      int flag=0;
      for(int i=0;i<buffer.length;i++) {


        if(buffer[i]<0) buffer[i]+=128;
        else buffer[i]-=128;

      }
      //decoding(num_bit,samples_per_bit,buffer);

  }catch (Exception e){
    e.printStackTrace();
  }
}

public int find_start(byte[] tmpbuffer,byte[] preamble,int packlength,int scale){
  int patternlength = preamble.length;

  //looking for the starting point
  int inner_prod;
  int start_index=0;
  //magic parameters
  double power=0;
  //double lowerbound = 5000;
  double lowerbound = 100*scale;
  double local_max = 0;

  int relaxion = 0; //in case that start_index is always zero

  for(int i=0;i<tmpbuffer.length-patternlength;i++){

    /*if the preamble is partialy included in the current buffer,
     we do a shift operation and recall the function outside*/
    if(start_index+patternlength>=tmpbuffer.length){
       int shiftlength = start_index-patternlength;

       for(int m = shiftlength;m<tmpbuffer.length;m++) tmpbuffer[m-shiftlength+relaxion] = tmpbuffer[m];
       return -(tmpbuffer.length-shiftlength+relaxion); //take the reverse value to call find_start again after shifting
    }

        inner_prod = 0;
        inner_prod = correlation(tmpbuffer,preamble,i,0,patternlength);
        inner_prod = inner_prod/200*128;

//                 if(i>pack_head+breaktime){
//                   System.out.print("inner product--  ");
//                   System.out.print(inner_prod);
//                   System.out.print("  power--  ");
//                   System.out.print(power);
//                   System.out.print("  index--  ");
//                   System.out.print(i+patternlength);
//                   System.out.print("  raw data--  ");
//                   System.out.print(tmpbuffer[i+patternlength]);
//                   System.out.print("\n");
//                  }

        power = power/64*63+tmpbuffer[i+patternlength]*tmpbuffer[i+patternlength]/64;
        if(inner_prod>2*power && inner_prod>local_max && inner_prod>lowerbound ) {
            start_index = i + patternlength;
            local_max = inner_prod;
         }

       if(start_index!=0&&i-start_index>patternlength) {
         //System.out.print("find start point");
         if(start_index+packlength>tmpbuffer.length){
           int shiftlength = start_index-patternlength;
           for(int m = shiftlength;m<tmpbuffer.length;m++) tmpbuffer[m-shiftlength+relaxion] = tmpbuffer[m];
           return -(tmpbuffer.length-shiftlength+relaxion);
         }
         return start_index;
       }
  }
  return 0;
}

//decode the data in the given buffer from the start_index
 public int decoding(int packlength,byte[] tmpbuffer,int start_index,byte[] refer0,byte[] refer1){
    //packlength>>=1;
    fft_test ft = new fft_test();
     System.out.println("start "+start_index);

     //System.out.print("bufferlength "+tmpbuffer.length);


     int truncate = 6;
     int samples_per_bit = 44;
     int stop_index = start_index+packlength;

      System.out.print("stop_index "+stop_index+"\n");

     int inner_prod0 = 0;
     int inner_prod1 = 0;

     //try{
     //Writer output = new BufferedWriter(new FileWriter("test.txt", true));
     int windowsize = 32;
     double freq[] = new double[windowsize];

     for(int i=start_index;i<stop_index;i=i+samples_per_bit){
       ft.getfreq(tmpbuffer,i+truncate,32,windowsize,freq);
       //inner_prod0 = correlation(tmpbuffer,refer0,i+truncate,truncate,samples_per_bit-truncate);
       //inner_prod1 = correlation(tmpbuffer,refer1,i+truncate,truncate,samples_per_bit-truncate);
       // System.out.print("inner product0--  ");
       // System.out.print(inner_prod0);
       // System.out.print("inner product1--  ");
       // System.out.print(inner_prod1);
       // System.out.print("\n ");
       // if(inner_prod0>1) output.append("1");
       // else output.append("0");
       // if(inner_prod1>0) output.append("1");
       // else output.append("0");
       if(freq[2]>460) tri_result += "1";
       else tri_result += "0";
       if(freq[9]>300) tri_result += "1";
       else tri_result += "0";
       if(freq[15]>130) {tri_result += "1";}
       else tri_result += "0";
     }
     //output.flush();

   //}catch(IOException e){e.printStackTrace();}

     return stop_index;
 }


  public void analysisAudio(int num_pack,int num_bit,int samples_per_bit){

    // try{
    //   Writer output = new BufferedWriter(new FileWriter("test.txt", false));
    // }catch(IOException e){e.printStackTrace();}

    int scale = 50;
    byte preamble[] = new byte[6*samples_per_bit];

    byte refer0[] = new byte[samples_per_bit];
    byte refer1[] = new byte[samples_per_bit];

    int sample_rate = 44000;

    get_preamble(preamble,samples_per_bit,sample_rate,scale);

    get_ref(refer0,refer1,samples_per_bit,sample_rate,scale);


      try {
          final AudioFormat format = getFormat();
          DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
          final TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
          line.open(format);
          line.start();


          Runnable runner = new Runnable() {
              int breaktime = sample_rate*2/(num_pack-1);
              int packlength = num_bit*samples_per_bit/num_pack/3;
              byte buffer[] = new byte[preamble.length+packlength+breaktime];
              int patternlength = 6*samples_per_bit;


              public void run() {
                  out = new ByteArrayOutputStream();
                  running = true;

                  int scale = 50;
                  int read_start = 0;
                  int pack_id = -1;
                  try {
                      while (running) {
                          int count = line.read(buffer, read_start, buffer.length-read_start);
                          if (count > 0) {
                              out.write(buffer, 0, count);
                          }
                          read_start = find_start(buffer,preamble,packlength,scale);
                          if(read_start<0){
                            //if read_start<0, then we should do a shifting and find the start point from scratch
                            read_start = -read_start;
                            System.out.print("Need to shift backwards, padding from "+read_start+"/"+buffer.length+"\n");
                            continue;
                          }else if(read_start>0){
                            //if read_start>0 we just find the start_index
                              System.out.print("find the start at "+read_start+"\n");
                              decoding(packlength,buffer,read_start,refer0,refer1);
                              pack_id++;
                              System.out.print("pack "+pack_id+" process done\n");
                              if(num_pack==pack_id+1) break;

                        }
                        if(read_start==0) System.out.print("Did not find the start\n");

                        read_start = 0;
                      }
                      byte tmpbuffer[] = out.toByteArray();
                      out.close();
                      // analysis
                      System.out.print(tmpbuffer.length);
                      ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
                      AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
                      AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));

                      System.out.println("------------->");

                      //for(int i=0;i<tmpbuffer.length;i++) if(tmpbuffer[i]>1) System.out.print(10*tmpbuffer[i]+"\n");
                      //decoding(num_pack,num_bit,samples_per_bit,tmpbuffer);



                  //     // end of analysis
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
  public int correlation(byte[] array0,byte[] array1,int begin0,int begin1,int length){
    int result = 0;
    for (int i = 0; i<length;i++){
      result+=array0[begin0+i]*array1[begin1+i];
    }
    if(result<0) result=~result+1;
    return result;
  }

  public void get_preamble(byte[] preamble,int samples_per_bit, double sample_rate,int scale){

    double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
    double frequencyOfSignal4 = 15000.0;

    double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
    double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);

    for(int i =0;i<preamble.length;i++){

        double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
        double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
        if (i>=2*samples_per_bit && i < 4*samples_per_bit){
            preamble[i] = (byte)(scale*Math.sin(angle3));
        }
        else {
            preamble[i] = (byte)(scale*Math.sin(angle4));
        }
        //System.out.print(preamble[i]);
        //System.out.print("\n");
    }

  }

  public void get_ref(byte[] refer0,byte[] refer1,int samples_per_bit,double sample_rate,int scale){

    int freq0 = 2000;
    int freq1 = 10000;

    //generate reference 0 and 1
    double samplingInterval0 = (double) (sample_rate/freq0);
    double samplingInterval1 = (double) (sample_rate/freq1);
    for(int i =0;i<samples_per_bit;i++){

        double angle0 = (2.0 * Math.PI * i) / samplingInterval0;
        double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
        refer0[i] = (byte)(scale*Math.sin(angle0));
        refer1[i] = (byte)(scale*Math.sin(angle1));
        //System.out.print(preamble[i]);
        //System.out.print("\n");
    }
  }


    public void FSK(int num_pack,int samples_per_bit){
      int scale = 50;
      int fft_winsize = 32;
        try {


            String pathname = "./INPUT0.txt";
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader txtbr = new BufferedReader(reader);
            String line = "";
            String result = "";
            line = txtbr.readLine();
            //System.out.print(line);
            while (line != null) {
                result = result + line;
                line = txtbr.readLine(); // 一次读入一行数据
            }

            result = result + result +result;//transfer 3 times

            int sample_rate = 44000;
            int bit_input_number = result.length();
            int breaktime = sample_rate*2/(num_pack-1);
            int patternlength = 6*samples_per_bit;

            byte inputarray[] = result.getBytes();
            //System.out.print(inputarray[0]);
            byte soundarray[] = new byte[inputarray.length*samples_per_bit/3+num_pack*patternlength+(num_pack-1)*breaktime];
            double frequencyOfSignal1 = 44000/fft_winsize*2;
            double frequencyOfSignal2 = 44000/fft_winsize*9;

            double frequencyOfSignal5 = 44000/fft_winsize*15;

            byte preamble[] = new byte[patternlength];
            double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
            double frequencyOfSignal4 = 15000.0;
            double samplingInterval1 = (double) (sample_rate/frequencyOfSignal1);
            double samplingInterval2 = (double) (sample_rate/frequencyOfSignal2);

            double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
            double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);

            double samplingInterval5 = (double) (sample_rate/frequencyOfSignal5);

            for(int i =0;i<preamble.length;i++){

                double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
                double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
                if (i>=2*samples_per_bit && i < 4*samples_per_bit){
                    preamble[i] = (byte)(scale*Math.sin(angle3));
                }
                else {
                    preamble[i] = (byte)(scale*Math.sin(angle4));
                }
            }

            int index = 0;
            int input_index = 0;
            int bits_per_pack = inputarray.length / num_pack;

            //loop to generate packs
            System.out.print(soundarray.length+"  "+index+"\n");
            for(int j = 0;j<num_pack;j++){

              //add preamble
              for(int k=0;k<patternlength;k++) soundarray[index++] = (byte)preamble[k];


              //modulation
              for(int i = 0;i<bits_per_pack;i+=3){
                if (inputarray [input_index] ==49){
                  //System.out.print(index+"\n");
                    for (int k = 0;k<samples_per_bit;k++){
                      double angle1 = (2.0 * Math.PI * k) / samplingInterval1;
                      soundarray[index++] = (byte) (scale * Math.sin(angle1));
                    }
                }else {
                    // for (int k = 0;k<samples_per_bit;k++){
                    //   double angle1 = (2.0 * Math.PI * k ) / samplingInterval1 + Math.PI;
                    //   soundarray[index++] = (byte) (scale * Math.sin(angle1));
                    // }
                    index += samples_per_bit;
                }
                if(inputarray [input_index+1] ==49){
                  for (int k = 0;k<samples_per_bit;k++){
                    double angle2 = (2.0 * Math.PI * k) / samplingInterval2;

                    soundarray[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle2));

                  }
                }

                if(inputarray [input_index+2] ==49){
                  for (int k = 0;k<samples_per_bit;k++){
                    double angle5 = (2.0 * Math.PI * k) / samplingInterval5;

                    soundarray[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle5));

                  }
                }


              input_index+=3;
            }

            //add break time , prevent some echo hopefully
            if(j!=num_pack-1) for(int k=0;k<breaktime;k++) soundarray[index++]=(byte)0;
          }

        System.out.print(soundarray.length+"  "+index+"\n");
            // byte out_array[] = new byte[preamble.length+soundarray.length];
            //
            // for(int x=0;x<preamble.length;x++){
            //     out_array[x] = preamble[x];
            // }
            // for(int y=0;y<soundarray.length;y++){
            //     out_array[preamble.length+y]=soundarray[y];
            // }

            final AudioFormat txtformat = new AudioFormat(44000, 8, 1, true, true);
            SourceDataLine txtline = AudioSystem.getSourceDataLine(txtformat);
            txtline.open(txtformat);
            txtline.start();
            txtline.write(soundarray,0,soundarray.length);
            txtline.drain();
            txtline.close();
            ByteArrayInputStream temp = new ByteArrayInputStream(soundarray);
            AudioInputStream aud = new AudioInputStream(temp,txtformat,soundarray.length);
            AudioSystem.write(aud,AudioFileFormat.Type.WAVE,new File("out1.wav"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void correction(int num_bits, int rep_times){
      byte rep[] = tri_result.getBytes();
      int tmp = num_bits*rep_times;
      int check = 0;

      System.out.print("the length of repetition output. Should be "+tmp+", actually is "+rep.length +"\n");
      try{
        Writer output = new BufferedWriter(new FileWriter("test0.txt", false));
//          Writer output1 = new BufferedWriter(new FileWriter("test_rep.txt", false));
//          for (int i =0;i<rep.length;i++){
//              if(rep[i]==49) output1.append("1");
//              else output1.append("0");
//          }
//          output1.flush();
        for (int i =0;i<num_bits;i++){
          for (int j = 0;j<rep_times;j++) check += rep[i+j*num_bits]-48;
          if(check/((rep_times>>1)+1)==1) output.append("1");
          else output.append("0");

          check = 0;
        }
        output.flush();



      }catch(IOException e){e.printStackTrace();}

    }



  public static void main(String[] args) throws IOException,InterruptedException{

    System.out.println("---------Program Start---------");
    boolean ProgramRun=true;
    int samples_per_bit = 44;
    int num_bits = 30000;
    int num_pack = 1000;
    int rep_times = 1;

    while (ProgramRun) {
      System.out.println("Choose one mood (1,2,3) --->\n 1(Part1-1)\n 2(Part1-2)\n 3(part2)\n 4(transfer msg)\n 5(listen and decode)\n...");
      Scanner scan = new Scanner(System.in);
      int userChoice = scan.nextInt();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      SoundCtrl_FEC sc = new SoundCtrl_FEC();

      //1->record 2->palyisn 3->exit
      if (userChoice == 1) {
        System.out.println("Enter Part 1-1...");
        System.out.println("Press Enter to start recording...");
        int temp = br.read();
        System.out.println("---------Record Start---------");
        sc.recordAudio();
        Thread.sleep(1000 * 5);
        sc.running = false;
        System.out.println("Press Enter to start playing...");
        br.read();
        System.out.println("---------Playing Start---------");
        sc.playAudio();

      } else if(userChoice==2){
        ///////  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        System.out.println("Enter Part 1-2...");
        System.out.println("Press Enter to start playing the predefined audio and recording...");
        br.read();
        System.out.println("---------Playing and Recording Start---------");
        sc.recordAudio();
        sc.playpredefined();
        System.out.println("Recording finishedm..Press Enter to play the record");
        br.read();
        System.out.println("---------Playing Start---------");
        sc.playAudio();


      } else if (userChoice == 3) {
        System.out.println("Enter Part 2 Paly Sin Sound ...");
        System.out.println("Press Enter to start playing...");
        br.read();
        System.out.println("---------Playing Sin Sound ---------");
        System.out.println("Press Enter to stop playing...");
        sc.playSinAudio();

        br.read();
        sc.running=false;



      } else if (userChoice==4){
          //@@ num_bit = bits number in txt  samples_per_bit = 440 --> 100kbs // 44 -->  1000kbs
          //sc.analysisAudio(5,10000,samples_per_bit);
          System.out.println("Enter part 3...");
          sc.FSK(num_pack,samples_per_bit);

          System.out.println("---------Playing Start---------\n");
          br.read();

          System.out.println("---------Playing end---------\n");

      }else if(userChoice==5){
        sc.analysisAudio(num_pack,num_bits*rep_times,samples_per_bit);

        br.read();
        sc.running = false;
        sc.correction(num_bits,rep_times);

        System.out.println("Press Enter to start playing...");

        br.read();
        sc.playAudio();
      }else if(userChoice==0){
        System.out.println("--------run offline test----------");
        sc.processoffline(1000,44);

      }
      else if(userChoice==6){
        System.out.println("--------Program End ----------");
        ProgramRun=false;
      }
      else{
        System.out.println("Wrong Input");
        continue;
      }
    }


  }

}
