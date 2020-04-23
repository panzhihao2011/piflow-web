package com.nature.component.process.utils;

import com.nature.base.util.JsonUtils;
import com.nature.base.util.SqlUtils;
import com.nature.base.util.SvgUtils;
import com.nature.base.vo.UserVo;
import com.nature.common.Eunm.PortType;
import com.nature.common.Eunm.ProcessParentType;
import com.nature.common.Eunm.ProcessState;
import com.nature.common.Eunm.RunModeType;
import com.nature.component.dataSource.utils.DataSourceUtils;
import com.nature.component.flow.model.*;
import com.nature.component.mxGraph.model.MxGraphModel;
import com.nature.component.mxGraph.utils.MxGraphModelUtils;
import com.nature.component.mxGraph.vo.MxGraphModelVo;
import com.nature.component.process.model.*;
import com.nature.component.process.model.Process;
import com.nature.component.process.vo.ProcessPathVo;
import com.nature.component.process.vo.ProcessStopPropertyVo;
import com.nature.component.process.vo.ProcessStopVo;
import com.nature.component.process.vo.ProcessVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.*;

public class ProcessUtils {

    public static Process processNewNoId(String username) {
        Process process = new Process();
        // basic properties (required when creating)
        process.setCrtDttm(new Date());
        process.setCrtUser(username);
        // basic properties
        process.setEnableFlag(true);
        process.setLastUpdateUser(username);
        process.setLastUpdateDttm(new Date());
        process.setVersion(0L);
        return process;
    }

    public static Process initProcessBasicPropertiesNoId(Process process, String username) {
        if (null == process) {
            return processNewNoId(username);
        }
        // basic properties (required when creating)
        process.setCrtDttm(new Date());
        process.setCrtUser(username);
        // basic properties
        process.setEnableFlag(true);
        process.setLastUpdateUser(username);
        process.setLastUpdateDttm(new Date());
        process.setVersion(0L);
        return process;
    }

    public static ProcessVo processOnePoToVo(Process process) {
        ProcessVo processVo = null;
        if (null != process) {
            processVo = new ProcessVo();
            BeanUtils.copyProperties(process, processVo);
            processVo.setCrtDttm(process.getCrtDttm());
        }
        return processVo;
    }

    public static ProcessVo processPoToVo(Process process) {
        if (null == process) {
            return null;
        }
        ProcessVo processVo = new ProcessVo();
        BeanUtils.copyProperties(process, processVo);
        MxGraphModel mxGraphModel = process.getMxGraphModel();
        if (null != mxGraphModel) {
            MxGraphModelVo mxGraphModelVo = MxGraphModelUtils.mxGraphModelPoToVo(mxGraphModel);
            processVo.setMxGraphModelVo(mxGraphModelVo);
        }
        List<ProcessStop> processStopList = process.getProcessStopList();
        if (CollectionUtils.isNotEmpty(processStopList)) {
            List<ProcessStopVo> processStopVos = processStopListPoToVo(processStopList);
            if (CollectionUtils.isNotEmpty(processStopVos)) {
                processVo.setProcessStopVoList(processStopVos);
            }
        }
        List<ProcessPath> processPathList = process.getProcessPathList();
        if (CollectionUtils.isNotEmpty(processPathList)) {
            List<ProcessPathVo> processPathVos = processPathListPoToVo(processPathList);
            if (CollectionUtils.isNotEmpty(processPathVos)) {
                processVo.setProcessPathVoList(processPathVos);
            }
        }
        return processVo;
    }

    public static List<ProcessStopVo> processStopListPoToVo(List<ProcessStop> processStopList) {
        List<ProcessStopVo> processStopVoList = null;
        if (null != processStopList && processStopList.size() > 0) {
            processStopVoList = new ArrayList<ProcessStopVo>();
            for (ProcessStop processStop : processStopList) {
                ProcessStopVo processStopVo = processStopPoToVo(processStop);
                if (null != processStopVo) {
                    processStopVoList.add(processStopVo);
                }
            }
        }
        return processStopVoList;
    }

