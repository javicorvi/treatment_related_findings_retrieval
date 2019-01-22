package es.bsc.inb.limtox.main;

import java.io.File;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import es.bsc.inb.limtox.config.AppConfig;
import es.bsc.inb.limtox.services.CountAnnotationsService;
import es.bsc.inb.limtox.services.ReportService;
import es.bsc.inb.limtox.services.TaggerService;
import gate.Gate;
import gate.util.GateException;

class Main {

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
