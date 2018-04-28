import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class dataUtils{

    static int sampleRate = 48000;
    static int samples_per_bit = 48;
    protected boolean Rx_run = true;
    int preamble_size = 3 * samples_per_bit;
    int scale = 32;
    int fft_winsize = 32;
    int num_pack = 100;



    byte[] preamble;





    public byte[] generate_all(String pathname) throws IOException,InterruptedException,FileNotFoundException{
        preamble_init();

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
        int total_input_size = result.length();  // read file total_input_size = 10000 bits
        int pack_data_size = total_input_size / num_pack;  // 10000 bits / 100 bags = 100
        int pack_size = preamble_size + pack_data_size * samples_per_bit; // pack_size = preamble + pure_data per bag * sample per bit
        System.out.print(pack_size);
        int total_output_size = total_input_size * samples_per_bit + preamble_size * num_pack;
        byte[] output = new byte[total_output_size];

        for(int i=0;i<num_pack;i++){
            int outbegin = i*pack_size;
            int outend = (i+1)*pack_size;
            String sub_result = result.substring(i*pack_data_size,(i+1)*pack_data_size);

            //System.out.print(sub_result.length()+"\n");
            generate_pack(sub_result,output,outbegin,outend);

        }

        return output;

    }

    public void generate_pack(String input,byte[] output,int outbegin,int outend){
        System.out.print("generate pack  No. "+ outbegin/(outend-outbegin) +" -----> "+ input.length() +" \n" );

        int pack_size = outend - outbegin;
        int index = outbegin;
        int input_index = 0;
        byte inputarray[] = input.getBytes();
        double frequencyOfSignal1 = sampleRate/fft_winsize*1;
        double frequencyOfSignal2 = sampleRate/fft_winsize*7;

        double samplingInterval1 = (double) (sampleRate/frequencyOfSignal1);
        double samplingInterval2 = (double) (sampleRate/frequencyOfSignal2);
        for(int k=0;k<preamble_size;k++) output[index++] = (byte)preamble[k];
        for(int i = 0;i<input.length();i+=2){

            if (inputarray[input_index] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle1 = (2.0 * Math.PI * k) / samplingInterval1;
                    output[index++] = (byte) (scale * Math.sin(angle1));
                }
            }else {
                    index += samples_per_bit;
                }
            if(inputarray[input_index+1] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle2 = (2.0 * Math.PI * k) / samplingInterval2;
                    output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle2));
                }
            }
            input_index+=2;
        }
    }


    public void preamble_init(){
      preamble = new byte [preamble_size];

      double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
      double frequencyOfSignal4 = 15000.0;

      double samplingInterval3 = (double) (sampleRate/frequencyOfSignal3);
      double samplingInterval4 = (double) (sampleRate/frequencyOfSignal4);

      for(int i =0;i<preamble.length;i=i+2){

          double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
          double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
          if (i>=preamble.length/3 && i < preamble.length/3*2){
              //System.out.print(scale*Math.sin(angle3)+" ");
              preamble[i] = (byte)(scale*Math.sin(angle3));
              //System.out.print(preamble[i]+"\n");

          }
          else {
            //System.out.print(scale*Math.sin(angle4)+" ");
            preamble[i] = (byte)(scale*Math.sin(angle4));
            //System.out.print(preamble[i]+"\n");
          }

      }

    }



    public static void main(String[] args) throws IOException,InterruptedException{
      dataUtils du = new dataUtils();
      System.out.println("---------Program Start---------");
      byte[] output;
      String pathname = "./input.txt";
      output = du.generate_all(pathname);



    }



  }
