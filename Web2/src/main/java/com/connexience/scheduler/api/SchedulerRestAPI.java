package com.connexience.scheduler.api;

import com.connexience.scheduler.*;
import com.connexience.scheduler.model.*;
import com.connexience.server.ConnexienceException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;


/**
 * This is the API which the Scheduler exposes to engines (and other parties
 * Created by Jacek on 15/12/2015.
 */
@Path("/rest")
public class SchedulerRestAPI
{
    private static final Logger _Logger = LoggerFactory.getLogger(SchedulerRestAPI.class);

    @EJB
    InvocationSchedulerLocal _schedulerBean;


    @GET
    @Path("/engines")
    @Produces("application/json")
    public String listRegisteredEngines()
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
            return mapper.writeValueAsString(_schedulerBean.listRegisteredNodes());
        } catch (IOException x) {
            return _ReportError("Internal error: " + x.getMessage());
        }
    }


    @PUT
    @Path("/engine/{engineId}")
    @Produces("application/json")
    public String registerEngine(@PathParam(value = "engineId") String engineId)
    {
        if (engineId == null || "".equals(engineId.trim())) {
            return _ReportError("engineId cannot be null nor empty");
        }

        ComputeNode node;
        // TODO: Upon engine registration, the performance monitor will unlikely have resource information about it.
        // But let's try to construct a view anyway.
        try {
            node = Utils.ToComputeNode(PerformanceClient.GetEngineInfo(engineId));
        } catch (ConnexienceException x) {
            _Logger.error("Cannot read engine information.", x);

            // If we can't read from the Perfomance Monitor, the default, minimalistic view of the ComputeNode is used.
            // This information should be updated later on (somehow???).
            node = Utils.GetDefaultComputeNode(engineId, 7);
        }

        try {
            String regId = _schedulerBean.registerNode(node);
            if (regId != null) {
                return _ReportOk(null,
                        new AbstractMap.SimpleEntry<String, Object>("queueName", QueueManager.GetQueueNameForEngine(engineId)),
                        new AbstractMap.SimpleEntry<String, Object>("registrationId", regId));
            } else {
                return _ReportError("Unable to register node: " + node.id);
            }
        } catch (AlreadyRegisteredException x) {
            // TODO: Think about whether this exception can be harmful
            _Logger.warn("Engine {} has already been registered.", engineId);
            return _ReportError("Engine has already been registered");
        }
    }


    @DELETE
    @Path("/engine/{engineId}/{registrationId}")
    @Produces("application/json")
    public String unregisterEngine(@PathParam(value = "engineId") String engineId, @PathParam(value = "registrationId") String regId) {
        try {
            _schedulerBean.unregisterNode(engineId, regId);
            return _ReportOk(null);
        } catch (ResourceNotAvailableException x) {
            _Logger.info("Invalid engine or registration id.", x);
            return _ReportError("Cannot unregister engine.");
        }
    }


    private static String _ReportOk(String message) {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("error", false);
            if (message != null && message.length() > 0) {
                jsonObj.put("statusMessage", message);
            }
        } catch (JSONException x) {
            // This should never happened.
            throw new RuntimeException(x);
        }

        return jsonObj.toString();
    }


    private static String _ReportOk(String message, Map.Entry<String, Object>... props)
    {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("error", false);

            if (message != null && message.length() > 0) {
                jsonObj.put("statusMessage", message);
            }

            if (props != null && props.length > 0) {
                for (Map.Entry<String, Object> prop : props) {
                    jsonObj.put(prop.getKey(), prop.getValue());
                }
            }
        } catch (JSONException x) {
            // This should never happened.
            throw new RuntimeException(x);
        }

        return jsonObj.toString();
    }


    private static String _ReportOk(String message, Map<String, Object> props)
    {
        JSONObject jsonObj = new JSONObject(props);

        try {
            jsonObj.put("error", false);
            if (message != null && message.length() > 0) {
                jsonObj.put("statusMessage", message);
            }
        } catch (JSONException x) {
            // This should never happened.
            throw new RuntimeException(x);
        }

        return jsonObj.toString();
    }


    private static String _ReportError(String message) {
        JSONObject jsonObj = new JSONObject();

        try {
            jsonObj.put("error", true);
            if (message != null && message.length() > 0) {
                jsonObj.put("statusMessage", message);
            }
        } catch (JSONException x) {
            // This should never happened.
            throw new RuntimeException(x);
        }

        return jsonObj.toString();
    }
}
