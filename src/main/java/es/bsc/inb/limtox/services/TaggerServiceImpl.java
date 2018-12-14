package es.bsc.inb.limtox.services;


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
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotation;
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
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import es.bsc.inb.limtox.model.Domain;
import es.bsc.inb.limtox.model.EtoxSENDTerm;
import es.bsc.inb.limtox.model.Manifestation;
import es.bsc.inb.limtox.model.ToxicityRisk;
import es.bsc.inb.limtox.model.TreatmentRelatedFinding;
@Service
class TaggerServiceImpl implements TaggerService {

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
			Properties propertiesParameters = this.loadPropertiesParameters(propertiesParametersPath);
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
		    generateRulesForTaggingEtoxSEND(etox_send_dict, etox_send_codelist_rules);
		    
		    String etox_anatomy_rules = "etox_anatomy_rules.txt";
		    generateRulesForTaggingEtoxAnatomy(etox_anatomy_dict, etox_anatomy_rules);
		    
		    
		    String etox_moa_rules = "etox_moa_rules.txt";
		    generateRulesForTaggingEtoxMOA(etox_moa_dict, etox_moa_rules);
		    
		    String etox_in_life_obs_rules = "etox_in_life_obs_rules.txt";
		    generateRulesForTaggingEtoxInLifeObservation(etox_in_life_obs_dict, etox_in_life_obs_rules);
		    
		    String cdi_send_terminology_rules = "cdis_send_terminology_rules.txt";
		    generateRulesForTaggingCDISEND(cdi_send_terminology_dict, cdi_send_terminology_rules);
		    
		    
		    Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, entitymentions ");
			
			
			//regener is for adding tab separated tagger
			props.put("regexner.mapping", "etransafe_rules_manual_curated.txt");
			props.put("regexner.posmatchtype", "MATCH_ALL_TOKENS");
			
			
			//props.put("rulesFiles", "extended_rules_treatment_related_findings.rules");
			
			props.put("rulesFiles", "extended_rules_treatment_related_findings_plain_document.rules,"+etox_send_codelist_rules+","+
					etox_anatomy_rules+","+
					etox_moa_rules+","+
					etox_in_life_obs_rules);
			
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
			BufferedWriter filesPrecessedWriter = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.dat", true));
		    if (java.nio.file.Files.isDirectory(Paths.get(inputDirectoryPath))) {
				File inputDirectory = new File(inputDirectoryPath);
				File[] files =  inputDirectory.listFiles();
				for (File file_to_classify : files) {
					if(file_to_classify.getName().endsWith(".txt") && filesProcessed!=null && !filesProcessed.contains(file_to_classify.getName())){
						Boolean result = this.process(file_to_classify, cdc, outputDirectory, relevantLabel, is_sentences_classification, pipeline, extractor,isPlainText.equals("true"), generate_gate_format);
						if(result) {
							filesPrecessedWriter.write(file_to_classify.getName()+"\n");
							filesPrecessedWriter.flush();
						}
					}
				}
			}
		    filesPrecessedWriter.close();
		}  catch (Exception e) {
			log.error("Generic error in the classification step",e);
		}
	}

	private void generateRulesForTaggingCDISEND(String cdi_send_terminology_dict, String cdi_send_terminology_rules) throws IOException {
		BufferedWriter termWriter = new BufferedWriter(new FileWriter(cdi_send_terminology_rules));
		Set<String> terms = new HashSet<String>();
		for (String line : ObjectBank.getLineIterator(cdi_send_terminology_dict, "utf-8")) {
			if(!line.startsWith("keyword")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toLowerCase()+"_cdis_send\"}\n");
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
			if(!line.startsWith("keyword")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toLowerCase()+"_etox_send\"}\n");
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
			if(!line.startsWith("keyword")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toLowerCase()+"_etox_send\"}\n");
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
			if(!line.startsWith("keyword")) {
				String[] data = line.split("\t");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toLowerCase()+"_etox_send\"}\n");
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
			if(!line.startsWith("keyword")) {
				String[] data = line.split("\t");
				//terms.add("{ ruleType: \"text\", pattern: /\\Q" + getScapedKeyWord(data[0].toLowerCase()) + "\\E/, result:  \"" +  data[1].toLowerCase()+"_etox_send\"}\n");
				terms.add("{ ruleType: \"text\", pattern: " + getScapedKeyWord(data[0].toLowerCase()) + ", result:  \"" +  data[1].toLowerCase()+"_etox_send\"}\n");
			}
		}
		for (String string : terms) {
			termWriter.write(string);
			termWriter.flush();
		}
		termWriter.close();
	}


