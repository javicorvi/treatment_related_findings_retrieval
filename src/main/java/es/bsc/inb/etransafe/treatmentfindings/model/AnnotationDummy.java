package es.bsc.inb.etransafe.treatmentfindings.model;

public class AnnotationDummy {

	private String annotationType;
	
	private String annotationSet;
	
	private Integer quantity;
	
	private String context;

	private Integer quantityETOX;
	
	private Integer quantityCDISC;
	
	private Integer quantityMANUAL;
	
	public AnnotationDummy(String annotationType, String annotationSet, String context) {
		super();
		this.annotationType = annotationType;
		this.annotationSet = annotationSet;
		this.context = context;
		this.quantity=0;
		this.quantityETOX=0;
		this.quantityCDISC=0;
		this.quantityMANUAL=0;
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

	public Integer getQuantityETOX() {
		return quantityETOX;
	}

	public void setQuantityETOX(Integer quantityETOX) {
		this.quantityETOX = quantityETOX;
	}

	public Integer getQuantityCDISC() {
		return quantityCDISC;
	}

	public void setQuantityCDISC(Integer quantityCDISC) {
		this.quantityCDISC = quantityCDISC;
	}

	public Integer getQuantityMANUAL() {
		return quantityMANUAL;
	}

	public void setQuantityMANUAL(Integer quantityMANUAL) {
		this.quantityMANUAL = quantityMANUAL;
	}

	
	
	
	
}
