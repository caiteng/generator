package Io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
/**
 * CREATED_BY caiT
 */
public class IoFile {
	public static boolean createFile(String filePath, String filecontent) {
		Boolean bool = false;
		File file = new File(filePath);
		String folderName = filePath.substring(0, filePath.lastIndexOf("\\"));
		File folder = new File(folderName);
		if (!folder.exists() && !folder.isDirectory()) {
			folder.mkdir();
		} else {
		}
		try {
			// 如果文件不存在，则创建新的文件
			if (!file.exists()) {
				file.createNewFile();
				bool = true;
				System.out.println("创建文件成功：" + filePath);
				// 创建文件成功后，写入内容到文件里
				writeFileContent(filePath, filecontent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bool;
	}

	/**
	 * 向文件中写入内容
	 * 
	 * @param filepath
	 *            文件路径与名称
	 * @param newstr
	 *            写入的内容
	 * @return
	 * @throws IOException
	 */
	public static boolean writeFileContent(String filepath, String newstr) throws IOException {
		Boolean bool = false;
		String filein = newstr + "\r\n";// 新写入的行，换行
		String temp = "";

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			File file = new File(filepath);// 文件路径(包括文件名称)
			// 将文件读入输入流
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buffer = new StringBuffer();

			buffer.append(temp);
			// 行与行之间的分隔符 相当于“\n”
			buffer = buffer.append(System.getProperty("line.separator"));
			buffer.append(filein);
			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			pw.write(buffer.toString().toCharArray());
			pw.flush();
			bool = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 不要忘记关闭
			if (pw != null) {
				pw.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
		return bool;
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 *            文件名称
	 * @return
	 */
	public static boolean delFile(String fileName, String filePath) {
		Boolean bool = false;
		File file = new File(filePath);
		try {
			if (file.exists()) {
				file.delete();
				bool = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return bool;
	}

}