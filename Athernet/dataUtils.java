import java.io.*;
import javax.sound.sampled.*;
import java.util.*;
import javax.swing.*;
import java.nio.ByteBuffer;


public class dataUtils{

    static int sampleRate = 48000;
    static int samples_per_bit = 32;
    protected boolean Rx_run = true;

    byte[] preamble = {0, 36, 69, 97, 117, 126, 123, 108, 81, 45, 4, -38, -77, -107, -124, -125, -110,
            -80, -38, 8, 56, 95, 120, 126, 112, 79, 32, -20, -71, -108, -126, -119, -89, -41, 16, 71, 111,
            126, 114, 75, 18, -43, -95, -124, -121, -87, -29, 36, 93, 124, 120, 80, 17, -51, -105, -126,
            -107, -53, 18, 85, 123, 118, 72, 0, -73, -120, -121, -76, 0, 75, 121, 118, 66, -14, -89,
            -126, -107, -41, 44, 110, 124, 80, -2, -85, -126, -104, -28, 61, 120, 115, 49, -44, -114,
            -120, -59, 36, 111, 121, 59, -38, -113, -119, -50, 50, 119, 112, 30, -71, -125, -95, 0,
            96, 125, 65, -41, -118, -109, -19, 85, 126, 70, -40, -120, -105, -6, 98, 123, 46, -68,
            -126, -77, 38, 121, 98, -11, -111, -111, -10, 100, 119, 26, -91, -122, -35, 86, 124,
            39, -84, -124, -37, 87, 122, 29, -94, -119, -16, 104, 111, -3, -115, -98, 29, 124,
            76, -59, -126, -45, 89, 118, 5, -114, -94, 41, 126, 53, -87, -117, 2, 119, 81,-62,
             -125, -21, 111, 94, -50, -126, -28, 109, 94, -52, -125, -19, 114, 83, -69, -120, 6,
            124, 56};//, -96, -103, 47, 125, 9, -121, -62, 94, 101, -53, -123, 6, 125, 39, -112, -77,
            // 86, 105, -53, -121, 18, 126, 20, -120, -59, 100, 93, -67, -118, 23, 126, 26, -116, -74,
            // 84, 110, -35, -126, -23, 115, 79, -75, -117, 15, 125, 52, -97, -105, 37, 126, 37, -104,
            // -100, 42, 126, 37, -102, -104, 32, 126, 53, -88, -116, 5, 120, 82, -59, -126, -37, 97,
            // 113, -8, -119, -88, 46, 126, 59, -76, -124, -31, 96, 116, 8, -109, -107, 9, 116, 99, -21,
            // -120, -94, 27, 121, 93, -27, -120, -95, 21, 118, 101, -10, -112, -110, -7, 101, 119, 31,
            // -84, -125, -58, 58, 125, 87, -22, -114, -112, -20, 87, 126, 66, -44, -120, -105, -12, 90,
            // 126, 71, -34, -115, -115, -35, 67, 125, 98, 7, -88, -126, -84, 10, 98, 126, 76, -17, -101,
            // -125, -77, 14, 98, 126, 85, -1, -87, -126, -100, -22, 65, 121, 116, 55, -31, -103, -126,
            // -92, -16, 66, 119, 120, 70, -8, -83, -124, -115, -60, 16, 87, 124, 115, 64, -8, -78, -121,
            // -121, -82, -15, 55, 108, 126, 107, 55, -12, -75, -117, -125, -99, -47, 16, 76, 115, 126,
            // 106, 61, 2, -56, -102, -125, -121, -92, -44, 11, 64, 105, 125, 122, 98, 56, 6, -44, -87,
            // -116, -126, -118, -93, -55, -10, 35, 76, 106, 123, 126, 113};
             //88, 54, 15, -25, -62, -92,-114, -125, -125, -115,};

        int preamble_size = preamble.length;



    int fft_winsize = 32;
    int total_data_size = 50000;
    int num_pack = 50;
    int num_freq = 10;
    int freq_step = 1;
    int pack_data_size = total_data_size /num_pack * samples_per_bit / num_freq;
    int pack_size = preamble_size + pack_data_size;

    int scale = 127/num_freq;









