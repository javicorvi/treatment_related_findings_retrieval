package es.bsc.inb.etransafe.treatmentfindings.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.Generics;
import es.bsc.inb.etransafe.treatmentfindings.model.AnnotationDummy;

public class AnnotationUtil {
	
	//SOURCES
	//SUFFIX
	public static final String SOURCE_ETOX_SUFFIX = "_ETOX_SOURCE";
	public static final String SOURCE_MANUAL_SUFFIX = "_MANUAL_SOURCE";
	public static final String SOURCE_CDISC_SUFFIX = "_CDISC_SOURCE";
	
	
	//SOURCE
	public static final String SOURCE_ETOX = "ETOX";
	public static final String SOURCE_MANUAL = "MANUAL";
	public static final String SOURCE_CDISC = "CDISC";
	public static final String STANDFORD_CORE_NLP_SOURCE = "STANDFORD_CORE_NLP";
	
	
	//GENERAL DATA
	public static final String SENTENCE_SUFFIX = "_SENTENCE";
	
	public static final String SENTENCES = "SENTENCES";
	public static final String TOKENS = "TOKENS";
	
	
	//FEATURE LABEL
	public static final String FEATURE_SUFFIX = "_FEATURE_";
	
	//ANNOTATIONS
	public static final String TREATMENT_RELATED_EFFECT_DETECTED = "TREATMENT_RELATED_EFFECT_DETECTED";
	public static final String NO_TREATMENT_RELATED_EFFECT_DETECTED = "NO_TREATMENT_RELATED_EFFECT_DETECTED";
	public static final String DOSE = "DOSE";
	public static final String MANIFESTATION_OF_FINDING_SUFFIX = "_MANIFESTATION_FINDING";
	public static final String GROUP = "GROUP";
	public static final String STUDY_DOMAIN_SUFFIX = "_DOMAIN";
	public static final String STUDY_DOMAIN_TESTCD_SUFFIX = " TEST CODE";
	public static final String STUDY_DOMAIN_TESTCD = "STUDY_TESTCD";
	//TEST NAME BY DOMAIN
	public static Map<String, String> SEND_DOMAIN_TO_DEFAULT_TESTCD = new HashMap<String, String>(){{
	    //DOMAIN THAT HAS TO BE CONSIDERED IN ETRANSAFE
		put("BODY_WEIGHT_DOMAIN", "BWTESTCD");
	    put("BODY_WEIGHT_GAIN_DOMAIN", "BGTESTCD");
	    put("CLINICAL_DOMAIN", "NOTEST");
	    put("DEATH_DIAGNOSIS_DOMAIN", "DDTESTCD");
	    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FWTESTCD");
	    put("LABORATORY_FINDINGS_DOMAIN", "LBTESTCD");
	    put("MACROSCOPIC_FINDINGS_DOMAIN", "MATESTCD");
	    put("MICROSCOPIC_FINDINGS_DOMAIN", "MITESTCD");
	    put("ORGAN_MEASUREMENT_DOMAIN", "OMTESTCD");
	    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "PKPARMCD");
	    put("TUMOR_FINDINGS_DOMAIN", "TFTESTCD");
	    put("VITAL_SIGNS_DOMAIN", "VSTESTCD");
	    put("ECG_DOMAIN", "EGTESTCD");
	    put("CARDIOVASCULAR_DOMAIN", "SCVTSTCD");
	    put("RESPIRATORY_FINDINGS_DOMAIN", "SRETSTCD");
	    
	    //OTHER DOMAINS
	    put("BEHAVIORAL_DOMAIN", "NOTEST");
	    put("COMMENTS_DOMAIN", "NOTEST");
	    put("DEMOGRAPHICS_DOMAIN", "NOTEST");
	    put("DISPOSITION_DOMAIN", "NOTEST");
	    put("EXPOSURE_DOMAIN", "NOTEST");
	    put("FERTILITY_DOMAIN", "NOTEST");
	    put("FETAL_DOMAIN", "FMTESTCD");
	    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FXTESTCD");
	    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "ICTESTCD");
	    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "NOTEST");
	    put("NERVOUS_SYSTEM_DOMAIN", "NOTEST");
	    put("PARING_EVENTS_DOMAIN", "NOTEST");
	    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "NOTEST");
	    put("PALPABLE_MASSES_DOMAIN", "PALPABLE_MASSES_DOMAIN");
	    put("NONCLINICAL_PREGNANCY_DOMAIN", "PYTESTCD");
	    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SBCCDSND");
	    put("SUBJECT_ELEMENTS_DOMAIN", "NOTEST");
	    put("SUBJECT_STAGES_DOMAIN", "NOTEST");
	    put("TRIAL_ARMS_DOMAIN", "NOTEST");
	    put("TRIAL_ELEMENTS_DOMAIN", "NOTEST");
	    put("TRIAL_PATHS_DOMAIN", "NOTEST");
	    put("TRIAL_SUMMARY_DOMAIN", "STSPRMCD");
	    put("TRIAL_STAGES_DOMAIN", "NOTEST");
	    put("TRIAL_SETS_DOMAIN", "NOTEST");
	 }};
	
	//TEST NAME BY DOMAIN
		public static Map<String, String> SEND_DOMAIN_TO_DEFAULT_TESTCDVALUE = new HashMap<String, String>(){{
		    put("BODY_WEIGHT_DOMAIN", "BW(DEFAULT TEST)");
		    put("BODY_WEIGHT_GAIN_DOMAIN", "BWGAIN(DEFAULT TEST)");
		    
		    put("BEHAVIORAL_DOMAIN", "NOTEST");
		    put("CLINICAL_DOMAIN", "NOTEST");
		    put("COMMENTS_DOMAIN", "NOTEST");
		    put("CARDIOVASCULAR_DOMAIN", "SCVTSTCD(DEFAULT TEST)");
		    put("DEATH_DIAGNOSIS_DOMAIN", "DEATHD(DEFAULT TEST)");
		    put("DEMOGRAPHICS_DOMAIN", "NOTEST");
		    put("DISPOSITION_DOMAIN", "NOTEST");
		    put("ECG_DOMAIN", "ECG(DEFAULT TEST)");
		    put("EXPOSURE_DOMAIN", "NOTEST");
		    put("FERTILITY_DOMAIN", "NOTEST");
		    put("FETAL_DOMAIN", "FMTESTCD(DEFAULT TEST)");
		    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FWTESTCD(DEFAULT TEST)");
		    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FXTESTCD(DEFAULT TEST)");
		    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "IMPSCHCTD(DEFAULT TEST)");
		    put("LABORATORY_FINDINGS_DOMAIN", "LBTESTCD(DEFAULT TEST)");
		    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "NOTEST");
		    put("MACROSCOPIC_FINDINGS_DOMAIN", "CLSFUP(DEFAULT TEST)");
		    put("MICROSCOPIC_FINDINGS_DOMAIN", "GHISTXQL(DEFAULT TEST)");
		    put("NERVOUS_SYSTEM_DOMAIN", "NOTEST");
		    put("ORGAN_MEASUREMENT_DOMAIN", "OMTESTCD(DEFAULT TEST)");
		    put("PARING_EVENTS_DOMAIN", "NOTEST");
		    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "NOTEST");
		    put("PALPABLE_MASSES_DOMAIN", "NOTEST");
		    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "NOTEST");
		    put("NONCLINICAL_PREGNANCY_DOMAIN", "PYTESTCD(DEFAULT TEST)");
		    put("RESPIRATORY_FINDINGS_DOMAIN", "SRETSTCD(DEFAULT TEST)");
		    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SBCCDSND(DEFAULT TEST)");
		    put("SUBJECT_ELEMENTS_DOMAIN", "NOTEST");
		    put("SUBJECT_STAGES_DOMAIN", "NOTEST");
		    put("TRIAL_ARMS_DOMAIN", "NOTEST");
		    put("TRIAL_ELEMENTS_DOMAIN", "NOTEST");
		    put("TUMOR_FINDINGS_DOMAIN", "TUMEX(DEFAULT TEST)");
		    put("TRIAL_PATHS_DOMAIN", "NOTEST");
		    put("TRIAL_SUMMARY_DOMAIN", "STSPRMCD(DEFAULT TEST)");
		    put("TRIAL_STAGES_DOMAIN", "NOTEST");
		    put("TRIAL_SETS_DOMAIN", "NOTEST");
		    put("VITAL_SIGNS_DOMAIN", "VSTESTCD(DEFAULT TEST)");
		}};
	
	//TEST DEFAULT BY DOMAIN
	public static	Map<String, String> SEND_DOMAIN_DESC_TO_SEND_DOMAIN_CODE = new HashMap<String, String>(){{
		    put("BODY_WEIGHT_DOMAIN", "BW");
		    put("BEHAVIORAL_DOMAIN", "BEHAVIORAL_DOMAIN");
		    put("BODY_WEIGHT_GAIN_DOMAIN", "BG");
		    put("CLINICAL_DOMAIN", "CL");
		    put("COMMENTS_DOMAIN", "COMMENTS_DOMAIN");
		    put("CARDIOVASCULAR_DOMAIN", "CV");
		    put("DEATH_DIAGNOSIS_DOMAIN", "DD");
		    put("DEMOGRAPHICS_DOMAIN", "DEMOGRAPHICS_DOMAIN");
		    put("DISPOSITION_DOMAIN", "DISPOSITION_DOMAIN");
		    put("ECG_DOMAIN", "EG");
		    put("EXPOSURE_DOMAIN", "EXPOSURE_DOMAIN");
		    put("FERTILITY_DOMAIN", "FERTILITY_DOMAIN");
		    put("BODY_WEIGHT_DOMAIN", "BODY_WEIGHT_DOMAIN");
		    put("FETAL_DOMAIN", "FM");
		    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FW");
		    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FX");
		    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "IC");
		    put("LABORATORY_FINDINGS_DOMAIN", "LB");
		    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN");
		    put("MACROSCOPIC_FINDINGS_DOMAIN", "MA");
		    put("MICROSCOPIC_FINDINGS_DOMAIN", "MI");
		    put("NERVOUS_SYSTEM_DOMAIN", "NERVOUS_SYSTEM_DOMAIN");
		    put("ORGAN_MEASUREMENT_DOMAIN", "OM");
		    put("PARING_EVENTS_DOMAIN", "PARING_EVENTS_DOMAIN");
		    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "PHARMACOKINETIC_CONCENTRATION_DOMAIN");
		    put("PALPABLE_MASSES_DOMAIN", "PALPABLE_MASSES_DOMAIN");
		    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "PHARMACOKINETICS_PARAMETERS_DOMAIN");
		    put("NONCLINICAL_PREGNANCY_DOMAIN", "NONCLINICAL_PREGNANCY_DOMAIN");
		    put("RESPIRATORY_FINDINGS_DOMAIN", "RE");
		    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SC");
		    put("SUBJECT_ELEMENTS_DOMAIN", "SUBJECT_ELEMENTS_DOMAIN");
		    put("SUBJECT_STAGES_DOMAIN", "SUBJECT_STAGES_DOMAIN");
		    put("TRIAL_ARMS_DOMAIN", "TRIAL_ARMS_DOMAIN");
		    put("TRIAL_ELEMENTS_DOMAIN", "TRIAL_ELEMENTS_DOMAIN");
		    put("TUMOR_FINDINGS_DOMAIN", "TF");
		    put("TRIAL_PATHS_DOMAIN", "TRIAL_PATHS_DOMAIN");
		    put("TRIAL_SUMMARY_DOMAIN", "TRIAL_SUMMARY_DOMAIN");
		    put("TRIAL_STAGES_DOMAIN", "TRIAL_STAGES_DOMAIN");
		    put("TRIAL_SETS_DOMAIN", "TRIAL_SETS_DOMAIN");
		    put("VITAL_SIGNS_DOMAIN", "VS");
		}};
	
		
	public static final Set<String> entityMentionsToDelete = Generics.newHashSet(Arrays.asList(new String[]{"DOSE_UNIT","NUMBER", "MONEY", "DATE","TIME","TITLE","CAUSE_OF_DEATH","PERSON", 
			 "TREATMENT_RELATED_EFFECT_DETECTED","NO_TREATMENT_RELATED_EFFECT_DETECTED", "INCREASE_MANIFESTATION_FINDING", "DECREASE_MANIFESTATION_FINDING","JUSTPRESENT_MANIFESTATION_FINDING"}));	
		
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
