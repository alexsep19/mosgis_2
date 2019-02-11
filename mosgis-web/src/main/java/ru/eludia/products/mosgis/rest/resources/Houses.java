package ru.eludia.products.mosgis.rest.resources;

import ru.eludia.products.mosgis.rest.misc.EJBResource;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import ru.eludia.products.mosgis.rest.ValidationException;
import ru.eludia.products.mosgis.rest.api.HousesLocal;

@Path ("houses")
public class Houses extends EJBResource <HousesLocal> {
    
    private JsonObject selectWrapper (JsonObject p) {
        
        if (securityContext.isUserInRole ("admin")    ||
            securityContext.isUserInRole ("nsi_20_4") ||
            securityContext.isUserInRole ("nsi_20_7"))
            return back.selectAll(p);
        
        if (securityContext.isUserInRole ("nsi_20_8"))
            return back.selectOktmo(p, getUserOrg ());
        
        return back.select (p, getUser ());
        
    }
    
    private String getUserOrg () {

        String userOrg = getUser ().getUuidOrg ();

        if (userOrg == null) {
            logger.warning ("User has no org set, access prohibited");
            throw new ValidationException ("foo", "Доступ запрещён");
        }

        return userOrg;
        
    }
    
    private boolean getAccessCheck (JsonObject item) {
        
        if (securityContext.isUserInRole ("admin")    ||
            securityContext.isUserInRole ("nsi_20_4") ||
            securityContext.isUserInRole ("nsi_20_7"))
            return true;
        
        if (securityContext.isUserInRole ("nsi_20_1")  ||
            securityContext.isUserInRole ("nsi_20_19") ||
            securityContext.isUserInRole ("nsi_20_20") ||
            securityContext.isUserInRole ("nsi_20_21") ||
            securityContext.isUserInRole ("nsi_20_22"))
            return item.containsKey ("cach") && getUserOrg ().equals (item.getJsonObject ("cach").getString("org.uuid"));
        
        String itemOktmo = item.getJsonObject ("item").get ("fias.oktmo").toString ();
        return securityContext.isUserInRole("nsi_20_8") && securityContext.isUserInRole("oktmo_" + itemOktmo);
    }
    
    private void checkGet (JsonObject item) {
        
        if (!getAccessCheck (item)) throw new ValidationException ("foo", "Доступ запрещен");
        
    }
    
    private void checkOrg(JsonObject item) {

        if (securityContext.isUserInRole("admin")
                || securityContext.isUserInRole("nsi_20_7")
                || securityContext.isUserInRole("nsi_20_14")) {
            return;
        }

        if ((securityContext.isUserInRole("nsi_20_1")
                || securityContext.isUserInRole("nsi_20_19")
                || securityContext.isUserInRole("nsi_20_20")
                || securityContext.isUserInRole("nsi_20_21")
                || securityContext.isUserInRole("nsi_20_22"))
                && item.containsKey("cach") && getUserOrg ().equals (item.getJsonObject ("cach").getString("org.uuid")))
            return;
        
        String itemOktmo = item.getJsonObject ("item").get ("fias.oktmo").toString ();
        
        if (securityContext.isUserInRole("nsi_20_8") && securityContext.isUserInRole("oktmo_" + itemOktmo)) {
            return;
        }
        
        if (securityContext.isUserInRole("nsi_20_2"))
            throw new ValidationException("foo", "Доcтуп запрещён. Добавить проверку договора РСО");
                
        if (securityContext.isUserInRole("nsi_20_34")) {
            throw new ValidationException("foo", "Доступ запрещён. Добавить проверки единоличного собственника МКД");
        }

        throw new ValidationException("foo", "Доступ запрещён");
    }
    
    private JsonObject getInnerItem (String id) {
        final JsonObject data = back.getItem (id);        
        final JsonObject item = data.getJsonObject ("item");
        if (item == null) throw new InternalServerErrorException ("Wrong data from back.getItem (" + id + "), no item: " + data);
        return item;
    }

    @POST
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject select (JsonObject p) { 
        return selectWrapper (p);
    }

    @POST
    @Path("{id}") 
    @Produces (APPLICATION_JSON)
    public JsonObject getItem (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkGet (item);
        return item;
    }
    
    @POST
    @Path ("vocs")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocs () {
        return back.getVocs ();
    }

    @POST
    @Path("create") 
    @Produces (APPLICATION_JSON)
    public JsonObject doCreate (JsonObject p) {
        return back.doCreate (p, getUser());
    }
    
    @POST
    @Path("{id}/update") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doUpdate (@PathParam ("id") String id, JsonObject p) { 
        return back.doUpdate (id, p, getUser());
    }
    
    @POST
    @Path("{id}/set_multiple") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doSetMultiple (@PathParam ("id") String id, JsonObject p) {
        return back.doSetMultiple (id, p);
    }

    @POST
    @Path("{id}/patch") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject doPatch (@PathParam ("id") String id, JsonObject p) { 
        return back.doPatch (id, p);
    }

