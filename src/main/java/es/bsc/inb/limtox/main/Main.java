package es.bsc.inb.limtox.main;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import es.bsc.inb.limtox.config.AppConfig;
import es.bsc.inb.limtox.services.TaggerService;

class Main {

    public static void main(String[] args) {
    	AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        ctx.refresh();
        TaggerService mainService = (TaggerService)ctx.getBean("taggerServiceImpl");
        String properties_parameters_path = args[0];
        mainService.execute(properties_parameters_path);
    }      
}
