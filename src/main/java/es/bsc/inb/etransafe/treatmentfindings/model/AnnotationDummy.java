package es.bsc.inb.etransafe.treatmentfindings.model;

public class AnnotationDummy {

	private String annotationType;
	
	private String annotationSet;
	
	private Integer quantity;
	
	private String context;

	
	
	public AnnotationDummy(String annotationType, String annotationSet, String context) {
		super();
		this.annotationType = annotationType;
		this.annotationSet = annotationSet;
		this.context = context;
		this.quantity=0;
	}

	public String getAnnotationType() {
		return annotationType;
	}

	public void setAnnotationType(String annotationType) {
		this.annotationType = annotationType;
	}

	public String getAnnotationSet() {
		return annotationSet;
	}

	public void setAnnotationSet(String annotationSet) {
		this.annotationSet = annotationSet;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	
	
}
