package jdelogreader;

import java.io.IOException;
import java.util.ArrayList;

import auxiliary.filetype.xlsx;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("<--- START --->");
        long startTime = System.nanoTime();
        String suffix_folder = "EnterpriseServer";
        String suffix_subfolder = "logs";
        

        String dir = "C:\\JDE_E1_logs\\"+suffix_folder+"\\"+suffix_subfolder+"\\";
        // create output xlsx file
        xlsx excel = new xlsx();
        excel.createEmptyFile("C:\\JDE_E1_logs\\xlsx\\"+suffix_folder+suffix_subfolder+".xlsx", "JDELog-"+suffix_folder);
        // create array to maniualte data and pass it to xlsx
        ArrayList<ArrayList<String>> array2d = new ArrayList<ArrayList<String>>();
        // when initializing sortAndParse will create Excel header line as well
        array2d = JdeLogParser.sortAndParseJdeLogFiles(dir);
        excel.appendWorkbookOnFirstSheet("C:\\JDE_E1_logs\\xlsx\\"+suffix_folder+suffix_subfolder+".xlsx", array2d);
        long stopTime = System.nanoTime();
        // elapsed time in seconds
        double elapsedTime = (double) (stopTime - startTime) / 1_000_000_000;
        System.out.println("Code Execution Time: " +elapsedTime +" seconds");
        System.out.println("<--- END --->");
    }

}



