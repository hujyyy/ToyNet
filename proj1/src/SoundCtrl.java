import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


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
        // if(buffer[i]!=0&&flag==0&&i==20794){
        //     System.out.print(buffer[i]);
        //     System.out.print("\n");
        //    System.out.print(i);
        //    System.out.print("\n");
        //    flag=1;
        //  }
      //   if(buffer[i]!=-128){
      //
      //   //System.out.print(buffer[i]);
      //   //System.out.print("\n");
      // }
      }
      decoding(num_bit,samples_per_bit,buffer);

  }catch (Exception e){
    e.printStackTrace();
  }
}

 public void decoding(int num_bit,int samples_per_bit,byte[] tmpbuffer){
   byte preamble[] = new byte[6*samples_per_bit];

   byte refer0[] = new byte[samples_per_bit];
   byte refer1[] = new byte[samples_per_bit];

   double sample_rate = 44000;
   double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
   double frequencyOfSignal4 = 15000.0;

   double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
   double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);

   //generate a spacial pattern for synchornization
   for(int i =0;i<preamble.length;i++){

       double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
       double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
       if (i>=2*samples_per_bit && i < 4*samples_per_bit){
           preamble[i] = (byte)(5*Math.sin(angle3));
       }
       else {
           preamble[i] = (byte)(5*Math.sin(angle4));
       }
       //System.out.print(preamble[i]);
       //System.out.print("\n");
   }

   //generate reference 0 and 1
   double samplingInterval0 = (double) (sample_rate/2000);
   double samplingInterval1 = (double) (sample_rate/10000);
   for(int i =0;i<samples_per_bit;i++){

       double angle0 = (2.0 * Math.PI * i) / samplingInterval0;
       double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
       refer0[i] = (byte)(5*Math.sin(angle0));
       refer1[i] = (byte)(5*Math.sin(angle1));
       //System.out.print(preamble[i]);
       //System.out.print("\n");
   }

   int patternlength = 6*samples_per_bit;


   //magic parameters
   double power=0;
   double lowerbound = 500;
   double local_max = 0;

   int start_index = 0;

     //looking for the starting point
     int inner_prod;
     for(int i=44;i<tmpbuffer.length-patternlength;i++){
         //System.out.print(1000*Math.abs(tmpbuffer[i])+"e \t");
         inner_prod = 0;
           inner_prod = correlation(tmpbuffer,preamble,i,0,patternlength);
           inner_prod = inner_prod/200*128;

           if(inner_prod>lowerbound){
             System.out.print("inner product--  ");
             System.out.print(inner_prod);
             System.out.print("  power--  ");
             System.out.print(power);
             System.out.print("  index--  ");
             System.out.print(i+patternlength);
             System.out.print("  raw data--  ");
             System.out.print(tmpbuffer[i+patternlength]);
             System.out.print("\n");
            }

           power = power/64*63+tmpbuffer[i+patternlength]*tmpbuffer[i+patternlength]/64;
           if(inner_prod>2*power && inner_prod>local_max && inner_prod>lowerbound ) {
               start_index = i + patternlength;
               local_max = inner_prod;
            }
          if(start_index!=0&&i-start_index>patternlength) break;


     }
     System.out.println("start\t");
     System.out.print(start_index);
     System.out.print("\n");
     System.out.print(tmpbuffer.length);


     //int input_bit = 8;
     //int input_sample_rate = 440;
     int stop_index = start_index+num_bit*samples_per_bit;

     try{
     File file = new File("testoutput.txt");
     PrintStream ps = new PrintStream(new FileOutputStream(file));
     int inner_prod0 = 0;
     int inner_prod1 = 0;



     for(int i=start_index;i<stop_index;i=i+samples_per_bit){
       inner_prod0 = correlation(tmpbuffer,refer0,i+5,5,samples_per_bit-5);
       inner_prod1 = correlation(tmpbuffer,refer1,i+5,5,samples_per_bit-5);
       System.out.print("inner product0--  ");
       System.out.print(inner_prod0);
       System.out.print("inner product1--  ");
       System.out.print(inner_prod1);
       System.out.print("\n ");

       if(inner_prod0>inner_prod1) ps.append("0");
       else ps.append("1");
     }

     } catch (FileNotFoundException e) {
         e.printStackTrace();
     }

     // end of analysis



 }

 public int correlation(byte[] array0,byte[] array1,int begin0,int begin1,int length){
   int result = 0;
   for (int i = 0; i<length;i++){
     result+=array0[begin0+i]*array1[begin1+i];
   }
   if(result<0) result=~result+1;
   return result;
 }




  public void analysisAudio(int input_bit,int input_sample_rate){
      try {
          final AudioFormat format = getFormat();
          DataLine.Info info = new DataLine.Info(
                  TargetDataLine.class, format);
          final TargetDataLine line = (TargetDataLine)
                  AudioSystem.getLine(info);
          line.open(format);
          line.start();

          // byte preamble[] = new byte[6*input_sample_rate];
          // double sample_rate = 44000;
          // double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
          // double frequencyOfSignal4 = 15000.0;
          //
          // double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
          // double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);
          //
          // for(int i =0;i<preamble.length;i++){
          //
          //     double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
          //     double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
          //     if (i>=2*input_sample_rate && i < 4*input_sample_rate){
          //         preamble[i] = (byte)(5*Math.sin(angle3));
          //     }
          //     else {
          //         preamble[i] = (byte)(5*Math.sin(angle4));
          //     }
          // }

          Runnable runner = new Runnable() {
              int bufferSize = (int)format.getSampleRate()
                      * format.getFrameSize();
              byte buffer[] = new byte[bufferSize];
              int patternlength = 6*input_sample_rate;


              // //magic parameters
              // double power=0;
              // double threshold = 0.5;
              // double prev_val = 0;
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
                      System.out.print(tmpbuffer.length);
                      ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
                      AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
                      AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));

                      System.out.println("------------->");

                      decoding(input_bit,input_sample_rate,tmpbuffer);

                  //     //looking for the starting point
                  //     int inner_prod;
                  //     for(int i=0;i<tmpbuffer.length-patternlength;i++){
                  //         //System.out.print(1000*Math.abs(tmpbuffer[i])+"e \t");
                  //         inner_prod = 0;
                  //         for (int k=i;k<i+patternlength;k++){
                  //             for(int j=0;j<patternlength;j++) inner_prod+=preamble[j]*tmpbuffer[k];
                  //           }
                  //
                  //           power = power*0.9+tmpbuffer[i+patternlength]*tmpbuffer[i+patternlength];
                  //           if(inner_prod>power && inner_prod>prev_val && inner_prod>threshold ) start_index = i;
                  //           prev_val = power;
                  //
                  //     }
                  //     System.out.println("strat\t");
                  //     System.out.print(start_index);
                  //     //int input_bit = 8;
                  //     //int input_sample_rate = 440;
                  //     int stop_index = start_index+input_bit*input_sample_rate;
                  //     int count_array[] = new int[input_bit];
                  //     int count_sum = 0;
                  //
                  //     //System.out.println("hahhahah\n");
                  //     for(int round =0;round<input_bit;round++) {
                  //         //System.out.print("\n");
                  //         int count = 0;
                  //         for (int k = start_index+round*input_sample_rate+4; k < start_index+round*input_sample_rate+input_sample_rate-4; k++) {
                  //             System.out.print(500*tmpbuffer[k]+"\t");
                  //             int last_node = 500*tmpbuffer[k-1]+100;
                  //             int next_node = 500*tmpbuffer[k]+100;
                  //             //System.out.print(last_node * next_node +"\t");
                  //             if (last_node * next_node < 0) {
                  //                 count += 1;
                  //             }
                  //             //System.out.print("\n");
                  //
                  //         }
                  //         count_sum+=count;
                  //         count_array[round]=count;
                  //         //System.out.println("count:");
                  //         //System.out.print(count);
                  //         //System.out.print("\n");
                  //     }
                  //     int threshold = count_sum/input_bit;
                  //     //System.out.println("threshold is \n");
                  //     //System.out.print(threshold);
                  //     //System.out.print(start_index);
                  //
                  //     try {
                  //         File file = new File("testoutput.txt");
                  //         PrintStream ps = new PrintStream(new FileOutputStream(file));
                  //         for(int i=0;i<input_bit;i++) {
                  //             if(count_array[i]<=20){
                  //                 ps.append("0");// 在已有的基础上添加字符串
                  //             }
                  //             else{
                  //                 ps.append("1");// 在已有的基础上添加字符串
                  //             }
                  //         }
                  //     } catch (FileNotFoundException e) {
                  //
                  //         e.printStackTrace();
                  //     }
                  //
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


    public void FSK(int input_sample_rate){

        try {

            /* 读入TXT文件 */
            String pathname = "./INPUT.txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader txtbr = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            String result = "";
            line = txtbr.readLine();
            //System.out.print(line);
            while (line != null) {
                result = result + line;
                line = txtbr.readLine(); // 一次读入一行数据
            }
            int sample_rate = 44000;
            int bit_input_number = result.length();

            byte inputarray[] = result.getBytes();
            //System.out.print(inputarray[0]);
            byte soundarray[] = new byte[inputarray.length*input_sample_rate];
            double frequencyOfSignal1 = 2000.0;
            double frequencyOfSignal2 = 10000.0;
            byte preamble[] = new byte[6*input_sample_rate];
            double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
            double frequencyOfSignal4 = 15000.0;
            double samplingInterval1 = (double) (sample_rate/frequencyOfSignal1);
            double samplingInterval2 = (double) (sample_rate/frequencyOfSignal2);

            double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
            double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);

            for(int i =0;i<preamble.length;i++){

                double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
                double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
                if (i>=2*input_sample_rate && i < 4*input_sample_rate){
                    preamble[i] = (byte)(5*Math.sin(angle3));
                }
                else {
                    preamble[i] = (byte)(5*Math.sin(angle4));
                }
            }
            for(int i = 0;i<soundarray.length;i++){
                double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
                double angle2 = (2.0 * Math.PI * i) / samplingInterval2;
                int index = i/input_sample_rate;
                if (inputarray[index] ==48){
                    soundarray[i] = (byte) (5 * Math.sin(angle1));
                }
                else{
                    soundarray[i] = (byte) (5 * Math.sin(angle2));
                }
                System.out.print(soundarray[i]);
                System.out.print("----------\n");
            }


            byte out_array[] = new byte[preamble.length+soundarray.length];

            for(int x=0;x<preamble.length;x++){
                out_array[x] = preamble[x];
            }
            for(int y=0;y<soundarray.length;y++){
                out_array[preamble.length+y]=soundarray[y];
            }

            final AudioFormat txtformat = new AudioFormat(44000, 8, 1, true, true);
            SourceDataLine txtline = AudioSystem.getSourceDataLine(txtformat);
            txtline.open(txtformat);
            txtline.start();
            txtline.write(out_array,0,out_array.length);
            txtline.drain();
            txtline.close();
            ByteArrayInputStream temp = new ByteArrayInputStream(out_array);
            AudioInputStream temp1 = new AudioInputStream(temp,txtformat,out_array.length);
            AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out1.wav"));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }





  public static void main(String[] args) throws IOException,InterruptedException{

    System.out.println("---------Program Start---------");
    boolean ProgramRun=true;
    while (ProgramRun) {
      System.out.println("Choose one mood (1,2,3) ---> 1(Part1-1) 2(Part1-2) 3(part2) 4(read)...");
      Scanner scan = new Scanner(System.in);
      int userChoice = scan.nextInt();
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      SoundCtrl sc = new SoundCtrl();

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
          int input_sample_rate = 44;
          //@@ input_bit = bits number in txt  input_sample_rate = 440 --> 100kbs // 44 -->  1000kbs
          sc.analysisAudio(90,input_sample_rate);
          System.out.println("Enter part 3...");
          sc.FSK(input_sample_rate);
          //br.read();
          sc.running = false;
          System.out.println("Press Enter to start playing...");
          br.read();
          //System.out.println("---------Playing Start---------");
          sc.playAudio();

      }else if(userChoice==0){
        System.out.println("--------run offline test----------");
        sc.processoffline(90,44);

      }
      else if(userChoice==5){
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
