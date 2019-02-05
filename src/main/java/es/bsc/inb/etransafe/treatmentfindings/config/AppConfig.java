package es.bsc.inb.etransafe.treatmentfindings.config;

import java.io.File;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import gate.Gate;
import gate.util.GateException;


@Configuration
@ComponentScan("es.bsc.inb.etransafe.treatmentfindings")
public class AppConfig {

	
	public static void initGate(String gateHome, String gatePlugins) {
		try {
			Gate.init();
			Gate.setGateHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
			Gate.setPluginsHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
		} catch (GateException e1) {
			e1.printStackTrace();
		}
	}
	
}
