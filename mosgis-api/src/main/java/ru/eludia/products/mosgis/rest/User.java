package ru.eludia.products.mosgis.rest;

import java.security.Principal;
import javax.json.Json;
import javax.json.JsonObject;

public class User implements Principal {
    
    String id;
    String name;
    String uuid_org;

    public User (String id, String name, String uuid_org) {
        this.id = id;
        this.name = name;
        this.uuid_org = uuid_org;
    }

    @Override
    public String getName () {
        return name;
    }

    public String getId () {
        return id;
    }

    public String getUuidOrg () {
        return uuid_org;
    }

    @Override
    public String toString () {
        return toJsonObject ().toString ();
    }
    
    public JsonObject toJsonObject () {
        return Json.createObjectBuilder ().add ("id", id).add ("name", name).build ();
    }
    
}