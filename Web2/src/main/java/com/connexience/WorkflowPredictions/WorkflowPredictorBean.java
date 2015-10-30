/*
package com.connexience.WorkflowPredictions;

import com.connexience.api.StorageClient;
import com.connexience.server.model.workflow.WorkflowInvocationMessage;
import com.connexience.server.workflow.api.API;
import com.connexience.server.workflow.cloud.download.DownloadException;
import com.connexience.server.workflow.cloud.download.WorkflowDataFetcher;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pipeline.core.drawing.BlockExecutionReport;
import org.pipeline.core.drawing.BlockModel;
import org.pipeline.core.drawing.model.DefaultDrawingModel;
import org.pipeline.core.drawing.spanning.DrawingExecutionProcessor;
import org.pipeline.core.xmlstorage.XmlDataStore;
import org.pipeline.core.xmlstorage.XmlStorage;
import org.pipeline.core.xmlstorage.XmlStorageException;
import javax.jms.Message;
import java.util.Enumeration;

*/
/**
 * Created by naa166 - Anirudh Agarwal on 28/07/2015.
 *//*


public class WorkflowPredictorBean {

    private static StorageClient client;
    private static Logger logger = Logger.getLogger(WorkflowPredictorBean.class);

static {
        XmlStorage.overrideClass("com.connexience.server.workflow.blocks.processor.DataProcessorBlock", DataProcessorPredictionBlock.class);
    }

    public static String predictExecutionTime(DefaultDrawingModel wf) throws Exception {
        // Setup the drawing blocks
        Enumeration blocks = wf.blocks();
        BlockModel b;
        DataProcessorPredictionBlock predictionBlock;
        while(blocks.hasMoreElements()){
            b = (BlockModel)blocks.nextElement();
            if(b instanceof DataProcessorPredictionBlock){
                predictionBlock = (DataProcessorPredictionBlock)b;
                predictionBlock.initialiseForService(client);
            }
        }

        DrawingExecutionProcessor processor = new DrawingExecutionProcessor(wf);
        processor.blockingExecuteFromAllSourceBlocks();
        Enumeration reports = processor.getExecutionReports().elements();
        BlockExecutionReport r;
        JSONObject resultJson = new JSONObject();
        JSONArray reportsArray = new JSONArray();
        JSONObject blockDetails;

        while(reports.hasMoreElements()){
            r = (BlockExecutionReport)reports.nextElement();
            if(r.containsCommandOutput()){
                try {
                    blockDetails = new JSONObject();
                    blockDetails.put("models", new JSONObject(r.getCommandOutput()));
                    blockDetails.put("blockGuid", r.getBlockGuid());
                    reportsArray.put(blockDetails);
                } catch (Exception e){
                    logger.error("Error saving block details data: " + e.getMessage());
                }
            }
        }

        resultJson.put("reports", reportsArray);
        return resultJson.toString(2);
    }




    public static String predictExecutionTime(Message receivedMessage) {

        System.out.println("inside predictExecutionTime");
        WorkflowDataFetcher fetcher;
        API apiLink = null;
        String result = null;

        if (receivedMessage instanceof WorkflowInvocationMessage) {
            System.out.println("inside if loop");
            WorkflowInvocationMessage message = (WorkflowInvocationMessage) receivedMessage;
            try {
                System.out.println("inside try loop");
                if (message.isUseLatest()) {
                    fetcher = new WorkflowDataFetcher(message.getWorkflowId(), apiLink);
                } else {
                    fetcher = new WorkflowDataFetcher(message.getWorkflowId(), message.getVersionId(), apiLink);
                }
                System.out.println("before fetcher.download");
                XmlDataStore workflowData = fetcher.download();
                System.out.println("before after.download");
                DefaultDrawingModel wf = new DefaultDrawingModel();
                System.out.println("before recreate object");
                wf.recreateObject(workflowData);
                System.out.println("after recreate object");
                ;

                System.out.println("before predict execution time ");
                //result = predictExecutionTime(wf);

                System.out.println("after predict execution time ");

                return result;

            } catch (XmlStorageException e) {
                e.printStackTrace();
            } catch (DownloadException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
*/
