package es.bsc.inb.etransafe.treatmentfinding.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.Generics;
import es.bsc.inb.etransafe.treatmentfinding.model.AnnotationDummy;

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
	public static final String STUDY_DOMAIN_TESTCD_SUFFIX = "_TESTCD";
	public static final String STUDY_DOMAIN_TESTCD = "STUDY_TESTCD";
	//TEST DEFAULT BY DOMAIN
	public static Map<String, String> SEND_DOMAIN_TO_DEFAULT_TESTCD = new HashMap<String, String>(){{
	    put("BODY_WEIGHT_DOMAIN", "BW");//done
	    put("BEHAVIORAL_DOMAIN", "BEHAVIORAL_DOMAIN");
	    put("BODY_WEIGHT_GAIN_DOMAIN", "BWGAIN");//done
	    put("CLINICAL_DOMAIN", "CLINICAL_DOMAIN");
	    put("COMMENTS_DOMAIN", "COMMENTS_DOMAIN");
	    put("CARDIOVASCULAR_DOMAIN", "SCVTSTCD");//done, review
	    put("DEATH_DIAGNOSIS_DOMAIN", "DEATHD");//done
	    put("DEMOGRAPHICS_DOMAIN", "DEMOGRAPHICS_DOMAIN");
	    put("DISPOSITION_DOMAIN", "DISPOSITION_DOMAIN");
	    put("ECG_DOMAIN", "ECG_DOMAIN");
	    put("EXPOSURE_DOMAIN", "EXPOSURE_DOMAIN");
	    put("FERTILITY_DOMAIN", "FERTILITY_DOMAIN");
	    put("BODY_WEIGHT_DOMAIN", "BODY_WEIGHT_DOMAIN");
	    put("FETAL_DOMAIN", "FMTESTCD");//done, review
	    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FC-WC");//done, review
	    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FXFINDRS");//done, review
	    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "IC");//done, review
	    put("LABORATORY_FINDINGS_DOMAIN", "LBTESTCD");//done, review
	    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN");
	    put("MACROSCOPIC_FINDINGS_DOMAIN", "CLSFUP");//done, review
	    put("MICROSCOPIC_FINDINGS_DOMAIN", "GHISTXQL");//done
	    put("NERVOUS_SYSTEM_DOMAIN", "NERVOUS_SYSTEM_DOMAIN");
	    put("ORGAN_MEASUREMENT_DOMAIN", "WEIGHT");//done, review
	    put("PARING_EVENTS_DOMAIN", "PARING_EVENTS_DOMAIN");
	    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "PHARMACOKINETIC_CONCENTRATION_DOMAIN");
	    put("PALPABLE_MASSES_DOMAIN", "PALPABLE_MASSES_DOMAIN");
	    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "PHARMACOKINETICS_PARAMETERS_DOMAIN");
	    put("NONCLINICAL_PREGNANCY_DOMAIN", "NONCLINICAL_PREGNANCY_DOMAIN");
	    put("RESPIRATORY_FINDINGS_DOMAIN", "SRETSTCD");//done,review
	    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SBCCDSND");//done, review
	    put("SUBJECT_ELEMENTS_DOMAIN", "SUBJECT_ELEMENTS_DOMAIN");
	    put("SUBJECT_STAGES_DOMAIN", "SUBJECT_STAGES_DOMAIN");
	    put("TRIAL_ARMS_DOMAIN", "TRIAL_ARMS_DOMAIN");
	    put("TRIAL_ELEMENTS_DOMAIN", "TRIAL_ELEMENTS_DOMAIN");
	    put("TUMOR_FINDINGS_DOMAIN", "TUMEX");//done
	    put("TRIAL_PATHS_DOMAIN", "TRIAL_PATHS_DOMAIN");
	    put("TRIAL_SUMMARY_DOMAIN", "TRIAL_SUMMARY_DOMAIN");
	    put("TRIAL_STAGES_DOMAIN", "TRIAL_STAGES_DOMAIN");
	    put("TRIAL_SETS_DOMAIN", "TRIAL_SETS_DOMAIN");
	    put("VITAL_SIGNS_DOMAIN", "VSTESTCD");//done, review
	}};
	
	//TEST DEFAULT BY DOMAIN
	public static	Map<String, String> SEND_DOMAIN_DESC_TO_SEND_DOMAIN_CODE = new HashMap<String, String>(){{
		    put("BODY_WEIGHT_DOMAIN", "BW");//done
		    put("BEHAVIORAL_DOMAIN", "BEHAVIORAL_DOMAIN");
		    put("BODY_WEIGHT_GAIN_DOMAIN", "BG");//done
		    put("CLINICAL_DOMAIN", "CL");
		    put("COMMENTS_DOMAIN", "COMMENTS_DOMAIN");
		    put("CARDIOVASCULAR_DOMAIN", "CV");//done
		    put("DEATH_DIAGNOSIS_DOMAIN", "DD");//done
		    put("DEMOGRAPHICS_DOMAIN", "DEMOGRAPHICS_DOMAIN");
		    put("DISPOSITION_DOMAIN", "DISPOSITION_DOMAIN");
		    put("ECG_DOMAIN", "EG");//done
		    put("EXPOSURE_DOMAIN", "EXPOSURE_DOMAIN");
		    put("FERTILITY_DOMAIN", "FERTILITY_DOMAIN");
		    put("BODY_WEIGHT_DOMAIN", "BODY_WEIGHT_DOMAIN");
		    put("FETAL_DOMAIN", "FM");//done
		    put("FOOD_WATER_CONSUMPTION_DOMAIN", "FW");//done
		    put("FETAL_PATOLOGY_FINDINGS_DOMAIN", "FX");
		    put("IMPLANTATION_CLASSIFICATION_DOMAIN", "IC");//done
		    put("LABORATORY_FINDINGS_DOMAIN", "LB");//done
		    put("CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN", "CESARIAN_SECTION_DELIVERY_LITTER_DOMAIN");
		    put("MACROSCOPIC_FINDINGS_DOMAIN", "MA");//done
		    put("MICROSCOPIC_FINDINGS_DOMAIN", "MI");//done
		    put("NERVOUS_SYSTEM_DOMAIN", "NERVOUS_SYSTEM_DOMAIN");
		    put("ORGAN_MEASUREMENT_DOMAIN", "OM");//done
		    put("PARING_EVENTS_DOMAIN", "PARING_EVENTS_DOMAIN");
		    put("PHARMACOKINETIC_CONCENTRATION_DOMAIN", "PHARMACOKINETIC_CONCENTRATION_DOMAIN");
		    put("PALPABLE_MASSES_DOMAIN", "PALPABLE_MASSES_DOMAIN");
		    put("PHARMACOKINETICS_PARAMETERS_DOMAIN", "PHARMACOKINETICS_PARAMETERS_DOMAIN");
		    put("NONCLINICAL_PREGNANCY_DOMAIN", "NONCLINICAL_PREGNANCY_DOMAIN");
		    put("RESPIRATORY_FINDINGS_DOMAIN", "RE");//done
		    put("SUBJECT_CHARACTERISTICS_DOMAIN", "SC");//done
		    put("SUBJECT_ELEMENTS_DOMAIN", "SUBJECT_ELEMENTS_DOMAIN");
		    put("SUBJECT_STAGES_DOMAIN", "SUBJECT_STAGES_DOMAIN");
		    put("TRIAL_ARMS_DOMAIN", "TRIAL_ARMS_DOMAIN");
		    put("TRIAL_ELEMENTS_DOMAIN", "TRIAL_ELEMENTS_DOMAIN");
		    put("TUMOR_FINDINGS_DOMAIN", "TF");//done
		    put("TRIAL_PATHS_DOMAIN", "TRIAL_PATHS_DOMAIN");
		    put("TRIAL_SUMMARY_DOMAIN", "TRIAL_SUMMARY_DOMAIN");
		    put("TRIAL_STAGES_DOMAIN", "TRIAL_STAGES_DOMAIN");
		    put("TRIAL_SETS_DOMAIN", "TRIAL_SETS_DOMAIN");
		    put("VITAL_SIGNS_DOMAIN", "VS");//done
		}};
	
		
	public static final Set<String> entityMentionsToDelete = Generics.newHashSet(Arrays.asList(new String[]{"NUMBER", "MONEY", "DATE","TIME","TITLE","CAUSE_OF_DEATH","PERSON"}));	
		
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
