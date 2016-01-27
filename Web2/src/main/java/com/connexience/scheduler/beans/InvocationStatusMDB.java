package com.connexience.scheduler.beans;

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.api.InvocationSchedulerLocal;
import com.connexience.scheduler.model.Resource;
import com.connexience.server.jms.APIMessageHandler;
import com.connexience.server.jms.JMSBiDirEndpoint;
import com.connexience.server.model.workflow.WorkflowInvocationFolder;
import com.connexience.server.workflow.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import javax.inject.Inject;
import javax.jms.*;
import java.io.Serializable;


@MessageDriven(name = "InvocationStatusMDB",
        activationConfig
                = {
                @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/WorkflowManagerTopic")
        },
        mappedName = "topic/WorkflowManagerTopic",
        messageListenerInterface = MessageListener.class)
public class InvocationStatusMDB implements MessageListener, APIMessageHandler
{
    private static Logger _Logger = LoggerFactory.getLogger(InvocationStatusMDB.class);


    @Inject
    private Connection _connection;

    @EJB
    private InvocationSchedulerLocal _schedulerBean;

    private JMSBiDirEndpoint _jmsEndpoint;


    @Override
    public void onMessage(Message message)
    {
        try {
            _Logger.debug("Received message to process: " + message);
            if (_jmsEndpoint == null) {
                _Logger.info("JMS endpoint is null: creating a new instance");

                // Create the endpoint in the monitoring mode, so any responses will no be sent.
                _jmsEndpoint = new JMSBiDirEndpoint(_connection, null, false);
                _jmsEndpoint.registerAPI(API.class.getName(), this);
                        _Logger.debug("New JMS endpoint created: " + _jmsEndpoint);
            } else {
                _Logger.debug("Reusing the existing JMS endpoint instance: " + _jmsEndpoint);
            }

            _jmsEndpoint.onMessage(message);
            _Logger.debug("Message has been processed: " + message);

//                BytesMessage bm = (BytesMessage) message;
//
//                bm.reset();
//                byte[] messageData = new byte[(int) bm.getBodyLength()];
//                bm.readBytes(messageData);
//
//                //Deserialise and interpret the object
//                Object payload = SerializationUtils.deserialize(messageData);
//
////            if (payload instanceof WorkflowEngineStats) {
////                WorkflowEngineStats engineStats = (WorkflowEngineStats)payload;
////
////                if (engineStats.isInvocationFinished()) {
////                    _Logger.debug("Scheduler calling updateEngineStats");
////                    //EngineInformationManager manager = (EngineInformationManager) new InitialContext().lookup("java:global/ejb/EngineInformationManager");
////                    _manager.updateEngineStatus(engineStats.getIpAddress(), false, true);
////                }
////            }
////            else if(payload instanceof WorkflowInvocation){
//                if (payload instanceof WorkflowInvocation) {
//                    _Logger.debug("Got a WorkflowInvocation message");
//                    WorkflowInvocation invocation = (WorkflowInvocation) payload;
//                    //EngineInformationManager manager = new EngineInformationManager();
//                    //_manager.freeResourceInformation(invocation.getInvocationId());
//                }
        } catch (Exception e) {
            _Logger.error("Error processing message", e);
        }
    }

    @Override
    public Serializable handleMessage(String operationName, Serializable securityContext, Serializable... args) throws Exception
    {
        switch (operationName) {
            case "notifyEngineShutdown":
                _notifyEngineShutdown((String) args[0]);
                return "";
            case "notifyEngineStartup":
                _notifyEngineStartup((String) args[0]);
                return "";
            case "logWorkflowComplete":
                _logWorkflowComplete((String) args[0], (String) args[1]);
                return "";
            case "setWorkflowStatus":
                _setWorkflowStatus((String) args[0], (Integer) args[1], (String) args[2]);
                return "";
            default:
                _Logger.trace("Operation {} has been ignored", operationName);
        }

        return "";
    }


    private void _logWorkflowComplete(String invocationId, String status) {
        try {
            _Logger.debug("WorkflowComplete {}: releasing resources for invocation: {}", status, invocationId);
            _schedulerBean.releaseResources(invocationId);
        } catch (ResourceNotAvailableException x) {
            _Logger.warn("Internal scheduler error: cannot release resources for invocation: {}, status: {}", invocationId, status, x);
        }
    }


    private void _notifyEngineShutdown(String hostId) {
        try {
            _schedulerBean.unregisterNode(hostId, null);
        } catch (ResourceNotAvailableException x) {
            _Logger.error("Cannot unregister node: {}: {}", hostId, x);
        }
    }


    private void _notifyEngineStartup(String hostId) {
        // For the moment engine startup event is ignored until the engine asks for the queue
    }


    private void _setWorkflowStatus(String invocationId, int status, String message) {
        switch (status) {
            case WorkflowInvocationFolder.INVOCATION_FINISHED_WITH_ERRORS:
            case WorkflowInvocationFolder.INVOCATION_FINISHED_OK:
                try {
                    _Logger.debug("WorkflowStatus {}: releasing resources for invocation: {}", status, invocationId);
                    _schedulerBean.releaseResources(invocationId);
                } catch (ResourceNotAvailableException x) {
                    _Logger.warn("Internal scheduler error: cannot release resources for invocation: {}, status: {}", invocationId, status, x);
                }
                break;
        }
    }
}
