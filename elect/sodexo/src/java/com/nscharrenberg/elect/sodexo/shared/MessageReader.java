package com.nscharrenberg.elect.sodexo.shared;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nscharrenberg.elect.sodexo.domain.OfferReply;
import com.nscharrenberg.elect.sodexo.domain.OfferRequest;
import com.nscharrenberg.elect.sodexo.gateways.messaging.requestreply.RequestReply;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class MessageReader {
    public static HashBiMap<String, RequestReply<OfferRequest, OfferReply>> getRequests() {
        String path = "sodexo.json";
        FileInputStream fis = null;
        ObjectInputStream ois = null;

        try {
            MessageWriter.create(path);

            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            String obj = (String)ois.readObject();

            Gson gson = new Gson();
            HashBiMap<String, RequestReply<OfferRequest, OfferReply>> replies = HashBiMap.create(gson.fromJson(obj, new TypeToken<HashBiMap<String, RequestReply<OfferRequest, OfferReply>>>(){}.getType()));

            return replies;
        } catch (Exception e) {
            e.printStackTrace();
            return HashBiMap.create();
        }
    }
}
