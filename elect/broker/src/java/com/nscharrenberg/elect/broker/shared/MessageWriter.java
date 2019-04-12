package com.nscharrenberg.elect.broker.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.broker.domain.ListLine;
import com.nscharrenberg.elect.broker.domain.OfferReply;
import com.nscharrenberg.elect.broker.domain.OfferRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageWriter {
    public static void writeReplies(HashBiMap<String, ListLine> requests) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        //TODO: Store ListLine in the in-memory database (for this prototype it's a JSON file)
        try {
            String path = "broker.json";
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
    public static void add(String correlationId, ListLine request) {
        HashBiMap<String, ListLine> requests = MessageReader.getRequests();
        requests.put(correlationId, request);

        writeReplies(requests);
    }

    public static void update(String correlationId, OfferReply offerReply) {
        HashBiMap<String, ListLine> requests = MessageReader.getRequests();

        ListLine found = requests.get(correlationId);

        if(found != null) {
            found.addReply(offerReply);
            requests.remove(correlationId);
            requests.put(correlationId, found);
        }

        writeReplies(requests);
    }

    public static void update(String correlationId, OfferRequest offerRequest) {
        HashBiMap<String, ListLine> requests = MessageReader.getRequests();

        ListLine found = requests.get(correlationId);

        if(found != null) {
            found.setOfferRequest(offerRequest);
            requests.remove(correlationId);
            requests.put(correlationId, found);
        }

        writeReplies(requests);
    }
}
