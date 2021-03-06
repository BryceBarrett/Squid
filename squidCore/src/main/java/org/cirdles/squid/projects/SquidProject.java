/*
 * Copyright 2017 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.squid.projects;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.bind.JAXBException;
import org.cirdles.squid.constants.Squid3Constants;
import static org.cirdles.squid.constants.Squid3Constants.DUPLICATE_STRING;
import org.cirdles.squid.core.PrawnFileHandler;
import org.cirdles.squid.dialogs.SquidMessageDialog;
import org.cirdles.squid.exceptions.SquidException;
import org.cirdles.squid.prawn.PrawnFile;
import org.cirdles.squid.prawn.PrawnFile.Run;
import org.cirdles.squid.tasks.Task;
import org.cirdles.squid.tasks.TaskInterface;
import org.cirdles.squid.tasks.squidTask25.TaskSquid25;
import org.cirdles.squid.tasks.expressions.Expression;
import org.cirdles.squid.tasks.expressions.constants.ConstantNode;
import org.cirdles.squid.tasks.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.squid.tasks.squidTask25.TaskSquid25Equation;
import org.cirdles.squid.utilities.IntuitiveStringComparator;
import org.xml.sax.SAXException;
import org.cirdles.squid.utilities.squidPrefixTree.SquidPrefixTree;
import org.cirdles.squid.utilities.fileUtilities.PrawnFileUtilities;
import org.cirdles.squid.shrimp.ShrimpDataFileInterface;

/**
 *
 * @author bowring
 */
public final class SquidProject implements Serializable {

    private static final long serialVersionUID = 7099919411562934142L;

    private transient SquidPrefixTree prefixTree;

    private PrawnFileHandler prawnFileHandler;
    private String projectName;
    private String analystName;
    private String projectNotes;
    private File prawnXMLFile;
    private ShrimpDataFileInterface prawnFile;
    private String filterForRefMatSpotNames;
    private String filterForConcRefMatSpotNames;
    private double sessionDurationHours;
    private TaskInterface task;

    private Map<String, Integer> filtersForUnknownNames;
    private String delimiterForUnknownNames;

    private static boolean projectChanged;

    public SquidProject() {
        this.prawnFileHandler = new PrawnFileHandler();
        this.projectName = "NO_NAME";
        this.prawnXMLFile = new File("");
        this.prawnFile = null;

        this.filterForRefMatSpotNames = "";
        this.filterForConcRefMatSpotNames = "";

        this.sessionDurationHours = 0.0;

        projectChanged = false;

        this.task = new Task("New Task", prawnFileHandler.getNewReportsEngine());

        this.filtersForUnknownNames = new HashMap<>();
        this.delimiterForUnknownNames = Squid3Constants.SampleNameDelimetersEnum.HYPHEN.getName();
    }

    public Map< String, TaskInterface> getTaskLibrary() {
        Map< String, TaskInterface> builtInTasks = new HashMap<>();

        return builtInTasks;
    }

    public void loadAndInitializeLibraryTask(TaskInterface task) {
        this.task = task;
        task.setPrawnFile(prawnFile);
        this.task.setReportsEngine(prawnFileHandler.getReportsEngine());
        this.task.setFilterForRefMatSpotNames(filterForRefMatSpotNames);
        this.task.setFilterForConcRefMatSpotNames(filterForConcRefMatSpotNames);
        this.task.setFiltersForUnknownNames(filtersForUnknownNames);
        // first pass
        this.task.setChanged(true);
        this.task.setupSquidSessionSpecsAndReduceAndReport();
        this.task.updateAllExpressions();
        this.task.setChanged(true);
        this.task.setupSquidSessionSpecsAndReduceAndReport();
    }

    public void initializeTaskAndReduceData() {
        if (task != null) {
            task.setPrawnFile(prawnFile);
            task.setReportsEngine(prawnFileHandler.getReportsEngine());
            task.setFilterForRefMatSpotNames(filterForRefMatSpotNames);
            task.setFilterForConcRefMatSpotNames(filterForConcRefMatSpotNames);
            this.task.setFiltersForUnknownNames(filtersForUnknownNames);
            // four passes needed for percolating results
            task.updateAllExpressions();
            task.setChanged(true);
            task.setupSquidSessionSpecsAndReduceAndReport();
        }
    }

    public void createNewTask() {
        this.task = new Task(
                "New Task", prawnFile, prawnFileHandler.getNewReportsEngine());
        this.task.setChanged(true);
        initializeTaskAndReduceData();
    }

