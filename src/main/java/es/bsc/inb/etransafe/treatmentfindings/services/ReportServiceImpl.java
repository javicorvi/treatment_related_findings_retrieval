package es.bsc.inb.etransafe.treatmentfindings.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.objectbank.ObjectBank;
import es.bsc.inb.etransafe.treatmentfindings.model.AnnotationDummy;
import es.bsc.inb.etransafe.treatmentfindings.util.AnnotationUtil;
import es.bsc.inb.etransafe.treatmentfindings.util.PropertiesUtil;
import gate.creole.ResourceInstantiationException;

@Service
public class ReportServiceImpl implements ReportService{

	static final Logger log = Logger.getLogger("log");
	
	@Autowired
	GateService gateService;
	/**
	 * 
	 */
	public void execute(String propertiesParametersPath) {
		try {
			log.info("Generate reports articles with properties :  " +  propertiesParametersPath);
			Properties propertiesParameters = PropertiesUtil.loadPropertiesParameters(propertiesParametersPath);
			
			log.info("Outup directory with the relevant articles : " + propertiesParameters.getProperty("outputDirectory"));
			String outputDirectoryPath = propertiesParameters.getProperty("outputDirectory");
			//gateService.countAnnotationForDirectory(outputDirectoryPath);
		    this.generateReport(outputDirectoryPath);
		}  catch (Exception e) {
			log.error("Generic error in the classification step",e);
		}
	}
	
	
	/**
	 * 
	 * @param inputDirectoryPath
	 */
	private void generateReport(String inputDirectoryPath) {
		if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
			File inputDirectory = new File(inputDirectoryPath);
			File[] files =  inputDirectory.listFiles();
			HashMap<String, AnnotationDummy> annotationTotalResume = new HashMap<String, AnnotationDummy>();
			try {
				//total annotation measurement
				BufferedWriter totalAnnotationMeasurement = new BufferedWriter(new FileWriter(inputDirectoryPath + File.separator + "total_annotation_measurement.dat", false));
				BufferedWriter documentAnnotationMeasurement = new BufferedWriter(new FileWriter(inputDirectoryPath + File.separator + "documents_annotation_measurement.dat", false));
				for (File file : files) {
					if(file.getAbsolutePath().endsWith(".xml")) {
						try {
							
							//this.readDocumentGeneralInformation(file.getAbsolutePath(), annotationTotalResume, documentAnnotationMeasurement );
							gateService.reportDocumentGateAnnotations(annotationTotalResume, documentAnnotationMeasurement, file);	
						
						} catch (ResourceInstantiationException e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file,e);
						} catch (MalformedURLException e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file,e);
						}catch (Exception e) {
							log.error("GateServiceImpl :: countAnnotationForDirectory :: Error procession : " +  file,e);
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
					totalAnnotationMeasurement.write(annotationDummy.getAnnotationType() + "\t"+ annotationDummy.getAnnotationSet() + "\t"+ annotationDummy.getQuantity() + "\t"+ annotationDummy.getQuantityCDISC() + "\t"+ annotationDummy.getQuantityETOX() + "\t"+ annotationDummy.getQuantityMANUAL() +"\n");
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
	 * Read general annotated information of the document
	 * @param absolutePath
	 * @throws IOException 
	 */
	private void readDocumentGeneralInformation(String xmlGatePath, HashMap<String, AnnotationDummy> annotationTotalResume, BufferedWriter documentAnnotationMeasurement) throws IOException {
		String plainAnnotationsPath = xmlGatePath.replace("GATE", "PLAIN").replace(".xml",".txt");
		String report_name = xmlGatePath.substring(0, xmlGatePath.indexOf('.'));
		if (Files.isRegularFile(Paths.get(plainAnnotationsPath))) {
			for (String line : ObjectBank.getLineIterator(plainAnnotationsPath, "UTF-8")) {
				String[] data = line.split("\t");
		    	if(data.length==6 && data[0]!=null) {
		    		String label = data[4].toLowerCase();
		    		String source = data[5].toLowerCase();
		    		if(label.equals("SENTENCES") || label.equals("TOKENS")) {
		    			documentAnnotationMeasurement.write(report_name + "\t"+  label + "\tDEFAULT\t"+  source+"\n");
		    		}
		    	}
			}
		}
	}
	
	
}
