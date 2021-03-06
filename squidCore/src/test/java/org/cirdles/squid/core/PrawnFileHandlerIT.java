/*
 * Copyright 2016 James F. Bowring and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cirdles.squid.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.squid.constants.Squid3Constants;
import org.cirdles.squid.prawn.PrawnFile;
import org.cirdles.squid.projects.SquidProject;
import org.cirdles.squid.reports.reportSettings.ReportSettings;
import org.cirdles.squid.reports.reportSettings.ReportSettingsInterface;
import org.cirdles.squid.shrimp.ShrimpFractionExpressionInterface;
import org.cirdles.squid.shrimp.SquidRatiosModel;
import org.cirdles.squid.shrimp.SquidSessionModel;
import org.cirdles.squid.shrimp.SquidSpeciesModel;
import org.cirdles.squid.tasks.Task;
import org.cirdles.squid.tasks.TaskInterface;
import org.cirdles.squid.utilities.csvSerialization.ReportSerializerToCSV;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

/**
 *
 * @author bowring
 */
public class PrawnFileHandlerIT {

    private static final String PRAWN_FILE_RESOURCE
            = "/org/cirdles/squid/prawn/100142_G6147_10111109.43.xml";

    private static final String PRAWN_FILE_RESOURCE_Z6266
            = "/org/cirdles/squid/prawn/836_1_2016_Nov_28_09.50.xml";

    private static final String PRAWN_FILE_RESOURCE_Z6266_TASK_PERM1
            = "/org/cirdles/squid/prawn/SquidTask_Z6266 = 11pk Perm1.SB.xls";

    private static final ResourceExtractor RESOURCE_EXTRACTOR
            = new ResourceExtractor(PrawnFileHandlerIT.class);

    /**
     *
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public TemporaryFolder temporaryFolderPerm1 = new TemporaryFolder();

    /**
     *
     */
    @Rule
    public Timeout timeout = Timeout.seconds(120);

    private PrawnFileHandler prawnFileHandler;