    public void createTaskFromImportedSquid25Task(File squidTaskFile) {

        TaskSquid25 taskSquid25 = TaskSquid25.importSquidTaskFile(squidTaskFile);

        // if Task is ashort of nominal masses, add them
        int prawnSpeciesCount = Integer.parseInt(((PrawnFile)prawnFile).getRun().get(0).getPar().get(2).getValue());
        if (prawnSpeciesCount != taskSquid25.getNominalMasses().size()) {
            SquidMessageDialog.showWarningDialog(
                    "The PrawnFile has "
                    + prawnSpeciesCount
                    + " mass stations and the Task has "
                    + taskSquid25.getNominalMasses().size()
                    + " masses - please confirm.",
                    null);
            for (int i = 0; i < (prawnSpeciesCount - taskSquid25.getNominalMasses().size()); i++) {
                taskSquid25.getNominalMasses().add("DUMMY" + (i + 1));
            }
        }

        // need to remove stored expression results on fractions to clear the decks
        this.task.getShrimpFractions().forEach((spot) -> {
            spot.getTaskExpressionsForScansEvaluated().clear();
            spot.getTaskExpressionsEvaluationsPerSpot().clear();
        });

        this.task = new Task(
                taskSquid25.getTaskName(), prawnFile, prawnFileHandler.getNewReportsEngine());
        this.task.setType(taskSquid25.getTaskType());
        this.task.setDescription(taskSquid25.getTaskDescription());
        this.task.setProvenance(taskSquid25.getSquidTaskFileName());
        this.task.setAuthorName(taskSquid25.getAuthorName());
        this.task.setLabName(taskSquid25.getLabName());
        this.task.setNominalMasses(taskSquid25.getNominalMasses());
        this.task.setRatioNames(taskSquid25.getRatioNames());
        this.task.setFilterForRefMatSpotNames(filterForRefMatSpotNames);
        this.task.setFilterForConcRefMatSpotNames(filterForConcRefMatSpotNames);
        this.task.setFiltersForUnknownNames(filtersForUnknownNames);
        this.task.setParentNuclide(taskSquid25.getParentNuclide());
        this.task.setDirectAltPD(taskSquid25.isDirectAltPD());

        this.task.generateBuiltInExpressions();

        // determine index of background mass as specified in task
        for (int i = 0; i < taskSquid25.getNominalMasses().size(); i++) {
            if (taskSquid25.getNominalMasses().get(i).compareToIgnoreCase(taskSquid25.getBackgroundMass()) == 0) {
                task.setIndexOfTaskBackgroundMass(i);
                break;
            }
        }

        // first pass
        this.task.setChanged(true);
        this.task.setupSquidSessionSpecsAndReduceAndReport();

        List<TaskSquid25Equation> task25Equations = taskSquid25.getTask25Equations();
        for (TaskSquid25Equation task25Eqn : task25Equations) {
            Expression expression = this.task.generateExpressionFromRawExcelStyleText(task25Eqn.getEquationName(),
                    task25Eqn.getExcelEquationString(),
                    task25Eqn.isEqnSwitchNU(),
                    false, false);

            ExpressionTreeInterface expressionTree = expression.getExpressionTree();
            expressionTree.setSquidSwitchSTReferenceMaterialCalculation(task25Eqn.isEqnSwitchST());
            expressionTree.setSquidSwitchSAUnknownCalculation(task25Eqn.isEqnSwitchSA());
            expressionTree.setSquidSwitchConcentrationReferenceMaterialCalculation(task25Eqn.isEqnSwitchConcST());

            expressionTree.setSquidSwitchSCSummaryCalculation(task25Eqn.isEqnSwitchSC());
            expressionTree.setSquidSpecialUPbThExpression(task25Eqn.isEqnSwitchSpecialBuiltin());

            this.task.getTaskExpressionsOrdered().add(expression);
        }

        List<String> constantNames = taskSquid25.getConstantNames();
        List<String> constantValues = taskSquid25.getConstantValues();
        for (int i = 0; i < constantNames.size(); i++) {
            double constantDouble = Double.parseDouble(constantValues.get(i));
            ConstantNode constant = new ConstantNode(constantNames.get(i), constantDouble);
            task.getNamedConstantsMap().put(constant.getName(), constant);
        }

        initializeTaskAndReduceData();
    }

