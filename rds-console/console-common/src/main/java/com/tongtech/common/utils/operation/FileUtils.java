package com.tongtech.common.utils.operation;

import java.io.*;

/**
 *
 * @author Zhang Chen Long
 * @since 2012-7-24
 *
 */
public class FileUtils {

	/**
	 * <p>Get file name from file full path string.</p>
	 *
	 * For example: parseFileName("d:\path\for\a\docFile.txt") will return "docFile.txt"
	 *
	 * @param filePathName
	 * @return
	 * @since 2012-7-24
	 */
    public static String parseFileName(String filePathName)
    {
        int separatorIdx = filePathName.lastIndexOf('/');
        if(separatorIdx < 0) {
        	separatorIdx = filePathName.lastIndexOf('\\');
        }

        if(separatorIdx >= 0)
        {
            return filePathName.substring(separatorIdx + 1);
        }
        else {
        	return filePathName;
        }
    }


	/**
	 * Write InputStream into a file
	 * @param file
	 * @param input
	 * @return file size in byte
	 * @throws IOException
	 * @since 2012-7-24
	 */
	public static long writeToFile(File file, InputStream input)
			throws IOException {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);

			byte buffer[] = new byte[4096];
			long count = 0L;
			for (int n = 0; -1 != (n = input.read(buffer));) {
				output.write(buffer, 0, n);
				count += n;
			}

			return count;
		}
		finally {
			if(output != null) output.close();
		}
	}

	/**
	 * Open InputStream for a file.
	 * @param file
	 * @return
	 * @throws IOException
	 * @since 2012-7-24
	 */
    public static FileInputStream openInputStream(File file)
            throws IOException
    {
        if(file.exists()) {
            if(file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if(!file.canRead()) {
                throw new IOException("File '" + file + "' cannot be read");
            }
            else {
                return new FileInputStream(file);
            }
        }
        else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
    }

}
