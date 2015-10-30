package com.connexience.test;

import com.connexience.server.model.document.DocumentRecord;
import com.connexience.server.model.document.DocumentVersion;
import com.connexience.server.model.folder.Folder;
import com.connexience.server.model.security.Ticket;
import com.connexience.server.model.security.User;
import com.connexience.server.model.workflow.WorkflowInvocationFolder;
import com.connexience.server.workflow.api.API;
import com.connexience.server.workflow.api.ApiProvider;
import com.connexience.server.workflow.service.DataProcessorReuirementsDefinition;
import com.connexience.server.workflow.service.DataProcessorServiceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Test setting up and calling REST version of the API
 * Created by naa166 - Anirudh Agarwal on 17/08/2015.
 */
public class APITest {

    public static void test() {
        try {
            ApiProvider apiProvider = new ApiProvider();
            apiProvider.setHostName("localhost");
            apiProvider.setHttpPort(8080);
            apiProvider.setServerContext("/workflow");

            API api = apiProvider.createApi();
            api.authenticate("anirudh", "admin123");
            Ticket t = api.getTicket();
            System.out.println("Authenticated: " + t.getUserId());

            /**Retrieve the requirements.xml */
            String serviceId = "blocks-io-core-importclustering";
            String versionId = "6363";

            System.out.println("before calling api");
            DataProcessorReuirementsDefinition def = api.getRequirements(serviceId, versionId);
            System.out.println(def.getTest());














            /*String documentId = "900";
            DocumentRecord doc = api.getDocument(documentId);
            System.out.println("Got doc: " + doc.getName());

            String invocationId = "4266";
            WorkflowInvocationFolder inv = api.getWorkflowInvocation(invocationId);
            System.out.println("Got invocation: " + inv.getName());

            *//**New test code added *//*
            List<DocumentVersion> versions = new ArrayList<DocumentVersion>();
            versions = api.getDocumentVersions(doc);
            for(DocumentVersion version : versions)
                System.out.println("Document Versions :" + version.getDocumentRecordId() + " versions : " + version.getVersionNumber());

            inv.setDescription("Modified at: " + System.currentTimeMillis());
            System.out.println("Modified: " + inv.getDescription());
            api.saveWorkflowInvocation(inv);

            WorkflowInvocationFolder inv2 = api.getWorkflowInvocation(invocationId);
            System.out.println("Modified: " + inv2.getDescription());

            api.setCurrentBlockAsync(invocationId, "B" + System.currentTimeMillis(), 0);
            inv2 = api.getWorkflowInvocation(invocationId);
            System.out.println("ContetID: " + inv2.getCurrentBlockId());

            api.setCurrentBlockStreamingProcessAsync(invocationId, inv2.getCurrentBlockId(), 1000000, 40);

            api.logWorkflowDequeuedAsync(invocationId);
            api.logWorkflowExecutionStartedAsync(invocationId);
            api.logWorkflowCompleteAsync(invocationId, "Completed OK");

            User u = api.getUser(t.getUserId());
            System.out.println("Home folder of: " + u.getName() + ": " + u.getHomeFolderId());

            Folder f = new Folder();
            f.setContainerId(u.getHomeFolderId());
            f.setName("Test Folder");
            f = api.saveFolder(f);
            System.out.println("Saved folder: " + f.getId() + ": " + f.getOrganisationId() + ": " + f.getCreatorId());

            DataProcessorServiceDefinition def = api.getService("blocks-core-io-upzip");
            System.out.println("Service definition: " + def.getCategory() + ": " + def.getName() + ": " + def.getServiceBackend() + ": " + def.getServiceRoutine());

            System.out.println("Latest version of: blocks-core-io-upzip: " + api.getLatestVersionId("blocks-core-io-upzip"));

            System.out.println("CORE library id: " + api.getDynamicWorkflowLibraryByName("core").getId());*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

