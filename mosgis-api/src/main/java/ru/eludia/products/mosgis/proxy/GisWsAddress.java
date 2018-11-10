package ru.eludia.products.mosgis.proxy;

public class GisWsAddress {
    
    String url;
    String login;
    String password;

    public GisWsAddress (String url, String login, String password) {
        this.url = url.endsWith ("/") ? url : url + '/';
        this.login = login;
        this.password = password;
    }

    public String getUrl () {
        return url;
    }

    public String getLogin () {
        return login;
    }

    public String getPassword () {
        return password;
    }
    
}
