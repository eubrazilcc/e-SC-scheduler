/**
 * e-Science Central
 * Copyright (C) 2008-2013 School of Computing Science, Newcastle University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation at:
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, 5th Floor, Boston, MA 02110-1301, USA.
 *//*

package com.connexience.WorkflowPredictions;

import com.connexience.api.StorageClient;
import com.connexience.api.model.EscDocumentVersion;
import com.connexience.performance.model.PerformanceModel;
import com.connexience.performance.model.PerformanceModelParameter;
import com.connexience.performance.model.PerformanceModelPrediction;
import com.connexience.server.model.document.DocumentRecord;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pipeline.core.data.Data;
import org.pipeline.core.drawing.BlockExecutionException;
import org.pipeline.core.drawing.BlockExecutionReport;
import org.pipeline.core.drawing.DrawingException;
import org.pipeline.core.drawing.TransferData;
import org.pipeline.core.drawing.customio.CustomOutputDefinition;
import org.pipeline.core.drawing.customio.CustomisableDefaultBlockModel;
import org.pipeline.core.drawing.model.DefaultInputPortModel;
import org.pipeline.core.xmlstorage.XmlDataObject;
import org.pipeline.core.xmlstorage.XmlDataStore;
import org.pipeline.core.xmlstorage.XmlStorageException;
import org.pipeline.core.xmlstorage.xmldatatypes.XmlStorableDataObject;

import java.util.Enumeration;
import java.util.List;


public class DataProcessorPredictionBlock extends CustomisableDefaultBlockModel {
    private static Logger logger = Logger.getLogger(DataProcessorPredictionBlock.class);
    private String serviceId = null;
    private String versionId = null;
    private boolean latestVersion = true;
    private StorageClient sc = null;

    public DataProcessorPredictionBlock() throws DrawingException {
    }

    */
