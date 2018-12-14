package es.bsc.inb.limtox.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import edu.stanford.nlp.objectbank.ObjectBank;
import gate.Factory;
import gate.Gate;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import gate.util.InvalidOffsetException;

@Service
public class GateServiceImpl implements GateService {

	/**
	  * 
	  */
	 public void generateGateFormat(String inputGATEFile, String plainAnnotationsFiles, String outPutGateFile) {
		 
			   try {
					
					if (Files.isRegularFile(Paths.get(plainAnnotationsFiles))) {
						gate.Document toxicolodyReportWitAnnotations = Factory.newDocument((new File(inputGATEFile)).toURI().toURL(), "UTF-8");
						for (String line : ObjectBank.getLineIterator(plainAnnotationsFiles, "UTF-8")) {
							String[] data = line.split("\t");
					    	if(data.length>1 && data[0]!=null) {
					    		Long startOff = new Long(data[1]);
					    		Long endOff =  new Long(data[2]);
					    		//toxicolodyReportWitAnnotations.getAnnotations("TREATMENT_RELATED_FINDING").add(startOff+34, endOff+35, data[4], gate.Factory.newFeatureMap());
					    		toxicolodyReportWitAnnotations.getAnnotations("TREATMENT_RELATED_FINDING").add(startOff, endOff, data[4], gate.Factory.newFeatureMap());
					    	}
					    }
						
						java.io.Writer out = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new FileOutputStream(new File(outPutGateFile), false)));
					    out.write(toxicolodyReportWitAnnotations.toXml());
					    out.close();
						
					} else {
						
					}
					
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