    public static ProcessStopVo processStopPoToVo(ProcessStop processStop) {
        ProcessStopVo processStopVo = null;
        if (null != processStop) {
            processStopVo = new ProcessStopVo();
            BeanUtils.copyProperties(processStop, processStopVo);
            processStopVo.setState((null != processStop.getState() ? processStop.getState().name() : ""));
            List<ProcessStopProperty> processStopPropertyList = processStop.getProcessStopPropertyList();
            if (null != processStopPropertyList && processStopPropertyList.size() > 0) {
                List<ProcessStopPropertyVo> processStopPropertyVoList = new ArrayList<ProcessStopPropertyVo>();
                for (ProcessStopProperty processStopProperty : processStopPropertyList) {
                    if (null != processStopProperty) {
                        ProcessStopPropertyVo processStopPropertyVo = new ProcessStopPropertyVo();
                        BeanUtils.copyProperties(processStopProperty, processStopPropertyVo);
                        processStopPropertyVoList.add(processStopPropertyVo);
                    }
                }
                processStopVo.setProcessStopPropertyVoList(processStopPropertyVoList);
            }
        }
        return processStopVo;
    }

    public static List<ProcessPathVo> processPathListPoToVo(List<ProcessPath> processPathList) {
        List<ProcessPathVo> processPathVoList = null;
        if (null != processPathList && processPathList.size() > 0) {
            processPathVoList = new ArrayList<ProcessPathVo>();
            for (ProcessPath processPath : processPathList) {
                ProcessPathVo processPathVo = processPathPoToVo(processPath);
                if (null != processPathVo) {
                    processPathVoList.add(processPathVo);
                }
            }
        }
        return processPathVoList;
    }

    public static ProcessPathVo processPathPoToVo(ProcessPath processPath) {
        ProcessPathVo processPathVo = null;
        if (null != processPath) {
            processPathVo = new ProcessPathVo();
            BeanUtils.copyProperties(processPath, processPathVo);
        }
        return processPathVo;
    }

