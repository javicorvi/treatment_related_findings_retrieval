package es.bsc.inb.etransafe.treatmentfinding.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.objectbank.ObjectBank;
import es.bsc.inb.etransafe.treatmentfinding.model.AnnotationDummy;
import es.bsc.inb.etransafe.treatmentfinding.util.AnnotationUtil;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ResourceInstantiationException;

@Service
public class GateServiceImpl implements GateService {

	static final Logger log = Logger.getLogger("log");
	@Autowired
	AnniePluginService anniePluginService;
	
	
	/**
	 * Report of the gate document
	 * @param annotationTotalResume
	 * @param documentAnnotationMeasurement
	 * @param file_to_classify
	 * @throws ResourceInstantiationException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public void reportDocumentGateAnnotations(HashMap<String, AnnotationDummy> annotationTotalResume,
			BufferedWriter documentAnnotationMeasurement, File file_to_classify)
			throws ResourceInstantiationException, MalformedURLException, IOException {
		String report_name = file_to_classify.getName().substring(0, file_to_classify.getName().indexOf('.'));
		log.info("GateServiceImpl :: countAnnotationForDirectory :: PROCESS DOCUMENT : " +  file_to_classify);
		gate.Document toxicolodyReportWitAnnotations = Factory.newDocument((file_to_classify).toURI().toURL(), "UTF-8");
		log.info("GateServiceImpl :: countAnnotationForDirectory :: SET: DEFAULT");
		AnnotationSet annSetDef = toxicolodyReportWitAnnotations.getAnnotations(); 
		for (String annotationType : annSetDef.getAllTypes()) {
			if(annotationType.equals("SENTENCES_QUANTITY") || annotationType.equals("TOKENS_QUANTITY")) {
				AnnotationSet annotationSet = annSetDef.get(annotationType);
				Integer quantity = 0;
				for (Annotation annotation : annotationSet) {
					quantity = new Integer (annotation.getFeatures().get("quantity").toString());
				}
				if(annotationTotalResume.get(annotationType)==null) {
					annotationTotalResume.put(annotationType,new AnnotationDummy(annotationType.toUpperCase(), "GENERAL_DOCUMENT", "corpus"));
				}
				AnnotationDummy annotationDummy = annotationTotalResume.get(annotationType);
				annotationDummy.setQuantity(annotationDummy.getQuantity()+quantity);
				annotationTotalResume.put(annotationType, annotationDummy);
				log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + quantity);
				documentAnnotationMeasurement.write(report_name + "\t"+  annotationType + "\tGENERAL_DOCUMENT\t"+  quantity+"\n");
			}else {
				AnnotationSet annotationSet = annSetDef.get(annotationType);
				if(annotationTotalResume.get(annotationType)==null) {
					annotationTotalResume.put(annotationType,new AnnotationDummy(annotationType.toUpperCase(), "DEFAULT", "corpus"));
				}
				AnnotationDummy annotationDummy = annotationTotalResume.get(annotationType);
				annotationDummy.setQuantity(annotationDummy.getQuantity()+annotationSet.size());
				annotationTotalResume.put(annotationType, annotationDummy);
				log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + annotationSet.size());
				documentAnnotationMeasurement.write(report_name + "\t"+  annotationType + "\tDEFAULT\t"+  annotationSet.size()+"\n");
			}
			
			
			
		};
		
		Set<String> annotationsSet = toxicolodyReportWitAnnotations.getAnnotationSetNames();
		for (String annotationSet : annotationsSet) {
			log.info("GateServiceImpl :: countAnnotationForDirectory :: SET: " + annotationSet);
			AnnotationSet annSet = toxicolodyReportWitAnnotations.getAnnotations(annotationSet); 
			for (String annotationType : annSet.getAllTypes()) {
				AnnotationSet annotationSet2 = annSet.get(annotationType);
				if(annotationTotalResume.get(annotationType)==null) {
					annotationTotalResume.put(annotationType,new AnnotationDummy(annotationType.toUpperCase(), annotationSet.toUpperCase(), "corpus"));
				}
				AnnotationDummy annotationDummy = annotationTotalResume.get(annotationType);
				annotationDummy.setQuantity(annotationDummy.getQuantity()+annotationSet2.size());
				annotationTotalResume.put(annotationType, annotationDummy);
				log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + annotationSet2.size());
				documentAnnotationMeasurement.write(report_name + "\t"+  annotationType + "\t"+ annotationSet + "\t"+ annotationSet2.size()+"\n");
			};
		}
		documentAnnotationMeasurement.flush();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void countAnnotationForDirectory(String inputDirectoryPath) {
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			HashMap<String,Integer> total = new HashMap<String, Integer>();
			HashMap<String, AnnotationDummy> annotationTotalResume = new HashMap<String, AnnotationDummy>();
			HashMap<String, AnnotationDummy> annotationTotalResumeByYear = new HashMap<String, AnnotationDummy>();
			try {
				//total annotation measurement
				BufferedWriter totalAnnotationMeasurement = new BufferedWriter(new FileWriter(inputDirectoryPath + File.separator + "total_annotation_measurement.dat", false));
				BufferedWriter documentAnnotationMeasurement = new BufferedWriter(new FileWriter(inputDirectoryPath + File.separator + "documents_annotation_measurement.dat", false));
				for (File file_to_classify : files) {
					if(file_to_classify.getAbsolutePath().endsWith(".xml")) {
						try {
							String report_name = file_to_classify.getName().substring(0, file_to_classify.getName().indexOf('.'));
							log.info("GateServiceImpl :: countAnnotationForDirectory :: PROCESS DOCUMENT : " +  file_to_classify);
							gate.Document toxicolodyReportWitAnnotations = Factory.newDocument((file_to_classify).toURI().toURL(), "UTF-8");
							log.info("GateServiceImpl :: countAnnotationForDirectory :: SET: DEFAULT");
							AnnotationSet annSetDef = toxicolodyReportWitAnnotations.getAnnotations(); 
							for (String annotationType : annSetDef.getAllTypes()) {
								AnnotationSet annotationSet = annSetDef.get(annotationType);
								if(annotationTotalResume.get(annotationType)==null) {
									annotationTotalResume.put(annotationType,new AnnotationDummy(annotationType.toUpperCase(), "DEFAULT", "corpus"));
								}
								//total.put(annotationType, total.get(annotationType) + annotationSet.size());
								AnnotationDummy annotationDummy = annotationTotalResume.get(annotationType);
								annotationDummy.setQuantity(annotationDummy.getQuantity()+annotationSet.size());
								annotationTotalResume.put(annotationType, annotationDummy);
								//annotationTotalResume.get(annotationType).setQuantity(annotationTotalResume.get(annotationType).getQuantity()+annotationSet.size());
								
								log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + annotationSet.size());
								documentAnnotationMeasurement.write(report_name + "\t"+  annotationType + "\tDEFAULT\t"+  annotationSet.size()+"\n");
							};
							
							Set<String> annotationsSet = toxicolodyReportWitAnnotations.getAnnotationSetNames();
							for (String annotationSet : annotationsSet) {
								log.info("GateServiceImpl :: countAnnotationForDirectory :: SET: " + annotationSet);
								AnnotationSet annSet = toxicolodyReportWitAnnotations.getAnnotations(annotationSet); 
								for (String annotationType : annSet.getAllTypes()) {
									AnnotationSet annotationSet2 = annSet.get(annotationType);
									if(annotationTotalResume.get(annotationType)==null) {
										annotationTotalResume.put(annotationType,new AnnotationDummy(annotationType.toUpperCase(), annotationSet.toUpperCase(), "corpus"));
									}
									//total.put(annotationType, total.get(annotationType) + annotationSet2.size());
									
									//annotationTotalResume.get(annotationType).setQuantity(annotationTotalResume.get(annotationType).getQuantity()+1);
									
									AnnotationDummy annotationDummy = annotationTotalResume.get(annotationType);
									annotationDummy.setQuantity(annotationDummy.getQuantity()+annotationSet2.size());
									annotationTotalResume.put(annotationType, annotationDummy);
									
									//annotationTotalResume.get(annotationType).setQuantity(annotationTotalResume.get(annotationType).getQuantity()+annotationSet2.size());
									
									log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + annotationSet2.size());
									documentAnnotationMeasurement.write(report_name + "\t"+  annotationType + "\t"+ annotationSet + "\t"+ annotationSet2.size()+"\n");
									
								};
							}
							documentAnnotationMeasurement.flush();	
						} catch (ResourceInstantiationException e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file_to_classify,e);
						} catch (MalformedURLException e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file_to_classify,e);
						}catch (Exception e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file_to_classify,e);
						}
					}
				}
				log.info("GateServiceImpl :: countAnnotationForDirectory :: TOTAL COUNT ");
				/*for (String annotationType : total.keySet()) {
					log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationType + " quantity : " + total.get(annotationType));
					totalAnnotationMeasurement.write(annotationType + "\t"+ total.get(annotationType)+"\n");
				}*/
				List<AnnotationDummy> list = new ArrayList<AnnotationDummy>(annotationTotalResume.values());
				AnnotationUtil.order(list);
				for (AnnotationDummy annotationDummy : list) {
					log.info("GateServiceImpl :: countAnnotationForDirectory :: annotation " +  annotationDummy.getAnnotationType() + " quantity : " + annotationDummy.getQuantity());
					totalAnnotationMeasurement.write(annotationDummy.getAnnotationType() + "\t"+ annotationDummy.getAnnotationSet() + "\t"+ annotationDummy.getQuantity() +"\n");
				}
				totalAnnotationMeasurement.flush();
				totalAnnotationMeasurement.close();
				documentAnnotationMeasurement.close();
			} catch (IOException e) {
				log.error("GateServiceImpl :: countAnnotationForDirectory :: Error writting file : " +  inputDirectoryPath + File.separator + "annotation_measurement.dat",e);
			}	
		}
	}
	
	/**
	  * Generate GATE format from plain annotation file
	  */
	 public void generateGateFormat(String inputGATEFile, String plainAnnotationsFiles, String outPutGateFile) {
		 	try {
					if (Files.isRegularFile(Paths.get(plainAnnotationsFiles))) {
						gate.Document toxicolodyReportWitAnnotations = Factory.newDocument((new File(inputGATEFile)).toURI().toURL(), "UTF-8");
						/*Corpus corpus = Factory.newCorpus("StandAloneAnnie corpus");
						corpus.add(toxicolodyReportWitAnnotations);
						anniePluginService.init();
						anniePluginService.setCorpus(corpus);
						anniePluginService.execute();*/
						for (String line : ObjectBank.getLineIterator(plainAnnotationsFiles, "UTF-8")) {
							String[] data = line.split("\t");
					    	if(data.length==7 && data[0]!=null) {
					    		try {
					    			Long startOff = new Long(data[1]);
						    		Long endOff =  new Long(data[2]);
						    		String label = data[4];
						    		String source = data[5];
						    		String annotationMethod = data[6];
						    		String text = data[3].toLowerCase();
						    		//todo set another field and put source ?
						    		FeatureMap features = gate.Factory.newFeatureMap();
						    		features.put("source", source);
						    		features.put("annotationMethod", annotationMethod);
						    		features.put("text", text);
						    		if(label.endsWith(AnnotationUtil.STUDY_DOMAIN_TESTCD_SUFFIX)) {
						    			toxicolodyReportWitAnnotations.getAnnotations(AnnotationUtil.STUDY_DOMAIN_TESTCD).add(startOff, endOff, label, features);
						    		}else if(label.endsWith(AnnotationUtil.STUDY_DOMAIN_SUFFIX)) {
						    			toxicolodyReportWitAnnotations.getAnnotations("STUDY_DOMAIN").add(startOff, endOff, label, features);
						    		}else if(label.endsWith("_SEX")  || label.contains("SEXPOP")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("SEX").add(startOff, endOff, label, features);
						    		}else if(label.endsWith("_MANIFESTATION_FINDING")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("MANIFESTATION_OF_FINDING").add(startOff, endOff, label, features);
						    		}else if(label.contains("ROUTE")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "ROUTE_OF_ADMINISTRATION", features);
						    		}else if(label.contains("LBTEST") || label.endsWith("TEST NAME") || label.endsWith("TEST CODE")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("STUDY TEST CODE(SRTSTCD)").add(startOff, endOff,  label, features);//ACA deberia ponerse el TESTCODE
						    		}else if(label.contains("NEOPLASM_TYPE") || label.contains("NON-NEOPLASTIC FINDING TYPE")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("FINDING (SRFNDNG)").add(startOff, endOff, label, features);
						    		}else if(label.contains("ANATOMY") || label.contains("ANATOMICAL LOCATION")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "ANATOMY", features);
						    		}else if(label.contains("SPECIMEN")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "SPECIMEN", features);
						    		}else if(label.contains("SPECIES")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "SPECIES", features);
						    		}else if(label.contains("MOA")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "MODE_OF_ACTION", features);
						    		}else if(label.contains("PKPARM")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "PKPARM", features);
						    		}else if(label.contains(AnnotationUtil.NO_TREATMENT_RELATED_EFFECT_DETECTED)) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, label, features);
						    		}else if(label.contains(AnnotationUtil.TREATMENT_RELATED_EFFECT_DETECTED)) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, label, features);
						    		}else if(label.contains("STRAIN")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "STRAIN", features);
						    		}else if(label.contains("STATICAL_")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "STATICAL_SIGNIFICANCE", features);
						    		}else if(label.contains("DOSE")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "DOSE", features);
						    		}else if(label.contains("DURATION_")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "DURATION", features);
						    		}else if(label.contains("RISK_LEVEL")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("RISK_LEVEL").add(startOff, endOff, label, features);
						    		}else if(label.contains("GROUP")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, label, features);
						    		}else if(label.equals(AnnotationUtil.TOKENS) || label.equals(AnnotationUtil.SENTENCES)) {//review
						    			FeatureMap features2 = gate.Factory.newFeatureMap();
						    			features2.put("quantity", text);
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, (label+"_QUANTITY").toUpperCase(), features2);
						    		}else if(source.equals("CDISC")){
						    			//log.error("Error reading line: \n " + line,e);
						    			toxicolodyReportWitAnnotations.getAnnotations("CDISC").add(startOff, endOff, label, features);
						    		}else {
						    			//log.error("Error reading line: \n " + line,e);
						    			toxicolodyReportWitAnnotations.getAnnotations("UNKNOW").add(startOff, endOff, label, features);
						    		}
						    	}catch (Exception e) {
					    			log.error("Error reading line: \n " + line,e);
								}
					    	}else {
					    		log.error("Error reading line  " + line + " quantity of tabs incorrect" );
					    	}
					    }
						java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File(outPutGateFile), false)));
					    out.write(toxicolodyReportWitAnnotations.toXml());
					    out.close();
					} else {
						log.warn("This is not a regular file " + plainAnnotationsFiles);
					}
				} catch (ResourceInstantiationException e) {
					log.error("ResourceInstantiationException Gate error review problem ",e);
				} catch (MalformedURLException e) {
					log.error("MalformedURLException ",e);
				} catch (FileNotFoundException e) {
					log.error("File not found Exception for file path: " + plainAnnotationsFiles  + " or " +  inputGATEFile,e);
				} catch (IOException e) {
					log.error("IOException  ",e);
				}catch (Exception e) {
					log.error("Error Generic Exception, has to be controlled  ",e);
				}
	 }
	
}
