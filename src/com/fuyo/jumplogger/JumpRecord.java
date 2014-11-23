package com.fuyo.jumplogger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by 05YFU on 11/23/2014.
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
            records.add(readJumpRecord(line));
        }
        reader.close();
        return records;
    }
    private static JumpRecord readJumpRecord (String line) {
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

}
