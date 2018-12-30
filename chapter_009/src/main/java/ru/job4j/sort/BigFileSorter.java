package ru.job4j.sort;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Class for sorting large text files.
 * External merge sort is used.
 * @author Roman Bednyashov (hipnorosva@gmail.com)
 * @version 0.1$
 * @since 0.1
 * 29.12.2018
 */
public class BigFileSorter implements Sorter {
    private static final Logger LOG = LogManager.getLogger(BigFileSorter.class.getName());

    /**
     * Maximum file size to sort.
     * 1Kb by default.
     */
    private final long maxFileSize;

    public BigFileSorter() {
        this.maxFileSize = 1024;
    }

    public BigFileSorter(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * The method sorts lines in a file by length and writes the result to a new file.
     * @param source to be sorted.
     * @param dest result file.
     * @throws IOException if exception.
     */
    @Override
    public void sort(File source, File dest) throws IOException {
        if (maxFileSize > source.length()) {
            sortFile(source, dest);
        } else {
            File[] files = splitFile(source);
            sort(files[0], files[0]);
            sort(files[1], files[1]);
            mergeFiles(files[0], files[1], dest);
        }
    }


    /**
     * The method divides one file into two parts.
     * @param input file to be splitted.
     * @return array of two files.
     * @throws IOException of exception.
     */
    public File[] splitFile(File input) throws IOException {
        File[] files = {File.createTempFile("temp", ".txt"), File.createTempFile("temp", ".txt")};
        try (var inputRa = new RandomAccessFile(input, "r")) {
            long delimiter = inputRa.length() / 2;
            try (
                    var leftRa = new BufferedWriter(new FileWriter(files[0]));
                    var rightRa = new BufferedWriter(new FileWriter(files[1]))
            ) {
                inputRa.seek(0);
                String temp;
                while (inputRa.getFilePointer() < delimiter) {
                    temp = decode(inputRa.readLine());
                    leftRa.write(temp);
                    leftRa.write("\n");
                }
                while ((temp = inputRa.readLine()) != null) {
                    temp = decode(temp);
                    rightRa.write(temp);
                    rightRa.write("\n");
                }
            }
        }
        LOG.info("{} was splitted into {} and {} ", input.getName(), files[0].getName(), files[1].getName());
        return files;
    }

    /**
     * The method sorts the file by line length from min to max.
     * Blank lines are ignored.
     * @param input file to be sorted.
     * @param output sorted file.
     * @throws IOException if exception.
     */
    public void sortFile(File input, File output) throws IOException {
        List<String> lines = getListFromFile(input);
        try (var writer = new BufferedWriter(new FileWriter(output))) {
            lines.stream()
                    .filter(line -> line.length() > 0)
                    .sorted(Comparator.comparingInt(String::length))
                    .forEach(line -> {
                        try {
                            writer.write(line);
                            writer.write("\n");
                        } catch (IOException e) {
                            LOG.error("IOException", e);
                        }
                    });
        }
        LOG.info("{} has been sorted", input.getName());
    }

    /**
     * The method merge two files into one and sort them by line length.
     * @param left first input file.
     * @param right second input file.
     * @param output resulting file.
     * @throws IOException if exception.
     */
    public void mergeFiles(File left, File right, File output) throws IOException {
        try (var writer = new BufferedWriter(new FileWriter(output));
             var leftReader = new BufferedReader(new FileReader(left));
             var rightReader = new BufferedReader(new FileReader(right))
        ) {
            var leftLine = leftReader.readLine();
            var rightLine = rightReader.readLine();
            var empty = false;
            while (!empty) {
                if (leftLine  != null && rightLine != null) {
                    if (leftLine.length() < rightLine.length()) {
                        writer.write(leftLine);
                        writer.write("\n");
                        leftLine = leftReader.readLine();
                    } else {
                        writer.write(rightLine);
                        writer.write("\n");
                        rightLine = rightReader.readLine();
                    }
                }
                if (leftLine == null && rightLine != null) {
                    writer.write(rightLine);
                    writer.write("\n");
                    rightLine = rightReader.readLine();
                }
                if (rightLine == null && leftLine != null) {
                    writer.write(leftLine);
                    writer.write("\n");
                    leftLine = leftReader.readLine();
                }
                if (leftLine == null && rightLine == null) {
                    empty = true;
                }
            }
        }
        LOG.info("{} and {} merged", left.getName(), right.getName());
        LOG.info("{} deleted? {}", left.getName(), left.delete());
        LOG.info("{} deleted? {}", right.getName(), right.delete());
    }

    /**
     * The method gets a list of all lines from the file.
     * @param file to read.
     * @return list with all lines.
     */
    public List<String> getListFromFile(File file) {
        List<String> result = new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(file))) {
            String temp;
            while ((temp = reader.readLine()) != null) {
                result.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Decoding method for strings in UTF-8 format.
     * @param line to be decoded.
     * @return decoded string.
     */
    private String decode(String line) {
        return new String(line.getBytes(StandardCharsets.ISO_8859_1), UTF_8);
    }
}
