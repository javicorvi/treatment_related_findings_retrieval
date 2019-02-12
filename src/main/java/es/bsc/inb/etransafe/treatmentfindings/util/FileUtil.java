package es.bsc.inb.etransafe.treatmentfindings.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtil {

	public static void createTxtFile(String path, String plainText) throws FileNotFoundException, IOException {
		File fout = new File(path);
		FileOutputStream fos = new FileOutputStream(fout);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		bw.write(plainText);
		bw.flush();
		bw.close();
	}
	
}
