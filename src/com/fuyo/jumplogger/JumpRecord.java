package com.fuyo.jumplogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;

/**
 * Created by Yohei FUJII on 11/23/2014.
 */
public class JumpRecord {
    public String id;
    public String date;
    public double duration = 0;
    public double distance = 0;
    public String location = "";
    public String trickName = "";
    public String sportsType = "";
    public int isSuccess = -1;
    public static final String FILENAME = "FILE";


    public static ArrayList<JumpRecord> readAll(InputStream fileInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        String line = "";
        ArrayList<JumpRecord> records = new ArrayList<JumpRecord>();
        while ((line = reader.readLine()) != null) {
            final int BLANK_LINE_THRESHOLD = 5;
            if (line.length() <= BLANK_LINE_THRESHOLD) {
                continue;
            }
            records.add(JumpRecord.valueOf(line));
        }
        reader.close();
        return records;
    }
    public static void writeAll(ArrayList<JumpRecord> records, OutputStream fileOutputStream)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        for (JumpRecord record : records) {
            writer.write(record.toString());
            writer.write("\t");
        }
        writer.close();
    }
    private static JumpRecord valueOf(String line) {
        JumpRecord record = new JumpRecord();

        String[] cols = line.split("\t");
        record.id = cols[0];
        record.date = cols[1];
        record.duration = Double.valueOf(cols[2]);
        record.distance = Double.valueOf(cols[3]);
        record.sportsType = cols[4];
        record.trickName = cols[5];
        record.isSuccess = Integer.valueOf(cols[6]);
        record.location = cols[7];
        return record;
    }
    public String toString() {
        String line = id + "\t" + date + "\t" + duration + "\t" + distance + "\t" + sportsType
                + "\t" + trickName + "\t" + isSuccess + "\t" + location;
        return line;
    }

}
