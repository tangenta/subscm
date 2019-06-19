package com.tangenta;

import com.tangenta.parser.Parser;
import com.tangenta.parser.result.Panic;
import com.tangenta.util.Util;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Application {
    private static String FILES = "file";
    private static String ROUNDS = "round";
    private static String PARSE = "parse";

    public static void main(String[] args) {
        Options options = setupOptions();
        CommandLine cmd = parseCmd(args, options);

        // check validation
        String[] fileNames = checkFilename(cmd);
        int rounds = checkRound(cmd);
        String fileToParse = checkParseFile(cmd);

        if (fileNames != null) {
            System.out.println("\nSimulating Bit war...");
            List<Strategy> strategies = fetchStgFromFileNames(fileNames);
            String result = Util.format(Strategy.sortStrategies(strategies, rounds));
            System.out.println(result);
            System.out.println("done\n");
        }

        if (fileToParse != null) {
            System.out.println("Parsing input file: " + fileToParse);
            String code = readStrFromFile(fileToParse);
            try {
                String fmResult = Util.format(Parser.program().parseAndUnwrap(code));
                System.out.println(fmResult);
            } catch (Panic e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static Options setupOptions() {
        Options options = new Options();

        Option fileOption = new Option("f", FILES, true, "Files to be parsed");
        fileOption.setRequired(false);
        fileOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(fileOption);

        Option roundOption = new Option("r", ROUNDS, true, "Duel rounds");
        roundOption.setRequired(false);
        options.addOption(roundOption);

        Option parseOption = new Option("p", PARSE, true, "Parse a specific file");
        parseOption.setRequired(false);
        options.addOption(parseOption);
        return options;
    }

    private static CommandLine parseCmd(String[] args, Options options) {
        CommandLineParser cmdParser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = cmdParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        return cmd;
    }

    private static String checkParseFile(CommandLine cmd) {
        return cmd.getOptionValue(PARSE);
    }

    private static String[] checkFilename(CommandLine cmd) {
        String[] fileNames = cmd.getOptionValues(FILES);
        if (fileNames == null) return null;
        System.out.println(Arrays.toString(fileNames));
        if (fileNames.length <= 1) {
            System.err.println("Multiple input files is needed");
            System.exit(1);
        }
        return fileNames;
    }

    private static int checkRound(CommandLine cmd) {
        int rounds = 0;
        String roundStr = cmd.getOptionValue(ROUNDS);
        if (roundStr == null) {
            System.out.println("Round is set to default(200).");
            rounds = 200;
        } else {
            try {
                rounds = Integer.parseInt(roundStr);
            } catch (NumberFormatException e) {
                System.err.println("Error when parsing `round` argument: " + e.getMessage());
                System.exit(1);
            }
        }
        return rounds;
    }

    private static List<Strategy> fetchStgFromFileNames(String[] fileNames) {
        return Arrays.stream(fileNames)
                .map(Application::fetchStgFromFileName)
                .collect(Collectors.toList());
    }

    private static Strategy fetchStgFromFileName(String fileName) {
        String code = readStrFromFile(fileName);
        if (code == null) return null;
        return Strategy.builder()
                .name(fileName)
                .code(code).build();
    }

    private static String readStrFromFile(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
