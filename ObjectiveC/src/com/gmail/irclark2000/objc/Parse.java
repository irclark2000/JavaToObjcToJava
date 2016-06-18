package com.gmail.irclark2000.objc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.gmail.irclark2000.objc.java.JavaLexer;
import com.gmail.irclark2000.objc.java.JavaParser;
import com.gmail.irclark2000.objc.java.translate.JavaClassDescription;
import com.gmail.irclark2000.objc.java.translate.ParserJavaListener;
import com.gmail.irclark2000.objc.parser.ObjCLexer;
import com.gmail.irclark2000.objc.parser.ObjCParser;

/**
 * @author Isaac Clark Driver class for running the parser from the command line
 * 
 */

public class Parse {

	/**
	 * ERRORCODES
	 * 
	 * @author irclark
	 * 
	 *
	 */
	// private static final String FILENAME = "HTTPOperations.h";
	public enum ERRORCODE {
		USAGE_METHOD_CODE, MANGLED_COMMANDLINE_CODE, MANGLED_SCRIPT_FILE_CODE, MANGLED_REPLACE_OPTION, MISSING_DIRECTORIES_OPTION, BAD_LANGUAGE_SPECIFICATION
	}

	private static final String USAGE_MESSAGE = "Usage: %s options [inputFile1,inputFile2] \n" + "Options:"
			+ "\n-help              print this usage message"
			+ "\n-header headerfile use fileName as headerFile when parsing input file"
			+ "\n-lang java			Language for input files [ default is objc ] "
			+ "\n-autoheader        derive header file name from input file name "
			+ "\n-allheader dir     parse all headerfile in directry dir"
			+ "\n-all dir           parse all headers and implementation files in directry dir"
			+ "\n-o filename        write output to filename default is stdout"
			+ "\n-package package   add package statement to output"
			+ "\n-cstruct item,item read list of constructor prefixes. Default is 'init'"
			+ "\n-replace id=newid  replace id with newid (can use this option multiple times)"
			+ "\n-transfile file    read addtional translationns from file"
			+ "\n-f scriptfile      read options from scriptfile" + "";

	private static final String MANGLED_MESSAGE = "Command line error.\nType %s -help for usage information";
	private static final String SCRIPT_MANGLED_MESSAGE = "Error specifing script file.\nType %s -help for usage information";
	private static final String REPLACE_MANGLED_MESSAGE = "Error specifing replace options file.\nType %s -help for usage information";
	private static final String MISSING_DIRECTORIES_MESSAGE = "Error specifing directories.\nType %s -help for usage information";
	private static final String BAD_LANGUAGE_SPECIFICATION_MESSAGE = "Error specifying input language.  Must be java or objectiveC";
	private static boolean useHeaderFile = false;
	private static boolean useAutoHeaderFile = false;
	private static boolean doAll = false;
	private static String outputFileName = "";
	private static String directoryName = "";
	private static ArrayList<String> inputFileNames = new ArrayList<String>();
	private static String baseName = "";
	private static String packageName = "";
	private static String language = "objc";
	private static ArrayList<String> headerFileNames = new ArrayList<String>();
	private static ArrayList<String> cStructSignals = new ArrayList<String>();

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		List<String> argsList = Arrays.asList(args);