//	private String getScapedKeyWord(String keyword) {
//		//String example ="potassium/creatinine \\ ejemplo + - * \b [] % ( pepe ))";
//		String char_b = "/";
//		String char_e = "/";
//		keyword = keyword.replaceAll("\\/", "\\\\/").
//		replaceAll("\\(", "\\\\(").
//		replaceAll("\\)", "\\\\)").
//		replaceAll("\\[", "\\\\[").
//		replaceAll("\\]", "\\\\]").
//		replaceAll("\\{", "\\\\{").
//		replaceAll("\\}", "\\\\}").
//		replaceAll("\\*", "\\\\*").
//		replaceAll("\\-", "\\\\-").
//		replaceAll("\\^", "\\\\^").
//		replaceAll("\\.", "\\\\.").
//		replaceAll("\\?", "\\\\?").
//		replaceAll("\\+", "\\\\+").
//		replaceAll("\\%", "\\\\%").
//		replaceAll("\\$", "\\\\$").
//		replaceAll("\\$", "\\\\$").
//		replaceAll("\\|", "\\\\|");
//		//scape all special characters, it cannot be used \Q and \E because of a bug in the Standford NLP Core library, 
//		// the / is not scaped if its arounded  \Q \E by the framework because is used to delimited the begin and end of a string.
//		//So we have to scape all the special characters with \, for example \/ \( \) etc.
//		
//		return char_b +  keyword + char_e;
//	}
	
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
	 * Classify 
	 * @param file_to_classify
	 */
	 private Boolean process(File file_to_classify,  ColumnDataClassifier cdc , File outputDirectory, String relevantLabel, Boolean is_sentences_classification, StanfordCoreNLP pipeline, CoreMapExpressionExtractor extractor, Boolean isPlainText,Boolean generate_gate_format) {
		 long startTime = System.currentTimeMillis();
		 String outputFile = outputDirectory.getAbsoluteFile() + File.separator + file_to_classify.getName();
		 File fout = new File(outputFile);
		 FileOutputStream fos;
		 try {
			 fos = new FileOutputStream(fout);
			 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
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
				 tagging2(pipeline, id, IOUtils.slurpFile(file_to_classify.getAbsolutePath(), "UTF-8"), file_to_classify.getName(), bw, extractor, isPlainText);
			 }
			 
			 if(generate_gate_format) {
				 String inputGateFile = file_to_classify.getAbsolutePath().substring(0, file_to_classify.getAbsolutePath().indexOf("_PLAIN.txt")) + "_GATE.xml";
				 String outputGATE_File = outputFile.substring(0, outputFile.indexOf("_PLAIN.txt")) + "_GATE_ANNOTATED.xml";
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
						retrieveRiskOfFinding(finding, me);
						
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
							retrieveManifestationOfFinding(finding, me);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		private void tagging2(StanfordCoreNLP pipeline, String id, String text_to_tag, String fileName, BufferedWriter bw, CoreMapExpressionExtractor extractor, Boolean isPlainText) {
			//String text = "tyrosine protein kinase abl family subcutaneously, in the neck region potassium & creatinine \\ ejemplo + - * [] % ( pepe )) skin, hair loss, head $ \b body-weight pepepe p<0.05 or p  > 0.05, treatment related finding increase liver toxicity Urine protein/creatinine ratio (Prot-U/Cre)";
			String text = " * - [ ] ^ glucose (uglu body-weight-gain  xxx xxxx treatment related finding both pinnae reddened  post dose";
			long startTime = System.currentTimeMillis();
			//Annotation document = new Annotation(text_to_tag.toLowerCase());
			
			Annotation document = new Annotation(text.toLowerCase());
			//run all Annotators on this text
			pipeline.annotate(document);
			long endTime = System.currentTimeMillis();
			log.info(" Annotation document execution time  " + (endTime - startTime) + " milliseconds");
	        try {
	    			
	        		List<CoreLabel> tokens = document.get(TokensAnnotation.class);
	        		
	        		List<CoreMap> entityMentions = document.get(MentionsAnnotation.class);
	        		
	        		List<MatchedExpression> matchedExpressions = extractor.extractExpressions(document);
			    	// print out the matched expressions
			        for (MatchedExpression me : matchedExpressions) {
			        	if(!(me.getText().length()< 4 && ( me.getValue().get().equals("sexpop_etox_send") || me.getValue().get().equals("lbtest_etox_send") || me.getValue().get().equals("moa_etox_send") || me.getValue().get().equals("strain_etox_send")))) {
			        		bw.write(id + "\t"+ (me.getCharOffsets().getBegin()) + "\t" + me.getCharOffsets().getEnd() + "\t" + me.getText() + "\t" + me.getValue().get() + "\n");
			        	}
			        }
			        
			        //List<SequenceMatchResult<CoreMap>> multiMatcher.findNonOverlapping(tokens);
			        
			        bw.flush();
	    		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	
		}
		
		
		
	private void retrieveManifestationOfFinding(TreatmentRelatedFinding finding, MatchedExpression me) {
		if(me.getValue().get().equals("INCREASE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.INCREASE);
		}else if(me.getValue().get().equals("DECREASE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.DECREASE);
		}else if(me.getValue().get().equals("TRANSITORY_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.TRANSITORY);
		}else if(me.getValue().get().equals("REVERSIBLE_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.REVERSIBLE);
		}else if(me.getValue().get().equals("JUSTPRESENT_MANIFESTATION_FINDING")){
			finding.setManifestationOfFinding(Manifestation.PRESENT);
		}
	}

	private void retrieveRiskOfFinding(TreatmentRelatedFinding finding, MatchedExpression me) {
		if(me.getValue().get().toString().endsWith("_RISK_LEVEL")) {
			if(me.getValue().get().equals("NOEL_RISK_LEVEL")) {
				finding.setRisk(ToxicityRisk.NOEL);
			}else if(me.getValue().get().equals("LOEL_RISK_LEVEL")) {
				finding.setRisk(ToxicityRisk.LOEL);
			}else if(me.getValue().get().equals("NOAEL_RISK_LEVEL")) {
				finding.setRisk(ToxicityRisk.NOAEL);
			}else if(me.getValue().get().equals("LOAEL_RISK_LEVEL")) {
				finding.setRisk(ToxicityRisk.NOAEL);
			}
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
		}else if(me.getValue().get().equals("VITAL_SIGNS_DOMAIN")) {
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
