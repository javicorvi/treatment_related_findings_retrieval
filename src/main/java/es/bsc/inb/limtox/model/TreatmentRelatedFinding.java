package es.bsc.inb.limtox.model;

/**
 * Treatment Related Finding model entity.  This entity describe all the data that is extracted from the 
 * reports orinted to describe Treatment Related Findings
 * @author jcorvi
 *
 */
public class TreatmentRelatedFinding {
	/** 
	 * Unique identifier for a study within a SEND submission.
	 */
	private String studyId;
	/**
	 * Domain, always SR: Study Report
	 */
	private String domain;
	/**
	 * A toxicity risk indicator. Can be one of: NOEL, LOEL, NOAEL or LOAEL.
	 */
	private ToxicityRisk risk;
	/**
	 * Number of the treatment group in which the finding was observed
	 */
	private String groupId;
	/**
	 * Sex of the animals in which the finding was observed
	 */
	private Character sex;
	/**
	 * Day on which the finding was observed. Can be blank for time-course observations (e.g. clinical signs, bodyweight, bodyweight gain, etc.).
	 */
	private String studyDayOfFinding;
	/**
	 * Domain in which the finding was observed
	 */
	private Domain domainOfFinding;
	/**
	 * Specimen in which the finding was observed. This is not required for all domains. For CL (Clinical Signs) domain, (BODSYS) is used instead of (SPEC).
	 */
	private String specimen;
	/**
	 * Short name of the measurement, test, or examination relating to the finding. The controlled term list is determined by the Domain in which the finding was observed.
	 */
	private String testShortName;
	/**
	 * Finding in those cases where there is no associated test (such as for domains CL, MA, MI, etc.). There may be controlled terms (depending on the domain).
	 */
	private String finding;
	/**
	 * Manifestation of Findining.
	 */
	private Manifestation manifestationOfFinding; 
	/**
	 * Severity of the observation. 
	 */
	private String severityOfFinding;
	/**
	 * Statistical significance of the observation
	 */
	private String statisticalSignificanceOfFinding;
	
	/**
	 * Dosis description
	 */
	private String dosis;
	/**
	 * “Y” = finding is treatment-related; “N” = finding is not treatment-related; “U” = uncertain; “NA” = not applicable.
	 */
	private Character isTreatmentRelated;
	/**
	 * Auxiliar boolean to save finding
	 */
	private transient Boolean recordFounded=false;
	
	
	public String getStudyId() {
		return studyId;
	}
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public ToxicityRisk getRisk() {
		return risk;
	}
	public void setRisk(ToxicityRisk risk) {
		this.risk = risk;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Character getSex() {
		return sex;
	}
	public void setSex(Character sex) {
		this.sex = sex;
	}
	public String getStudyDayOfFinding() {
		return studyDayOfFinding;
	}
	public void setStudyDayOfFinding(String studyDayOfFinding) {
		this.studyDayOfFinding = studyDayOfFinding;
	}
	public Domain getDomainOfFinding() {
		return domainOfFinding;
	}
	public void setDomainOfFinding(Domain domainOfFinding) {
		this.domainOfFinding = domainOfFinding;
	}
	public String getSpecimen() {
		return specimen;
	}
	public void setSpecimen(String specimen) {
		this.specimen = specimen;
	}
	public String getTestShortName() {
		return testShortName;
	}
	public void setTestShortName(String testShortName) {
		this.testShortName = testShortName;
	}
	public String getFinding() {
		return finding;
	}
	public void setFinding(String finding) {
		this.finding = finding;
	}
	public Manifestation getManifestationOfFinding() {
		return manifestationOfFinding;
	}
	public void setManifestationOfFinding(Manifestation manifestationOfFinding) {
		this.manifestationOfFinding = manifestationOfFinding;
	}
	public String getSeverityOfFinding() {
		return severityOfFinding;
	}
	public void setSeverityOfFinding(String severityOfFinding) {
		this.severityOfFinding = severityOfFinding;
	}
	public String getStatisticalSignificanceOfFinding() {
		return statisticalSignificanceOfFinding;
	}
	public void setStatisticalSignificanceOfFinding(String statisticalSignificanceOfFinding) {
		this.statisticalSignificanceOfFinding = statisticalSignificanceOfFinding;
	}
	public Character getIsTreatmentRelated() {
		return isTreatmentRelated;
	}
	public void setIsTreatmentRelated(Character isTreatmentRelated) {
		this.isTreatmentRelated = isTreatmentRelated;
	}
	public String getDosis() {
		return dosis;
	}
	public void setDosis(String dosis) {
		this.dosis = dosis;
	}
	public Boolean getRecordFounded() {
		return recordFounded;
	}
	public void setRecordFounded(Boolean recordFounded) {
		this.recordFounded = recordFounded;
	} 
	
	
	public String toString() {
		return "studyId\t"+studyId+"\n"+
				"domain\t"+domain+"\n"+
				"risk\t"+risk+"\n"+
				"groupId\t"+groupId+"\n"+
				"sex\t"+sex+"\n"+
				"studyDayOfFinding\t"+studyDayOfFinding+"\n"+
				"domainOfFinding\t"+domainOfFinding+"\n"+
				"specimen\t"+specimen+"\n"+
				"testShortName\t"+testShortName+"\n"+
				"finding\t"+finding+"\n"+
				"manifestationOfFinding\t"+manifestationOfFinding+"\n"+
				"severityOfFinding\t"+severityOfFinding+"\n"+
				"dosis\t"+dosis+"\n"+
				"isTreatmentRelated\t"+isTreatmentRelated+"\n";
	}
	
}