    public void setupPrawnFile(File prawnXMLFileNew)
            throws IOException, JAXBException, SAXException, SquidException {

        prawnXMLFile = prawnXMLFileNew;
        updatePrawnFileHandlerWithFileLocation();
        prawnFile = prawnFileHandler.unmarshallCurrentPrawnFileXML();
        task.setPrawnFile(prawnFile);
        ((Task) task).setupSquidSessionSkeleton();
    }

    public void setupPrawnFileByJoin(List<File> prawnXMLFilesNew)
            throws IOException, JAXBException, SAXException, SquidException {

        if (prawnXMLFilesNew.size() == 2) {
            PrawnFile prawnFile1 = prawnFileHandler.unmarshallPrawnFileXML(prawnXMLFilesNew.get(0).getCanonicalPath(), false);
            PrawnFile prawnFile2 = prawnFileHandler.unmarshallPrawnFileXML(prawnXMLFilesNew.get(1).getCanonicalPath(), false);

            long start1 = PrawnFileUtilities.timeInMillisecondsOfRun(prawnFile1.getRun().get(0));
            long start2 = PrawnFileUtilities.timeInMillisecondsOfRun(prawnFile2.getRun().get(0));

            if (start1 > start2) {
                prawnFile2.getRun().addAll(prawnFile1.getRun());
                prawnFile2.setRuns((short) prawnFile2.getRun().size());
                prawnXMLFile = new File(
                        prawnXMLFilesNew.get(1).getName().replace(".xml", "").replace(".XML", "")
                        + "-JOIN-"
                        + prawnXMLFilesNew.get(0).getName().replace(".xml", "").replace(".XML", "") + ".xml");
                prawnFile = prawnFile2;
            } else {
                prawnFile1.getRun().addAll(prawnFile2.getRun());
                prawnFile1.setRuns((short) prawnFile1.getRun().size());
                prawnXMLFile = new File(
                        prawnXMLFilesNew.get(0).getName().replace(".xml", "").replace(".XML", "")
                        + "-JOIN-"
                        + prawnXMLFilesNew.get(1).getName().replace(".xml", "").replace(".XML", "") + ".xml");
                prawnFile = prawnFile1;
            }

            updatePrawnFileHandlerWithFileLocation();
            // write and read merged file to confirm conforms to schema
            serializePrawnData(prawnFileHandler.getCurrentPrawnFileLocation());
            prawnFile = prawnFileHandler.unmarshallCurrentPrawnFileXML();
        } else {
            throw new IOException("Two files not present");
        }
    }

    public void updatePrawnFileHandlerWithFileLocation()
            throws IOException {
        prawnFileHandler.setCurrentPrawnFileLocation(prawnXMLFile.getCanonicalPath());
    }

    public void savePrawnFile(File prawnXMLFileNew)
            throws IOException, JAXBException, SAXException {

        preProcessPrawnSession();

        prawnXMLFile = prawnXMLFileNew;
        prawnFileHandler.setCurrentPrawnFileLocation(prawnXMLFile.getCanonicalPath());
        serializePrawnData(prawnFileHandler.getCurrentPrawnFileLocation());
    }

    public boolean prawnFileExists() {
        return prawnFile != null;
    }

    public PrawnFile deserializePrawnData()
            throws IOException, JAXBException, SAXException, SquidException {
        return prawnFileHandler.unmarshallCurrentPrawnFileXML();
    }

    private void serializePrawnData(String fileName)
            throws IOException, JAXBException, SAXException {
        prawnFileHandler.writeRawDataFileAsXML(((PrawnFile)prawnFile), fileName);
    }

    public String getPrawnXMLFileName() {
        return prawnXMLFile.getName();
    }

    /**
     * @param prawnFile the prawnFile to set
     */
    public void setPrawnFile(PrawnFile prawnFile) {
        this.prawnFile = prawnFile;
    }

    public String getPrawnXMLFilePath() {
        return prawnXMLFile.getAbsolutePath();
    }

    public String getPrawnFileShrimpSoftwareVersionName() {
        return ((PrawnFile)prawnFile).getSoftwareVersion();
    }

    public String getPrawnFileLoginComment() {
        return ((PrawnFile)prawnFile).getLoginComment();
    }

    public List<Run> getPrawnFileRuns() {
        return new ArrayList<>(((PrawnFile)prawnFile).getRun());
    }

