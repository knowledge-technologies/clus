package feature.ranking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import weka.core.Instances;

public class RndForestR {
	String potRscr = "C:/Program Files/R/R-3.2.2/bin/Rscript.exe";
	String skripta = "C:/Users/matejp/Documents/aproksimacijaKUmulativne/rPart/rndForest.r";
	String tempFile; // zacasna kopija trening seta ... neki bolj variabilnega kot "C:/Users/matejp/Desktop/zacasno.arff";
	String arf; // absolutna pot do arff datoteke
	String rezFileR;  //3. od argumentov za r scripto neki bolj variabilnega kot ..."C:/Users/matejp/Documents/aproksimacijaKUmulativne/rPart/rezultati.abc";
	String imeArfa;
	
	public RndForestR(String arf, Instances data) throws IOException{
		this.arf = arf;
		this.imeArfa = arf.substring(arf.lastIndexOf("/") + 1, arf.lastIndexOf("."));
		this.rezFileR = "C:/Users/matejp/Documents/aproksimacijaKUmulativne/rPart/rezultati" + this.imeArfa + ".abc";
		this.tempFile = "C:/Users/matejp/Documents/aproksimacijaKUmulativne/rPart/tempo/" + this.imeArfa + "Tempo.arff";
				
		// naredimo kopijo
		BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
		writer.write(data.toString());
		writer.close();
		
	}
	public double[] pozeni() throws IOException{
		String arg = String.join(" ", tempFile, imeArfa, rezFileR);
		// caranje z R
		BufferedReader reader = null;
        Process shell = null;    	
        shell = Runtime.getRuntime().exec(potRscr + " " +  skripta + " " + arg);
        reader = new BufferedReader(new InputStreamReader(shell.getInputStream()));
        BufferedReader erorReader = new BufferedReader(new InputStreamReader(shell.getErrorStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if(line.contains("KONEC")){
            	return dobiRanking();
            }
        }
        Boolean prvic = true;
        int i = 0;
        while((line = erorReader.readLine()) != null && i < 1000){
        	if(prvic){
        		prvic = false;
        		System.out.println("Eror:");
        	}
        	System.out.println(line);
        	i++;
        }
        System.out.println("i = " + i);
		return null;

	}
	/**
	 * Odpre rezFile z rankingi, produced by R in ga sparsa, da dobi vrednosti ...
	 * @return
	 * @throws IOException 
	 */
	private double[] dobiRanking() throws IOException {
		BufferedReader readr = new BufferedReader(new FileReader(this.rezFileR));
		String line;
	    while ((line = readr.readLine()) != null) {
	        if(line.contains("povprecja")){
	        	break;
	        }
	     }
		String[] pomo = readr.readLine().trim().split(" ");
		System.out.println(Arrays.toString(pomo));
		double[] odg = new double[pomo.length + 1]; // +1 za class
		for(int i = 0; i < pomo.length; i++){
			odg[i] = Double.parseDouble(pomo[i].trim());
		}
		odg[pomo.length] = 0.0;//kar je itak ze
		return odg;
	}
	

}
