package feature;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Range;
import weka.core.Utils;
import weka.experiment.ClassifierSplitEvaluator;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.Experiment;
import weka.experiment.InstancesResultListener;
import weka.experiment.PairedCorrectedTTester;
import weka.experiment.PairedTTester;
import weka.experiment.PropertyNode;
import weka.experiment.RandomSplitResultProducer;
import weka.experiment.RegressionSplitEvaluator;
import weka.experiment.ResultMatrix;
import weka.experiment.ResultMatrixPlainText;
import weka.experiment.SplitEvaluator;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.DefaultListModel;


/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * ExperimentDemo.java
 * Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 */

/**
 * A class for demonstrating the use of the Experiment class for one classifier
 * and one or more datasets. Cross-validation or random splits are possible, as
 * well as classification or regression.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ExperimentDemo {

  /**
   * Expects the following parameters: 
   * <ul>
   *   <li>-classifier "classifier incl. parameters"</li>
   *   <li>-exptype "classification|regression"</li>
   *   <li>-splittype "crossvalidation|randomsplit"</li>
   *   <li>-runs "# of runs"</li>
   *   <li>-folds "# of cross-validation folds"</li>
   *   <li>-percentage "percentage for randomsplit"</li>
   *   <li>-result "arff file for storing the results"</li>
   *   <li>-t "dataset" (can be supplied multiple times)</li>
   * </ul>
   * 
   * @param args	the commandline arguments
   * @throws Exception	if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    // parameters provided?
    if (args.length == 0) {
      System.out.println(
	  "\nUsage: ExperimentDemo\n"
	  + "\t   -classifier <classifier incl. parameters>\n"
	  + "\t   -exptype <classification|regression>\n"
	  + "\t   -splittype <crossvalidation|randomsplit>\n"
	  + "\t   -runs <# of runs>\n"
	  + "\t   -folds <folds for CV>\n"
	  + "\t   -percentage <percentage for randomsplit>\n"
	  + "\t   -result <ARFF file for storing the results>\n"
	  + "\t   -t dataset (can be supplied multiple times)\n");
      System.exit(1);
    }
    
    // 1. setup the experiment
    System.out.println("Setting up...");
    Experiment exp = new Experiment();
    exp.setPropertyArray(new Classifier[0]);
    exp.setUsePropertyIterator(true);

    String option;
    
    // classification or regression
    option = Utils.getOption("exptype", args);
    if (option.length() == 0)
      throw new IllegalArgumentException("No experiment type provided!");

    SplitEvaluator se = null;
    Classifier sec    = null;
    boolean classification = false;
    if (option.equals("classification")) {
      classification = true;
      se  = new ClassifierSplitEvaluator();
      sec = ((ClassifierSplitEvaluator) se).getClassifier();
    }
    else if (option.equals("regression")) {
      se  = new RegressionSplitEvaluator();
      sec = ((RegressionSplitEvaluator) se).getClassifier();
    }
    else {
      throw new IllegalArgumentException("Unknown experiment type '" + option + "'!");
    }
    
    // crossvalidation or randomsplit
    option = Utils.getOption("splittype", args);
    if (option.length() == 0)
      throw new IllegalArgumentException("No split type provided!");

    if (option.equals("crossvalidation")) {
      CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
      option = Utils.getOption("folds", args);
      if (option.length() == 0)
	throw new IllegalArgumentException("No folds provided!");
      cvrp.setNumFolds(Integer.parseInt(option));
      cvrp.setSplitEvaluator(se);
      
      PropertyNode[] propertyPath = new PropertyNode[2];
      try {
	propertyPath[0] = new PropertyNode(
	    se, 
	    new PropertyDescriptor("splitEvaluator",
		CrossValidationResultProducer.class),
		CrossValidationResultProducer.class);
	propertyPath[1] = new PropertyNode(
	    sec, 
	    new PropertyDescriptor("classifier",
		se.getClass()),
		se.getClass());
      }
      catch (IntrospectionException e) {
	e.printStackTrace();
      }
      
      exp.setResultProducer(cvrp);
      exp.setPropertyPath(propertyPath);

    }
    else if (option.equals("randomsplit")) {
      RandomSplitResultProducer rsrp = new RandomSplitResultProducer();
      rsrp.setRandomizeData(true);
      option = Utils.getOption("percentage", args);
      if (option.length() == 0)
	throw new IllegalArgumentException("No percentage provided!");
      rsrp.setTrainPercent(Double.parseDouble(option));
      rsrp.setSplitEvaluator(se);

      PropertyNode[] propertyPath = new PropertyNode[2];
      try {
	propertyPath[0] = new PropertyNode(
	    se, 
	    new PropertyDescriptor("splitEvaluator",
		RandomSplitResultProducer.class),
		RandomSplitResultProducer.class);
	propertyPath[1] = new PropertyNode(
	    sec, 
	    new PropertyDescriptor("classifier",
		se.getClass()),
		se.getClass());
      }
      catch (IntrospectionException e) {
	e.printStackTrace();
      }

      exp.setResultProducer(rsrp);
      exp.setPropertyPath(propertyPath);
    }
    else {
      throw new IllegalArgumentException("Unknown split type '" + option + "'!");
    }
    
    // runs
    option = Utils.getOption("runs", args);
    if (option.length() == 0)
      throw new IllegalArgumentException("No runs provided!");
    exp.setRunLower(1);
    exp.setRunUpper(Integer.parseInt(option));
    
    // classifier
    option = Utils.getOption("classifier", args);
    if (option.length() == 0)
      throw new IllegalArgumentException("No classifier provided!");
    String[] options = Utils.splitOptions(option);
    String classname = options[0];
    options[0]       = "";
    Classifier c     = (Classifier) Utils.forName(Classifier.class, classname, options);
    exp.setPropertyArray(new Classifier[]{c}); 
    
    // datasets
    boolean data = false;
    DefaultListModel model = new DefaultListModel();
    do {
      option = Utils.getOption("t", args);
      if (option.length() > 0) {
	File file = new File(option);
	if (!file.exists())
	  throw new IllegalArgumentException("File '" + option + "' does not exist!");
	data = true;
	model.addElement(file);
      }
    }
    while (option.length() > 0);
    if (!data)
      throw new IllegalArgumentException("No data files provided!");
    exp.setDatasets(model);

    // result
    option = Utils.getOption("result", args);
    if (option.length() == 0)
      throw new IllegalArgumentException("No result file provided!");
    InstancesResultListener irl = new InstancesResultListener();
    irl.setOutputFile(new File(option));
    exp.setResultListener(irl);
    
    // 2. run experiment
    System.out.println("Initializing...");
    exp.initialize();
    System.out.println("Running...");
    exp.runExperiment();
    System.out.println("Finishing...");
    exp.postProcess();
    
    // 3. calculate statistics and output them
    System.out.println("Evaluating...");
    PairedTTester tester = new PairedCorrectedTTester();
    Instances result = new Instances(new BufferedReader(new FileReader(irl.getOutputFile())));
    tester.setInstances(result);
    tester.setSortColumn(-1);
    tester.setRunColumn(result.attribute("Key_Run").index());
    if (classification)
      tester.setFoldColumn(result.attribute("Key_Fold").index());
    tester.setResultsetKeyColumns(
	new Range(
	    "" 
	    + (result.attribute("Key_Dataset").index() + 1)));
    tester.setDatasetKeyColumns(
	new Range(
	    "" 
	    + (result.attribute("Key_Scheme").index() + 1)
	    + ","
	    + (result.attribute("Key_Scheme_options").index() + 1)
	    + ","
	    + (result.attribute("Key_Scheme_version_ID").index() + 1)));
    tester.setResultMatrix(new ResultMatrixPlainText());
    tester.setDisplayedResultsets(null);       
    tester.setSignificanceLevel(0.05);
    tester.setShowStdDevs(true);
    // fill result matrix (but discarding the output)
    if (classification)
      tester.multiResultsetFull(0, result.attribute("Percent_correct").index());
    else
      tester.multiResultsetFull(0, result.attribute("Correlation_coefficient").index());
    // output results for reach dataset
    System.out.println("\nResult:");
    ResultMatrix matrix = tester.getResultMatrix();
    for (int i = 0; i < matrix.getColCount(); i++) {
      System.out.println(matrix.getColName(i));
      System.out.println("    Perc. correct: " + matrix.getMean(i, 0));
      System.out.println("    StdDev: " + matrix.getStdDev(i, 0));
    }
  }
}
