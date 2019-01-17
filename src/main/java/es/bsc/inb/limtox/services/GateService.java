package es.bsc.inb.limtox.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import es.bsc.inb.limtox.model.AnnotationDummy;
import gate.creole.ResourceInstantiationException;

public interface GateService {

	public void generateGateFormat(String inputGATEFile, String plainAnnotationsFiles, String outPutGateFile);
	
	public void countAnnotationForDirectory(String inputDirectory);
		
	public void reportDocumentGateAnnotations(HashMap<String, AnnotationDummy> annotationTotalResume, BufferedWriter documentAnnotationMeasurement, File file_to_classify)
			throws ResourceInstantiationException, MalformedURLException, IOException;
}
