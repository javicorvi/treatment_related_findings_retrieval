package es.bsc.inb.etransafe.treatmentfindings.services;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;

import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.MentionsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBEscapingProcessor;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import es.bsc.inb.etransafe.treatmentfindings.model.Domain;
import es.bsc.inb.etransafe.treatmentfindings.model.EtoxSENDTerm;
import es.bsc.inb.etransafe.treatmentfindings.model.Manifestation;
import es.bsc.inb.etransafe.treatmentfindings.model.ToxicityRisk;
import es.bsc.inb.etransafe.treatmentfindings.model.TreatmentRelatedFinding;
import es.bsc.inb.etransafe.treatmentfindings.util.AnnotationUtil;
import es.bsc.inb.etransafe.treatmentfindings.util.PropertiesUtil;
import es.bsc.inb.etransafe.treatmentfindings.util.StopWords;
@Service
class AnnotationServiceImpl implements AnnotationService {

	static final Logger log = Logger.getLogger("log");
	
	Map<String, EtoxSENDTerm> etoxSENDTerms = new HashMap<String, EtoxSENDTerm>();
	@Autowired
	GateService gateService;
	
	// key for matched expressions
	public static class MyMatchedExpressionAnnotation implements CoreAnnotation<List<CoreMap>> {
	    public Class<List<CoreMap>> getType() {
	      return ErasureUtils.<Class<List<CoreMap>>> uncheckedCast(String.class);
	    }
	}