    public void processPrawnSessionForDuplicateSpotNames() {
        List<Run> runs = ((PrawnFile)prawnFile).getRun();
        Map<String, Integer> spotNameCountMap = new HashMap<>();
        for (int i = 0; i < runs.size(); i++) {
            String spotName = runs.get(i).getPar().get(0).getValue().trim();
            String spotNameKey = spotName.toUpperCase(Locale.US);
            // remove existing duplicate label in case editing occurred
            int indexDUP = spotName.indexOf("-DUP");
            if (indexDUP > 0) {
                runs.get(i).getPar().get(0).setValue(spotName.substring(0, spotName.indexOf("-DUP")));
                spotName = runs.get(i).getPar().get(0).getValue();
                spotNameKey = spotName.toUpperCase(Locale.US);
            }
            if (spotNameCountMap.containsKey(spotNameKey)) {
                int count = spotNameCountMap.get(spotNameKey);
                count++;
                spotNameCountMap.put(spotNameKey, count);
                runs.get(i).getPar().get(0).setValue(spotName + DUPLICATE_STRING + count);
            } else {
                spotNameCountMap.put(spotNameKey, 0);
            }
        }
    }

    public void preProcessPrawnSession() {

        processPrawnSessionForDuplicateSpotNames();

        // determine time in hours for session
        List<Run> runs = ((PrawnFile)prawnFile).getRun();
        long startFirst = PrawnFileUtilities.timeInMillisecondsOfRun(runs.get(0));
        long startLast = PrawnFileUtilities.timeInMillisecondsOfRun(runs.get(runs.size() - 1));
        long sessionDuration = startLast - startFirst;

        sessionDurationHours = (double) sessionDuration / 1000 / 60 / 60;

    }

    public void removeRunFromPrawnFile(Run run) {
        ((PrawnFile)prawnFile).getRun().remove(run);

        // save new count
        ((PrawnFile)prawnFile).setRuns((short) ((PrawnFile)prawnFile).getRun().size());
        
        // update fractions
        ((Task) task).setupSquidSessionSkeleton();       
    }

    public SquidPrefixTree generatePrefixTreeFromSpotNames() {
        prefixTree = new SquidPrefixTree();

        List<Run> copyOfRuns = new ArrayList<Run>(((PrawnFile)prawnFile).getRun());
        Comparator<String> intuitiveString = new IntuitiveStringComparator<>();
        Collections.sort(copyOfRuns, (Run pt1, Run pt2)
                -> (intuitiveString.compare(pt1.getPar().get(0).getValue(), pt2.getPar().get(0).getValue())));

        for (int i = 0; i < copyOfRuns.size(); i++) {
            SquidPrefixTree leafParent = prefixTree.insert(copyOfRuns.get(i).getPar().get(0).getValue());
            leafParent.getChildren().get(0).setCountOfSpecies(Integer.parseInt(copyOfRuns.get(i).getPar().get(2).getValue()));
            leafParent.getChildren().get(0).setCountOfScans(Integer.parseInt(copyOfRuns.get(i).getPar().get(3).getValue()));
        }

        prefixTree.prepareStatistics();

        return prefixTree;
    }

    /**
     * Splits the current PrawnFile into two sets of runs based on the index of
     * the run supplied and writes out two prawn xml files in the same folder as
     * the original.
     *
     * @param run
     * @param useOriginalData, when true, the original unedited file is used,
     * otherwise the edited file is used.
     * @return String [2] containing the file names of the two Prawn XML files
     * written as a result of the split.
     */
    public String[] splitPrawnFileAtRun(Run run, boolean useOriginalData)
            throws SquidException {
        String[] retVal = new String[2];
        retVal[0] = prawnFileHandler.getCurrentPrawnFileLocation().replace(".xml", "-PART-A.xml").replace(".XML", "-PART-A.xml");
        retVal[1] = prawnFileHandler.getCurrentPrawnFileLocation().replace(".xml", "-PART-B.xml").replace(".XML", "-PART-B.xml");

        // get index from original prawnFile
        int indexOfRun = ((PrawnFile)prawnFile).getRun().indexOf(run);

        List<Run> runs = ((PrawnFile)prawnFile).getRun();
        List<Run> runsCopy;

        if (useOriginalData) {
            PrawnFile prawnFileOriginal = null;
            try {
                prawnFileOriginal = deserializePrawnData();
                runsCopy = new CopyOnWriteArrayList<>(prawnFileOriginal.getRun());
            } catch (IOException | JAXBException | SAXException | SquidException iOException) {
                throw new SquidException(iOException.getMessage());
            }
        } else {
            runsCopy = new CopyOnWriteArrayList<>(runs);
        }

        List<Run> first = runsCopy.subList(0, indexOfRun);
        List<Run> second = runsCopy.subList(indexOfRun, runs.size());

        // keep first
        runs.clear();
        // preserve order
        for (Run runF : first) {
            runs.add(runF);
        }

        ((PrawnFile)prawnFile).setRuns((short) runs.size());
        try {
            prawnFileHandler.writeRawDataFileAsXML(((PrawnFile)prawnFile), retVal[0]);
        } catch (JAXBException jAXBException) {
        }

        runs.clear();
        // preserve order
        for (Run runS : second) {
            runs.add(runS);
        }
        ((PrawnFile)prawnFile).setRuns((short) runs.size());
        try {
            prawnFileHandler.writeRawDataFileAsXML(((PrawnFile)prawnFile), retVal[1]);
        } catch (JAXBException jAXBException) {
        }

        // restore list
        runs.clear();
        // preserve order
        for (Run runF : first) {
            runs.add(runF);
        }
        for (Run runS : second) {
            runs.add(runS);
        }
        ((PrawnFile)prawnFile).setRuns((short) runs.size());

        return retVal;
    }

