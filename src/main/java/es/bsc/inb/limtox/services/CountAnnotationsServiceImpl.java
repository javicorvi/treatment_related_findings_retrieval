package es.bsc.inb.limtox.services;


import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.bsc.inb.limtox.util.PropertiesUtil;
@Service
class CountAnnotationsServiceImpl implements CountAnnotationsService {

	static final Logger log = Logger.getLogger("log");
	
	@Autowired
	GateService gateService;

	public void execute(String propertiesParametersPath) {
		try {
			log.info("Classify articles with properties :  " +  propertiesParametersPath);
			Properties propertiesParameters = PropertiesUtil.loadPropertiesParameters(propertiesParametersPath);
			log.info("Classify articles with the model  :  " +  propertiesParameters.getProperty("classificatorModel"));
			log.info("Input directory with the articles to classify : " + propertiesParameters.getProperty("inputDirectory"));
			log.info("Outup directory with the relevant articles : " + propertiesParameters.getProperty("outputDirectory"));
			log.info("Relevant articles label: " + propertiesParameters.getProperty("relevantLabel"));
			log.info("Is sentence classification: " + propertiesParameters.getProperty("is_sentences_classification"));
			String outputDirectoryPath = propertiesParameters.getProperty("outputDirectory");
			
			
			gateService.countAnnotationForDirectory(outputDirectoryPath);
		    
		}  catch (Exception e) {
			log.error("Generic error in the classification step",e);
		}
	}
	
	
	
	
		
		
	

}
