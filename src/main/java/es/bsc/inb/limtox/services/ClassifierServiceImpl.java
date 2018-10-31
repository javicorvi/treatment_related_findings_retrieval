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
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.classify.ColumnDataClassifier;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.MentionsAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.ling.tokensregex.NodePattern;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.objectbank.ObjectBank;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import es.bsc.inb.limtox.model.HepatotoxicityTerm;
@Service
class ClassifierServiceImpl implements ClassifierService {

	static final Logger classifierLog = Logger.getLogger("classifierLog");
	
	 // key for matched expressions
	  public static class MyMatchedExpressionAnnotation implements CoreAnnotation<List<CoreMap>> {
	    public Class<List<CoreMap>> getType() {
	      return ErasureUtils.<Class<List<CoreMap>>> uncheckedCast(String.class);
	    }
	  }

	
	
	public void classify(String propertiesParametersPath) {
		try {
			classifierLog.info("Classify articles with properties :  " +  propertiesParametersPath);
			Properties propertiesParameters = this.loadPropertiesParameters(propertiesParametersPath);
			classifierLog.info("Classify articles with the model  :  " +  propertiesParameters.getProperty("classificatorModel"));
			classifierLog.info("Input directory with the articles to classify : " + propertiesParameters.getProperty("inputDirectory"));
			classifierLog.info("Outup directory with the relevant articles : " + propertiesParameters.getProperty("outputDirectory"));
			classifierLog.info("Relevant articles label: " + propertiesParameters.getProperty("relevantLabel"));
			classifierLog.info("Is sentence classification: " + propertiesParameters.getProperty("is_sentences_classification"));
			
			String classificatorModel = propertiesParameters.getProperty("classificatorModel");
			String inputDirectoryPath = propertiesParameters.getProperty("inputDirectory");
			String outputDirectoryPath = propertiesParameters.getProperty("outputDirectory");
			String relevantLabel = propertiesParameters.getProperty("relevantLabel");
			String is_sentences_classification_ = propertiesParameters.getProperty("is_sentences_classification");
			Boolean is_sentences_classification = false;
			if(is_sentences_classification_!=null & is_sentences_classification_.equals("true")) {
				is_sentences_classification = true;
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
		    
		    
		    Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, entitymentions, parse, natlog, openie, sentiment");
			//regener is for adding tab separated tagger
			props.put("regexner.mapping", "etransafe_rules_manual_curated.txt");
			props.put("regexner.posmatchtype", "MATCH_ALL_TOKENS");
			
			props.put("rulesFiles", "extended_rules_treatment_related_findings.rules");
			//props.setProperty("tokenize.class", "true");
			
			
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			
			// set up the TokensRegex pipeline
		    // get the rules files
		    String[] rulesFiles = props.getProperty("rulesFiles").split(",");

		    // set up an environment with reasonable defaults
		    Env env = TokenSequencePattern.getNewEnv();
		    // set to case insensitive
		    env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		    env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		    // build the CoreMapExpressionExtractor
		    CoreMapExpressionExtractor extractor = CoreMapExpressionExtractor.createExtractorFromFiles(env, rulesFiles);
			BufferedWriter filesPrecessedWriter = new BufferedWriter(new FileWriter(outputDirectoryPath + File.separator + "list_files_processed.dat", true));
		    if (Files.isDirectory(Paths.get(inputDirectoryPath))) {
				File inputDirectory = new File(inputDirectoryPath);
				File[] files =  inputDirectory.listFiles();
				for (File file_to_classify : files) {
					if(file_to_classify.getName().endsWith(".txt") && filesProcessed!=null && !filesProcessed.contains(file_to_classify.getName())){
						Boolean result = this.process(file_to_classify, cdc, outputDirectory, relevantLabel, is_sentences_classification, pipeline, extractor);
						if(result) {
							//filesPrecessedWriter.write(file_to_classify.getName()+"\n");
							filesPrecessedWriter.flush();
						}
					}
				}
			}
		    filesPrecessedWriter.close();
		}  catch (Exception e) {
			classifierLog.error("Generic error in the classification step",e);
		}
	}

	private List<String> readFilesProcessed(String outputDirectoryPath) {
		List<String> files_processed = new ArrayList<String>();
		try {
			if(Files.isRegularFile(Paths.get(outputDirectoryPath + File.separator + "list_files_processed.dat"))) {
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
	 private Boolean process(File file_to_classify,  ColumnDataClassifier cdc , File outputDirectory, String relevantLabel, Boolean is_sentences_classification, StanfordCoreNLP pipeline, CoreMapExpressionExtractor extractor) {
		 File fout = new File(outputDirectory.getAbsoluteFile() + File.separator + file_to_classify.getName());
		 FileOutputStream fos;
		 try {
			 fos = new FileOutputStream(fout);
			 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			 classifierLog.info(" File to classify " + file_to_classify.getAbsolutePath());
			 for (String line : ObjectBank.getLineIterator(file_to_classify.getAbsolutePath(), "utf-8")) {
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
					 
					 HashMap<String,String> treatment_related_finding = new HashMap<String,String>();
					 
					 if(is_sentences_classification) {
						 bw.write(cdc.classOf(d) + "\t" + cdc.scoresOf(d).getCount(cdc.classOf(d)) + "\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\n");
					}else {
						 bw.write(cdc.classOf(d) + "\t" + cdc.scoresOf(d).getCount(cdc.classOf(d)) + "\t" + data[0] + "\t" + data[1] + "\t" + data[2] + "\t" + data[3] + "\t" + data [4] + "\n");
						 tagging(pipeline, id, data [4], file_to_classify.getName(), bw, extractor);
					 }
					 bw.newLine();
				}catch (ArrayIndexOutOfBoundsException e) {
					classifierLog.error(" Error with line: "+ line );
					classifierLog.error(" Error with line, in file: " + file_to_classify.getAbsolutePath() + " there is not the correct columns for the line ", e);
				}catch (Exception e) {
					classifierLog.error(" Error with line: "+ line );
					classifierLog.error(" Error with line, in file: " + file_to_classify.getAbsolutePath(), e);
				} 
			 }
			 bw.close(); 
			 return true;
		} catch (FileNotFoundException e) {
			classifierLog.error(" File not Found " + file_to_classify.getAbsolutePath(), e);
			return false;
		} catch (IOException e) {
			classifierLog.error(" IOException " + file_to_classify.getAbsolutePath(), e);
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
			//String text = " pepepe p<0.05 or p  > 0.05, increase liver toxicity";
			Annotation document = new Annotation(text_to_tag.toLowerCase());
			//Annotation document = new Annotation(text);
			//run all Annotators on this text
			pipeline.annotate(document);
	        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	        for(CoreMap sentence: sentences) {
	        	Boolean lowLevelAnalisys =false;
	        	List<CoreMap> entityMentions = sentence.get(MentionsAnnotation.class);
	    		try {
	    			bw.write(sentence.get(TextAnnotation.class)+ "\n");
	        		for (CoreMap entityMention : entityMentions) {
		    			String keyword = entityMention.get(TextAnnotation.class);
		        		String entityType = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
		        		CoreLabel token = entityMention.get(TokensAnnotation.class).get(0);
		        		bw.write(id + "\t"+ token.beginPosition() + "\t" + (token.beginPosition() + keyword.length())  + "\t" + keyword + "\t" + entityType + "\n");
					    if(entityType!=null && entityType.endsWith("_DOMAIN")) {
					    	lowLevelAnalisys=true;
		        		}
					}
	        		List<MatchedExpression> matchedExpressions = extractor.extractExpressions(sentence);
			    	// print out the matched expressions
			        for (MatchedExpression me : matchedExpressions) {
			        	bw.write(id + "\t"+ me.getCharOffsets().getBegin() + "\t" + me.getCharOffsets().getEnd() + "\t" + me.getText() + "\t" + me.getValue() + "\n");
						if(me.getValue().get().equals("TREATMENT_RELATED_FINDING")) {
							lowLevelAnalisys=true;
						}
			        }
			        //More analisys in the sentence
			        if(lowLevelAnalisys) {
			        	this.sentenceAnalisys(sentence, bw);
			        }
			        bw.flush();
	    		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
