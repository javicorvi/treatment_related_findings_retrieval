package es.bsc.inb.etransafe.treatmentfindings.main;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import es.bsc.inb.etransafe.treatmentfindings.config.AppConfig;
import es.bsc.inb.etransafe.treatmentfindings.services.AnnotationService;
import es.bsc.inb.etransafe.treatmentfindings.services.ReportService;
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
        
        AppConfig.initGate("/home/jcorvi/GATE_Developer_8.5.1/", "/home/jcorvi/GATE_Developer_8.5.1/");
        String properties_parameters_path = args[0];
        AnnotationService mainService = (AnnotationService)ctx.getBean("annotationServiceImpl");
    	mainService.execute(properties_parameters_path);
        
        ReportService reportService = (ReportService)ctx.getBean("reportServiceImpl");
        reportService.execute(properties_parameters_path);
    }      
}
