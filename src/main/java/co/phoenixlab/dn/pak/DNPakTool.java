/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Vincent Zhang/PhoenixLAB
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

package co.phoenixlab.dn.pak;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.InflaterOutputStream;

public class DNPakTool {

    private static final Pattern TOKENIZE = Pattern.compile("\"(\\\\\"|[^\"])*?\"|[^ ]+");
    private static final String[] EMPTY_STR_ARRAY = new String[0];

    private static int filesDumped;
    private static Pattern filterPatternCached;
    @SuppressWarnings({"WeakerAccess", "CanBeFinal"})
    public static long PRINT_INTERVAL = 500L;
    private static final FileVisitor<Path> visitor = new FileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.TERMINATE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

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
            System.gc();
        }
    }

    private static String[] tokenize(String s) {
        List<String> list = new ArrayList<>();
        Matcher matcher = TOKENIZE.matcher(s);
        while (matcher.find()) {
            String s1 = matcher.group();
            if (s1.startsWith("\"") && s1.endsWith("\"")) {
                s1 = s1.substring(1, s1.length() - 1);
            }
            list.add(s1);
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
            case "find":
                find(args);
                break;
            case "dump":
                dump(args);
                break;
            case "cont":
                continuity(args);
                break;
            default:
                System.out.println("Unknown command. Try \"help\" for a list of a commands");
        }
    }

    private static void printHelp() {
        System.out.println("Available commands:");
        printHelpLine("help", "Prints this list");
        printHelpLine("exit|quit|stop", "Exits the program");
        printHelpLine("ls file...", "Prints the file paths in the pak(s)");
        printHelpLine("find [-r] string file", "Finds all paths in the pak that match the given string, " +
                "or if -r is provided, the string is treated as a regex");
        printHelpLine("dump [-ds] [-fr string] src...[*] dest", "Dumps all files in the src paks into the dest " +
                "directory. If src is terminated with /* then all .pak files within the directory will be dumped. " +
                "If -d is provided, the output directory is EMPTIED before dumping. If -s is provided, " +
                "then the deletion prompt with -d will be suppressed. -s implies -d. If -f is provided, it will only " +
                "dump files matching the string (or, if -r is provided, string is treated as a regex. -r implies -f");
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
            PakFileReader reader = new PakFileReader();
            try (PakFile pakFile = reader.load(path)) {
                System.out.printf("Read %d files\n", pakFile.getNumFiles());
                printDirectory(pakFile.getRoot(), 0);
            } catch (IOException e) {
                System.err.println("Error reading: " + e.toString());
                e.printStackTrace(System.err);
            }
            System.out.println("---------------");
            System.gc();
        }
    }

    private static void printDirectory(DirEntry dirEntry, int depth) {
        StringBuilder builder = new StringBuilder();
        tabs(depth, builder).append("+ ").append(dirEntry.name);
        System.out.println(builder.toString());
        ++depth;
        List<DirEntry> directories = new ArrayList<>();
        List<FileEntry> files = new ArrayList<>();
        for (Entry entry : dirEntry.getChildren().values()) {
            if (entry instanceof DirEntry) {
                directories.add((DirEntry) entry);
            } else if (entry instanceof FileEntry) {
                files.add((FileEntry) entry);
            }
        }
        Collections.sort(directories);
        Collections.sort(files);
        for (FileEntry fe : files) {
            builder.setLength(0);
            tabs(depth, builder).append("- ").append(fe.name).append("    ").
                    append(fe.getFileInfo().getDecompressedSize());
            System.out.println(builder.toString());
        }
        for (DirEntry de : directories) {
            printDirectory(de, depth);
        }
    }

    private static StringBuilder tabs(int depth, StringBuilder builder) {
        int spaces = 2 * depth;
        for (int i = 0; i < spaces; ++i) {
            builder.append(' ');
        }
        return builder;
    }

    private static void find(String[] args) {
        if (args.length > 1) {
            String string = null;
            boolean ok = false;
            boolean regex = false;
            Path file = null;
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
                List<String> results = searchResults(string, regex, file);
                System.out.printf("Found %d files\n", results.size());
                Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
                for (String s : results) {
                    int last = s.lastIndexOf('\\');
                    String name;
                    String path;
                    if (last != -1) {
                        name = s.substring(last + 1);
                        path = s.substring(0, last);
                    } else {
                        name = s;
                        path = "\\";
                    }
                    System.out.printf("%1$s\n\tin %2$s%n", name, path);
                }
                return;
            }
        }
        System.out.println("Usage: find [-r] string file; see help");
    }

    private static List<String> searchResults(String pattern, boolean regex, Path file) {
        Predicate<String> matcher;
        if (regex) {
            filterPatternCached = Pattern.compile(pattern);
            matcher = filterPatternCached.asPredicate();
        } else {
            matcher = s -> s.contains(pattern);
        }
        List<String> ret = new ArrayList<>();
        PakFileReader reader = new PakFileReader();
        try (PakFile pakFile = reader.load(file)) {
            int toRead = pakFile.getNumFiles();
            System.out.printf("Read %d files\n", toRead);
            DirEntry dir = pakFile.getRoot();
            searchDir(dir, ret, matcher);
        } catch (IOException e) {
            System.err.printf("Error searching %s:%n", file.toString());
            e.printStackTrace(System.err);
        }
        return ret;
    }

    private static void searchDir(DirEntry dirEntry, List<String> results, Predicate<String> matcher) {
        for (Entry entry : dirEntry.getChildren().values()) {
            if (entry instanceof DirEntry) {
                searchDir((DirEntry) entry, results, matcher);
            } else if (entry instanceof FileEntry) {
                FileEntry fileEntry = (FileEntry) entry;
                if (matcher.test(fileEntry.name)) {
                    results.add(fileEntry.getFileInfo().getFullPath());
                }
            }
        }
    }

    private static void printDumpUsage() {
        System.out.println("Usage: dump [-ds] [-fr string] src... dest; see help");
    }

    private static void dump(String[] args) {
        if (args.length == 0) {
            printDumpUsage();
            return;
        }
        boolean delete = false,
                suppress = false,
                find = false,
                regex = false;
        String patternArg = null;
        List<String> files = new ArrayList<>();
        for (String s : args) {
            if (s.startsWith("-")) {
                s = s.substring(1);
                for (char c : s.toCharArray()) {
                    switch (c) {
                        case 's':
                            suppress = true;
                            //  FALL THROUGH
                        case 'd':
                            delete = true;
                            break;
                        case 'r':
                            regex = true;
                            //  FALL THROUGH
                        case 'f':
                            find = true;
                    }
                }
            } else if (find && patternArg == null) {
                patternArg = s;
            } else {
                files.add(s);
            }
        }
        if (files.size() < 2 || (find && patternArg == null)) {
            printDumpUsage();
            return;
        }
        Path dest = Paths.get(files.remove(files.size() - 1)).toAbsolutePath().normalize();
        if (delete) {
            if (deleteDir(suppress, dest)) {
                return;
            }
        }
        final boolean useFilter = find;
        final boolean useRegex = regex;
        final String filterArg = patternArg;
        for (String src : files) {
            if (src.endsWith("/*") || src.endsWith("\\*")) {
                try (Stream<Path> stream = Files.list(Paths.get(src.substring(0, src.length() - 2)))) {
                    stream.filter(p -> p.getFileName().toString().endsWith(".pak")).
                            forEach(path -> dumpPak(useFilter, useRegex, filterArg, path, dest));
                } catch (IOException e) {
                    System.err.printf("Error while dumping %s:%n", src);
                    e.printStackTrace(System.err);
                }
            } else {
                dumpPak(useFilter, useRegex, filterArg, Paths.get(src), dest);
            }
            System.gc();
        }
    }

    private static boolean deleteDir(boolean suppress, Path dest) {
        if (Files.isDirectory(dest)) {
            if (!suppress) {
                System.out.println("Are you sure you wish to delete the directory " +
                        dest.toString() + " and all of its children? (yes/no)");
                String resp = new Scanner(System.in).nextLine();
                if (!"yes".equalsIgnoreCase(resp)) {
                    System.out.println("Operation cancelled");
                    return true;
                }
            }
            try {
                Files.walkFileTree(dest, visitor);
            } catch (IOException e) {
                System.err.println("Error: Unable to clean output directory");
            }
        }
        return false;
    }

    private static void dumpPak(boolean find, boolean regex, String patternArg, Path source, Path dest) {
        System.out.println("Dumping " + source.toString() + " into " + dest.toString());
        PakFileReader reader = new PakFileReader();
        try (PakFile pakFile = reader.load(source)) {
            int toRead = pakFile.getNumFiles();
            System.out.printf("Read %d files\n", toRead);
            Files.createDirectories(dest);
            filesDumped = 0;
            int fmtLen = String.format("%d", toRead).length();
            String fmt = "Dumping... %2$," + fmtLen + "d/%3$," + fmtLen + "d %1$3d%% %4$4d f/s %5$,6d KB/s\r";
            Predicate<String> filter;
            if (find) {
                final String filterStr = patternArg;
                if (regex) {
                    filterPatternCached = Pattern.compile(filterStr);
                    filter = filterPatternCached.asPredicate();
                } else {
                    filter = s -> s.contains(filterStr);
                }
            } else {
                filter = s -> true;
            }
            dumpDir(pakFile.getRoot(), dest, pakFile, toRead, fmt, filter);
            System.out.printf(fmt, 100, filesDumped, toRead, 0, 0);
            System.out.println("\nFiles dumped");
        } catch (IOException e) {
            System.err.println("Error dumping: " + e.toString());
            e.printStackTrace(System.err);
        }
    }

    private static void dumpDir(DirEntry dirEntry, Path root, PakFile pakFile, int total, String progressFmt,
                                Predicate<String> filter) throws IOException {
        //  It is the previous call's responsibility to create each subdirectory on the FS
        long lastPrintTime = System.currentTimeMillis() - PRINT_INTERVAL;
        float scalar = 1000F / (float) PRINT_INTERVAL;
        int filesAccum = 0;
        long bytesAccum = 0L;
        for (Entry entry : dirEntry.getChildren().values()) {
            Path path = root.resolve(entry.name);
            if (entry instanceof DirEntry) {
                Files.createDirectories(path);
                dumpDir((DirEntry) entry, path, pakFile, total, progressFmt, filter);
            } else if (entry instanceof FileEntry) {
                if (filter.test(entry.name)) {
                    dumpFile((FileEntry) entry, path, pakFile);
                }
                bytesAccum += Files.size(path);
                ++filesDumped;
                ++filesAccum;
                long time = System.currentTimeMillis();
                if (time - lastPrintTime >= PRINT_INTERVAL) {
                    lastPrintTime = time;
                    System.out.printf(progressFmt, (int) (100 * ((float) (filesDumped) / (float) total)),
                            filesDumped, total,
                            (int) (filesAccum * scalar),
                            (long) (bytesAccum * scalar / 1024));
                    filesAccum = 0;
                    bytesAccum = 0;
                }
            }
        }
    }

    private static void dumpFile(FileEntry fileEntry, Path path, PakFile pakFile) throws IOException {
        try (InflaterOutputStream outputStream = new InflaterOutputStream(Files.newOutputStream(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            WritableByteChannel byteChannel = Channels.newChannel(outputStream);
            pakFile.transferTo(fileEntry.getFileInfo(), byteChannel);
            outputStream.flush();
        }
    }

    private static void continuity(String[] args) {
        String pathStr = args[0];
        Path path = Paths.get(pathStr);
        PakFileReader reader = new PakFileReader();
        try (PakFile pakFile = reader.load(path)) {
            List<Segment> segments = pakFile.getEntryMap().values().stream().
                    map(FileEntry::getFileInfo).
                    map(fi -> new Segment(fi.getDiskOffset(), fi.getDiskSize(), -1)).
                    sorted().
                    collect(Collectors.toList());
            Stack<Segment> stack = new Stack<>();
            segments.forEach(stack::push);
            long diff = 0;
            int len = segments.size() - 1;
            for (int i = 0; i < len; i++) {
                Segment segment = segments.get(i);
                Segment next = segments.get(i + 1);
                diff += next.start - segment.end;
            }
            System.out.println(diff);
            List<Segment> done = new ArrayList<>(segments.size());
            while (!stack.isEmpty()) {
                Segment segment = stack.pop();
                if (stack.isEmpty()) {
                    done.add(segment);
                    break;
                }
                Segment next = stack.peek();
                Segment comb = segment.merge(next);
                if (comb != null) {
                    stack.pop();
                    stack.push(comb);
                } else {
                    done.add(segment);
                }
            }
            Collections.reverse(done);
            done.stream().
                    map(Segment::toString).
                    forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Error performing continuity test:");
            e.printStackTrace(System.err);
        }
    }
}