    public byte[] generate_all(String pathname) throws IOException,InterruptedException,FileNotFoundException{
        preamble_init();

        File filename = new File(pathname); // 要读取以上路径的input。txt文件
        //InputStreamReader reader = new InputStreamReader(new FileInputStream(filename)); // 建立一个输入流对象reader
        //BufferedReader txtbr = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(filename));
        String result = "";
        String slice = "";

        byte[] buf = new byte[1];

        int in =0;
        //System.out.print(line);
        while (in != -1) {
            result = result + slice;
            //slice = txtbr.readLine(); // 一次读入一行数据
            in = input.read(buf,0,1);
            slice = Integer.toBinaryString((buf[0] & 0xFF) + 0x100).substring(1);

        }
        int total_input_size = result.length();  // read file total_input_size = 10000 bits
        int pack_data_size_raw = total_input_size / num_pack;  // 10000 bits / 100 bags = 100
        pack_data_size = pack_data_size_raw*samples_per_bit/num_freq;
        pack_size = preamble_size + pack_data_size; // pack_size = preamble + pure_data per bag * sample per bit
        System.out.print("input file size: "+total_input_size+"\n");
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
        //System.out.print("generate pack  No. "+ outbegin/(outend-outbegin) +" -----> "+ input.length() +" \n" );
        int pack_size = outend - outbegin;
        int index = outbegin;
        int input_index = 0;
        byte inputarray[] = input.getBytes();
        // double frequencyOfSignal1 = sampleRate/fft_winsize*1;
        // double frequencyOfSignal2 = sampleRate/fft_winsize*3;
        // double frequencyOfSignal3 = sampleRate/fft_winsize*5;
        // double frequencyOfSignal4 = sampleRate/fft_winsize*7;
        // double frequencyOfSignal5 = sampleRate/fft_winsize*9;
        //
        //
        // double samplingInterval1 = (double) (sampleRate/frequencyOfSignal1);
        // double samplingInterval2 = (double) (sampleRate/frequencyOfSignal2);
        // double samplingInterval3 = (double) (sampleRate/frequencyOfSignal3);
        // double samplingInterval4 = (double) (sampleRate/frequencyOfSignal4);
        // double samplingInterval5 = (double) (sampleRate/frequencyOfSignal5);


        double[] frequencyOfSignal = new double[num_freq];
        double[] samplingInterval = new double[num_freq];

        for(int i = 0;i < num_freq;i++) {
          frequencyOfSignal[i] = sampleRate/fft_winsize*(freq_step*i+1);
          samplingInterval[i] = (double) (sampleRate/frequencyOfSignal[i]);
        }



        for(int k=0;k<preamble_size;k++) {
          output[index++] = (byte)preamble[k];
          //System.out.print(output[index-1]+"\t");
        }
        for(int i = 0;i<input.length();i+=num_freq){

            if (inputarray[input_index] ==49){
                for (int k = 0;k<samples_per_bit;k++){
                    double angle = (2.0 * Math.PI * k) / samplingInterval[0];
                    output[index++] = (byte) (scale * Math.sin(angle));
                }
            }else {
                    index += samples_per_bit;
                }

            for(int j=1;j<num_freq;j++){
              if(inputarray[input_index+j] ==49){
                  for (int k = 0;k<samples_per_bit;k++){
                      double angle0 = (2.0 * Math.PI * k) / samplingInterval[j];
                      output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle0));
                  }
                }
            }
            // if(inputarray[input_index+2] ==49){
            //     for (int k = 0;k<samples_per_bit;k++){
            //         double angle3 = (2.0 * Math.PI * k) / samplingInterval3;
            //         output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle3));
            //     }
            // }
            // if(inputarray[input_index+3] ==49){
            //     for (int k = 0;k<samples_per_bit;k++){
            //         double angle4 = (2.0 * Math.PI * k) / samplingInterval4;
            //         output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle4));
            //         //System.out.print(output[index-samples_per_bit+k]+" \n");
            //
            //     }
            // }
            // if(inputarray[input_index+4] ==49){
            //     for (int k = 0;k<samples_per_bit;k++){
            //         double angle5 = (2.0 * Math.PI * k) / samplingInterval5;
            //         output[index-samples_per_bit+k] += (byte) (scale * Math.sin(angle5));
            //         //System.out.print(output[index-samples_per_bit+k]+" \n");
            //
            //     }
            // }
            input_index+=num_freq;
        }
    }


    public int find_start(ArrayList<Byte> tmpbuffer,int start,int end){
      //looking for the starting point
      int inner_prod;
      int start_index=0;
      //magic parameters
      double power=0;
      double lowerbound = 50*scale*preamble_size;
      double local_max = 0;

      int relaxion = 0; //in case that start_index is always zero

      // System.out.print("find size: ");
      // System.out.print(end-start);
      // System.out.print("\n");
      if(end-start<=pack_size){
        //System.out.print("NEED TO WAIT FOR READING\n");
        return -1;
      }
      for(int i=start;i<end-preamble_size;i++){

            inner_prod = 0;
            inner_prod = correlation(tmpbuffer,preamble,i,0,preamble_size);
            inner_prod = inner_prod/200*128;

                    // if(inner_prod>lowerbound){
                    //   System.out.print("  index--  ");
                    //   System.out.print(i+preamble_size);
                    //   System.out.print("inner product--  ");
                    //   System.out.print(inner_prod);
                    //   System.out.print("  power--  ");
                    //   System.out.print(power);
                    //   System.out.print("  raw data--  ");
                    //   System.out.print(tmpbuffer.get(i+preamble_size));
                    //   System.out.print("\n");
                    //  }
            power = power/64*63+tmpbuffer.get(i+preamble_size)*tmpbuffer.get(i+preamble_size)/64;
            if(inner_prod>2*power && inner_prod>local_max && inner_prod>lowerbound ) {
                start_index = i + preamble_size;
                local_max = inner_prod;

                  // System.out.print("  index--  ");
                  // System.out.print(i+preamble_size);
                  // System.out.print("inner product--  ");
                  // System.out.print(inner_prod);
                  // System.out.print("  power--  ");
                  // System.out.print(power);
                  // System.out.print("  raw data--  ");
                  // System.out.print(tmpbuffer[i+preamble_size]);
                  //System.out.print("\n");

             }

           if(start_index!=0&&i-start_index>=preamble_size) {
             System.out.print("start_index: "+start_index+" localmax "+local_max+"\n");
             return start_index;
           }

      }
      //if(local_max>0)System.out.print("localmax: "+local_max+" lowerbound:"+lowerbound+"\n");
      System.out.print("Did not find start\n");
      return 0;

    }

    public int decode_pack(ArrayList<Byte> tmpbuffer,int start_index,byte[] out_array,int outbegin){

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

         for(int j = 0;j<num_freq;j++){
          if(freq[freq_step*j+1]>200) {
              //System.out.print("2: "+freq[2]+"\n");
              out_array[out_index++] = 1;
            }
          else  out_array[out_index++] = 0;
        }
          //
          // if(freq[3]>400) {
          //     //System.out.print("10: "+freq[10]+"\n");
          //     out_array[out_index++] = 1;
          //   }
          // else out_array[out_index++] = 0;
          //
          // if(freq[5]>400) {
          //     //System.out.print("10: "+freq[10]+"\n");
          //     out_array[out_index++] = 1;
          //   }
          // else out_array[out_index++] = 0;
          //
          //
          // if(freq[7]>400) {
          //     //System.out.print("10: "+freq[10]+"\n");
          //     out_array[out_index++] = 1;
          //   }
          // else out_array[out_index++] = 0;
          //
          // if(freq[9]>400) {
          //     //System.out.print("10: "+freq[10]+"\n");
          //     out_array[out_index++] = 1;
          //   }
          // else out_array[out_index++] = 0;
          //
          }
       // /System.out.print("outindex "+out_index+"\n");


       return -1;

    }

    public void preamble_init(){
      //preamble = new byte [preamble_size];

//      double frequencyOfSignal3 = 5000.0; // prenmble frequency 15000hz 5000hz 15000hz
//      double frequencyOfSignal4 = 15000.0;
//
//      double samplingInterval3 = (double) (sampleRate/frequencyOfSignal3);
//      double samplingInterval4 = (double) (sampleRate/frequencyOfSignal4);
//
//      for(int i =0;i<preamble.length;i=i+2){
//
//          double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
//          double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
//          if (i>=preamble.length/3 && i < preamble.length/3*2){
//              //System.out.print(scale*Math.sin(angle3)+" ");
//              preamble[i] = (byte)(scale*Math.sin(angle3));
//              //System.out.print(preamble[i]+"\n");
//
//          }
//          else {
//            //System.out.print(scale*Math.sin(angle4)+" ");
//            preamble[i] = (byte)(scale*Math.sin(angle4));
//            //System.out.print(preamble[i]+"\n");
//          }
//
//      }




    }

    public int correlation(ArrayList<Byte> array0,byte[] array1,int begin0,int begin1,int length){
      int result = 0;
      for (int i = 0; i<length;i++){
        result+=array0.get(begin0+i)*array1[begin1+i];
      }
      return result;
    }




    public static void main(String[] args) throws IOException,InterruptedException{
      dataUtils du = new dataUtils();
      System.out.println("---------Program Start---------");
      byte[] output;
      String pathname = "./input.txt";
      output = du.generate_all(pathname);



    }



  }