    public static Process flowToProcess(Flow flow, UserVo user) {
        Process process = null;
        if (null != flow) {
            String username = (null != user) ? user.getUsername() : "-1";
            process = new Process();
            // Copy flow information to process
            BeanUtils.copyProperties(flow, process);
            // Set basic information
            process = initProcessBasicPropertiesNoId(process, username);
            process.setId(SqlUtils.getUUID32());
            FlowGroup flowGroup = flow.getFlowGroup();
            //Set default
            process.setProcessParentType(ProcessParentType.PROCESS);
            if (null != flowGroup) {
                process.setProcessParentType(ProcessParentType.GROUP);
            }
            // Take out the flow board information of the flow
            MxGraphModel mxGraphModel = flow.getMxGraphModel();
            // Flow artBoard information is converted to ViewXml
            /*
            String viewXml = SvgUtils.mxGraphModelToViewXml(mxGraphModel, false, false);
            // set viewXml
            process.setViewXml(viewXml);
            */
            MxGraphModel mxGraphModelProcess = new MxGraphModel();
            BeanUtils.copyProperties(mxGraphModel, mxGraphModelProcess);
            mxGraphModelProcess = MxGraphModelUtils.removeIdMxGraphModel(mxGraphModelProcess);
            mxGraphModelProcess = MxGraphModelUtils.initMxGraphModelBasicPropertiesNoId(mxGraphModelProcess, username);
            // unlink
            mxGraphModelProcess.setFlow(null);
            // add link
            mxGraphModelProcess.setProcess(process);
            process.setMxGraphModel(mxGraphModelProcess);

            // set flowId
            process.setFlowId(flow.getId());
            // Stops to remove flow
            List<Stops> stopsList = flow.getStopsList();
            // stopsList isEmpty
            if (null != stopsList && stopsList.size() > 0) {
                // List of stop of process
                List<ProcessStop> processStopList = new ArrayList<ProcessStop>();
                // Loop stopsList
                for (Stops stops : stopsList) {
                    // isEmpty
                    if (null == stops) {
                        continue;
                    }
                    ProcessStop processStop = new ProcessStop();
                    // Copy stops information into processStop
                    BeanUtils.copyProperties(stops, processStop);
                    // Set basic information
                    processStop = ProcessStopUtils.initProcessStopBasicPropertiesNoId(processStop, username);
                    processStop.setId(SqlUtils.getUUID32());
                    // Associate foreign key
                    processStop.setProcess(process);
                    // Remove the properties of stops
                    List<Property> properties = stops.getProperties();
                    // Determine if the stops attribute is empty
                    if (null != properties && properties.size() > 0) {
                        Map<String, String> dataSourcePropertyMap = DataSourceUtils.dataSourceToPropertyMap(stops.getDataSource());
                        List<ProcessStopProperty> processStopPropertyList = new ArrayList<>();
                        // Attributes of loop stops
                        for (Property property : properties) {
                            // isEmpty
                            if (null == property) {
                                continue;
                            }
                            ProcessStopProperty processStopProperty = new ProcessStopProperty();
                            // Copy property information into processStopProperty
                            BeanUtils.copyProperties(property, processStopProperty);
                            // Set basic information
                            processStopProperty = ProcessStopPropertyUtils.initProcessStopPropertyBasicPropertiesNoId(processStopProperty, username);
                            processStopProperty.setId(SqlUtils.getUUID32());
                            // "stop" attribute name
                            String name = property.getName();
                            // Judge empty
                            if (StringUtils.isNotBlank(name)) {
                                // Go to the map of the "datasource" attribute
                                String value = dataSourcePropertyMap.get(name.toLowerCase());
                                // Judge empty
                                if (StringUtils.isNotBlank(value)) {
                                    // Assignment
                                    processStopProperty.setCustomValue(value);
                                }
                            }
                            // Associated foreign key
                            processStopProperty.setProcessStop(processStop);
                            processStopPropertyList.add(processStopProperty);
                        }
                        processStop.setProcessStopPropertyList(processStopPropertyList);
                    }

                    // Take out the custom properties of stops
                    List<CustomizedProperty> customizedPropertyList = stops.getCustomizedPropertyList();
                    // Determine if the stops attribute is empty
                    if (null != customizedPropertyList && customizedPropertyList.size() > 0) {
                        List<ProcessStopCustomizedProperty> processStopCustomizedPropertyList = new ArrayList<>();
                        // Attributes of loop stops
                        for (CustomizedProperty customizedProperty : customizedPropertyList) {
                            // isEmpty
                            if (null == customizedProperty) {
                                continue;
                            }
                            ProcessStopCustomizedProperty processStopCustomizedProperty = new ProcessStopCustomizedProperty();
                            // Copy customizedProperty information into processStopCustomizedProperty
                            BeanUtils.copyProperties(customizedProperty, processStopCustomizedProperty);
                            // Set basic information
                            processStopCustomizedProperty = ProcessStopCustomizedPropertyUtils.initProcessStopCustomizedPropertyBasicPropertiesNoId(processStopCustomizedProperty, username);
                            processStopCustomizedProperty.setId(SqlUtils.getUUID32());
                            // Associated foreign key
                            processStopCustomizedProperty.setProcessStop(processStop);
                            processStopCustomizedPropertyList.add(processStopCustomizedProperty);
                        }
                        processStop.setProcessStopCustomizedPropertyList(processStopCustomizedPropertyList);
                    }
                    processStopList.add(processStop);
                }
                process.setProcessStopList(processStopList);
            }
            // Get the paths information of flow
            List<Paths> pathsList = flow.getPathsList();
            // isEmpty
            if (null != pathsList && pathsList.size() > 0) {
                List<ProcessPath> processPathList = new ArrayList<ProcessPath>();
                // Loop paths information
                for (Paths paths : pathsList) {
                    // isEmpty
                    if (null == paths) {
                        continue;
                    }
                    ProcessPath processPath = new ProcessPath();
                    // Copy paths information into processPath
                    BeanUtils.copyProperties(paths, processPath);
                    // Set basic information
                    processPath = ProcessPathUtils.initProcessPathBasicPropertiesNoId(processPath, username);
                    processPath.setId(SqlUtils.getUUID32());
                    // Associated foreign key
                    processPath.setProcess(process);
                    processPathList.add(processPath);
                }
                process.setProcessPathList(processPathList);
            }
        }
        return process;
    }

