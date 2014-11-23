package com.fuyo.jumplogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.BufferOverflowException;
import java.util.ArrayList;

/**
 * Created by Yohei FUJII on 11/23/2014.
 */
public class JumpRecord {
    public String date;
    public double duration = 0;
    public double distance = 0;
    public String location = "";
    public String trickName = "";
    public String sportsType = "";
    public int isSuccess = -1;


    public static ArrayList<JumpRecord> readAll(FileInputStream fileInputStream) throws IOException {
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
    public static void writeAll(ArrayList<JumpRecord> records, FileOutputStream fileOutputStream)
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
        record.date = cols[0];
        record.duration = Double.valueOf(cols[1]);
        record.distance = Double.valueOf(cols[2]);
        record.sportsType = cols[3];
        record.trickName = cols[4];
        record.isSuccess = Integer.valueOf(cols[5]);
        record.location = cols[6];
        return record;
    }
    public String toString() {
        String line = date + "\t" + duration + "\t" + distance + "\t" + sportsType
                + "\t" + trickName + "\t" + isSuccess + "\t" + location;
        return line;
    }

}