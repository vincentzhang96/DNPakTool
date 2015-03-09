/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Vince
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package co.phoenixlab.dn;

import co.phoenixlab.dn.pak.PakFileReader;
import co.phoenixlab.dn.pak.PakHeader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DNPakTool {

    private static final Pattern TOKENIZE = Pattern.compile("\"(\\\\\"|[^\"])*?\"|[^ ]+");
    private static final String[] EMPTY_STR_ARRAY = new String[0];

    public static void main(String[] args) {
        if (args.length > 1) {
            String[] subargs = new String[args.length - 1];
            System.arraycopy(args, 1, subargs, 0, subargs.length);
            handleCommand(args[0].toLowerCase(), subargs);
        } else {
            interactive();
        }
    }

    private static void interactive() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter a command followed by its arguments, or enter \"help\" for a list of commands.");
            System.out.print("> ");
            String[] line = scanner.nextLine().split(" ", 2);
            String cmd = line[0].toLowerCase();
            if ("exit".equals(cmd) || "quit".equals(cmd) || "stop".equals(cmd)) {
                return;
            }
            if (line.length == 1) {
                handleCommand(cmd, EMPTY_STR_ARRAY);
            } else {
                handleCommand(cmd, tokenize(line[1]));
            }
        }
    }

    private static String[] tokenize(String s) {
        List<String> list = new ArrayList<>();
        Matcher matcher = TOKENIZE.matcher(s);
        while (matcher.find()) {
            String s1 = matcher.group();
            list.add(s1.substring(1, s1.length() - 1));
        }
        return list.toArray(new String[list.size()]);
    }

    private static void handleCommand(String cmd, String[] args) {
        switch (cmd) {
            case "help":
            case "h":
            case "-h":
            case "-?":
            case "/h":
            case "/?":
                printHelp();
                break;
            case "ls":
                ls(args);
                break;


        }

    }

    private static void printHelp() {
        System.out.println("Available commands:");
        printHelpLine("help", "Prints this list");
        printHelpLine("exit|quit|stop", "Exits the program");
        printHelpLine("ls file...", "Prints the file paths in the pak(s)");
        printHelpLine("find [-r] string file", "Finds all paths in the pak that match the given string, " +
                "or if -r is provided, the string is treated as a regex");
        printHelpLine("dump [-ds] [-fr string] src dest", "Dumps all files in the src pak into the dest " +
                "directory. If -d is provided, the output directory is EMPTIED before dumping. If -s is provided, " +
                "then the deletion prompt with -d will be suppressed. -s implies -d. If -f is provided, it will only " +
                "dump files matching the string (or, if -r is provided, string is treated as a regex. -r implies -f");
        printHelpLine("pack [-as] dest src", "Packs all files and subdirectories inside " +
                "src into a pak file dest. If -a is provided, missing files will be inserted and existing " +
                "files will be overwritten. If the file is not a valid pak, no changes wil occur. Without -a, a new " +
                "pak will be created; if a file already exists and -s is not provided, then it will be deleted " +
                "without prompting.");
    }

    private static void printHelpLine(String cmds, String desc) {
        System.out.println(cmds + "\n\t" + desc);
    }

    private static void ls(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: ls file...");
            return;
        }
        for (String s : args) {
            Path path = Paths.get(s);
            System.out.println("-- FILE LIST --");
            System.out.println(path.toString());
            try (PakFileReader reader = new PakFileReader(path)) {
                reader.load();
                PakHeader header = reader.getHeader();
                System.out.printf("%s: 0x%08X 0x%08X\n",
                        header.getMagic(),
                        header.getNumFiles(),
                        header.getFileTableOffset());
                PakFileReader.insert("mapdata\\grid\\tn_aa\\foo\\bar.ini", null, null);
            } catch (IOException e) {
                System.out.println("Error reading: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private static void find(String[] args) {

        if (args.length > 1) {
            String string;
            boolean ok = false;
            boolean regex = false;
            Path file;
            if (args.length == 2) {
                string = args[0];
                file = Paths.get(args[1]);
                regex = false;
                ok = true;
            } else if (args.length == 3) {
                string = args[1];
                file = Paths.get(args[2]);
                if (args[0].equals("-r")) {
                    regex = true;
                    ok = true;
                } else {
                    ok = false;
                }
            }
            if (ok) {


            }
        }
        System.out.println("Usage: find [-r] string file; see help");
    }

    private static void dump(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: dump [-ds] [-fr string] src dest; see help");
            return;
        }

    }

    private static void pack(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: pack [-as] dest src; see help");
            return;
        }
    }
}
