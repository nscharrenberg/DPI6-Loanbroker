package com.nscharrenberg.elect.jobseeker.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.jobseeker.domain.ResumeReply;
import com.nscharrenberg.elect.jobseeker.gateways.messaging.requestreply.RequestReplyList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageWriter {
    public static void writeReplies(HashBiMap<String, RequestReplyList> requests) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        //TODO: Store RequestreplyList in the in-memory database (for this prototype it's a JSON file)
        try {
            String path = "jobseeker.json";
            create(path);

            fos = new FileOutputStream(path);

            // Convert Object to a JSON string
            Gson gson = new Gson();
            String json = gson.toJson(requests);

            // Convert String to a byte array
            byte[] bytes = json.getBytes();

            // Write content to File
            fos.write(bytes);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            // Close Open IO Connection
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

    /**
     * Create a File
     * @param path
     */
    public static void create(String path) {
        File file = new File(path);

        try {
            // Creates a File if it doesn't exist, does nothing if it already exists.
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a new item to the database
     * @param correlationId
     * @param request
     */
    public static void add(String correlationId, RequestReplyList request) {
        HashBiMap<String, RequestReplyList> requests = MessageReader.getRequests();
        requests.put(correlationId, request);

        writeReplies(requests);
    }

    /**
     * Update an existing item in the database
     * @param correlationId
     * @param resumeReply
     */
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
}
