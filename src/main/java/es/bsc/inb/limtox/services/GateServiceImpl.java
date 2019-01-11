package es.bsc.inb.limtox.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.objectbank.ObjectBank;
import gate.Corpus;
import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

@Service
public class GateServiceImpl implements GateService {

	static final Logger log = Logger.getLogger("log");
	@Autowired
	AnniePluginService anniePluginService;
	
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
					    	if(data.length==5 && data[0]!=null) {
					    		try {
					    			Long startOff = new Long(data[1]);
						    		Long endOff =  new Long(data[2]);
						    		
						    		String label = data[4].toLowerCase();
						    		
						    		if(label.contains("_domain")) {
						    			if(label.contains("_etox_send")) {
						    				toxicolodyReportWitAnnotations.getAnnotations("STUDY_DOMAIN").add(startOff, endOff, label.replaceAll("_etox_send", ""), gate.Factory.newFeatureMap());
						    			}else {
						    				//serian las internas revisar 
						    				toxicolodyReportWitAnnotations.getAnnotations("STUDY_DOMAIN").add(startOff, endOff, label, gate.Factory.newFeatureMap());
						    			}
						    		}else if(label.contains("_sex")  || label.contains("sexpop")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("SEX").add(startOff, endOff, label, gate.Factory.newFeatureMap());
						    		}else if(label.contains("_manifestation_finding")) {
						    			toxicolodyReportWitAnnotations.getAnnotations("MANIFESTATION_OF_FINDING").add(startOff, endOff, label, gate.Factory.newFeatureMap());
						    		}else if(label.contains("route_etox_send")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "ROUTE_OF_ADMINISTRATION", gate.Factory.newFeatureMap());
						    		}else if(label.contains("lbtest")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "LBTEST", gate.Factory.newFeatureMap());
						    		}else if(label.contains("anatomy")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "ANATOMY", gate.Factory.newFeatureMap());
						    		}else if(label.contains("species")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "SPECIES", gate.Factory.newFeatureMap());
						    		}else if(label.contains("moa")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "MODE_OF_ACTION", gate.Factory.newFeatureMap());
						    		}else if(label.contains("pkparm")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "PKPARM", gate.Factory.newFeatureMap());
						    		}else if(label.contains("no_treatment_related")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "NO_TREATMENT_RELATED_EFFECT_DETECTED", gate.Factory.newFeatureMap());
						    		}else if(label.contains("strain_")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "STRAIN", gate.Factory.newFeatureMap());
						    		}else if(label.contains("statical_")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "STATICAL_SIGNIFICANCE", gate.Factory.newFeatureMap());
						    		}else if(label.contains("dose")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "DOSE", gate.Factory.newFeatureMap());
						    		}else if(label.contains("duration_")) {
						    			toxicolodyReportWitAnnotations.getAnnotations().add(startOff, endOff, "DURATION", gate.Factory.newFeatureMap());
						    		}else {
						    			toxicolodyReportWitAnnotations.getAnnotations("TREATMENT_RELATED_FINDING").add(startOff, endOff, label, gate.Factory.newFeatureMap());
						    		}
						    	}catch (Exception e) {
					    			log.error("Error reading line: \n " + line,e);
								}
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
	
	/**
	  * 
	  */
	 private void test(String inputGATEFile, String plainAnnotationsFiles, String outPutGateFile) {
		 try {
				Gate.init();
			} catch (GateException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	Gate.setGateHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
			 Gate.setPluginsHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
			   try {
					gate.Document toxicolodyReportWitAnnotations = Factory.newDocument((new File("/home/jcorvi/eTRANSAFE_DATA/reports/bayer/Batch1_txt_GATE/GGA_BHC_BAY198004_2_PROCESSED_GATE.xml")).toURI().toURL(), "UTF-8");
					
					//toxicolodyReportWitAnnotations.getSourceUrlOffsets()
					
					
					
					
					toxicolodyReportWitAnnotations.getAnnotations("SDOMAIN").add(39l, 98l, "BODY_WEIGHT_DOMAIN", gate.Factory.newFeatureMap());
					
					toxicolodyReportWitAnnotations.getAnnotations("SDOMAIN").add(39855l, 39870l, "BODY_WEIGHT_DOMAIN", gate.Factory.newFeatureMap());
					
					java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File("/home/jcorvi/eTRANSAFE_DATA/findings/bayer/Batch1_txt_GATE/GGA_BHC_BAY198004_2_PROCESSED_GATE.xml"), false)));
				    out.write(toxicolodyReportWitAnnotations.toXml());
				    out.close();
				} catch (ResourceInstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidOffsetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	 }

	
}