/** Set up this prediction block for the service *//*

    public void initialiseForService(StorageClient sc) throws Exception {
        this.sc = sc;
        if(latestVersion){
            EscDocumentVersion v = sc.getLatestDocumentVersion(serviceId);
            versionId = v.getId();
        }
    }

    @Override
    public BlockExecutionReport execute() throws BlockExecutionException {
        try {
            // Get a model
            PerformanceModel model;

            JSONObject fullReport = new JSONObject();
            JSONArray outputResults = new JSONArray();
            JSONObject outputReport;

            // Set the outputs
            CustomOutputDefinition outDef;
            for(int i=0;i<getOutputDefinitions().getDefinitionCount();i++){
                outDef = (CustomOutputDefinition)getOutputDefinitions().getDefinition(i);
                model = LookupUtils.lookupPerformanceModelBean().getBestOutputSizeModelForService(serviceId, versionId, outDef.getName());
                outputReport = new JSONObject();
                outputReport.put("outputName", outDef.getName());
                outputReport.put("commentary", new JSONArray());
                if(model!=null){
                    // Now try and use this model
                    PerformanceModelPrediction p = getModelPrediction(model, outputReport);
                    if(p!=null){
                        PredictionDataWrapper d = new PredictionDataWrapper();
                        d.setDataSize(p.getPredictedValue());
                        getOutput(outDef.getName()).setData(d);
                    } else {
                        // Try and get the mean value for the output
                        p = LookupUtils.lookupDataBean().getMeanPortSizeForAllVersionsOfService(serviceId, outDef.getName());
                        if(p.isValuePresent()){
                            // Mean for this service
                            putReportError(outputReport, "Using mean output value for this service");
                            outputReport.put("predictedValue", p.getPredictedValue());
                            PredictionDataWrapper d = new PredictionDataWrapper();
                            d.setDataSize(p.getPredictedValue());
                            getOutput(outDef.getName()).setData(d);

                        } else {
                            // Mean for all outputs of all services
                            PerformanceModelPrediction pMean = LookupUtils.lookupDataBean().getMeanPortSizeForAllPortsOfAllServices();
                            if(pMean.isValuePresent()){
                                // Can use average for all services
                                putReportError(outputReport, "Using mean output value for all services");
                                outputReport.put("predictedValue", pMean.getPredictedValue());
                                PredictionDataWrapper d = new PredictionDataWrapper();
                                d.setDataSize(pMean.getPredictedValue());
                                getOutput(outDef.getName()).setData(d);
                            } else {
                                // Nothing available
                                putReportError(outputReport, "No suitable data in database. Using 0");
                                outputReport.put("predictedValue", 0);
                                PredictionDataWrapper d = new PredictionDataWrapper();
                                d.setDataSize(0);
                                getOutput(outDef.getName()).setData(d);
                            }
                        }
                    }

                } else {
                    // Try and get the mean value for the output
                    PerformanceModelPrediction p = LookupUtils.lookupDataBean().getMeanPortSizeForAllVersionsOfService(serviceId, outDef.getName());
                    if(p.isValuePresent()){
                        // Mean for this service
                        putReportError(outputReport, "Using mean output value for this service");
                        outputReport.put("predictedValue", p.getPredictedValue());
                        PredictionDataWrapper d = new PredictionDataWrapper();
                        d.setDataSize(p.getPredictedValue());
                        getOutput(outDef.getName()).setData(d);

                    } else {
                        // Mean for all outputs of all services
                        PerformanceModelPrediction pMean = LookupUtils.lookupDataBean().getMeanPortSizeForAllPortsOfAllServices();
                        if(pMean.isValuePresent()){
                            // Can use average for all services
                            putReportError(outputReport, "Using mean output value for all services");
                            outputReport.put("predictedValue", pMean.getPredictedValue());
                            PredictionDataWrapper d = new PredictionDataWrapper();
                            d.setDataSize(pMean.getPredictedValue());
                            getOutput(outDef.getName()).setData(d);
                        } else {
                            // Nothing available
                            putReportError(outputReport, "No suitable data in database. Using 0");
                            outputReport.put("predictedValue", 0);
                            PredictionDataWrapper d = new PredictionDataWrapper();
                            d.setDataSize(0);
                            getOutput(outDef.getName()).setData(d);
                        }
                    }

                }
                outputResults.put(outputReport);
            }

            fullReport.put("outputResults", outputResults);

            // Predict the duration
            JSONObject durationReport = new JSONObject();
            model = LookupUtils.lookupPerformanceModelBean().getBestDurationModelForService(serviceId, versionId);
            if(model!=null){
                // Have a model
                PerformanceModelPrediction p = getModelPrediction(model, durationReport);
                if(p!=null){
                    durationReport.put("predictedValue", p.getPredictedValue());
                } else {
                    p = LookupUtils.lookupDataBean().getAverageExecutionTimeForService(serviceId);
                    if(p.isValuePresent()){
                        putReportError(durationReport, "Using mean value for all versions of this service");
                        durationReport.put("predictedValue", p.getPredictedValue());
                    } else {
                        PerformanceModelPrediction pMean = LookupUtils.lookupDataBean().getAverageExecutionTimeForAllVersionsOfAllServices();
                        if(pMean.isValuePresent()){
                            putReportError(durationReport, "Using mean duration for all services");
                            durationReport.put("predictedValue", pMean.getPredictedValue());
                        } else {
                            putReportError(durationReport, "No suitable data in database. Using 0");
                            durationReport.put("predictedValue", 0);
                        }
                    }
                }

            } else {
                // Fallbacks. Mean for all versions of this service.
                // Then mean for all versions of all services
                PerformanceModelPrediction p = LookupUtils.lookupDataBean().getAverageExecutionTimeForService(serviceId);
                if(p.isValuePresent()){
                    putReportError(durationReport, "Using mean value for all versions of this service");
                    durationReport.put("predictedValue", p.getPredictedValue());
                } else {
                    PerformanceModelPrediction pMean = LookupUtils.lookupDataBean().getAverageExecutionTimeForAllVersionsOfAllServices();
                    if(pMean.isValuePresent()){
                        putReportError(durationReport, "Using mean duration for all services");
                        durationReport.put("predictedValue", pMean.getPredictedValue());
                    } else {
                        putReportError(durationReport, "No suitable data in database. Using 0");
                        durationReport.put("predictedValue", 0);
                    }
                }
            }

            fullReport.put("duration", durationReport);
            BlockExecutionReport report = new BlockExecutionReport(this, BlockExecutionReport.NO_ERRORS);
            report.setCommandOutputStored(true);
            report.setCommandOutput(fullReport.toString());
            return report;

        } catch (Exception e){
            throw new BlockExecutionException("Error running block: " + e.getMessage());
        }
    }

    private PerformanceModelPrediction getModelPrediction(PerformanceModel model, JSONObject reportJson) throws Exception {
        // Get a list of the parameters
        reportJson.put("modelType", model.getClass().getSimpleName());
        reportJson.put("modelId", model.getId());
        reportJson.put("blockGuid", getBlockGUID());
        reportJson.put("serviceId", serviceId);
        reportJson.put("versionId", versionId);

        Data xData = new Data();
        boolean error = false;
        PerformanceModelParameter[] params = model.getInputSelectionList();
        for(PerformanceModelParameter p : params){
            if(p.getColumnType().equals(PerformanceModelParameter.INPUT_SIZE)){
                // Add an input size value
                try {
                    TransferData td = getInput(p.getColumnName()).getData();
                    if(td instanceof PredictionDataWrapper){
                        xData.addSingleValue(p.getColumnName(), ((PredictionDataWrapper)td).getDataSize());
                    } else {
                        putReportError(reportJson, "Cannot get input data size: " + p.getColumnName());
                        error = true;
                        logger.error("Cannot get input data size: " + p.getColumnName());
                    }
                } catch (Exception e){
                    putReportError(reportJson, "Cannot get input size parameter: " + e.getMessage());
                    error = true;
                    logger.error("Error getting input size parameter: " + e.getMessage());
                }

            } else if(p.getColumnType().equals(PerformanceModelParameter.SERVICE_PROPERTY)){
                // Add a service property value
                if(getEditableProperties().containsName(p.getColumnName())){
                    XmlDataObject propertyObject = getEditableProperties().get(p.getColumnName());
                    if(!(propertyObject instanceof XmlStorableDataObject)){
                        // Basic type
                        try {
                            double value = Double.parseDouble(propertyObject.getValue().toString());
                            xData.addSingleValue(p.getColumnName(), value);
                        } catch (Exception e){
                            error = true;
                            putReportError(reportJson, "Error converting property to double: " + e.getMessage());
                            logger.error("Error converting property to double : " + e.getMessage());
                        }

                    } else {
                        // More complex parameter
                        XmlStorableDataObject so = (XmlStorableDataObject)propertyObject;
                        Object o = so.getValue();
                        if(o instanceof DocumentRecord){
                            // Add the size of the document record
                            try {
                                EscDocumentVersion v = sc.getLatestDocumentVersion(((DocumentRecord)o).getId());
                                if(v!=null){
                                    xData.addSingleValue(p.getColumnName(), (double)v.getSize());
                                } else {
                                    error = true;
                                    putReportError(reportJson, "Error getting latest version of document: " + ((DocumentRecord)o).getId());
                                    logger.error("Cannot get latset version of document: " + ((DocumentRecord)o).getName());
                                }
                            } catch (Exception e){
                                error = true;
                                putReportError(reportJson, "Error getting document size: " + e.getMessage());
                                logger.error("Error getting document size: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        if(!error){
            List<PerformanceModelPrediction> predictions = model.getPredictions(xData);
            if(predictions.size()==1){
                putSuccessIfNoExistingError(reportJson);
                reportJson.put("predictedValue", predictions.get(0).getPredictedValue());
                return predictions.get(0);
            } else {
                putReportError(reportJson, "Wrong prediction data size");
                logger.error("Wrong predictions size");
                return null;
            }
        } else {
            return null;
        }
    }

    private void putSuccessIfNoExistingError(JSONObject report){
        if(!report.has("modelError")){
            try {
                report.put("modelError", false);
            } catch (Exception e){
                logger.error("Error setting report to contain no errors: " + e.getMessage());
            }
        }
    }

    private void putReportError(JSONObject report, String message){
        try {
            report.put("modelError", true);
            if(report.has("commentary")){
                report.getJSONArray("commentary").put(message);
            } else {
                JSONArray commentary = new JSONArray();
                commentary.put(message);
                report.put("commentary", commentary);
            }

        } catch (Exception e){
            logger.error("Error adding result to JSONObject: " + e.getMessage());
        }
    }

    @Override
    public void recreateObject(XmlDataStore xmlDataStore) throws XmlStorageException {
        super.recreateObject(xmlDataStore); //To change body of generated methods, choose Tools | Templates.

        Enumeration inputEnum = getInputs().elements();
        DefaultInputPortModel input;
        while(inputEnum.hasMoreElements()){
            input = (DefaultInputPortModel)inputEnum.nextElement();

            input.setDataTypeRestricted(true);
            input.addDataType(PredictionDataDataType.PREDICTION_WRAPPER_TYPE);
        }

        serviceId = xmlDataStore.stringValue("ServiceID", null);
        versionId = xmlDataStore.stringValue("VersionID", null);
        latestVersion = xmlDataStore.booleanValue("LatestVersion", true);
    }


}*/
