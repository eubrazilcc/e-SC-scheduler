package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.server.ConnexienceException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class might be moved to the MonitoringServer project.
 *
 * Created by Jacek on 15/12/2015.
 */
public class PerformanceClient
{
    private static final String PERFMON_URL = "http://localhost:8080/monitor";


    public static WorkflowEngineInstance[] RefreshEngineInfo() throws ConnexienceException
    {
        try {
            URL url = new URL(PERFMON_URL + "/engines/listActive");
            ObjectMapper mapper = new ObjectMapper();
            WorkflowEngineInstance[] engines = mapper.readValue(url, WorkflowEngineInstance[].class);

            return engines;
        } catch (IOException x) {
            throw new ConnexienceException("Error fetching engine information", x);
        }
    }


    public static WorkflowEngineInstance GetEngineInfo(String engineId) throws ConnexienceException
    {
        // If it's not in the cache, ask the performance monitor
        try {
            // FIXME: Add method to the monitor server to list only a selected engine
            URL url = new URL(PERFMON_URL + "/engines/info/" + engineId);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(url, WorkflowEngineInstance.class);

            /*
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new ConnexienceException("Errorneous response from the Monitoring Server: " + connection.getResponseCode() + ": " + connection.getResponseMessage());
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response;
                response = readAll(bufferedReader);

                ObjectMapper mapper = new ObjectMapper();
                WorkflowEngineInstance[] workflowEngineInstances =
                        mapper.readValue()
                        mapper.readValue(response, WorkflowEngineInstance[].class);
            }
            */
        } catch (IOException x) {
            throw new ConnexienceException("Error fetching engine information", x);
        }
    }
}