	public void execute(String propertiesParametersPath) {
		try {
			log.info("Classify articles with properties :  " +  propertiesParametersPath);
			Properties propertiesParameters = PropertiesUtil.loadPropertiesParameters(propertiesParametersPath);
			log.info("Classify articles with the model  :  " +  propertiesParameters.getProperty("classificatorModel"));
			log.info("Input directory with the articles to classify : " + propertiesParameters.getProperty("inputDirectory"));
			log.info("Outup directory with the relevant articles : " + propertiesParameters.getProperty("outputDirectory"));
			log.info("Relevant articles label: " + propertiesParameters.getProperty("relevantLabel"));
			log.info("Is sentence classification: " + propertiesParameters.getProperty("is_sentences_classification"));
			
			String classificatorModel = propertiesParameters.getProperty("classificatorModel");
			String inputDirectoryPath = propertiesParameters.getProperty("inputDirectory");
			String outputDirectoryPath = propertiesParameters.getProperty("outputDirectory");
			String relevantLabel = propertiesParameters.getProperty("relevantLabel");
			String is_sentences_classification_ = propertiesParameters.getProperty("is_sentences_classification");
			String isPlainText = propertiesParameters.getProperty("isPlainText");
			String etox_send_dict = propertiesParameters.getProperty("etox_send_dict");
			String etox_anatomy_dict = propertiesParameters.getProperty("etox_anatomy_dict");
			String etox_moa_dict = propertiesParameters.getProperty("etox_moa_dict");
			String etox_in_life_obs_dict = propertiesParameters.getProperty("etox_in_life_obs_dict");
			String generateGATEFormat = propertiesParameters.getProperty("generateGATEFormat");
			String cdi_send_terminology_dict = propertiesParameters.getProperty("cdis_send_terminology_dict");
			
			Boolean is_sentences_classification = false;
			if(is_sentences_classification_!=null & is_sentences_classification_.equals("true")) {
				is_sentences_classification = true;
			}
			
			Boolean generate_gate_format = false;
			if(generateGATEFormat==null || generateGATEFormat.equals("true")) {
				generate_gate_format = true;
			}
			
			//Levantar ColumnDataClassifier
			ByteArrayInputStream bais = new ByteArrayInputStream(FileUtils.readFileToByteArray(new File(classificatorModel)));
		    ObjectInputStream ois = new ObjectInputStream(bais);
		    ColumnDataClassifier cdc = ColumnDataClassifier.getClassifier(ois);
		    ois.close();
			
		    //ColumnDataClassifier cdc = new ColumnDataClassifier(propertiesClassificatorModelParameters);
		    File outputDirectory = new File(outputDirectoryPath);
		    if(!outputDirectory.exists())
		    	outputDirectory.mkdirs();
			
		    List<String> filesProcessed = readFilesProcessed(outputDirectoryPath); 
		    
		    String etox_send_codelist_rules = "etox_send_codelist_rules.txt";
		    String etox_send_codelist_ner = "etox_send_codelist_ner.txt";
		    //generateRulesForTaggingEtoxSEND(etox_send_dict, etox_send_codelist_rules);
		    generateNERGazzetterWithPriority(etox_send_dict, etox_send_codelist_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		    
		    String etox_anatomy_rules = "etox_anatomy_rules.txt";
		    String etox_anatomy_ner = "etox_anatomy_ner.txt";
		    //generateRulesForTaggingEtoxAnatomy(etox_anatomy_dict, etox_anatomy_rules);
		    //generateAnatomyNER(etox_anatomy_dict, etox_anatomy_ner);
		    generateNERGazzetterWithPriority(etox_anatomy_dict, etox_anatomy_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		    
		    String etox_moa_rules = "etox_moa_rules.txt";
		    String etox_moa_ner = "etox_moa_ner.txt";
		    //generateRulesForTaggingEtoxMOA(etox_moa_dict, etox_moa_rules);
		    generateNERGazzetterWithPriority(etox_moa_dict, etox_moa_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX, "MISC", "2.0");
		    
		    String etox_in_life_obs_rules = "etox_in_life_obs_rules.txt";
		    String etox_in_life_obs_ner = "etox_in_life_obs_ner.txt";
		    //generateRulesForTaggingEtoxInLifeObservation(etox_in_life_obs_dict, etox_in_life_obs_rules);
		    //generateInLifeObservationNER(etox_in_life_obs_dict, etox_in_life_obs_ner);
		    generateNERGazzetterWithPriority(etox_in_life_obs_dict, etox_in_life_obs_ner, AnnotationUtil.SOURCE_ETOX_SUFFIX,  "MISC", "15.0");
		    
		    
		    String cdi_send_terminology_rules = "cdis_send_terminology_rules.txt";
		    String cdi_send_terminology_ner = "cdis_send_terminology_ner.txt";
		    generateRulesForTaggingCDISCSEND(cdi_send_terminology_dict, cdi_send_terminology_rules);
		    //generateNERGazzetterWithPriority(cdi_send_terminology_dict, cdi_send_terminology_ner, AnnotationUtil.SOURCE_CDISC_SUFFIX, "MISC", "3.0");
		    
		    
		    
		    Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma,  ner, regexner, entitymentions ");
			//props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment, ner, regexner, entitymentions, coref ");
			/*Basically what you want to do is run the ner annotator, and use it's TokensRegex sub-annotator. Imagine you have some named entity rules in a file called my_ner.rules.
			You could run a command like this:
			java -Xmx5g edu.stanford.nlp.pipeline.StanfordCoreNLP -annotators tokenize,ssplit,pos,lemma,ner -ner.additional.tokensregex.rules my_ner.rules -outputFormat text -file example.txt
			 */
			//regener is for adding tab separated tagger, this is validated in https://nlp.stanford.edu/software/regexner.html
			//props.put("regexner.mapping", "etransafe_rules_manual_curated.txt");
			props.put("regexner.mapping", "etransafe_rules_manual_curated.txt,"+
			etox_anatomy_ner+","+
			etox_in_life_obs_ner+","+
			/*cdi_send_terminology_ner+","+*/
			etox_send_codelist_ner+","+
			etox_moa_ner);
			//props.put("regexner.mapping", "etransafe_rules_manual_curated.txt,"+etox_anatomy_ner);
			//props.put("regexner.mapping", etox_anatomy_ner);
			props.put("regexner.posmatchtype", "MATCH_ALL_TOKENS");
			props.put("rulesFiles", "extended_rules_treatment_related_findings_plain_document.rules," + cdi_send_terminology_rules);
			
//			props.put("rulesFiles", "extended_rules_treatment_related_findings_plain_document.rules,"+etox_send_codelist_rules+","+
//					etox_moa_rules+","+
//					etox_in_life_obs_rules+","+
//					cdi_send_terminology_rules+","+
//					etox_anatomy_rules);
			
			//props.put("rulesFiles", "etox_send_codelist_terms.txt");
			//props.setProperty("tokenize.class", "true");
			
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			
			// set up the TokensRegex pipeline
		    // get the rules files
		    String[] rulesFiles = props.getProperty("rulesFiles").split(",");
		    // set up an environment with reasonable defaults
		    Env env = TokenSequencePattern.getNewEnv();
		    // set to case insensitive
		    /*env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		    env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);*/

		    // build the CoreMapExpressionExtractor
		    CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFiles(env, rulesFiles);
			BufferedWriter filesProcessedWriter = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.dat", true));
			//BufferedWriter filesProcessedWriterGeneralDocumentInformation = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.dat", true));
		    if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
				File inputDirectory = new File(inputDirectoryPath);
				File[] files =  inputDirectory.listFiles();
				for (File file_to_classify : files) {
					if(file_to_classify.getName().endsWith(".txt") && filesProcessed!=null && !filesProcessed.contains(file_to_classify.getName())){
						Boolean result = this.process(file_to_classify, cdc, outputDirectory, relevantLabel, is_sentences_classification, pipeline, extractor,isPlainText.equals("true"), generate_gate_format);
						if(result) {
							filesProcessedWriter.write(file_to_classify.getName()+"\n");
							filesProcessedWriter.flush();
						}
					}
				}
			}
		    filesProcessedWriter.close();
		}  catch (Exception e) {
			log.error("Generic error in the classification step",e);
		}
	}
	
	
	private void generateNERGazzetter(String dictionaryPath, String outPutNerGazetterPath, String sourcePrefix) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(outPutNerGazetterPath));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(dictionaryPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" +  data[1].toUpperCase()+sourcePrefix +"\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	
	private void generateNERGazzetterWithPriority(String dictionaryPath, String outPutNerGazetterPath, String sourcePrefix, String tags_to_overwrite, String priority) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(outPutNerGazetterPath));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(dictionaryPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				if(data[1].toUpperCase().endsWith("TEST CODE")) {
					terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" +  data[1].toUpperCase()+sourcePrefix + "\t" +  tags_to_overwrite + "\t" +  "15.0" +"\n");
				}else {
					terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" +  data[1].toUpperCase()+sourcePrefix + "\t" +  tags_to_overwrite + "\t" +  priority +"\n");
				}
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	private void generateAnatomyNER(String etox_anatomy_dict, String etox_anatomy_ner) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(etox_anatomy_ner));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(etox_anatomy_dict, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" +  data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX +"\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
		
	}

