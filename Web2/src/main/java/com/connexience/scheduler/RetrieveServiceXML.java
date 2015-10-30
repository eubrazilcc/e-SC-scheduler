/*
package com.connexience.scheduler;


import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.util.SerializationUtils;
import com.connexience.server.workflow.api.API;
import com.connexience.server.workflow.api.ApiProvider;
import com.connexience.server.workflow.service.DataProcessorServiceDefinition;

*/
/**
 * Created by naa166 - Anirudh Agarwal on 10/08/2015.
 *//*



@MessageDriven(name = "RetrieveServiceXML",
        activationConfig =
                {
                        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @ActivationConfigProperty(propertyName = "destination", propertyValue = "engine58")
                }, mappedName = "engine58")
public class RetrieveServiceXML implements MessageListener {


    private ApiProvider apiProvider = new ApiProvider();
    private String serviceid = "blocks-core-io-upzip";
    private String versionid = "3805";

    @Override
    public void onMessage(Message message) {
        System.out.println("IN RETRIEVE SERVICE XML");
        if (message instanceof BytesMessage) {
            BytesMessage bm = (BytesMessage) message;
            try {
                bm.reset();
                byte[] data = new byte[(int) bm.getBodyLength()];
                bm.readBytes(data);
                Object payload = SerializationUtils.deserialize(data);
                if (payload instanceof WorkflowInvocationMessage) {
                    WorkflowInvocationMessage invocationMessage = (WorkflowInvocationMessage) payload;
                    RetrieveRequirementXMLForAllBlocks xmlForAllBlocks = new RetrieveRequirementXMLForAllBlocks();
                    xmlForAllBlocks.getXMLForAllBlocks(invocationMessage);
                    //message.acknowledge();
                    API apiLink = null;
                    try {
                        apiLink = apiProvider.createApi(invocationMessage.getTicket());
                    } catch (Exception e) {
                    }
                    System.out.println("BEFORE GET SERVICE");
                    DataProcessorServiceDefinition def = apiLink.getService(serviceid, versionid);
                    //def.loadXmlString(xml);
                    System.out.println(def);
                    System.out.println("DEF 1 : " + def.getCategory());
                    System.out.println("DEF 2 : " + def.getDescription());


                }
            } catch (Exception jmse) {
            }


        }
    }
}


















*/
/*

    *//*
*/
/*
*//*

*/
/** URL for the download servlet *//*
*/
/*
*//*

*/
/*
    private String serverContext = "/workflow";

    *//*
*/
/*
*//*

*/
/** Web host name *//*
*/
/*
*//*

*/
/*
    private String hostName = "localhost";

    *//*
*/
/*
*//*

*/
/** Webserver port of the host *//*
*/
/*
*//*

*/
/*
    private int httpPort = 8080;

    *//*
*/
/*
*//*

*/
/** Security ticket *//*
*/
/*
*//*

*/
/*
    private Ticket ticket;

    private ClientRequest createRequest(String url) throws IOException, ConnexienceException {
        ClientRequest request = new ClientRequest("http://" + hostName + ":" + httpPort + serverContext + "/rest/wf" + url);
        request.accept(MediaType.APPLICATION_JSON);

        // Add ticket if one is present
        if(ticket!=null){
            // Write the ticket as request header parameters
            request.header("cnx-userid", ticket.getUserId());
            request.header("cnx-organisationid", ticket.getOrganisationId());

            // Send in groups if there are any
            if(ticket instanceof WebTicket){
                WebTicket wt = (WebTicket)ticket;
                String[] gids = wt.getGroupIds();
                request.header("cnx-groups", gids.length);
                for(int i=0;i<gids.length;i++){
                    request.header("cnx-group" + i, gids[i]);
                }
            }
        }

        return request;
    }
    public void getServiceXML(String userId, String serviceId, String versionId){

        try {

            System.out.println("before getting ticket");
            ClientRequest request = createRequest("/usertickets/{userid}");
            request.pathParameter("userid", userId);
            ticket = request.get(Ticket.class).getEntity();
            System.out.println("got ticket " + ticket);

            System.out.println("before document request");
            ClientRequest documentRequest = createRequest("/workflows/services/{id}/definitions/{version}/get");
            System.out.println("after document request");
            documentRequest.pathParameter("id", serviceId);
            documentRequest.pathParameter("version", versionId);
            String xml = documentRequest.get(String.class).getEntity();
            System.out.println("after get entity");
            DataProcessorServiceDefinition def = new DataProcessorServiceDefinition();
            def.loadXmlString(xml);
            System.out.println(def);
            System.out.println("DEF 1 : " + def.getCategory());
            System.out.println("DEF 2 : " + def.getDescription());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*//*



*/
