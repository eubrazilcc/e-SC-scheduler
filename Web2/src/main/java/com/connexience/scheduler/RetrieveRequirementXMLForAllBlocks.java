package com.connexience.scheduler;

import com.connexience.server.ConnexienceException;
import com.connexience.server.model.document.DocumentRecord;
import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.workflow.api.API;
import com.connexience.server.workflow.api.ApiProvider;
import com.connexience.server.workflow.blocks.processor.DataProcessorBlock;
import com.connexience.server.workflow.cloud.download.DownloadException;
import com.connexience.server.workflow.cloud.download.WorkflowDataFetcher;
import com.connexience.server.workflow.service.DataProcessorRequirementsDefinition;
import org.hornetq.api.core.TransportConfiguration;
import org.pipeline.core.drawing.BlockModel;
import org.pipeline.core.drawing.DrawingModel;
import org.pipeline.core.drawing.model.DefaultDrawingModel;
import org.pipeline.core.xmlstorage.XmlDataStore;
import org.pipeline.core.xmlstorage.XmlStorageException;

import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by naa166 - Anirudh Agarwal on 12/08/2015.
 */
public class RetrieveRequirementXMLForAllBlocks {

    private API apiLink = null;
    private ApiProvider apiProvider;
    /** Drawing being executed Enumeration blocks = drawing.blocks();*/
    private transient DrawingModel drawing = null;

    /** Drawing data */
    private XmlDataStore drawingData = null;


    public RetrieveRequirementXMLForAllBlocks()
    {
        // Create and configure the API provider.
        apiProvider = new ApiProvider();

        // FIXME: All this should be read from a configuration file.
        apiProvider.setHostName("localhost");
        apiProvider.setHttpPort(8080);
        apiProvider.setServerContext("/workflow");
        apiProvider.setUseRmi(false);
        apiProvider.useJMS("localhost", 5445, "connexience", "1234", "WorkflowControl", "WorkflowManagerTopic");
    }


    public ArrayList<DataProcessorRequirementsDefinition> getXMLForAllBlocks(WorkflowInvocationMessage message){

        DocumentRecord serviceDoc;
        ArrayList<DataProcessorRequirementsDefinition> reuirementsDefinitionArrayList = new ArrayList<DataProcessorRequirementsDefinition>();
        //String serviceVersionId;DocumentVersion latestVersion = EJBLocator.lookupStorageBean().getLatestVersion(ticket, serviceId);
        try {
            apiLink = apiProvider.createApi(message.getTicket());
        } catch (Exception e){
            System.out.println("Error creating API link: " + e.getMessage());
        }

        System.out.println("successfully created API Link");

        /*try {
            DocumentRecord documentRecord = apiLink.getDocument(message.getWorkflowId());

            String latestVersionId = apiLink.getLatestVersionId(documentRecord.getId());

            // TODO : Write logic to store the retrieved xml in memory cache

        } catch (ConnexienceException e) {
            e.printStackTrace();
        }*/


        //Instantiate WorkflowDataFetcher
        try {
            WorkflowDataFetcher fetcher;
            if(message.isUseLatest()){
                fetcher = new WorkflowDataFetcher(message.getWorkflowId(), apiLink);
            } else {
                fetcher = new WorkflowDataFetcher(message.getWorkflowId(), message.getVersionId(), apiLink);
            }
            XmlDataStore workflowData = fetcher.download();

            drawing = new DefaultDrawingModel();
            drawingData = workflowData;

            try {

                ((DefaultDrawingModel) drawing).recreateObject(drawingData);
            } catch (XmlStorageException xmlse) {
                System.out.println("Error parsing workflow data. InvocationID=" + message.getInvocationId());
            }

            Enumeration blocks = drawing.blocks();
            BlockModel block;

            while (blocks.hasMoreElements()) {
                block = (BlockModel) blocks.nextElement();

                if (block instanceof DataProcessorBlock) {
                    DataProcessorBlock dpb = (DataProcessorBlock)block;
                    serviceDoc = new DocumentRecord();
                    serviceDoc.setId(dpb.getServiceId());

                    String latestVersionId = apiLink.getLatestVersionId(serviceDoc.getId());
                    DataProcessorRequirementsDefinition def = new DataProcessorRequirementsDefinition();

                     def = apiLink.getRequirements(dpb.getServiceId(), latestVersionId);
                    if(def.isRequirementsFilePresent()){
                        reuirementsDefinitionArrayList.add(def);
                    }

                    else
                        System.out.println("not found requirements XML for:-  " + dpb.getServiceId());
                    /*System.out.println("before getService VersionID :" + latestVersionId + " serviceID : " + latestVersionId);
                    DataProcessorServiceDefinition def = apiLink.getService(dpb.getServiceId(),latestVersionId);
                    System.out.println("after getService");
                    System.out.println(def);
                    System.out.println("DEF 1 : " + def.getCategory());
                    System.out.println("DEF 2 : " + def.getDescription());*/
                }
            }
        } catch (XmlStorageException e) {
            e.printStackTrace();
        } catch (DownloadException e) {
            e.printStackTrace();
        } catch (ConnexienceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reuirementsDefinitionArrayList;
    }
}
