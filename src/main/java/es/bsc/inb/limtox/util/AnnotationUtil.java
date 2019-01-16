package es.bsc.inb.limtox.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.bsc.inb.limtox.model.AnnotationDummy;

public class AnnotationUtil {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void order(List<AnnotationDummy> annotations) {

	    Collections.sort(annotations, new Comparator() {

	        public int compare(Object o1, Object o2) {

	            String x1 = ((AnnotationDummy) o1).getAnnotationSet();
	            String x2 = ((AnnotationDummy) o2).getAnnotationSet();
	            int sComp = x1.compareTo(x2);

	            if (sComp != 0) {
	               return sComp;
	            } 

	            String x3 = ((AnnotationDummy) o1).getAnnotationType();
	            String x4 = ((AnnotationDummy) o2).getAnnotationType();
	            return x3.compareTo(x4);
	    }});
	}
	
}
