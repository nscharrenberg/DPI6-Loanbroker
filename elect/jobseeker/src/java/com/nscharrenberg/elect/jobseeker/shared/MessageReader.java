package com.nscharrenberg.elect.jobseeker.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReplyList;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MessageReader {
    public static HashBiMap<String, RequestReplyList> getRequests() {
        String path = "jobseeker.json";
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        //TODO: Read the in-memory database (for this prototype it's a JSON file)
        try {
            // Create a file if it does not exist
            MessageWriter.create(path);

            // Read the File
            RandomAccessFile reader = new RandomAccessFile(path, "r");
            FileChannel channel = reader.getChannel();
            int bufferSize = 1024;

            if (bufferSize > channel.size()) {
                bufferSize = (int) channel.size();
            }

            ByteBuffer buff = ByteBuffer.allocate(bufferSize);
            channel.read(buff);
            buff.flip();

            // Convert Json back to RequestReplyList Object
            Gson gson = new Gson();
            return HashBiMap.create(gson.fromJson(new String(buff.array()), new TypeToken<HashBiMap<String, RequestReplyList>>(){}.getType()));
        } catch (Exception e) {
            e.printStackTrace();
            return HashBiMap.create();
        }
    }
}
