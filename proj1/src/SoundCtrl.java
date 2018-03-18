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

    public void analysisAudio(int input_bit,int input_sample_rate){
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
                        System.out.print(tmpbuffer.length);
                        ByteArrayInputStream temp = new ByteArrayInputStream(tmpbuffer);
                        AudioInputStream temp1 = new AudioInputStream(temp,format,tmpbuffer.length);
                        AudioSystem.write(temp1,AudioFileFormat.Type.WAVE,new File("out.wav"));
                        int start_index = 0;
                        for(int i=0;i<tmpbuffer.length;i++){
                            if(Math.abs(tmpbuffer[i])>2){
                                start_index =i;
                                break;
                            }
                        }
                        //int input_bit = 8;
                        //int input_sample_rate = 440;
                        int stop_index = start_index+input_bit*input_sample_rate;
                        int count_array[] = new int[input_bit];
                        int count_sum = 0;

                        System.out.println("hahhahah\n");
                        for(int round =0;round<input_bit;round++) {
                            int count = 0;
                            for (int k = start_index+round*input_sample_rate; k < start_index+round*input_sample_rate+input_sample_rate; k++) {
                                if (Math.abs(100 * tmpbuffer[k]) <= 2) {
                                    count += 1;
                                }
                            }
                            count_sum+=count;
                            count_array[round]=count;
                            System.out.print(count);
                            System.out.print("\n");
                        }
                        int threshold = count_sum/input_bit;
                        System.out.println("threshold is \n");
                        System.out.print(threshold);

                        try {
                            File file = new File("testoutput.txt");
                            PrintStream ps = new PrintStream(new FileOutputStream(file));
                            for(int i=0;i<input_bit;i++) {
                                if(count_array[i]>=threshold){
                                    ps.append("0");// 在已有的基础上添加字符串
                                }
                                else{
                                    ps.append("1");// 在已有的基础上添加字符串
                                }
                            }
                        } catch (FileNotFoundException e) {

                            e.printStackTrace();
                        }

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
            byte inputarray[] = result.getBytes();
            //System.out.print(inputarray[0]);
            byte soundarray[] = new byte[inputarray.length*input_sample_rate];
            double frequencyOfSignal1 = 1200.0;
            double frequencyOfSignal2 = 12000.0;
            double samplingInterval1 = (double) (sample_rate/frequencyOfSignal1);
            double samplingInterval2 = (double) (sample_rate/frequencyOfSignal2);
            for(int i = 0;i<soundarray.length;i++){
                double angle1 = (2.0 * Math.PI * i) / samplingInterval1;
                double angle2 = (2.0 * Math.PI * i) / samplingInterval2;
                int index = i/input_sample_rate;
                if (inputarray[index] ==48){
                    soundarray[i] = (byte) (10 * Math.sin(angle1));
                }
                else{
                    soundarray[i] = (byte) (10 * Math.sin(angle2));
                }
                //System.out.print(soundarray[i]);
            }

            final AudioFormat txtformat = new AudioFormat(44000, 8, 1, true, true);
            SourceDataLine txtline = AudioSystem.getSourceDataLine(txtformat);
            txtline.open(txtformat);
            txtline.start();
            txtline.write(soundarray,0,soundarray.length);
            txtline.drain();
            txtline.close();

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
          //@@ input_bit = bits number in txt  input_sample_rate = 440 --> 100kbs // 44 -->  1000kbs
          sc.analysisAudio(8,440);
          System.out.println("Enter part 3...");
          sc.FSK(440);
          sc.running = false;
          System.out.println("Press Enter to start playing...");
          br.read();
          System.out.println("---------Playing Start---------");
          sc.playAudio();

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