    Integer [] vocPassportFieldsCommon = {
        20032,
        11019,
        19229,
        20065,
        11532,
        10031,
        12502,
        11920,
        11528,
        20064,
        17023,
        20147,
        20815,
        20816,
        20168,
        20169,
        11053,
        11052,
        20817,
        20818,
        20024,
        20062,
        20025,
        13026,
        14526,
        20170,
        20136,
        20137,
        20138,
        20139,
        21819,
        11527,
        20140,
        20063,
        13027,
        20800,
        19924,
        14532,
        20145,
        11098,
        14023,
        14099,
        20807,
        20055,
        13028,
        20052,
        20049,
        20819,
        20149,
        20902,
        20901,
        20146
    };
        
    @POST
    @Path("{id}/passport_fields_common") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsCommon (@PathParam ("id") String id) { 
        return back.getVocPassportFields (id, vocPassportFieldsCommon);
    }
        
    Integer [] vocPassportFieldsConstr = {
        12042,
        12043,
        10050,
        10516,
        13516,
        15016,
        14581,
        20067,
        10748,
        16590,
        20070,
        14549,
        14011,
        13049,
        20072,
        20073,
        10587,
        15087,
        20808,
        12179,
        10721,
        20152,
        20078,
        15246,
        19220,
        12185,
        20083,
        10059,
        13059,
        20809,
        12139,
        20089,
        13590,
        10056,
        20120,
        13056,
        11556,
        20822,
        12248        
    };
    
    @POST
    @Path("{id}/passport_fields_constr") 
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsConstr (@PathParam ("id") String id) { 
        return back.getVocPassportFields (id, vocPassportFieldsConstr);
    }
    
    Integer [] vocPassportFieldsSystems = {
        11707,
        11767,
        11745,
        11789,
        11801,
        11665
    };
            
    Integer [] vocPassportFieldsSysHeating = {
        20094,
        13207,
        16207,
        12035,
        20058,
        20048,
        20047,
        20046,
        20154,
        20050,
        20051,
        10221,
        20096,
        15055,
        14721,
        10291,
        20097,
        11791,
        12060,
        20098,
        20099,
        10536,
        20100,
        12036,
        20101,
        20102,
        20103
    };
    
    Integer [] vocPassportFieldsSysColdWater = {
        20105,
        13267,
        11955,
        10278,
        20107,
        14778,
        10476,
        20108,
        11975,
        20109,
        20110
    };
    
    Integer [] vocPassportFieldsSysHotWater = {
        20112,
        14745,
        13477,
        20153,
        10524,
        20114,
        15024,
        16524,
        10478,
        20115,
        12023,
        20116,
        20117
    };
    
    Integer [] vocPassportFieldsSysSewer = {
        20143,
        13289,
        13412,
        20045
    };

    Integer [] vocPassportFieldsSysGas = {
        20118,
        13301,
        16301,
        20057,
        20148
    };
    
    Integer [] vocPassportFieldsSysElectro = {
        20122,
        12545      
    };
    
    @POST
    @Path("{id}/passport_fields_sys")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSystems (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSystems);
    }

    @POST
    @Path("{id}/passport_fields_sys_heating")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysHeating (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysHeating);
    }
    
    @POST
    @Path("{id}/passport_fields_sys_hot_water")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysHotWater (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysHotWater);
    }

    @POST
    @Path("{id}/passport_fields_sys_cold_water")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysColdWater (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysColdWater);
    }    
    
    @POST
    @Path("{id}/passport_fields_sys_sewer")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysSewer (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysSewer);
    }
    
    @POST
    @Path("{id}/passport_fields_sys_gas")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysGas (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysGas);
    }
    
    @POST
    @Path("{id}/passport_fields_sys_electro")
    @Produces (APPLICATION_JSON)
    public JsonObject getVocPassportFieldsSysElectro (@PathParam ("id") String id) {
        return back.getVocPassportFields (id, vocPassportFieldsSysElectro);
    }
    
    @POST
    @Path("{id}/log") 
    @Consumes (APPLICATION_JSON)
    @Produces (APPLICATION_JSON)
    public JsonObject getLog (@PathParam ("id") String id, JsonObject p) {
        final JsonObject item = back.getItem (id);
        checkGet (item);
        return back.getLog (id, p, getUser ());
    }
        
    @POST
    @Path("{id}/reload") 
    @Produces (APPLICATION_JSON)
    public JsonObject doReload (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkOrg (item);
        
        String orgUuid = null;
        if (item.getJsonObject("cach") != null)
            orgUuid = item.getJsonObject("cach").getString("org.uuid");
        
        return back.doReload (id, orgUuid, getUser());
    }
    
    @POST
    @Path("{id}/send") 
    @Produces (APPLICATION_JSON)
    public JsonObject doApprove (@PathParam ("id") String id) { 
        final JsonObject item = back.getItem (id);
        checkOrg (item);
        
        String orgUuid = null;
        if (item.getJsonObject("cach") != null)
            orgUuid = item.getJsonObject("cach").getString("org.uuid");
        
        return back.doSend (id, orgUuid, getUser ());
    }
}