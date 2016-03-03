package Homework;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class test {
	public static void main(String[] args) {
		 UUID uuid = UUID.randomUUID();
		 String uu_id=uuid.toString();
		 System.out.println(uu_id);
}
}