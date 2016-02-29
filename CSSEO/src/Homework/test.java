package Homework;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class test {
	public static void main(String[] args) {
	    DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
	        @Override
	        public boolean accept(Path file) throws IOException {
	            return (Files.isDirectory(file));
	        }
	    };

	    Path dir = FileSystems.getDefault().getPath("C:/Users/bps21/Desktop/en");
	    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir,
	            filter)) {
	        for (Path path : stream) {
	            // Iterate over the paths in the directory and print filenames
	            System.out.println(path.getFileName());
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
