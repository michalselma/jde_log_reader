package jdelogreader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import auxiliary.*;
import auxiliary.system.SystemFileManager;

public class JdeLogParser {

    public static ArrayList<ArrayList<String>> sortAndParseJdeLogFiles(String dir) throws IOException {
        ArrayList<ArrayList<String>> array = new ArrayList<ArrayList<String>>(); // used to return parsed logs array
        // *** Create header row
        array.add(new ArrayList<String>()); // add first row at index 0
        array.get(0).add("Proc id");
        array.get(0).add("Proc name");
        array.get(0).add("Call");
        array.get(0).add("Call detail");
        array.get(0).add("Call function");
        array.get(0).add("weekday");
        array.get(0).add("month");
        array.get(0).add("day");
        array.get(0).add("time");
        array.get(0).add("error");
        array.get(0).add("message");
        // *** Start scanning dir and sorting files
        // *** Get only 'jde_xxxxxx.log' files, where 'xxxxxx' is expected to be numeric string.
        ArrayList<String> files = SystemFileManager.listFilesAsArrayListString(dir, "jde_*.log"); // used to manage filenames in dir
        ArrayList<Integer> ints = new ArrayList<Integer>(); // used for sorting log numbers
        // *** Substring to get rid of 'jde_' and '.log' from string and have only log number in inits array
        for (String str : files) {
            String substr = str.substring(4,str.indexOf("."));
            ints.add(Integer.parseInt(substr));
        }
        // Sort xxxxxx ascending.
        Collections.sort(ints);
        // For Each value in sorted ints array, parse file and add to return array
        System.out.println("Processing file:");
        for(Integer num : ints) {
            String filename = "jde_" +num.toString() +".log";
            String paths = dir + filename;
            System.out.print("\033[2K\r"); // Erase line content
            System.out.print(filename);
            // *** Call parseJdeLogFile() method that parsers each file and add its result (array as well) to return array
            array.addAll(parseJdeLogFile(paths));
        }
        System.out.println();
        System.out.println("All files in " +dir +" have been processed and parsed");
        return array;
    }