    /**
     *
     */
    @Before
    public void setUp() {
        prawnFileHandler = new PrawnFileHandler();
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void writesReportsFromPrawnFile() throws Exception {
        File reportsFolder = temporaryFolder.getRoot();

        prawnFileHandler.getReportsEngine().setFolderToWriteCalamariReports(reportsFolder);

        File prawnFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile(PRAWN_FILE_RESOURCE);

        prawnFileHandler.initReportsEngineWithCurrentPrawnFileName(PRAWN_FILE_RESOURCE);

        PrawnFile prawnFileData = prawnFileHandler.unmarshallPrawnFileXML(prawnFile.getAbsolutePath(), true);

        List<SquidSpeciesModel> squidSpeciesModelList = new ArrayList<>();
        squidSpeciesModelList.add(new SquidSpeciesModel(0, "196Zr2O", "196", "Zr2O", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(1, "204Pb", "204", "Pb", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(2, "Bkgnd", "Bkgnd", "Bkgnd", true, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(3, "206Pb", "206", "Pb", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(4, "207Pb", "207", "Pb", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(5, "208Pb", "208", "Pb", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(6, "238U", "238", "U", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(7, "248ThO", "248", "ThO", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(8, "254UO", "254", "UO", false, "No", false));
        squidSpeciesModelList.add(new SquidSpeciesModel(9, "270UO2", "270", "UO2", false, "No", false));

        List<SquidRatiosModel> squidRatiosModelList = new ArrayList<>();
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(1), squidSpeciesModelList.get(3), 0));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(4), squidSpeciesModelList.get(3), 1));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(5), squidSpeciesModelList.get(3), 2));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(6), squidSpeciesModelList.get(0), 3));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(3), squidSpeciesModelList.get(6), 4));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(8), squidSpeciesModelList.get(6), 5));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(7), squidSpeciesModelList.get(8), 6));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(3), squidSpeciesModelList.get(9), 7));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(9), squidSpeciesModelList.get(8), 8));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(3), squidSpeciesModelList.get(8), 9));
        squidRatiosModelList.add(new SquidRatiosModel(squidSpeciesModelList.get(6), squidSpeciesModelList.get(3), 10));

        TaskInterface task = new Task();
        SquidSessionModel squidSessionModel = new SquidSessionModel(squidSpeciesModelList, squidRatiosModelList, true, false, 2, "T", "", new HashMap<>());
        List<ShrimpFractionExpressionInterface> shrimpFractions = task.processRunFractions(prawnFileData, squidSessionModel);

        try {
            prawnFileHandler.getReportsEngine().produceReports(shrimpFractions, true, false);
        } catch (IOException iOException) {
        }

        assertThat(reportsFolder.listFiles()).hasSize(1); //Temp Calamari Reports Folder
        assertThat(reportsFolder.listFiles()[0].listFiles()).hasSize(1); //Reports folder with name of this Prawn File
        assertThat(reportsFolder.listFiles()[0].listFiles()[0]).isDirectory(); // the currently written folder of reports
        assertThat(reportsFolder.listFiles()[0].listFiles()[0].listFiles()).hasSize(6); // 6 reports

        // reportsFolder has produced reports
        for (File report : reportsFolder.listFiles()[0].listFiles()[0].listFiles()) {
            File expectedReport = RESOURCE_EXTRACTOR
                    .extractResourceAsFile(report.getName());

            assertThat(report).hasSameContentAs(expectedReport);
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testingOutputForZ6266Permutation1() throws Exception {

        System.out.println("Testing 836_1_2016_Nov_28_09_TaskPerm1 with 4-,7-,8-corr reference materials and unknowns.");
        File prawnFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile(PRAWN_FILE_RESOURCE_Z6266);

        SquidProject squidProject = new SquidProject();
        PrawnFile prawnFileData = prawnFileHandler.unmarshallPrawnFileXML(prawnFile.getAbsolutePath(), true);
        squidProject.setPrawnFile(prawnFileData);

        File squidTaskFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile(PRAWN_FILE_RESOURCE_Z6266_TASK_PERM1);
        squidProject.createTaskFromImportedSquid25Task(squidTaskFile);

        squidProject.setDelimiterForUnknownNames("-");
        squidProject.getTask().setFilterForRefMatSpotNames("6266");
        squidProject.getTask().setFilterForConcRefMatSpotNames("6266");

        squidProject.getTask().applyTaskIsotopeLabelsToMassStations();

        File reportsFolder = temporaryFolderPerm1.getRoot();
        ReportSettingsInterface reportSettings = new ReportSettings("TEST", false, squidProject.getTask());

        File reportTableFile = new File(reportsFolder + File.separator + "836_1_2016_Nov_28_09_TaskPerm1_4Corr_Unknowns.csv");
        String[][] report = reportSettings.reportFractionsByNumberStyle(squidProject.getTask().getUnknownSpots(), true);
        ReportSerializerToCSV.writeCSVReport(false, reportTableFile, report);

        File expectedReport = RESOURCE_EXTRACTOR
                .extractResourceAsFile(reportTableFile.getName());

        assertThat(reportTableFile).hasSameContentAs(expectedReport);

        squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_207);
        squidProject.getTask().setChanged(true);
        squidProject.getTask().setupSquidSessionSpecsAndReduceAndReport();
        reportTableFile = new File(reportsFolder + File.separator + "836_1_2016_Nov_28_09_TaskPerm1_7Corr_Unknowns.csv");
        report = reportSettings.reportFractionsByNumberStyle(squidProject.getTask().getUnknownSpots(), true);
        ReportSerializerToCSV.writeCSVReport(false, reportTableFile, report);

        expectedReport = RESOURCE_EXTRACTOR
                .extractResourceAsFile(reportTableFile.getName());

        assertThat(reportTableFile).hasSameContentAs(expectedReport);

        squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_208);
        squidProject.getTask().setChanged(true);
        squidProject.getTask().setupSquidSessionSpecsAndReduceAndReport();
        reportTableFile = new File(reportsFolder + File.separator + "836_1_2016_Nov_28_09_TaskPerm1_8Corr_Unknowns.csv");
        report = reportSettings.reportFractionsByNumberStyle(squidProject.getTask().getUnknownSpots(), true);
        ReportSerializerToCSV.writeCSVReport(false, reportTableFile, report);

        expectedReport = RESOURCE_EXTRACTOR
                .extractResourceAsFile(reportTableFile.getName());

        assertThat(reportTableFile).hasSameContentAs(expectedReport);

        squidProject.getTask().setSelectedIndexIsotope(Squid3Constants.IndexIsoptopesEnum.PB_204);
        squidProject.getTask().setChanged(true);
        squidProject.getTask().setupSquidSessionSpecsAndReduceAndReport();
        reportTableFile = new File(reportsFolder + File.separator + "836_1_2016_Nov_28_09_TaskPerm1_RefMat.csv");
        reportSettings = new ReportSettings("TEST", true, squidProject.getTask());
        report = reportSettings.reportFractionsByNumberStyle(squidProject.getTask().getReferenceMaterialSpots(), true);
        ReportSerializerToCSV.writeCSVReport(false, reportTableFile, report);

        expectedReport = RESOURCE_EXTRACTOR
                .extractResourceAsFile(reportTableFile.getName());

        assertThat(reportTableFile).hasSameContentAs(expectedReport);

    }

}
