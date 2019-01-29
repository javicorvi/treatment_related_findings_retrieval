package es.bsc.inb.etransafe.treatmentfindings.main;

import java.io.File;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import es.bsc.inb.etransafe.treatmentfindings.config.AppConfig;
import es.bsc.inb.etransafe.treatmentfindings.services.ReportService;
import es.bsc.inb.etransafe.treatmentfindings.services.TaggerService;
import gate.Gate;
import gate.util.GateException;
/**
 * Main entry to run the Treatment-related finding retrieval
 * @author jcorvi
 *
 */
class Main {
	/**
	 * Entry method to execute the system
	 * @param args
	 */
    public static void main(String[] args) {
    	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        ctx.refresh();
        TaggerService mainService = (TaggerService)ctx.getBean("taggerServiceImpl");
        String properties_parameters_path = args[0];
        try {
			Gate.init();
		} catch (GateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	Gate.setGateHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
		Gate.setPluginsHome(new File("/home/jcorvi/GATE_Developer_8.5.1/"));
        mainService.execute(properties_parameters_path);
        
        
        /*CountAnnotationsService countAnnotations = (CountAnnotationsService)ctx.getBean("countAnnotationsServiceImpl");
        countAnnotations.execute(properties_parameters_path);*/
        
        ReportService reportService = (ReportService)ctx.getBean("reportServiceImpl");
        reportService.execute(properties_parameters_path);
    }      
}
