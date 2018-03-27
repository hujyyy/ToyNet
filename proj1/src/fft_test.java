import java.io.IOException;

public class fft_test {
    public static Complex[] fft(Complex[] x) {
        int N = x.length;
        //System.out.print(N);

        if (N == 1) return new Complex[] { x[0] };


        if (N % 2 != 0) {
            System.out.println(" Invalid number of Input for FFT");
            System.exit(0);
        }

        // Splitting the odd and even terms for calculation of FFT
        // fft of even terms
        Complex[] even = new Complex[N/2];
        for (int k = 0; k < N/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] q = fft(even);

        // fft of odd terms
        Complex[] odd  = even;
        for (int k = 0; k < N/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] r = fft(odd);

        // combine
        Complex[] y = new Complex[N];
        for (int k = 0; k < N/2; k++) {
            double kth = -2 * k * Math.PI / N;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = q[k].plus(wk.times(r[k]));
            y[k + N/2] = q[k].minus(wk.times(r[k]));
        }
        return y;
    }
    public static void getfreq(byte input[],double output[]){
        Complex comp[] = new Complex[64];
        for(int i =0;i<input.length;i++){
            comp[i] = new Complex(input[i],0);
        }

        Complex[] fft_out = fft(comp);

        for(int i=0;i<input.length;i++){
            output[i]=fft_out[i].abs();
            //System.out.print(y[i].toString()+"\n");
        }



    }
    public static void main(String[] args) throws IOException,InterruptedException{
        byte preamble[] = new byte[64];
        Complex test[] = new Complex[64];
        fft_test ft = new fft_test();
        double sample_rate = 44000;
        double frequencyOfSignal3 = 44000.0/64.0*10; // prenmble frequency 15000hz 5000hz 15000hz
        //double frequencyOfSignal4 = 15000.0;

        double samplingInterval3 = (double) (sample_rate/frequencyOfSignal3);
        //double samplingInterval4 = (double) (sample_rate/frequencyOfSignal4);

        for(int i =0;i<preamble.length;i++){
            double angle3 = (2.0 * Math.PI * i) / samplingInterval3;
            //double angle4 = (2.0 * Math.PI * i) / samplingInterval4;
            preamble[i] = (byte)(5*Math.sin(angle3)); // 5000

        }
        double output[]= new double[64];
        ft.getfreq(preamble,output);





    }


}


