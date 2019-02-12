package es.bsc.inb.etransafe.treatmentfindings.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import es.bsc.inb.etransafe.treatmentfindings.model.AnnotationDummy;
import gate.creole.ResourceInstantiationException;

public interface GateService {

	public void generateGateFormat(String inputGATEFile, String plainAnnotationsFiles, String outPutGateFile);
	
	public void countAnnotationForDirectory(String inputDirectory);
		
	public void reportDocumentGateAnnotations(HashMap<String, AnnotationDummy> annotationTotalResume, BufferedWriter documentAnnotationMeasurement, File file_to_classify)
			throws ResourceInstantiationException, MalformedURLException, IOException;

	/**
	 * Save a plain text file from the gate document.
	 * @param properties_parameters_path
	 */
	public void generatePlainText(String properties_parameters_path);
}
