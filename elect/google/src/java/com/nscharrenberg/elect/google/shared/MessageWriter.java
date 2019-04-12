package com.nscharrenberg.elect.google.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.nscharrenberg.elect.google.domain.OfferRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class MessageWriter {
    public static void writeReplies(HashBiMap<String, OfferRequest> replies) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        //TODO: Store OfferRequest in the in-memory database (for this prototype it's a JSON file)
        try {
            String path = "google.json";
            create(path);

            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);

            Gson gson = new Gson();
            String json = gson.toJson(replies);

            oos.writeObject(json);
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

    public static void add(String correlationId, OfferRequest request) {
        HashBiMap<String, OfferRequest> requests = MessageReader.getRequests();
        requests.put(correlationId, request);

        writeReplies(requests);
    }

    public static void remove(OfferRequest request) {
        HashBiMap<String, OfferRequest> requests = MessageReader.getRequests();

        requests.forEach((c, r) -> {
            if(r.equals(request)) {
                requests.remove(c);
            }
        });

        writeReplies(requests);
    }
}