    /**
     * @return the prawnFileHandler
     */
    public PrawnFileHandler getPrawnFileHandler() {
        return prawnFileHandler;
    }

    /**
     * @param prawnFileHandler the prawnFileHandler to set
     */
    public void setPrawnFileHandler(PrawnFileHandler prawnFileHandler) {
        this.prawnFileHandler = prawnFileHandler;
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName the projectName to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the analystName
     */
    public String getAnalystName() {
        return analystName;
    }

    /**
     * @param analystName the analystName to set
     */
    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

    /**
     * @return the projectNotes
     */
    public String getProjectNotes() {
        return projectNotes;
    }

    /**
     * @param projectNotes the projectNotes to set
     */
    public void setProjectNotes(String projectNotes) {
        this.projectNotes = projectNotes;
    }

    /**
     * @return the filterForRefMatSpotNames
     */
    public String getFilterForRefMatSpotNames() {
        if (filterForConcRefMatSpotNames == null) {
            filterForConcRefMatSpotNames = "";
        }
        return filterForRefMatSpotNames;
    }

    public void updateFilterForRefMatSpotNames(String filterForRefMatSpotNames) {
        this.filterForRefMatSpotNames = filterForRefMatSpotNames;
        if (task != null) {
            task.setFilterForRefMatSpotNames(filterForRefMatSpotNames);
        }
    }

    public String getFilterForConcRefMatSpotNames() {
        return filterForConcRefMatSpotNames;
    }

    public void updateFilterForConcRefMatSpotNames(String filterForConcRefMatSpotNames) {
        this.filterForConcRefMatSpotNames = filterForConcRefMatSpotNames;
        if (task != null) {
            task.setFilterForConcRefMatSpotNames(filterForConcRefMatSpotNames);
        }
    }

    public Map<String, Integer> getFiltersForUnknownNames() {
        if (filtersForUnknownNames == null) {
            filtersForUnknownNames = new HashMap<>();
        }
        return new HashMap<>(filtersForUnknownNames);
    }

    public void updateFiltersForUnknownNames(Map<String, Integer> filtersForUnknownNames) {
        this.filtersForUnknownNames = filtersForUnknownNames;
        if (task != null) {
            task.setFiltersForUnknownNames(filtersForUnknownNames);
        }
    }

    /**
     * @return the sessionDurationHours
     */
    public double getSessionDurationHours() {
        return sessionDurationHours;
    }

    /**
     * @return the prefixTree
     */
    public SquidPrefixTree getPrefixTree() {
        return prefixTree;
    }

    /**
     * @return the task
     */
    public TaskInterface getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask(TaskInterface task) {
        this.task = task;
    }

    /**
     * @return the projectChanged
     */
    public static boolean isProjectChanged() {
        return projectChanged;
    }

    /**
     * @param aProjectChanged the projectChanged to set
     */
    public static void setProjectChanged(boolean aProjectChanged) {
        projectChanged = aProjectChanged;
    }

    /**
     * @return the delimiterForUnknownNames
     */
    public String getDelimiterForUnknownNames() {
        if (delimiterForUnknownNames == null) {
            delimiterForUnknownNames = Squid3Constants.SampleNameDelimetersEnum.HYPHEN.getName();
        }
        return delimiterForUnknownNames;
    }

    /**
     * @param delimiterForUnknownNames the delimiterForUnknownNames to set
     */
    public void setDelimiterForUnknownNames(String delimiterForUnknownNames) {
        this.delimiterForUnknownNames = delimiterForUnknownNames;
    }

}