	private void generateRulesForTaggingCDISCSEND(String cdi_send_terminology_dict, String cdi_send_terminology_rules) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(cdi_send_terminology_rules));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(cdi_send_terminology_dict, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				if(!data[1].endsWith("TEST NAME") && !data[1].equals("PK PARAMETERS")) {
					terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toUpperCase()+ AnnotationUtil.SOURCE_CDISC_SUFFIX+"\"}\n");
				}else {
					log.debug("this term was not included into the rule :  " + data);
				}
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}

	private void generateRulesForTaggingEtoxSEND(String inputPath, String rulesPathOutput) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(rulesPathOutput));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(inputPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX+"\"}\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}

	private void generateRulesForTaggingEtoxMOA(String inputPath, String rulesPathOutput) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(rulesPathOutput));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(inputPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX+"\"}\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	private void generateRulesForTaggingEtoxInLifeObservation(String inputPath, String rulesPathOutput) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(rulesPathOutput));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(inputPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" + data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX+"\"}\n");
				
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	
	private void generateInLifeObservationNER(String inputPath, String rulesPathOutput) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(rulesPathOutput));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(inputPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add(getScapedKeyWordNER(data[0].toLowerCase()) + "\t" + data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX +"\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
		
		
	}
	
	
	private void generateRulesForTaggingEtoxAnatomy(String inputPath, String rulesPathOutput) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(rulesPathOutput));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(inputPath, "utf-8")) {
			if(!line.startsWith("KEYWORD")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toUpperCase()+ AnnotationUtil.SOURCE_ETOX_SUFFIX+"\"}\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}
	
	
	private String getScapedKeyWordNER(String keyword) {
		String example ="submandib + % 1.1 - ( $ * [ ] ) { } lan x # ? | javi ";
		PTBEscapingProcessor esc = new PTBEscapingProcessor();
		String keyword_esc = esc.escapeString(keyword);
		/*String char_b = "/";
		String char_e = "/";
		String word_boundary = "\\b";*/
		keyword_esc = keyword_esc.replaceAll("\\/", "\\\\/").
		replaceAll("\\*", "\\\\*").
		replaceAll("\\?", "\\\\?").
		replaceAll("\\+", "\\\\+").
		//replaceAll("\\$", "\\\\$").
		replaceAll("\\|", "\\\\|");
		return keyword_esc;
	}
	
	private String getScapedKeyWord(String keyword) {
		//String example ="potassium/creatinine \\ ejemplo + - * \b [] % ( pepe ))";
		String char_b = "/";
		String char_e = "/";
		String word_boundary = "\\b";
		keyword = keyword.replaceAll("\\/", "\\\\/").
		replaceAll("\\(", "\\\\(").
		replaceAll("\\)", "\\\\)").
		replaceAll("\\[", "\\\\[").
		replaceAll("\\]", "\\\\]").
		replaceAll("\\{", "\\\\{").
		replaceAll("\\}", "\\\\}").
		replaceAll("\\*", "\\\\*").
		replaceAll("\\-", "\\\\-").
		replaceAll("\\^", "\\\\^").
		replaceAll("\\.", "\\\\.").
		replaceAll("\\?", "\\\\?").
		replaceAll("\\+", "\\\\+").
		replaceAll("\\%", "\\\\%").
		replaceAll("\\$", "\\\\$").
		replaceAll("\\$", "\\\\$").
		replaceAll("\\|", "\\\\|");
		//scape all special characters, it cannot be used \Q and \E because of a bug in the Standford NLP Core library, 
		// the / is not scaped if its arounded  \Q \E by the framework because is used to delimited the begin and end of a string.
		//So we have to scape all the special characters with \, for example \/ \( \) etc.
		
		return char_b + word_boundary+ keyword + word_boundary + char_e;
	}

	private List<String> readFilesProcessed(String outputDirectoryPath) {
		List<String> files_processed = new ArrayList<String>();
		try {
			if(java.nio.file.Files.isRegularFile(Paths.get(outputDirectoryPath + File.separator + "list_files_processed.dat"))) {
				FileReader fr = new FileReader(outputDirectoryPath + File.separator + "list_files_processed.dat");
			    BufferedReader br = new BufferedReader(fr);
			    
			    String sCurrentLine;
			    while ((sCurrentLine = br.readLine()) != null) {
			    	files_processed.add(sCurrentLine);
				}
			    br.close();
			    fr.close();
			    return files_processed;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files_processed;
	}
	
	/**
	 * Process of report. 
	 * @param file_to_classify
	 */
	 private Boolean process(File file_to_classify,  ColumnDataClassifier cdc , File outputDirectory, String relevantLabel, Boolean is_sentences_classification, StanfordCoreNLP pipeline, CoreMapExpressionExtractor extractor, Boolean isPlainText,Boolean generate_gate_format) {
		 long startTime = System.currentTimeMillis();
		 String outputFile = outputDirectory.getAbsoluteFile() + File.separator + file_to_classify.getName();
		 String outputFileSentences = outputDirectory.getAbsoluteFile() + File.separator + file_to_classify.getName();
		 File fout = new File(outputFile);
		 FileOutputStream fos;
		 File foutSentences = new File(outputFileSentences);
		 FileOutputStream fosSentences;
		 try {
			 fos = new FileOutputStream(fout);
			 fosSentences = new FileOutputStream(foutSentences);
			 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			 BufferedWriter bwTreatmentRelatedFindingSentences = new BufferedWriter(new OutputStreamWriter(fosSentences));
			 log.info(" File to process " + file_to_classify.getAbsolutePath());
			 if(!isPlainText) {
				 for (String line : ObjectBank.getLineIterator(file_to_classify.getAbsolutePath(), "UTF-8")) {
					 try {
						 	String[] data = line.split("\t");
							 String line_to_classify = "";
							 String id = data[0];
							 String source = data[1];
							 String block_name = data[2];
							 if(is_sentences_classification) {
								 line_to_classify = "\t" + data[0] + "\t" + data[3];
							 }else {
								 //para abstracts
								 line_to_classify = "\t" + data[0] + "\t \t" + data[4]; 
							 }
							 Datum<String,String> d = cdc.makeDatumFromLine(line_to_classify);
							 if(is_sentences_classification) {
								 bw.write(cdc.classOf(d) + "\t" + cdc.scoresOf(d).getCount(cdc.classOf(d)) + "\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\n");
							 }else {
								 bw.write(cdc.classOf(d) + "\t" + cdc.scoresOf(d).getCount(cdc.classOf(d)) + "\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\t" + data [4] + "\n");
								 tagging(pipeline, id, data [4], file_to_classify.getName(), bw, extractor);
							 }
							 bw.newLine();
					}catch (ArrayIndexOutOfBoundsException e) {
						log.error(" Error with line: "+ line );
						log.error(" Error with line, in file: " + file_to_classify.getAbsolutePath() + " there is not the correct columns for the line ", e);
					}catch (Exception e) {
						log.error(" Error with line: "+ line );
						log.error(" Error with line, in file: " + file_to_classify.getAbsolutePath(), e);
					} 
				 }	 
			 } else {
				 String id = file_to_classify.getName().substring(0, file_to_classify.getName().indexOf('.'));
				 //tagging(pipeline, id, readFile(file_to_classify.getAbsolutePath()), file_to_classify.getName(), bw, extractor, isPlainText);
				 //tagging2(pipeline, id, readFile(file_to_classify.getAbsolutePath()), file_to_classify.getName(), bw, extractor, isPlainText);
				 tagging2(pipeline, id, IOUtils.slurpFile(file_to_classify.getAbsolutePath(), "UTF-8"), file_to_classify.getName(), bw, extractor, isPlainText, bwTreatmentRelatedFindingSentences);
			 }
			 
			 if(generate_gate_format) {
				 String inputGateFile = file_to_classify.getAbsolutePath().replace("PLAIN", "GATE").replace(".txt", ".xml");
				 String outputGATE_File = outputFile.replace("PLAIN", "GATE_ANNOTATED").replace(".txt", ".xml");
				 gateService.generateGateFormat(inputGateFile, outputFile ,outputGATE_File);
			 }
			 
			 
			 bw.close(); 
			 long endTime = System.currentTimeMillis();
			 log.info(" The execution time  " + (endTime - startTime) + " milliseconds");
			 return true;
		} catch (FileNotFoundException e) {
			log.error(" File not Found " + file_to_classify.getAbsolutePath(), e);
			return false;
		} catch (IOException e) {
			log.error(" IOException " + file_to_classify.getAbsolutePath(), e);
			return false;
		}
	 }
	 
	 	/**
		 * Findings of LTKB ChemicalCompunds
		 * 
		 * @param sourceId
		 * @param document_model
		 * @param first_finding_on_document
		 * @param section
		 * @param sentence_text
		 * @return
		 * @throws MoreThanOneEntityException
		 */
		private void tagging(StanfordCoreNLP pipeline, String id, String text_to_tag, String fileName, BufferedWriter bw, CoreMapExpressionExtractor extractor) {
			//String text = "tyrosine protein kinase abl family subcutaneously, in the neck region potassium & creatinine \\ ejemplo + - * [] % ( pepe )) skin, hair loss, head $ \b body-weight pepepe p<0.05 or p  > 0.05, treatment related finding increase liver toxicity Urine protein/creatinine ratio (Prot-U/Cre)";
			//String text = "necropsy findings no compound related macroscopic findings were observed in treated male and female rats neither sacrificed at the end of the 4-week treatment period nor after the 4-week recovery period.organ weights compound-related decreases in the absolute and or relative organ of prostate and seminal vesicles weights were present in animals treated with 100 mg/kg/day bay 85-3474 (and higher) when compared to controls.";
			long startTime = System.currentTimeMillis();
			Annotation document = new Annotation(text_to_tag.toLowerCase());
			//Annotation document = new Annotation(text.toLowerCase());
			//run all Annotators on this text
			pipeline.annotate(document);
			long endTime = System.currentTimeMillis();
			log.info(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	        for(CoreMap sentence: sentences) {
	        	TreatmentRelatedFinding finding = new TreatmentRelatedFinding();
	        	finding.setStudyId("ID_999");
	        	finding.setDomain("SR");
	        	Boolean lowLevelAnalisys =false;
	        	//List<CoreMap> entityMentions = sentence.get(MentionsAnnotation.class);
	    		try {
	    			bw.write(sentence.get(TextAnnotation.class)+ "\n");
	        		/*for (CoreMap entityMention : entityMentions) {
		    			String keyword = entityMention.get(TextAnnotation.class);
		        		String entityType = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
		        		CoreLabel token = entityMention.get(TokensAnnotation.class).get(0);
		        		bw.write(id + "\t"+ token.beginPosition() + "\t" + (token.beginPosition() + keyword.length())  + "\t" + keyword + "\t" + entityType + "\n");
					    if(entityType!=null && entityType.endsWith("_DOMAIN")) {
					    	lowLevelAnalisys=true;
		        		}
					}*/
	    			List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);
			    	// print out the matched expressions
			        for (MatchedExpression me : matchedExpressions) {
			        	if(me.getText().length()< 4 && (me.getValue().get().equals("lbtest_etox_send") || me.getValue().get().equals("moa_etox_send"))) {
			        		break;
			        	}
			        	bw.write(id + "\t"+ (me.getCharOffsets().getBegin()+1) + "\t" + me.getCharOffsets().getEnd() + "\t" + me.getText() + "\t" + me.getValue().get() + "\n");
						if(me.getValue().get().equals("TREATMENT_RELATED_FINDING")) {
							finding.setRecordFounded(true);
							lowLevelAnalisys=true;
						} 
						
						retrieveDomainOfStudy(finding, me);
						//retrieveRiskOfFinding(finding, me);
						
						if(me.getValue().get().equals("lbtest_etox_send")) {
							finding.setTestShortName(me.getText());
						}else if(me.getValue().get().equals("pkparm_etox_send")) {
							finding.setPkParameters(me.getText());
						}else if(me.getValue().get().equals("strain_etox_send")) {
							finding.setStrainOfTest(me.getText());
						}else if(me.getValue().get().equals("route_etox_send")) {
							finding.setRouteOfAdministration(me.getText());
						}
						
						// statical significance retrieval
						if(me.getValue().get().equals("STATICAL_SIGNIFICANCE")) {
							finding.setStatisticalSignificanceOfFinding(me.getText());
						}
						// dosis retrieval
						else if(me.getValue().get().equals("COMPLETE_DOSIS") || me.getValue().get().equals("DOSIS")) {
							finding.setDosis(me.getText());
						}
						// study of day of finding retrieval
						else if(me.getValue().get().equals("DURATION_DOSIS")) {
							finding.setStudyDayOfFinding(me.getText());
						}
						//sex retrieval
						else if(me.getValue().get().toString().endsWith("_SEX")) {
							if(me.getValue().get().equals("MALE_SEX")) {
								finding.setSex('M');
							}else if(me.getValue().get().equals("FEMALE_SEX")) {
								finding.setSex('F');
							}
						}else if(me.getValue().get().toString().contains("_MANIFESTATION_FINDING")) {
							//retrieveManifestationOfFinding(finding, me);
						} 
					}
			        
			        //More analisys in the sentence
			        if(lowLevelAnalisys) {
			        	//this.sentenceAnalisys(sentence, bw);
			        }
			        
			        if(finding.getRecordFounded()) {
		    			bw.write(finding.toString());
		    		}
			        
			        bw.flush();
	    		} catch (IOException e) {
					log.error("IOError Exception " , e);
				}
	    	}
		}

		/**
		 * Findings of LTKB ChemicalCompunds
		 * 
		 * @param sourceId
		 * @param document_model
		 * @param first_finding_on_document
		 * @param section
		 * @param sentence_text
		 * @return
		 * @throws MoreThanOneEntityException
		 */
		private void tagging2(StanfordCoreNLP pipeline, String id, String text_to_tag, String fileName, BufferedWriter bw, CoreMapExpressionExtractor extractor, Boolean isPlainText, BufferedWriter bwTreatmentRelatedFindingSentences) {
			//String text = "tyrosine protein kinase abl family subcutaneously, in the neck region potassium & creatinine \\ ejemplo + - * [] % ( pepe )) skin, hair loss, head $ \b body-weight pepepe p<0.05 or p  > 0.05, treatment related finding increase liver toxicity Urine protein/creatinine ratio (Prot-U/Cre)";
			//String text = " * - [ ] ^ glucose (uglu body-weight-gain  xxx xxxx treatment related finding both pinnae reddened  post dose";
			//String text = " = p = harry ** =p<001 test 5 to 300 mg/kg -11 % ( p  -0.01 ) and -9 % ( p < 0.05) test -11 % (p < 0.01)";
			//String text = "* (p < 0.05) or ** (p < 0.01) peppepepepe 5 to 300 mg/kg pepepepe 87 mg  pepeep ** (p < 0.01)   ffff  10, 30, and 100 mg/kg lallal pulmonary inspection lalalla related effect found javier  compound-related effect";
			//String text = "Neither the distribution nor the morphological appearance give any conclusions as to these being treatment-related.";
			//String text = "+6%, p<0.05 and +31%, p<0.01";
			//String text = "14 % pepelindo salivary glandulaf - parotid right submandib + % 1.1 - ( $ * [ ] ) { } ^ lan x # ? | javi pepelindo anatomia treatment related findings 6 pepito treatment related findings  fdsfdsf Median Fluorescence Intensity no The comparison of mean values treatment-related of absolute and relative organ weights of the test article treated animals to the corresponding values not pipo treatment-related of the control did not reveal a clear-cut statistically significant compound-related effect.";
			//String text = "fdgfdg liver/gall bladder  dfdsf pterygoid process of sphenoid bone  rr rer e parathyroid glans dfdsfdsf liver (excluding gall bladder) pepepep tibia discharge red - mouth gfdgfdgfdgfd treatment related findings";
			//String text = "group III Clinical Signs pappepe Accumulation Index using Lambda z pepepe food consumption pepepeppe 0.2, 2.0 mg/kg pepepepep  0.2, 2.0 and 20 mg/kg Group Abnormal Cells mean values which differ significantly systolic blood pressure from the control group are marked in Appendix I and in the group ab123, and of course group 1234 did not ocurr in group also. ";
			/*String text =  "ORGAN WEIGHTS Individual absolute and relative (related to 100 g body weight) organ weights as well as the corresponding group means with "
					+ " statistical information are given in Part 2 of this report. The results are presented as group means in Tables 20 and 21. Among the relative organ weights significantly "
					+ "increased means were calculated for the heart (+12 to 14%) at 200 mg/kg as well as for the kidneys at 40 mg/kg (+11 to 15%) and 200 mg/kg (+39 to 61%) each in both sexes."
					+ " The liver weights were significantly elevated in males (relative: +13%) and females (absolute: +17% and relative: +35%) at 200 mg/kg."
					+ " The significantly elevated mean relative liver weight at 40 mg/kg in females are of no toxicological relevance, since the difference to the control value is below 10%. Other as "
					+ "statistically significant marked organ weight means such as for the brain, adrenals, spleen, thymus, testes and ovaries in group 200 mg/kg are considered to "
					+ "be due to differences in the body weights and are of no toxicological relevance.  The Table 22 lists all histopathological findings considered to be treatment-related."
					+ " Up to the dose of 40 mg/kg there were no toxicologically relevant morphological lesions considered to be a consequence of the treatment. At the dose of 200 mg/kg the "
					+ "following treatment-related findings were observed: Heart -Minimal myocardial fibrosis in one male and one female receiving 200 mg/kg."
					+ " -An increase in myocardial mononuclear cell infiltration in both sexes of this group.";*/
			long startTime = System.currentTimeMillis();
			Annotation document = new Annotation(text_to_tag.toLowerCase());
			List<String> previousSentencences = new ArrayList<String>();
			//Annotation document = new Annotation(text.toLowerCase());
			
			pipeline.annotate(document);
			long endTime = System.currentTimeMillis();
			log.info(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
	        try {	
	        	List<CoreLabel> tokens= document.get(TokensAnnotation.class);
			    List<CoreMap> sentences= document.get(SentencesAnnotation.class);
			    Map<String, TreatmentRelatedFinding> treatmentRelatedFindings = new HashMap<String, TreatmentRelatedFinding>();
			    bw.write(id + "\t0\t0\t" + tokens.size() + "\t" + AnnotationUtil.TOKENS + "\t" + AnnotationUtil.STANDFORD_CORE_NLP_SOURCE + "\t \n");
			    bw.write(id + "\t0\t0\t" + sentences.size() + "\t" + AnnotationUtil.SENTENCES+ "\t" + AnnotationUtil.STANDFORD_CORE_NLP_SOURCE + "\t \n");
	        	for(CoreMap sentence: sentences) {
			    	//previousSentencences.add(sentence.toString());
			        Integer sentenceBegin = sentence.get(CharacterOffsetBeginAnnotation.class);
			        Integer sentenceEnd = sentence.get(CharacterOffsetEndAnnotation.class);
			        //List<CoreMap> entityMentions2 = sentence.get(MentionsAnnotation.class);
			        //List<CoreLabel> tokens2= sentence.get(TokensAnnotation.class);
			        /*
			        Tree tree = sentence.get(TreeAnnotation.class);
			        log.info(tree);
			        SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			        log.info(dependencies);
			        Map<Integer, CorefChain> graph =  document.get(CorefChainAnnotation.class);
			        log.info(graph);*/
			        
			        //"Very negative" = 0 "Negative" = 1 "Neutral" = 2 "Positive" = 3 "Very positive" = 4
			        //Tree tree_sentiment = sentence.get(SentimentAnnotatedTree.class);
			        //int sentiment = 0;
			        //int sentiment = RNNCoreAnnotations.getPredictedClass(tree_sentiment);
			        bw.write(id + "\t"+ sentenceBegin + "\t" + sentenceEnd + "\t \t" + AnnotationUtil.SENTENCES_TEXT + "\t" +  AnnotationUtil.STANDFORD_CORE_NLP_SOURCE + "\t \n");
			        TreatmentRelatedFinding  treatmentRelatedFinding = new TreatmentRelatedFinding();
			        String sentence_key = sentenceBegin + "_" + sentenceEnd;
			        treatmentRelatedFinding.setSentence_key(sentence_key);
					treatmentRelatedFindings.put(sentence_key, treatmentRelatedFinding);
					List<CoreMap> entityMentions = sentence.get(MentionsAnnotation.class);
			        for (CoreMap entityMention : entityMentions) {
			        	String term = entityMention.get(TextAnnotation.class).replaceAll("\n", " ");
			        	String label = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
			        	if(!StopWords.stopWordsEn.contains(term) && !AnnotationUtil.entityMentionsToDelete.contains(label)) {
			        		Integer termBegin = entityMention.get(CharacterOffsetBeginAnnotation.class);
			        		Integer termEnd = entityMention.get(CharacterOffsetEndAnnotation.class);
				        	annotate(id, bw, sentence, sentenceBegin, sentenceEnd, termBegin, termEnd, term, label, "dictionary", bwTreatmentRelatedFindingSentences, treatmentRelatedFindings, treatmentRelatedFinding);
			        	}
			        }
			        List<MatchedExpression> matchedExpressionssentence = extractor.extractExpressions(sentence);
			        for (MatchedExpression me : matchedExpressionssentence) {
			        	String term = me.getText().replaceAll("\n", " ");
			        	if(!StopWords.stopWordsEn.contains(term)) {
			        		Integer termBegin = me.getAnnotation().get(CharacterOffsetBeginAnnotation.class);
					       	Integer termEnd = me.getAnnotation().get(CharacterOffsetEndAnnotation.class);
			        		String label = me.getValue().get().toString().toUpperCase();
			        		annotate(id, bw, sentence, sentenceBegin, sentenceEnd, termBegin, termEnd, term, label, "rule", bwTreatmentRelatedFindingSentences, treatmentRelatedFindings, treatmentRelatedFinding);
			        	}
			        }
			    }
			     
			    bw.flush();
	    	} catch (IOException e) {
				log.error("TaggerServiceImpl :: tagging2 :: IOException ", e);
			}
	    }

		/**
		 * Annotate the information retrieved from the NER and from the Rules.
		 * @param id
		 * @param bw
		 * @param sentence
		 * @param sentenceBegin
		 * @param sentenceEnd
		 * @param meBegin
		 * @param meEnd
		 * @param term
		 * @param label
		 * @throws IOException
		 */
		private void annotate(String id, BufferedWriter bw, CoreMap sentence, Integer sentenceBegin, Integer sentenceEnd, int meBegin, int meEnd, 
				String term, String label, String annotationMethod, BufferedWriter bwTreatmentRelatedFindingSentences, Map<String, TreatmentRelatedFinding> treatmentRelatedFindings, TreatmentRelatedFinding treatmentRelatedFinding) throws IOException {
			String source="";
			String sentence_key = sentenceBegin + "_" + sentenceEnd;
			String sentenceText =  sentence.toString().replaceAll("[\\n\\t ]", " ");
			if(label.endsWith(AnnotationUtil.SOURCE_ETOX_SUFFIX)) {
				source = AnnotationUtil.SOURCE_ETOX;
				label = label.replaceAll(AnnotationUtil.SOURCE_ETOX_SUFFIX, "");
			}else if(label.endsWith(AnnotationUtil.SOURCE_CDISC_SUFFIX)){
				source = AnnotationUtil.SOURCE_CDISC;
				label = label.replaceAll(AnnotationUtil.SOURCE_CDISC_SUFFIX, "");
			}else {
				source =  AnnotationUtil.SOURCE_MANUAL;
			}
			//Una vez que la sentencia fue hallada como no treatmente related, no analizarla nuevamente.
			if(label.equals(AnnotationUtil.TREATMENT_RELATED_EFFECT_DETECTED)){
				bw.write(id + "\t"+ sentenceBegin + "\t" + sentenceEnd + "\t \t" + label + AnnotationUtil.SENTENCE_SUFFIX + "\t" + source + "\t" + annotationMethod + "\n");
				bw.write(id + "\t"+ meBegin + "\t" + meEnd + "\t" + term + "\t" + label + "\t" + source + "\t" + annotationMethod + "\n");
				bwTreatmentRelatedFindingSentences.write("TREATMENT_RELATED_FINDING_SENTENCE\t"+sentenceText);
				//if already is negative for now dont do anything.
				/*if(!treatmentRelatedFinding.getIsTreatmentRelated().equals('N')) {
					treatmentRelatedFinding.setIsTreatmentRelated('Y');
				}*/
			}else if(label.equals(AnnotationUtil.NO_TREATMENT_RELATED_EFFECT_DETECTED)){
				//output sentences to other file for trainning
				bw.write(id + "\t"+ sentenceBegin + "\t" + sentenceEnd + "\t \t" + label + AnnotationUtil.SENTENCE_SUFFIX + "\t" + source + "\t" + annotationMethod + "\n");
				bw.write(id + "\t"+ meBegin + "\t" + meEnd + "\t"   + term + "\t" + label + "\t" + source + "\t" + annotationMethod + "\n");
				treatmentRelatedFinding.setIsTreatmentRelated('N');
			}else if(label.contains(AnnotationUtil.STUDY_DOMAIN_SUFFIX)){
				bw.write(id + "\t"+ meBegin + "\t" + meEnd + "\t" + term + "\t" + label + "\t" + source + "\t" + annotationMethod + "\n");
				Domain study_domain_send_code = AnnotationUtil.SEND_DOMAIN_DESC_TO_SEND_DOMAIN_CODE.get(label);
				treatmentRelatedFinding.setDomainOfFinding(study_domain_send_code);
			}else {
				//the others modificar me.getvalue y ya setear todo mayuscula y constantes
			   	if(!(term.length()< 4 && ( label.equals("SEXPOP_ETOX_SEND") || 
			   			label.equals("LBTEST_ETOX_SEND") ||  label.equals("LBTEST") ||
			   			label.contains("TEST CODE") || 
			   			label.contains("TEST NAME") ||
			   			label.contains("NEOPLASM TYPE") ||
			   			label.equals("ROUTE OF ADMINISTRATION") || 
			   			label.equals("MOA_ETOX_SEND") || 
			   			label.equals("STRAIN_ETOX_SEND")))) {
			    		bw.write(id + "\t"+ meBegin + "\t" + meEnd + "\t" + term + "\t" + label + "\t" + source + "\t" + annotationMethod + "\n");
			    		
			    		if(label.endsWith(AnnotationUtil.RISK_LEVEL)) {
			    			this.retrieveRiskOfFinding(treatmentRelatedFinding, label);
			    		}else if(label.endsWith(AnnotationUtil.MANIFESTATION_OF_FINDING)){
			    			this.retrieveManifestationOfFinding(treatmentRelatedFinding, label);
			    		}else if(label.endsWith("_SEX")) {
							if(label.equals("MALE_SEX")) {
								treatmentRelatedFinding.setSex('M');
							}else if(label.equals("FEMALE_SEX")) {
								treatmentRelatedFinding.setSex('F');
							}
						}else if(label.endsWith("TEST CODE")) {
							String send_test_code = label.substring(label.indexOf('_')+1);
							send_test_code = send_test_code.substring(0, send_test_code.indexOf('_'));
							if(!label.endsWith("PHYSICAL PROPERTIES TEST CODE")) {
								treatmentRelatedFinding.setTestShortName(send_test_code);
							}
							
						}
			    }else {
			    	log.debug("Not tagged : " + term + " label : " + label);
			    }
			}
		}
		
		
	/**
	 * 	Manifestation of finding association
	 * @param finding
	 * @param me
	 */
	private void retrieveManifestationOfFinding(TreatmentRelatedFinding finding, String label) {
		if(label.equals("INCREASE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.INCREASE);
		}else if(label.equals("DECREASE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.DECREASE);
		}else if(label.equals("TRANSITORY_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.TRANSITORY);
		}else if(label.equals("REVERSIBLE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.REVERSIBLE);
		}else if(label.equals("JUSTPRESENT_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.PRESENT);
		}
	}
	/**
	 * Risk level association
	 * @param finding
	 * @param me
	 */
	private void retrieveRiskOfFinding(TreatmentRelatedFinding finding, String label) {
		if(label.equals("NOEL_RISK_LEVEL")) {
			finding.setRisk(ToxicityRisk.NOEL);
		}else if(label.equals("LOEL_RISK_LEVEL")) {
			finding.setRisk(ToxicityRisk.LOEL);
		}else if(label.equals("NOAEL_RISK_LEVEL")) {
			finding.setRisk(ToxicityRisk.NOAEL);
		}else if(label.equals("LOAEL_RISK_LEVEL")) {
			finding.setRisk(ToxicityRisk.NOAEL);
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	protected String readFile(String file_path) throws IOException {
		try{
			if(java.nio.file.Files.isRegularFile(Paths.get(file_path))) {
				BufferedReader br = new BufferedReader(
						   new InputStreamReader(
				                      new FileInputStream(file_path), "UTF-8"));
			
			    StringBuilder textBuilder = new StringBuilder();
			    String line;
			    while ((line=br.readLine())!=null) {
			    	textBuilder.append(line);
			    }
			    br.close();
			    System.out.println(textBuilder.toString());
			   
			    
			    
			     String outputFile = file_path+ "_dummy.txt";
				 File fout = new File(outputFile);
				 FileOutputStream fos;
				 fos = new FileOutputStream(fout);
				 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
				 bw.write(textBuilder.toString());
			     bw.close();
			    return textBuilder.toString();
			    
			}  else {
				
			}
	    }catch(IOException ex){
	       ex.printStackTrace();   
	    }
		return null;
	}
	
	/**
	 * Retrieve Domain of finding	
	 * @param finding
	 * @param me
	 */
	private void retrieveDomainOfStudy(TreatmentRelatedFinding finding, MatchedExpression me) {
		if(me.getValue().get().equals("BODY_WEIGHT_DOMAIN")) {
			finding.setDomainOfFinding(Domain.BW);
		}else if(me.getValue().get().equals("BODY_WEIGHT_GAIN_DOMAIN")) {
			finding.setDomainOfFinding(Domain.BG);
		}else if(me.getValue().get().equals("CLINICAL_DOMAIN")) {
			finding.setDomainOfFinding(Domain.CL);
		}else if(me.getValue().get().equals("CARDIOVASCULAR_DOMAIN")) {
			finding.setDomainOfFinding(Domain.CV);
		}else if(me.getValue().get().equals("MACROSCOPIC_FINDINGS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.MA);
		}else if(me.getValue().get().equals("MICROSCOPIC_FINDINGS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.MI);
		}else if(me.getValue().get().equals("ORGAN_MEASUREMENT_DOMAIN")) {
			finding.setDomainOfFinding(Domain.OM);
		}else if(me.getValue().get().equals("PHARMACOKINETICS_PARAMETERS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.PP);
		}else if(me.getValue().get().equals("TUMOR_FINDINGS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.TF);
		}else if(me.getValue().get().equals("RESPIRATORY_FINDINGS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.RE);
		}else if(me.getValue().get().equals("DEATH_DIAGNOSIS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.DD);
		}else if(me.getValue().get().equals("FOOD_WATER_CONSUMPTION_DOMAIN")) {
			finding.setDomainOfFinding(Domain.FW);
		}else if(me.getValue().get().equals("ECG_DOMAIN")) {
			finding.setDomainOfFinding(Domain.EG);
		}else if(me.getValue().get().equals("LABORATORY_FINDINGS_DOMAIN")) {
			finding.setDomainOfFinding(Domain.LB);
		}else if(me.getValue().get().equals("VITAL_SIGNS_DOMcountAnnotationsAIN")) {
			finding.setDomainOfFinding(Domain.VS);
		}
	}

		/**
		 * 
		 * @param sentence
		 * @throws IOException 
		 */
		private  void sentenceAnalisys(CoreMap sentence,BufferedWriter bw) throws IOException {
			List<CoreLabel> tokens= sentence.get(TokensAnnotation.class);
			for (CoreLabel token: tokens){
				String word = token.get(TextAnnotation.class);
				String pos = token.get(PartOfSpeechAnnotation.class);
				String ner = token.get(NamedEntityTagAnnotation.class);
				String lemma = token.get(LemmaAnnotation.class);
				bw.write(word + "\t" + token.beginPosition() + "\t" + token.endPosition() + "\t" + pos + "\t" + ner + "\t" + lemma + "\n");
			}
			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			bw.write(tree+"\n");
			/*for (Tree subTree : tree.children()) {
		        System.err.println(subTree.label());
		    }*/
			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
			bw.write(dependencies+"\n");
			Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
		    // Print the triples
		    for (RelationTriple triple : triples) {
		    	 bw.write(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss() + "\n");
		    }
			int mainSentiment = 0;
			int longest = 0;
			Tree tree_sentiment = sentence.get(SentimentAnnotatedTree.class);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree_sentiment);
            String partText = sentence.toString();
            if (partText.length() > longest) {
                mainSentiment = sentiment;
                longest = partText.length();
            }
            bw.write("Sentiment : "+ toCss(mainSentiment) + "\n");
        }	
	
		private String toCss(int sentiment) {
	        switch (sentiment) {
	        case 0:
	            return "very negative";
	        case 1:
	            return "negative";
	        case 2:
	            return "neutral";
	        case 3:
	            return "positive";
	        case 4:
	            return "very positive";
	        default:
	            return "default";
	        }
		}
		
		
	/**
	  * Load Properties
	  * @param properitesParametersPath
	  */
	 public Properties loadPropertiesParameters(String properitesParametersPath) {
		 Properties prop = new Properties();
		 InputStream input = null;
		 try {
			 input = new FileInputStream(properitesParametersPath);
			 // load a properties file
			 prop.load(input);
			 return prop;
		 } catch (IOException ex) {
			 ex.printStackTrace();
		 } finally {
			 if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			 }
		}
		return null;
	 }

}