    public static Process copyProcessAndNew(Process process, UserVo currentUser, RunModeType runModeType) {
        Process processCopy = null;
        if (null != currentUser) {
            String username = currentUser.getUsername();
            if (StringUtils.isNotBlank(username) && null != process) {
                processCopy = new Process();
                BeanUtils.copyProperties(process, processCopy);
                processCopy = ProcessUtils.initProcessBasicPropertiesNoId(processCopy, username);
                processCopy.setId(SqlUtils.getUUID32());
                processCopy.setState(ProcessState.STARTED);
                processCopy.setRunModeType(null != runModeType ? runModeType : RunModeType.RUN);

                processCopy.setParentProcessId(StringUtils.isNotBlank(process.getParentProcessId()) ? process.getParentProcessId() : process.getProcessId());
                processCopy.setProcessParentType(ProcessParentType.PROCESS);
                ProcessGroup processGroup = process.getProcessGroup();
                if (null != processGroup) {
                    processCopy.setProcessParentType(ProcessParentType.GROUP);
                }
                List<ProcessPath> processPathList = process.getProcessPathList();
                if (null != processPathList && processPathList.size() > 0) {
                    List<ProcessPath> processPathListCopy = new ArrayList<ProcessPath>();
                    for (ProcessPath processPath : processPathList) {
                        if (null != processPath) {
                            ProcessPath processPathCopy = new ProcessPath();
                            processPathCopy.setId(SqlUtils.getUUID32());
                            processPathCopy.setCrtDttm(new Date());
                            processPathCopy.setCrtUser(username);
                            processPathCopy.setLastUpdateDttm(new Date());
                            processPathCopy.setLastUpdateUser(username);
                            processPathCopy.setEnableFlag(true);
                            processPathCopy.setFrom(processPath.getFrom());
                            processPathCopy.setTo(processPath.getTo());
                            processPathCopy.setInport(processPath.getInport());
                            processPathCopy.setOutport(processPath.getOutport());
                            processPathCopy.setPageId(processPath.getPageId());
                            processPathCopy.setProcess(processCopy);
                            processPathListCopy.add(processPathCopy);
                        }
                    }
                    processCopy.setProcessPathList(processPathListCopy);
                }
                List<ProcessStop> processStopList = process.getProcessStopList();
                if (null != processStopList && processStopList.size() > 0) {
                    List<ProcessStop> processStopListCopy = new ArrayList<ProcessStop>();
                    for (ProcessStop processStop : processStopList) {
                        if (null != processStop) {
                            ProcessStop processStopCopy = new ProcessStop();
                            processStopCopy.setId(SqlUtils.getUUID32());
                            processStopCopy.setCrtDttm(new Date());
                            processStopCopy.setCrtUser(username);
                            processStopCopy.setLastUpdateDttm(new Date());
                            processStopCopy.setLastUpdateUser(username);
                            processStopCopy.setEnableFlag(true);
                            processStopCopy.setBundel(processStop.getBundel());
                            processStopCopy.setName(processStop.getName());
                            processStopCopy.setDescription(processStop.getDescription());
                            processStopCopy.setGroups(processStop.getGroups());
                            processStopCopy.setInports(processStop.getInports());
                            processStopCopy.setInPortType(processStop.getInPortType());
                            processStopCopy.setOutports(processStop.getOutports());
                            processStopCopy.setOutPortType(processStop.getOutPortType());
                            processStopCopy.setOwner(processStop.getOwner());
                            processStopCopy.setPageId(processStop.getPageId());
                            processStopCopy.setProcess(processCopy);
                            List<ProcessStopProperty> processStopPropertyList = processStop.getProcessStopPropertyList();
                            if (null != processStopPropertyList && processStopPropertyList.size() > 0) {
                                List<ProcessStopProperty> processStopPropertyListCopy = new ArrayList<ProcessStopProperty>();
                                for (ProcessStopProperty processStopProperty : processStopPropertyList) {
                                    if (null != processStopProperty) {
                                        ProcessStopProperty processStopPropertyCopy = new ProcessStopProperty();
                                        processStopPropertyCopy.setId(SqlUtils.getUUID32());
                                        processStopPropertyCopy.setCrtDttm(new Date());
                                        processStopPropertyCopy.setCrtUser(username);
                                        processStopPropertyCopy.setLastUpdateDttm(new Date());
                                        processStopPropertyCopy.setLastUpdateUser(username);
                                        processStopPropertyCopy.setEnableFlag(true);
                                        processStopPropertyCopy.setCustomValue(processStopProperty.getCustomValue());
                                        processStopPropertyCopy.setName(processStopProperty.getName());
                                        processStopPropertyCopy.setAllowableValues(processStopProperty.getAllowableValues());
                                        processStopPropertyCopy.setDescription(processStopProperty.getDescription());
                                        processStopPropertyCopy.setDisplayName(processStopProperty.getDisplayName());
                                        processStopPropertyCopy.setRequired(processStopProperty.getRequired());
                                        processStopPropertyCopy.setSensitive(processStopPropertyCopy.getSensitive());
                                        processStopPropertyCopy.setProcessStop(processStopCopy);
                                        processStopPropertyListCopy.add(processStopPropertyCopy);
                                    }
                                }
                                processStopCopy.setProcessStopPropertyList(processStopPropertyListCopy);
                            }
                            processStopListCopy.add(processStopCopy);
                        }
                    }
                    processCopy.setProcessStopList(processStopListCopy);
                }
            }
        }
        return processCopy;
    }

