
package es.bsc.inb.etransafe.treatmentfindings.services; 

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.stereotype.Service;

import gate.Corpus;
import gate.CorpusController;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.Plugin;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.Out;
import gate.util.persistence.PersistenceManager;

@Service
public class AnniePluginServiceImpl  implements AnniePluginService{

  
  private CorpusController annieController;

  /**
   * Initialise the ANNIE system. This creates a "corpus pipeline"
   * application that can be used to run sets of documents through
   * the extraction system.
   */
  public void init() throws GateException, IOException {
	  Out.prln("Initialising ANNIE...");
	  // load the ANNIE application from the saved state in plugins/ANNIE
	  File pluginsHome = new File("/home/jcorvi/GATE_plugins");
	  File anniePlugin = new File(pluginsHome, "ANNIE");
	  File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
	  annieController = (CorpusController) PersistenceManager.loadObjectFromFile(annieGapp);
  } 

  /** Tell ANNIE's controller about the corpus you want to run on */
  public void setCorpus(Corpus corpus) {
	  annieController.setCorpus(corpus);
  }

  /** Run ANNIE */
  public void execute() throws GateException {
	  //annieController.execute();
  
	  Plugin anniePlugin = new Plugin.Maven( 
	        "uk.ac.gate.plugins", "annie", "8.5"); 
	  Gate.getCreoleRegister().registerPlugin(anniePlugin); 
	   
	  // create a serial analyser controller to run ANNIE with 
	  SerialAnalyserController annieController = 
	   (SerialAnalyserController) Factory.createResource( 
	       "gate.creole.SerialAnalyserController", 
	      Factory.newFeatureMap(), 
	       Factory.newFeatureMap(), "ANNIE"); 
	   
	  // load each PR as defined in ANNIEConstants 
	  // Note this code is for demonstration purposes only, 
	  // in practice if you want to load the ANNIE app you 
	  // should use the PersistenceManager as shown at the 
	  // start of this chapter 
	  
	   String[] PR_NAMES = {
			       "gate.creole.annotdelete.AnnotationDeletePR",
			      "gate.creole.tokeniser.DefaultTokeniser",
			      "gate.creole.gazetteer.DefaultGazetteer",
			     "gate.creole.splitter.SentenceSplitter",
			    "gate.creole.POSTagger",
			     "gate.creole.ANNIETransducer",
			    "gate.creole.orthomatcher.OrthoMatcher"
			  };
	  
	  for(int i = 0; i < PR_NAMES.length; i++) { 
	    // use default parameters 
	    FeatureMap params = Factory.newFeatureMap(); 
	    ProcessingResource pr = (ProcessingResource) 
	        Factory.createResource(PR_NAMES[i], 
	                               params); 
	    // add the PR to the pipeline controller 
	    annieController.add(pr); 
	  } // for each ANNIE PR 
	  
	  Gate.getCreoleRegister().registerPlugin(new Plugin.Maven( 
			 "uk.ac.gate.plugins", "tagger-numbers", "8.5")); 
	  Gate.getCreoleRegister().registerPlugin(new Plugin.Maven( 
				 "uk.ac.gate.plugins", "tagger-measurements", "8.5"));
	  
	  ProcessingResource numbers = (ProcessingResource) 
	  Factory.createResource("gate.creole.numbers.NumbersTagger");
	  annieController.add(numbers);
	  ProcessingResource measurements = (ProcessingResource) 
	  Factory.createResource("gate.creole.measurements.MeasurementsTagger");
	  
	  annieController.add(measurements);
	  // Tell ANNIE’s controller about the corpus you want to run on 
	  annieController.setCorpus(this.annieController.getCorpus()); 
	  
	  
	  FeatureMap params = Factory.newFeatureMap(); 
	  try {
		params.put("listsURL", new File("/home/jcorvi/eTRANSAFE_DATA/dictionaries/lists.def").toURL());
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  params.put("gazetteerFeatureSeparator", "\t");
	  ProcessingResource treatment_related_finding_gazetter = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer", params); 
	  
	  
	  
	  annieController.add(treatment_related_finding_gazetter);
	  
	  
	  try {
		LanguageAnalyser jape = (LanguageAnalyser)gate.Factory.createResource(
		          "gate.creole.Transducer", gate.Utils.featureMap(
		              "grammarURL", new File("/home/jcorvi/eTRANSAFE_DATA/jape_rules/STUDY_DOMAIN.jape").toURI().toURL(),
		              "encoding", "UTF-8"));
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  
	  // Run ANNIE 
	  annieController.execute();
	  
  } 
  
  
  
  
  

//  /**
//   * Run from the command-line, with a list of URLs as argument.
//   * <P><B>NOTE:</B><BR>
//   * This code will run with all the documents in memory - if you
//   * want to unload each from memory after use, add code to store
//   * the corpus in a DataStore.
//   */
//  public static void main(String args[]) throws GateException, IOException {
//    // initialise the GATE library
//    Out.prln("Initialising GATE...");
//    Gate.init();
//    Out.prln("...GATE initialised");
//
//    // initialise ANNIE (this may take several minutes)
//    StandAloneAnnie annie = new StandAloneAnnie();
//    annie.initAnnie();
//
//    // create a GATE corpus and add a document for each command-line
//    // argument
//    Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
//    for(int i = 0; i < args.length; i++) {
//      URL u = new URL(args[i]);
//      FeatureMap params = Factory.newFeatureMap();
//      params.put("sourceUrl", u);
//      params.put("preserveOriginalContent", new Boolean(true));
//      params.put("collectRepositioningInfo", new Boolean(true));
//      Out.prln("Creating doc for " + u);
//      Document doc = (Document)
//        Factory.createResource("gate.corpora.DocumentImpl", params);
//      corpus.add(doc);
//    } // for each of args
//
//    // tell the pipeline about the corpus and run it
//    annie.setCorpus(corpus);
//    annie.execute();
//
//    // for each document, get an XML document with the
//    // person and location names added
//    Iterator iter = corpus.iterator();
//    int count = 0;
//    String startTagPart_1 = "<span GateID=\"";
//    String startTagPart_2 = "\" title=\"";
//    String startTagPart_3 = "\" style=\"background:Red;\">";
//    String endTag = "</span>";
//
//    while(iter.hasNext()) {
//      Document doc = (Document) iter.next();
//      AnnotationSet defaultAnnotSet = doc.getAnnotations();
//      Set annotTypesRequired = new HashSet();
//      annotTypesRequired.add("Person");
//      annotTypesRequired.add("Location");
//      Set<Annotation> peopleAndPlaces =
//        new HashSet<Annotation>(defaultAnnotSet.get(annotTypesRequired));
//
//      FeatureMap features = doc.getFeatures();
//      String originalContent = (String)
//        features.get(GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);
//      RepositioningInfo info = (RepositioningInfo)
//        features.get(GateConstants.DOCUMENT_REPOSITIONING_INFO_FEATURE_NAME);
//
//      ++count;
//      File file = new File("StANNIE_" + count + ".HTML");
//      Out.prln("File name: '"+file.getAbsolutePath()+"'");
//      if(originalContent != null && info != null) {
//        Out.prln("OrigContent and reposInfo existing. Generate file...");
//
//        Iterator it = peopleAndPlaces.iterator();
//        Annotation currAnnot;
//        SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
//
//        while(it.hasNext()) {
//          currAnnot = (Annotation) it.next();
//          sortedAnnotations.addSortedExclusive(currAnnot);
//        } // while
//
//        StringBuffer editableContent = new StringBuffer(originalContent);
//        long insertPositionEnd;
//        long insertPositionStart;
//        // insert anotation tags backward
//        Out.prln("Unsorted annotations count: "+peopleAndPlaces.size());
//        Out.prln("Sorted annotations count: "+sortedAnnotations.size());
//        for(int i=sortedAnnotations.size()-1; i>=0; --i) {
//          currAnnot = (Annotation) sortedAnnotations.get(i);
//          insertPositionStart =
//            currAnnot.getStartNode().getOffset().longValue();
//          insertPositionStart = info.getOriginalPos(insertPositionStart);
//          insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
//          insertPositionEnd = info.getOriginalPos(insertPositionEnd, true);
//          if(insertPositionEnd != -1 && insertPositionStart != -1) {
//            editableContent.insert((int)insertPositionEnd, endTag);
//            editableContent.insert((int)insertPositionStart, startTagPart_3);
//            editableContent.insert((int)insertPositionStart,
//                                                          currAnnot.getType());
//            editableContent.insert((int)insertPositionStart, startTagPart_2);
//            editableContent.insert((int)insertPositionStart,
//                                                  currAnnot.getId().toString());
//            editableContent.insert((int)insertPositionStart, startTagPart_1);
//          } // if
//        } // for
//
//        FileWriter writer = new FileWriter(file);
//        writer.write(editableContent.toString());
//        writer.close();
//      } // if - should generate
//      else if (originalContent != null) {
//        Out.prln("OrigContent existing. Generate file...");
//
//        Iterator it = peopleAndPlaces.iterator();
//        Annotation currAnnot;
//        SortedAnnotationList sortedAnnotations = new SortedAnnotationList();
//
//        while(it.hasNext()) {
//          currAnnot = (Annotation) it.next();
//          sortedAnnotations.addSortedExclusive(currAnnot);
//        } // while
//
//        StringBuffer editableContent = new StringBuffer(originalContent);
//        long insertPositionEnd;
//        long insertPositionStart;
//        // insert anotation tags backward
//        Out.prln("Unsorted annotations count: "+peopleAndPlaces.size());
//        Out.prln("Sorted annotations count: "+sortedAnnotations.size());
//        for(int i=sortedAnnotations.size()-1; i>=0; --i) {
//          currAnnot = (Annotation) sortedAnnotations.get(i);
//          insertPositionStart =
//            currAnnot.getStartNode().getOffset().longValue();
//          insertPositionEnd = currAnnot.getEndNode().getOffset().longValue();
//          if(insertPositionEnd != -1 && insertPositionStart != -1) {
//            editableContent.insert((int)insertPositionEnd, endTag);
//            editableContent.insert((int)insertPositionStart, startTagPart_3);
//            editableContent.insert((int)insertPositionStart,
//                                                          currAnnot.getType());
//            editableContent.insert((int)insertPositionStart, startTagPart_2);
//            editableContent.insert((int)insertPositionStart,
//                                                  currAnnot.getId().toString());
//            editableContent.insert((int)insertPositionStart, startTagPart_1);
//          } // if
//        } // for
//
//        FileWriter writer = new FileWriter(file);
//        writer.write(editableContent.toString());
//        writer.close();
//      }
//      else {
//        Out.prln("Content : "+originalContent);
//        Out.prln("Repositioning: "+info);
//      }
//
//      String xmlDocument = doc.toXml(peopleAndPlaces, false);
//      String fileName = new String("StANNIE_toXML_" + count + ".HTML");
//      FileWriter writer = new FileWriter(fileName);
//      writer.write(xmlDocument);
//      writer.close();
//
//    } // for each doc
//  } // main

//  /**
//   *
//   */
//  public static class SortedAnnotationList extends Vector {
//    public SortedAnnotationList() {
//      super();
//    } // SortedAnnotationList
//
//    public boolean addSortedExclusive(Annotation annot) {
//      Annotation currAnot = null;
//
//      // overlapping check
//      for (int i=0; i<size(); ++i) {
//        currAnot = (Annotation) get(i);
//        if(annot.overlaps(currAnot)) {
//          return false;
//        } // if
//      } // for
//
//      long annotStart = annot.getStartNode().getOffset().longValue();
//      long currStart;
//      // insert
//      for (int i=0; i < size(); ++i) {
//        currAnot = (Annotation) get(i);
//        currStart = currAnot.getStartNode().getOffset().longValue();
//        if(annotStart < currStart) {
//          insertElementAt(annot, i);
//          /*
//           Out.prln("Insert start: "+annotStart+" at position: "+i+" size="+size());
//           Out.prln("Current start: "+currStart);
//           */
//          return true;
//        } // if
//      } // for
//
//      int size = size();
//      insertElementAt(annot, size);
////Out.prln("Insert start: "+annotStart+" at size position: "+size);
//      return true;
//    } // addSorted
//  } // SortedAnnotationList
} 