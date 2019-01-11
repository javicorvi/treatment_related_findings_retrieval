package es.bsc.inb.limtox.services;

import java.io.IOException;

import gate.Corpus;
import gate.util.GateException;

public interface AnniePluginService {
	
	
	public void init() throws GateException, IOException;
	
	public void setCorpus(Corpus corpus);
	
	public void execute() throws GateException;
}