    public static String processToJson(Process process, String checkpoint, RunModeType runModeType) {
        Map<String, Object> flowVoMap = processToMap(process, checkpoint, runModeType);
        return JsonUtils.toFormatJsonNoException(flowVoMap);
    }

    public static String processGroupToJson(ProcessGroup processGroup, RunModeType runModeType) {
        Map<String, Object> flowGroupVoMap = processGroupToMap(processGroup, runModeType);
        return JsonUtils.toFormatJsonNoException(flowGroupVoMap);
    }

    public static Map<String, Object> processToMap(Process process, String checkpoint, RunModeType runModeType) {
        Map<String, Object> rtnMap = new HashMap<>();
        Map<String, Object> flowVoMap = new HashMap<>();

        flowVoMap.put("driverMemory", process.getDriverMemory());
        flowVoMap.put("executorMemory", process.getExecutorMemory());
        flowVoMap.put("executorCores", process.getExecutorCores());
        flowVoMap.put("executorNumber", process.getExecutorNumber());
        flowVoMap.put("name", process.getName());
        flowVoMap.put("uuid", process.getId());

        // all stops
        Map<String, ProcessStop> stopsMap = new HashMap<>();

        List<Map<String, Object>> processStopMapList = new ArrayList<>();
        List<ProcessStop> processStopList = process.getProcessStopList();
        for (ProcessStop processStop : processStopList) {
            stopsMap.put(processStop.getPageId(), processStop);
        }

        // paths
        List<Map<String, Object>> thirdPathVoMapList = new ArrayList<>();
        List<ProcessPath> processPathList = process.getProcessPathList();
        if (null != processPathList && processPathList.size() > 0) {
            for (ProcessPath processPath : processPathList) {
                ProcessStop fromProcessStop = stopsMap.get(processPath.getFrom());
                ProcessStop toProcessStop = stopsMap.get(processPath.getTo());
                if (null == fromProcessStop) {
                    fromProcessStop = new ProcessStop();
                }
                if (null == toProcessStop) {
                    toProcessStop = new ProcessStop();
                }
                String to = (null != toProcessStop.getName() ? toProcessStop.getName() : "");
                String outport = (null != processPath.getOutport() ? processPath.getOutport() : "");
                String inport = (null != processPath.getInport() ? processPath.getInport() : "");
                String from = (null != fromProcessStop.getName() ? fromProcessStop.getName() : "");
                Map<String, Object> pathVoMap = new HashMap<>();
                pathVoMap.put("from", from);
                pathVoMap.put("outport", outport);
                pathVoMap.put("inport", inport);
                pathVoMap.put("to", to);
                if (PortType.ROUTE == fromProcessStop.getOutPortType() && StringUtils.isNotBlank(outport)) {

                }
                if (PortType.ROUTE == toProcessStop.getInPortType() && StringUtils.isNotBlank(inport)) {

                }
                thirdPathVoMapList.add(pathVoMap);
            }
        }
        flowVoMap.put("paths", thirdPathVoMapList);

        for (String stopPageId : stopsMap.keySet()) {
            ProcessStop processStop = stopsMap.get(stopPageId);
            Map<String, Object> thirdStopVo = new HashMap<>();
            thirdStopVo.put("uuid", processStop.getId());
            thirdStopVo.put("name", processStop.getName());
            thirdStopVo.put("bundle", processStop.getBundel());

            // StopProperty
            List<ProcessStopProperty> processStopPropertyList = processStop.getProcessStopPropertyList();
            Map<String, Object> properties = new HashMap<String, Object>();
            if (null != processStopPropertyList && processStopPropertyList.size() > 0) {
                for (ProcessStopProperty processStopProperty : processStopPropertyList) {
                    String name = processStopProperty.getName();
                    if (StringUtils.isNotBlank(name)) {
                        String customValue2 = processStopProperty.getCustomValue();
                        String customValue = (null != customValue2 ? customValue2 : "");
                        properties.put(name, customValue);

                    }
                }
            }
            thirdStopVo.put("properties", properties);

            // StopCustomizedProperty
            List<ProcessStopCustomizedProperty> processStopCustomizedPropertyList = processStop.getProcessStopCustomizedPropertyList();
            Map<String, Object> customizedProperties = new HashMap<String, Object>();
            if (null != processStopCustomizedPropertyList && processStopCustomizedPropertyList.size() > 0) {
                for (ProcessStopCustomizedProperty processStopCustomizedProperty : processStopCustomizedPropertyList) {
                    String name = processStopCustomizedProperty.getName();
                    if (StringUtils.isNotBlank(name)) {
                        String customValue2 = processStopCustomizedProperty.getCustomValue();
                        String customValue = (null != customValue2 ? customValue2 : "");
                        customizedProperties.put(name, customValue);
                    }
                }
            }
            thirdStopVo.put("customizedProperties", customizedProperties);

            processStopMapList.add(thirdStopVo);
        }
        flowVoMap.put("stops", processStopMapList);

        //checkpoint
        if (StringUtils.isNotBlank(checkpoint)) {
            flowVoMap.put("checkpoint", checkpoint);
            if (StringUtils.isNotBlank(process.getParentProcessId())) {
                flowVoMap.put("checkpointParentProcessId", process.getParentProcessId());
            }
        }
        if (RunModeType.DEBUG == runModeType) {
            flowVoMap.put("runMode", runModeType.getValue());
        }
        rtnMap.put("flow", flowVoMap);
        return rtnMap;
    }

