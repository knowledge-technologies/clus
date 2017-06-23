//daniela

package clus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;

class Numero {
    double x;
    double y;
    double v;
    double v1;
    
    public Numero(double x, double y, double v, double v1){
        this.x = x;
        this.y = y;
        this.v = v;
        this.v1 = v1;
    }
}

public class PredictionAnalyzer {
    static float D[][];
    static int N;
    static Numero punti[];
    static Numero puntiM[];
    static float maxdist;
    
    public static void calcolaW(){
        for(int i = 0;i<N;i++)
            for(int j = 0;j<N;j++){
                D[i][j] = ((float)Math.sqrt((float)(Math.pow(punti[i].x-punti[j].x, 2)+Math.pow(punti[i].y-punti[j].y, 2))));
                if (D[i][j]>maxdist) maxdist = D[i][j];
            }
    }    
    
    public static float weight(int i, int j, float bandwidth){
        if(D[i][j] == 0){
            return 0;
        } else if(D[i][j] <= bandwidth){
            return 1-(((float)Math.sqrt((float)(Math.pow(punti[i].x-punti[j].x, 2)+Math.pow(punti[i].y-punti[j].y, 2))))/bandwidth);
        } else{
            return 0;
        }
        
    }
    
    public static float MoranIndex1(float bandwidth){
        float num,den;
        float tempSumN = 0;
        float tempSumW = 0;
        float tempSumD = 0;    
        float MediaV = 0;
        for(int i = 0;i<N;i++){
            MediaV += punti[i].v;
        }
        MediaV /= N;
        
        for(int i = 0;i<N;i++){
            for(int j = 0;j<N;j++){
                tempSumN += ((weight(i,j,bandwidth)*(punti[i].v-MediaV)*(punti[j].v-MediaV)));
                tempSumW += (weight(i,j,bandwidth));
            }
            tempSumD += Math.pow(punti[i].v-MediaV, 2);
        }
        num = N*tempSumN;
        den = tempSumW*tempSumD;
        float I = num/den;        
        return I;
    }
    
    public static float MoranIndex2(float bandwidth){
        float num,den;
        float tempSumN = 0;
        float tempSumW = 0;
        float tempSumD = 0;    
        float MediaV = 0;
        for(int i = 0;i<N;i++){
            MediaV += punti[i].v1;
        }
        MediaV /= N;
        
        for(int i = 0;i<N;i++){
            for(int j = 0;j<N;j++){
                tempSumN += ((weight(i,j,bandwidth)*(punti[i].v1-MediaV)*(punti[j].v1-MediaV)));
                tempSumW += (weight(i,j,bandwidth));
            }
            tempSumD += Math.pow(punti[i].v1-MediaV, 2);
        }
        num = N*tempSumN;
        den = tempSumW*tempSumD;
        float I = num/den;        
        return I;
    }
    
    public static float MoranIndex11(float bandwidth){
        float num,den;
        float tempSumN = 0;
        float tempSumW = 0;
        float tempSumD = 0;    
        float MediaV = 0;
        for(int i = 0;i<N;i++)
            MediaV += puntiM[i].v;
        MediaV /= N;
        
        for(int i = 0;i<N;i++){
            for(int j = 0;j<N;j++){
                tempSumN += ((weight(i,j,bandwidth)*(puntiM[i].v-MediaV)*(puntiM[j].v-MediaV)));
                tempSumW += (weight(i,j,bandwidth));
            }
            tempSumD += Math.pow(puntiM[i].v-MediaV, 2);
        }
        num = N*tempSumN;
        den = tempSumW*tempSumD;
        float I = num/den;        
        return I;
    }
    
    public static float MoranIndex22(float bandwidth){
        float num,den;
        float tempSumN = 0;
        float tempSumW = 0;
        float tempSumD = 0;    
        float MediaV = 0;
        for(int i = 0;i<N;i++)
            MediaV += puntiM[i].v1;
        MediaV /= N;
        
        for(int i = 0;i<N;i++){
            for(int j = 0;j<N;j++){
                tempSumN += ((weight(i,j,bandwidth)*(puntiM[i].v1-MediaV)*(puntiM[j].v1-MediaV)));
                tempSumW += (weight(i,j,bandwidth));
            }
            tempSumD += Math.pow(puntiM[i].v1-MediaV, 2);
        }
        num = N*tempSumN;
        den = tempSumW*tempSumD;
        float I = num/den;        
        return I;
    }
    
    public static void calculateI(String fileName, int fileSize, double b) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        boolean datastarted = false;
        int n = 0; N = fileSize; D= new float[N][N];punti = new Numero[N];
        while (br.ready()){
            String s = br.readLine();
            StringTokenizer st = new StringTokenizer(s,",");
            float[] values = new float[11]; //7
            if (s.contains("Fold")) continue;
            if (datastarted){
                int i = 0; 
                while (st.hasMoreTokens()){
                    String mystring = st.nextToken();
                    mystring = mystring.replace("\"","");
                    values[i++] = Float.parseFloat(mystring);
                }            
                i = 0;        
                punti[n] = new Numero(values[0],values[1],values[2],values[7]); //5
                n++;
            }else{
                if (s.contains("DATA")){
                    datastarted = true;
                }
            }
        }
        br.close();
        calcolaW();
        float bandwidth = (float)(0.95*maxdist);
        System.out.println("maxDist: "+maxdist+" Ireal: "+MoranIndex1((float)b)+" IrealMax: "+MoranIndex1(bandwidth)+" Ipredicted: "+MoranIndex2((float)b)+" IpredictedMax: "+MoranIndex2(bandwidth));
    }

    public static void calculateBI(String fileName, int fileSize, double b) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        boolean datastarted = false; float bandwidth;
        int n = 0;
        N = fileSize;
        D = new float[N][N];
        punti = new Numero[N];
        puntiM = new Numero[N];
        while (br.ready()){
            String s = br.readLine();
            StringTokenizer st = new StringTokenizer(s, ",");
            float[] values = new float[10];
            if (s.contains("Fold")){
                continue;
            }
            if (datastarted){
                int i = 0; 
                while (st.hasMoreTokens()){
                    String mystring = st.nextToken();
                    mystring = mystring.replace("\"","");
                    values[i++] = Float.parseFloat(mystring);
                }            
                i = 0;        
                punti[n] = new Numero(values[0],values[1],values[2],values[3]); //xy,real 2
                puntiM[n] = new Numero(values[0],values[1],values[7],values[8]); //xy,predicted 2
                n++;                                 
            }else{
                if (s.contains("DATA")){
                    datastarted = true;
                }
            }
        }
        br.close();
        calcolaW();
        bandwidth = (float)(0.95*maxdist); 
        System.out.println("bandwidth: "+b+" Real1: "+MoranIndex1((float)b)+" Real2: "+MoranIndex2((float)b)+" Pred1: "+MoranIndex11((float)b)+" Pred2: "+MoranIndex22((float)b));
        System.out.println("maxDist: "+maxdist+" Real1: "+MoranIndex1(bandwidth)+" Real2: "+MoranIndex2(bandwidth)+" Pred1: "+MoranIndex11(bandwidth)+" Pred2: "+MoranIndex22(bandwidth));
    }
}
