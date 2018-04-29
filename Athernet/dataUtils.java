import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class dataUtils{

    static int sampleRate = 48000;
    static int samples_per_bit = 45;
    protected boolean Rx_run = true;
    int preamble_size = 1 * samples_per_bit;
    int scale = 32;

    int fft_winsize = 32;
    int num_pack = 100;
    int num_freq = 5;
    int pack_data_size = 10000 /num_pack * samples_per_bit / num_freq;
    int pack_size = preamble_size + pack_data_size;



    byte[] preamble = new byte[preamble_size];





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
        int pack_data_size_raw = total_input_size / num_pack;  // 10000 bits / 100 bags = 100
        pack_data_size = pack_data_size_raw*samples_per_bit/num_freq;
        pack_size = preamble_size + pack_data_size; // pack_size = preamble + pure_data per bag * sample per bit
        System.out.print(pack_size);
        int total_output_size = pack_size*num_pack;
        byte[] output = new byte[total_output_size];

        for(int i=0;i<num_pack;i++){
            int outbegin = i*pack_size;
            int outend = (i+1)*pack_size;
            String sub_result = result.substring(i*pack_data_size_raw,(i+1)*pack_data_size_raw);
            generate_pack(sub_result,output,outbegin,outend);

        }
        System.out.print("soundarray length: "+output.length+" \n");
        return output;

    }

    public void generate_pack(String input,byte[] output,int outbegin,int outend){
        System.out.print("generate pack  No. "+ outbegin/(outend-outbegin) +" -----> "+ input.length() +" \n" );
        int pack_size = outend - outbegin;
        int index = outbegin;
        int input_index = 0;
        byte inputarray[] = input.getBytes();
        double frequencyOfSignal1 = sampleRate/fft_winsize*1;
        double frequencyOfSignal2 = sampleRate/fft_winsize*3;
        double frequencyOfSignal3 = sampleRate/fft_winsize*5;
        double frequencyOfSignal4 = sampleRate/fft_winsize*7;
        double frequencyOfSignal5 = sampleRate/fft_winsize*9;


        double samplingInterval1 = (double) (sampleRate/frequencyOfSignal1);
        double samplingInterval2 = (double) (sampleRate/frequencyOfSignal2);
        double samplingInterval3 = (double) (sampleRate/frequencyOfSignal3);
        double samplingInterval4 = (double) (sampleRate/frequencyOfSignal4);
        double samplingInterval5 = (double) (sampleRate/frequencyOfSignal5);


        for(int k=0;k<preamble_size;k++) output[index++] = (byte)preamble[k];
        for(int i = 0;i<input.length();i+=num_freq){

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
            if(inputarray[input_index+2] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle3 = (2.0 * Math.PI * k) / samplingInterval3;
                    output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle3));
                }
            }
            if(inputarray[input_index+3] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle4 = (2.0 * Math.PI * k) / samplingInterval4;
                    output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle4));
                    //System.out.print(output[index-samples_per_bit+k]+" \n");

                }
            }
            if(inputarray[input_index+4] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle5 = (2.0 * Math.PI * k) / samplingInterval5;
                    output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle5));
                    //System.out.print(output[index-samples_per_bit+k]+" \n");

                }
            }
            input_index+=num_freq;
        }
    }


    public int find_start(byte[] tmpbuffer){
      //looking for the starting point
      int inner_prod;
      int start_index=0;
      //magic parameters
      double power=0;
      double lowerbound = 100*scale;
      double local_max = 0;

      int relaxion = 0; //in case that start_index is always zero

      for(int i=0;i<tmpbuffer.length-preamble_size;i++){
        /*if the preamble is partialy included in the current buffer,
         we do a shift operation and recall the function outside*/
        if(start_index+preamble_size>=tmpbuffer.length){
           int shiftlength = start_index-preamble_size;

           for(int m = shiftlength;m<tmpbuffer.length;m++) tmpbuffer[m-shiftlength+relaxion] = tmpbuffer[m];
           return -(tmpbuffer.length-shiftlength+relaxion); //take the reverse value to call find_start again after shifting
        }

            inner_prod = 0;
            inner_prod = correlation(tmpbuffer,preamble,i,0,preamble_size);
            inner_prod = inner_prod/200*128;

                    // if(inner_prod>lowerbound){
                    //   System.out.print("  index--  ");
                    //   System.out.print(i+patternlength);
                    //   System.out.print("inner product--  ");
                    //   System.out.print(inner_prod);
                    //   System.out.print("  power--  ");
                    //   System.out.print(power);
                    //   System.out.print("  raw data--  ");
                    //   System.out.print(tmpbuffer[i+patternlength]);
                    //   System.out.print("\n");
                    //  }
            power = power/64*63+tmpbuffer[i+preamble_size]*tmpbuffer[i+preamble_size]/64;
            if(inner_prod>2*power && inner_prod>local_max && inner_prod>lowerbound ) {
                start_index = i + preamble_size;
                local_max = inner_prod;
             }
           if(start_index!=0&&i-start_index>preamble_size*2/3) {
             System.out.print(start_index+"\n");
             if(start_index+pack_data_size>tmpbuffer.length){
               int shiftlength = start_index-preamble_size;
               for(int m = shiftlength;m<tmpbuffer.length;m++) tmpbuffer[m-shiftlength+relaxion] = tmpbuffer[m];
               return -(tmpbuffer.length-shiftlength+relaxion);
             }
             return start_index;
           }
      }
      return 0;

    }

    public int decode_pack(byte[] tmpbuffer,int start_index,byte[] out_array,int outbegin){

      fft_test ft = new fft_test();
       // System.out.println("inside decode\n start "+start_index);
       //
       // System.out.print("bufferlength "+tmpbuffer.length);

       int winsize = 32;
       int truncate = (samples_per_bit-winsize)/2;
       int stop_index = start_index + pack_data_size;
        //System.out.print(" stop_index "+stop_index+"\n");


       byte[] output = new byte[pack_data_size];

       double freq[] = new double[winsize];

       int out_index=outbegin;
       for(int i=start_index;i<stop_index;i=i+samples_per_bit){

         ft.getfreq(tmpbuffer,i+truncate,winsize,winsize,freq);
          if(freq[1]>670) {
              //System.out.print("2: "+freq[2]+"\n");
              out_array[out_index++] = 1;
            }
          else  out_array[out_index++] = 0;


          if(freq[3]>500) {
              //System.out.print("10: "+freq[10]+"\n");
              out_array[out_index++] = 1;
            }
          else out_array[out_index++] = 0;

          if(freq[5]>390) {
              //System.out.print("10: "+freq[10]+"\n");
              out_array[out_index++] = 1;
            }
          else out_array[out_index++] = 0;


          if(freq[7]>365) {
              //System.out.print("10: "+freq[10]+"\n");
              out_array[out_index++] = 1;
            }
          else out_array[out_index++] = 0;

          if(freq[9]>350) {
              //System.out.print("10: "+freq[10]+"\n");
              out_array[out_index++] = 1;
            }
          else out_array[out_index++] = 0;

          }
       //System.out.print("outindex "+out_index+"\n");
       //output.flush();


       return -1;

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

    public int correlation(byte[] array0,byte[] array1,int begin0,int begin1,int length){
      int result = 0;
      for (int i = 0; i<length;i++){
        result+=array0[begin0+i]*array1[begin1+i];
      }
      return result;
    }



    public dataUtils(){
      preamble_init();

    }
    public static void main(String[] args) throws IOException,InterruptedException{
      dataUtils du = new dataUtils();
      System.out.println("---------Program Start---------");
      byte[] output;
      String pathname = "./input.txt";
      output = du.generate_all(pathname);



    }



  }
