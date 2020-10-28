package com.seeyon.ctp.rest.resources;

import com.alibaba.fastjson.JSONObject;
import com.seeyon.ctp.util.annotation.RestInterfaceAnnotation;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Path("h5")
@Produces({MediaType.APPLICATION_JSON})
public class H5ValidationResource extends BaseResource {

    @GET
    @Path("validationToken/{token}/{uid}")
    @Produces({"application/json"})
    @RestInterfaceAnnotation
    public Response validationToken(@PathParam("token") String token, @PathParam("uid") String uid) {
        String url = "http://fw.xze.cn:81/api/APIUCQE/ValidateAccessToken?ucToken=" + token + "&uid=" + uid;
        HttpPost httpPost = new HttpPost(url);
        String result = null;
        Map<String, Object> map = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse httpResponse = client.execute(httpPost);
            result = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            map = JSONObject.parseObject(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.ok(map).build();
    }


}