    public static Map<String, Object> processGroupToMap(ProcessGroup processGroup, RunModeType runModeType) {

        Map<String, Object> rtnMap = new HashMap<>();
        Map<String, Object> flowGroupVoMap = new HashMap<>();
        flowGroupVoMap.put("name", processGroup.getName());
        flowGroupVoMap.put("uuid", processGroup.getId());

        // all process
        Map<String, Process> processesMap = new HashMap<>();
        Map<String, ProcessGroup> processGroupsMap = new HashMap<>();

        List<Process> processList = processGroup.getProcessList();
        if (null != processList && processList.size() > 0) {
            List<Map<String, Object>> processesListMap = new ArrayList<>();
            for (Process process : processList) {
                processesMap.put(process.getPageId(), process);
                Map<String, Object> processMap = processToMap(process, null, runModeType);
                processesListMap.add(processMap);
            }
            flowGroupVoMap.put("flows", processesListMap);
        }

        List<ProcessGroup> processGroupList = processGroup.getProcessGroupList();
        if (null != processGroupList && processGroupList.size() > 0) {
            List<Map<String, Object>> processesGroupListMap = new ArrayList<>();
            for (ProcessGroup processGroupNew : processGroupList) {
                processGroupsMap.put(processGroupNew.getPageId(), processGroupNew);
                Map<String, Object> processGroupMap = processGroupToMap(processGroupNew, runModeType);
                processesGroupListMap.add(processGroupMap);
            }
            flowGroupVoMap.put("groups", processesGroupListMap);

        }

        List<ProcessGroupPath> processGroupPathList = processGroup.getProcessGroupPathList();
        if (null != processGroupPathList && processGroupPathList.size() > 0) {
            List<Map<String, Object>> pathListMap = new ArrayList<>();
            for (ProcessGroupPath processGroupPath : processGroupPathList) {
                if (null != processGroupPath) {
                    Map<String, Object> pathMap = new HashMap<>();
                    String formName = "";
                    String toName = "";
                    String from = processGroupPath.getFrom();
                    String to = processGroupPath.getTo();
                    if (null != processesMap.get(from)) {
                        formName = processesMap.get(from).getName();
                    } else if (null != processGroupsMap.get(from)) {
                        formName = processGroupsMap.get(from).getName();
                    }
                    if (null != processesMap.get(to)) {
                        toName = processesMap.get(to).getName();
                    } else if (null != processGroupsMap.get(to)) {
                        toName = processGroupsMap.get(to).getName();
                    }
                    pathMap.put("after", formName);
                    pathMap.put("entry", toName);
                    pathListMap.add(pathMap);
                }
            }
            flowGroupVoMap.put("conditions", pathListMap);
        }
        rtnMap.put("group", flowGroupVoMap);
        return rtnMap;
    }

}