		ParseOptions options = new ParseOptions();
		if (argsList.contains("-f")) {
			String scriptName = "";
			for (int i = 0; i < argsList.size(); i++) {
				if (argsList.get(i).equals("-f") && i < argsList.size() - 1) {
					scriptName = argsList.get(i + 1);
					break;
				}
			}
			if (scriptName.length() > 0) {
				handleArgumentsFromFile(scriptName, options);
			} else {

			}
		} else {
			handleArguments(argsList, options);
		}
		if (language.equals("objc")) {
			options.setUseExternalTranslations(Translations.readTranslations(Translations.OBJCTRANSLATIONFILE));
			for (String con : cStructSignals) {
				options.getConstructorSignatures().add(con);
			}
			ClassDescription cd = new ClassDescription();
			Map<String, ClassDescription.ClassDeclaration> headerDeclarations = cd.getHeaders();
			boolean first = true;
			if (doAll) {
				useHeaderFile = true;
				useAutoHeaderFile = false;
			}
			if (useHeaderFile) {
				if (!first) {
					cd = new ClassDescription(headerDeclarations);
				} else {
					first = false;
				}
				options.setParsingheader(true);
				for (String headerFileName : headerFileNames) {
					baseName = baseNameFromPath(headerFileName);
					options.setInputFileName(headerFileName);
					options.setClassName(baseName);
					cd.setTempClassName(baseName);
					String fName = combinePathWithFileName(directoryName, "dummy");
					options.setOutputFileName(fName);
					BufferedInputStream instream = null;
					instream = new BufferedInputStream(new FileInputStream(headerFileName));
					ANTLRInputStream antlrStream = null;
					antlrStream = new ANTLRInputStream(instream);
					ObjCParser.Translation_unitContext tree = new ObjCParser(
							new CommonTokenStream(new ObjCLexer(antlrStream))).translation_unit();
					// walk the tree and activate so we can listen
					ParseTreeWalker walker = new ParseTreeWalker();
					walker.walk(new ParserObjcListener(cd, null, options), tree);
					instream.close();
				}
			}

			// Set<String> keys = cd.getHeaders().keySet();
			options.setParsingheader(false);
			options.setPackageName(packageName);
			for (String inputFileName : inputFileNames) {
				baseName = baseNameFromPath(inputFileName);
				options.setInputFileName(inputFileName);
				if (inputFileName.length() > 0) {
					BufferedInputStream instream = null;
					instream = new BufferedInputStream(new FileInputStream(inputFileName));
					ANTLRInputStream antlrStream = null;
					antlrStream = new ANTLRInputStream(instream);
					// lexing
					ObjCLexer lexer = new ObjCLexer(antlrStream);
					CommonTokenStream tokens = new CommonTokenStream(lexer);
					ObjCParser parser = new ObjCParser(tokens);
					RuleContext tree = parser.translation_unit();

					// walk the tree and activate so we can listen
					ParseTreeWalker walker = new ParseTreeWalker();
					cd = new ClassDescription(headerDeclarations);
					options.setClassName(baseName);
					cd.setTempClassName(baseName);
					options.setParsingheader(false);
					String fName = outputFileName;
					if (fName.length() == 0) {
						fName = combinePathWithFileName(directoryName, baseName + ".java");
					}
					options.setOutputFileName(fName);

					walker.walk(new ParserObjcListener(cd, tokens, options), tree);
				}
			}

		} else if (language.equals("java")) {
			//JavaClassDescription jcd = new JavaClassDescription();
			options.setUseExternalTranslations(Translations.readTranslations(Translations.JAVATRANSLATIONFILE));
			options.setParsingheader(false);
			options.setPackageName(packageName);
			ArrayList<ArrayList<JavaClassDescription>> myClassDescriptions = new ArrayList<ArrayList<JavaClassDescription>>(); 
			for (String inputFileName : inputFileNames) {
				baseName = baseNameFromPath(inputFileName);
				options.setInputFileName(inputFileName);
				if (inputFileName.length() > 0) {
					BufferedInputStream instream = null;
					instream = new BufferedInputStream(new FileInputStream(inputFileName));
					ANTLRInputStream antlrStream = null;
					antlrStream = new ANTLRInputStream(instream);
					// lexing
					JavaLexer lexer = new JavaLexer(antlrStream);
					CommonTokenStream tokens = new CommonTokenStream(lexer);
					JavaParser parser = new JavaParser(tokens);
					RuleContext tree = parser.compilationUnit();

					// walk the tree and activate so we can listen
					ParseTreeWalker walker = new ParseTreeWalker();
					//jcd = new JavaClassDescription();
//					options.setClassName(baseName);
//					cd.setTempClassName(baseName);
//					options.setParsingheader(false);
//					String fName = outputFileName;
//					if (fName.length() == 0) {
//						fName = combinePathWithFileName(directoryName, baseName + ".java");
//					}
//					options.setOutputFileName(fName);
//
					walker.walk(new ParserJavaListener(options, myClassDescriptions), tree);
				}
			}
		} else {
			System.out.printf("Language %s is not supported.\n", language);
			System.exit(1);
		}
	}

	private static String generateHeaderFileName(String fName) {
		String hFileName = "";
		int index = fName.lastIndexOf('.');
		if (index >= 0) {
			String baseName = fName.substring(0, index);
			hFileName = baseName + ".h";
		} else {
			hFileName = fName + ".h";
		}
		return hFileName;
	}

	/**
	 * @param fName
	 *            filename
	 * @return returns the base for the given file name
	 */
	public static String getBaseName(String fName) {
		String bName = fName;
		int index = fName.lastIndexOf('.');
		if (index >= 0) {
			bName = fName.substring(0, index);
		}
		return bName;
	}

	private static void printErrorAndExit(ERRORCODE errorCode) {
		switch (errorCode) {
		case USAGE_METHOD_CODE:
			System.err.printf(USAGE_MESSAGE, "ObjectiveC");
			break;
		case MANGLED_COMMANDLINE_CODE:
			System.err.printf(MANGLED_MESSAGE, "ObjectiveC");
			break;
		case MANGLED_SCRIPT_FILE_CODE:
			System.err.printf(SCRIPT_MANGLED_MESSAGE, "ObjectiveC");
			break;
		case MANGLED_REPLACE_OPTION:
			System.err.printf(REPLACE_MANGLED_MESSAGE, "ObjectiveC");
			break;
		case MISSING_DIRECTORIES_OPTION:
			System.err.printf(MISSING_DIRECTORIES_MESSAGE, "ObjectiveC");
			break;
		case BAD_LANGUAGE_SPECIFICATION:
			System.err.printf(BAD_LANGUAGE_SPECIFICATION_MESSAGE, "ObjectiveC");
			break;
		}
		System.exit(errorCode.ordinal());
	}

	private static void handleArgumentsFromFile(String fileName, ParseOptions options) {
		File f = new File(fileName);
		Scanner sc = null;
		List<String> args = new ArrayList<String>();
		try {
			sc = new Scanner(f);
		} catch (FileNotFoundException e) {
			printErrorAndExit(ERRORCODE.MANGLED_SCRIPT_FILE_CODE);
		}
		while (sc.hasNextLine()) {
			String line = sc.nextLine().trim();
			String[] parts = line.split("[ ]+");
			for (String arg : parts) {
				args.add(arg);
			}
		}
		sc.close();
		handleArguments(args, options);
	}

	private static void handleArguments(List<String> list, ParseOptions options) {
		// look for crucial language element first
		if (list.contains("-lang")) {
			int i = list.indexOf("-lang");
			if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
				printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
			} else {
				String l = list.get(i + 1);
				if (l.toLowerCase().startsWith("java")) {
					language = "java";
				} else if (l.toLowerCase().startsWith("obj")) {
					language = "objc";
				} else {
					printErrorAndExit(ERRORCODE.BAD_LANGUAGE_SPECIFICATION);
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			String arg = list.get(i);
			if (arg.equals("help") || arg.equals("-help")) {
				printErrorAndExit(ERRORCODE.USAGE_METHOD_CODE);
			} else if (arg.equals("-o")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					outputFileName = list.get(i + 1);
					i++;
				}
			} else if (arg.equals("-autoheader")) {
				useHeaderFile = true;
				useAutoHeaderFile = true;
			} else if (arg.equals("-header")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					useAutoHeaderFile = false;
					useHeaderFile = true;
					headerFileNames.add(list.get(i + 1));
					i++;
				}
				continue;
			} else if (arg.equals("-all")) {
				doAll = true;
				directoryName = list.get(i + 1);
				File f = new File(list.get(i + 1));
				File[] files = f.listFiles();
				if (files != null) {
					for (File file : files) {
						if (file.isFile()) {
							if (language.equals("objc")) {
								if (file.getAbsolutePath().endsWith(".h")) {
									headerFileNames.add(file.getAbsolutePath());
								} else if (file.getAbsolutePath().endsWith(".m")) {
									inputFileNames.add(file.getAbsolutePath());
								}
							} else if (language.equals("java")) {
							} if (file.getAbsolutePath().endsWith(".java")) {
								inputFileNames.add(file.getAbsolutePath());
							}
						}
					}
					i++;
				} else {
					printErrorAndExit(ERRORCODE.MISSING_DIRECTORIES_OPTION);
				}
			} else if (arg.equals("-replace")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_REPLACE_OPTION);
				} else {
					String[] pair = list.get(i + 1).split("=");
					if (pair.length != 2) {
						printErrorAndExit(ERRORCODE.MANGLED_REPLACE_OPTION);
					} else {
						options.addIdentityPair(pair[0].trim(), pair[1].trim());
					}
					i++;
				}

			} else if (arg.equals("-lang")) {
				i++;
			} else if (arg.equals("-transfile")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					Translations.readTranslations(list.get(i + 1));
				}
				i++;
			} else if (arg.equals("-package")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					packageName = list.get(i + 1);
				}
				i++;
			} else if (arg.equals("-cstruct")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					String[] parts = list.get(i + 1).split(",");
					for (String cStruct : parts) {
						cStructSignals.add(cStruct);
					}
				}
			} else if (arg.equals("-allheader")) {
				if (i >= list.size() - 1 || list.get(i + 1).startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					directoryName = list.get(i + 1);
					File f = new File(list.get(i + 1));
					File[] files = f.listFiles();
					for (File file : files) {
						if (file.isFile() && file.getAbsolutePath().endsWith(".h")) {
							headerFileNames.add(file.getAbsolutePath());
						}
					}
					i++;

					useAutoHeaderFile = false;
					useHeaderFile = true;
				}
			} else if (i == list.size() - 1) {
				useHeaderFile = true;
				if (arg.startsWith("-")) {
					printErrorAndExit(ERRORCODE.MANGLED_COMMANDLINE_CODE);
				} else {
					if (!doAll) {
						String[] fNames = arg.split(",");
						for (String fName : fNames) {
							inputFileNames.add(combinePathWithFileName(directoryName, fName));
						}
					}
				}
			}
		}

		if (null != null) {
			useHeaderFile = false;
			useAutoHeaderFile = false;
		} else {
			if (useAutoHeaderFile) {
				headerFileNames.add(generateHeaderFileName(inputFileNames.get(0)));
			}
		}
	}

	static String baseNameFromPath(String fName) {
		File f = new File(fName);
		String bName = f.getName();
		return getBaseName(bName);
	}

	private static String combinePathWithFileName(String directory, String fName) {
		// directory.replace("/", File.separator);
		// directory.replace("\\", File.separator);
		if (directory.charAt(directory.length() - 1) != '/') {
			directory += "/";
		}
		return directory + fName;
	}
}