    public static ArrayList<ArrayList<String>> parseJdeLogFile(String filepath) throws IOException {
        ArrayList<ArrayList<String>> array = new ArrayList<ArrayList<String>>();
        File file = new File(filepath);
        Scanner filetoscan = new Scanner(file);
        int row = 0;
        String[] specificmessages = { "WARNING:", "INFO:", "KERNEL " };
        String[] daysofweek = { "Mon ", "Tue ", "Wed ", "Thu ", "Fri ", "Sat ", "Sun " };
        String spaceseparator = " ";
        String underscoreseparator = "_";
        while (filetoscan.hasNext()) {
            String line = filetoscan.nextLine();
            if (!(line.equals("") || line.equals(" "))) { // *** if line is not empty or not space then start parsing
                // do parsing stuff
                // if line contains Mon...Sun than this is header of the error message
                if (StringOperations.checkIfContainWords(line, daysofweek)) {
                    // *** start header parsing

                    // *** identify exact weekday that was found in header
                    String weekday = StringOperations.returnIfWordIsFound(line, daysofweek);

                    // *** find the position of found day
                    int weekdaypos = line.indexOf(weekday);
                    // System.out.println("weekdaypos: " +weekdaypos);

                    // *** substring data located before datetime of message header
                    int spaceseparatorpos = line.indexOf(spaceseparator); // *** Locate first spaceseparator
                    // System.out.println("spaceseparatorpos: " +spaceseparatorpos);
                    String hprocid = "";
                    String hprocname = "";
                    String hprocsubname = "";
                    String hprocdetails = "";
                    String hprocfunction = "";
                    if (weekdaypos < spaceseparatorpos) {
                        hprocid = line.substring(0, weekdaypos); // JDE process id
                    } else {
                        hprocid = line.substring(0, spaceseparatorpos); // JDE process id
                        if (line.substring(spaceseparatorpos + 1, spaceseparatorpos + 12).equals("MAIN_THREAD")) {
                            hprocname = "MAIN_THREAD"; // JDE process name
                            hprocsubname = line.substring(spaceseparatorpos + 12, weekdaypos); // JDE sub process name
                        } else if (line.substring(spaceseparatorpos + 1, spaceseparatorpos + 5).equals("SYS:")) {
                            hprocname = "SYS"; // JDE process name
                            if (line.substring(spaceseparatorpos + 5, spaceseparatorpos + 13).equals("Dispatch")) {
                                hprocsubname = "Dispatch"; // JDE sub process name
                                hprocdetails = line.substring(spaceseparatorpos + 13, weekdaypos); // JDE process details
                            } else if (line.substring(spaceseparatorpos + 5, spaceseparatorpos + 13)
                                    .equals("Metadata")) {
                                hprocsubname = "Metadata"; // JDE sub process name
                                hprocdetails = line.substring(spaceseparatorpos + 13, weekdaypos); // JDE process details
                            } else if (line.substring(spaceseparatorpos + 5, spaceseparatorpos + 16)
                                    .equals("XMLDispatch")) {
                                hprocsubname = "XMLDispatch"; // JDE sub process name
                                hprocdetails = line.substring(spaceseparatorpos + 16, weekdaypos); // JDE process details
                            } else {
                                hprocsubname = line.substring(spaceseparatorpos + 5, weekdaypos); // JDE sub process details name
                            }
                        } else if (line.substring(spaceseparatorpos + 1, spaceseparatorpos + 5).equals("WRK:")) {
                            hprocname = "WRK"; // JDE process name
                            String hproctmp = line.substring(spaceseparatorpos + 5, weekdaypos); // Tniemy to co jest po 'WRK:' do weekdaypos
                            int hproctmplength = hproctmp.length(); // liczymy ile to ma
                            int underscoreseparatorpos = hproctmp.indexOf(underscoreseparator); // *** Locate first underscoreseparator w hproctmp
                            if (underscoreseparatorpos == -1) { // jezeli nie ma to wklejaj jak jest
                                hprocsubname = hproctmp; // JDE sub process name
                            } else { // jezeli to tniemy stringa dalej
                                hprocsubname = hproctmp.substring(0, underscoreseparatorpos); // JDE sub process name wrzuc pozostala w kolejnego tempa wywalajac znalezionego underscore-a (underscoreseparatorpos+1)
                                String hprocdetailtmp = hproctmp.substring(underscoreseparatorpos + 1, hproctmplength); // JDE process  details
                                int hprocdetailtmplength = hprocdetailtmp.length(); // liczymy ile to ma kolejny tmp poszukaj kolejnego underscora-a
                                int underscoreseparator2pos = hprocdetailtmp.indexOf(underscoreseparator);
                                if (underscoreseparator2pos == -1) { // jezeli nie ma to wklejaj jak jest
                                    hprocdetails = hprocdetailtmp; // JDE process details
                                } else { // jezeli jest to tnij Panie tnij dalej
                                    hprocdetails = hprocdetailtmp.substring(0, underscoreseparator2pos); // JDE process details
                                    hprocfunction = hprocdetailtmp.substring(underscoreseparator2pos + 1,
                                            hprocdetailtmplength);
                                }
                            }
                        } else {
                            hprocname = line.substring(spaceseparatorpos + 1, weekdaypos); // JDE process name
                        }
                    }

                    // *** substring date time from message header
                    String hweekday = line.substring(weekdaypos, weekdaypos + 3);
                    String hmonth = line.substring(weekdaypos + 4, weekdaypos + 7);
                    String hday = line.substring(weekdaypos + 8, weekdaypos + 10);
                    String htime = line.substring(weekdaypos + 11, weekdaypos + 26);

                    // *** substring data located after datetime of message
                    String headerafterdatetime = line.substring(weekdaypos + 26, line.length());
                    // **** Add an new empty element (row) to each array dimension. If not
                    // .get(index) will always throw 'out of bounds' error.
                    array.add(new ArrayList<String>()); // *** Proper pick of row is controlled by row counter

                    // *** Remove leading and trailing spaces from the string
                    // *** trim() uses codepoint(ASCII) and removes chars having ASCII value less
                    // than or equal to ‘U+0020’ or '32' (Since Java 1)
                    // *** strip() uses Unicode charset and removes spaces having different unicode.
                    // (Since Java 11)
                    String hprocidstrip = hprocid.strip();
                    String hprocnamestrip = hprocname.strip();
                    String hprocsubnamestrip = hprocsubname.strip();
                    String hprocdetailsstrip = hprocdetails.strip();
                    String hprocfunctionstrip = hprocfunction.strip();
                    String hdaystrip = hday.strip();

                    String headerafterdatetimestrip = headerafterdatetime.strip();

                    String rhmonth = refactorMonth(hmonth);
                    String rhdaystrip = refactorDay(hdaystrip);
                    // *** Add parsed elements in desired order as columns to each row
                    array.get(row).add(hprocidstrip);
                    array.get(row).add(hprocnamestrip);
                    array.get(row).add(hprocsubnamestrip);
                    array.get(row).add(hprocdetailsstrip);
                    array.get(row).add(hprocfunctionstrip);
                    array.get(row).add(hweekday);
                    array.get(row).add(rhmonth + "-" + rhdaystrip);
                    array.get(row).add(rhdaystrip);
                    array.get(row).add(htime);
                    array.get(row).add(headerafterdatetimestrip);
                }
                // *** if line contains specific non-typical but identifiable messages
                else if (StringOperations.checkIfContainWords(line, specificmessages)) {
                    // do something with those messages
                } else {
                    // *** Parse error message.
                    // *** Per iteration an if-else logic this should always be very last column of
                    // row

                    String errmsgstip = line.strip(); // *** Remove leading and trailing spaces from the string
                    array.get(row).add(errmsgstip);
                }
            } else { // *** if line is empty or space then new array row (increment row counter and
                     // reset column counter)
                row = row + 1;
                // System.out.println("Empty Line Identified. Next iterate will process row
                // "+row+" and column "+column);
            }
        }
        filetoscan.close();
        return array;
    }

    public static String refactorMonth(String month){
        switch (month) {
            case "Jan":
                month = "01";
            case "Feb":
                month = "02";
            case "Mar":
                month = "03";
            case "Apr":
                month = "04";
            case "May":
                month = "05";
            case "Jun":
                month = "06";
            case "Jul":
                month = "07";
            case "Aug":
                month = "08";
            case "Sep":
                month = "09";
            case "Oct":
                month = "10";
            case "Nov":
                month = "11";
            case "Dec":
                month = "12";
            default: {
            }
        }
    return month;
    }

    public static String refactorDay(String day){
        switch (day) {
            case "1":
                day = "01";
            case "2":
                day = "02";
            case "3":
                day = "03";
            case "4":
                day = "04";
            case "5":
                day = "05";
            case "6":
                day = "06";
            case "7":
                day = "07";
            case "8":
                day = "08";
            case "9":
                day = "09";
            default: {
            }
        }
        return day;
    }

}

