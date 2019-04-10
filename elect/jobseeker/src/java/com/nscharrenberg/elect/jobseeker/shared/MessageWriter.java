package com.nscharrenberg.elect.jobseeker.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.domain.ResumeRequest;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReply;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReplyList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageWriter {
    public static void writeReplies(HashBiMap<String, RequestReplyList> requests) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        try {
            String path = "jobseeker.json";
            create(path);

            fos = new FileOutputStream(path);

            Gson gson = new Gson();
            String json = gson.toJson(requests);
            byte[] bytes = json.getBytes();

            fos.write(bytes);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void create(String path) {
        File file = new File(path);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(String correlationId, RequestReplyList request) {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();
        requests.put(correlationId, request);

        writeReplies(requests);
    }

    public static void update(String correlationId, ResumeReply resumeReply) {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();

        RequestReplyList found = requests.get(correlationId);

        if(found != null) {
            found.addReply(resumeReply);
            requests.remove(correlationId);
            requests.put(correlationId, found);
        }

        writeReplies(requests);
    }

    public static void remove(ResumeRequest request) {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            if(r.equals(request)) {
                requests.remove(c);
            }
        });

        writeReplies(requests);
    }
}
