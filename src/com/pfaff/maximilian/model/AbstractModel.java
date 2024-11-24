package com.pfaff.maximilian.model;

import com.pfaff.maximilian.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractModel {
    private static final int UP = 1;
    private static final int DOWN = -1;

    /**
     * @return The angle that the helix turns with each nucleotide times 10.
     */
    protected abstract int helixTurn();

    /**
     * @return The distance the helix rises with each nucleotide times 100.
     */
    protected abstract int elevation();

    /**
     * @return The atom names and base coordinates of the phosphate.
     */
    protected abstract Value getPhosphateData();

    /**
     * @return The atom names and base coordinates of the ribose or deoxyribose.
     */
    protected abstract Value getSugarData();

    /**
     * @param letterCode One-letter code of a nucleic base.
     * @return The atom names and base coordinates of the given nucleic base.
     */
    protected abstract Value eval(char letterCode);

    /**
     * Container for all implementations of this class.
     */
    public enum Model {
        A_DNA("A-DNA", A_DNA::new),
        B_DNA("B-DNA", B_DNA::new),
        A_RNA("A-RNA", A_RNA::new);

        private final String name;
        private final Supplier<AbstractModel> model;

        Model(String name, Supplier<AbstractModel> model) {
            this.name = name;
            this.model = model;
        }

        @Override
        public String toString() {
            return name;
        }

        AbstractModel getModel() {
            return model.get();
        }
    }

    /**
     * Utility container for atoms and their respective base coordinates.
     * @param names All atom names inside that part, e.g. inside Adenine.
     * @param coords Base coordinates scaled as integer values for more efficient and robust calculations.
     */
    protected record Value(String[] names, int[][] coords) {
        List<CylinderCoords> eval(int currentTheta, int currentZ, int direction) {
            final List<CylinderCoords> atoms = new ArrayList<>(names.length);

            for (int i = 0; i < names.length; i++) {
                final String atom = names[i];

                final int[] currentCoords = coords[i];

                final double radius = currentCoords[0] / 100d;

                int intermediateTheta = (currentTheta + currentCoords[1] * direction) % 3600;

                // Prevent negative angles
                if (intermediateTheta < 0) {
                    intermediateTheta = 3600 + intermediateTheta;
                }

                final double theta = intermediateTheta / 10d;

                final double height = (currentZ + currentCoords[2] * direction) / 100d;

                atoms.add(new CylinderCoords(atom, radius, theta, height));
            }

            return atoms;
        }
    }

    /**
     * Container for the finished model, has two lists of nucleotides - one for each strand
     */
    public static class Helix {
        List<Nucleotide> strand1;
        List<Nucleotide> strand2;

        public List<Nucleotide> strand1() {
            return strand1;
        }
        public List<Nucleotide> strand2() {
            return strand2;
        }
    }

    /**
     * Another container for all atoms of a nucleotide.
     * @param letter One-letter code of the nucleotide.
     * @param sugar List of coordinates for the (deoxy)ribose.
     * @param phosphate List of coordinates for the phosphate.
     * @param base List of coordinates for the nucleic base.
     */
    public record Nucleotide(char letter, List<CylinderCoords> sugar, List<CylinderCoords> phosphate, List<CylinderCoords> base) {
        public String fullName() {
            return getName(letter);
        }
    }

    /**
     * Container that holds the calculated coordinates for a single atom as double-precision floating-point numbers.
     * @param atom Name of the atom.
     * @param radius Distance from the helical axis, in Ångström.
     * @param theta Angle around the axis, in degrees.
     * @param height Height along the axis, in Ångström.
     */
    public record CylinderCoords(String atom, double radius, double theta, double height) {
        @Override
        public String toString() {
            return String.format(Locale.US, "%s\t%.2f\t%.1f\t%.2f", atom, radius, theta, height);
        }

        public String toString(double scale, double extraHeight) {
            return String.format(Locale.US, "%s\t%.2f\t%.1f\t%.1f\t%.2f\t%.1f", atom, radius, radius * scale, theta, height, height * scale + extraHeight);
        }
    }

    /**
     * @param letter One-letter code of a nucleotide.
     * @return The complementary nucleotide that pairs with the input.
     */
    public static char complementary(char letter) {
        return switch (letter) {
            case 'a', 'A' -> 'T';
            case 't', 'T' -> 'A';
            case 'c', 'C' -> 'G';
            case 'g', 'G' -> 'C';
            default -> throw new IllegalStateException("Unexpected value: " + letter);
        };
    }

    /**
     * @param letter One-letter code of a nucleotide.
     * @return Full name of the input.
     */
    public static String getName(char letter) {
        return switch (letter) {
            case 'a', 'A' -> "Adenine";
            case 't', 'T' -> "Thymine";
            case 'c', 'C' -> "Cytosine";
            case 'g', 'G' -> "Guanine";
            case 'u', 'U' -> "Uracil";
            default -> throw new IllegalStateException("Unexpected value: " + letter);
        };
    }

    /**
     * Our model - Group D14
     */
    public static void ourModelDump() throws IOException {
        createModelAndDumpToFile(Model.A_DNA, "TCCCCGGGGA", 15d);
    }

    /**
     * Opens a dialog to select a directory where the file will be saved in, creates the model and dumps it inside said file.
     * @param model The DNA/RNA model.
     * @param query The DNA/RNA sequence.
     * @param extraHeight Additional height offset, if needed.
     */
    public static void createModelAndDumpToFile(Model model, String query, double extraHeight) throws IOException {
        final File dir = FileUtil.openDir();

        if (dir == null) {
            return;
        }

        final String fileName = String.format(Locale.US, "Model_%s_%s.tsv", model, query);
        final File file = FileUtil.resolveUniqueFilePath(dir.toPath(), fileName).toFile();

        final List<String> table = createModelDump(model, query, extraHeight);

        FileUtil.dumpToFile(file, table);
    }

    /**
     * Prepares a complete dump of the model.
     * @param model The DNA/ RNA model.
     * @param query The DNA/ RNA sequence.
     * @param extraHeight Additional height offset, if needed.
     * @return A list of all the lines that make up this model.
     */
    public static List<String> createModelDump(Model model, String query, double extraHeight) {
        final Helix helix = createModel(model, query);

        final List<String> table = new ArrayList<>();

        // From Å to cm
        final double scalingFactor = 1.25d;

        final Consumer<List<Nucleotide>> filler = nucleotides -> {
            for (Nucleotide nucleotide : nucleotides) {
                table.add(nucleotide.fullName());

                table.add("Sugar");
                for (CylinderCoords coords : nucleotide.sugar()) {
                    table.add(coords.toString(scalingFactor, extraHeight));
                }

                table.add("Phosphate");
                for (CylinderCoords coords : nucleotide.phosphate()) {
                    table.add(coords.toString(scalingFactor, extraHeight));
                }

                table.add("Base");
                for (CylinderCoords coords : nucleotide.base()) {
                    table.add(coords.toString(scalingFactor, extraHeight));
                }

                table.add("");
            }
        };

        table.add("Atom\tRadius [Å]\tRadius [cm]\tθ [°]\tHeight [Å]\tHeight [cm]");

        table.add("3' -> 5'");
        filler.accept(helix.strand1);

        if (model != Model.A_RNA) {
            table.add("5' -> 3'");
            filler.accept(helix.strand2);
        }

        return table;
    }

    /**
     * Calculates unscaled and non-shifted coordinates according to the base coordinates of the model.
     * @param model The DNA/ RNA model.
     * @param query The DNA/ RNA sequence.
     * @return A helix container holding all the information.
     */
    public static Helix createModel(Model model, String query) {
        final AbstractModel modelImpl = model.getModel();

        final Helix helix = new Helix();

        final char[] letters = query.toCharArray();

        helix.strand1 = makeStrand(modelImpl, letters, UP);

        if (model != Model.A_RNA) {
            final int length = letters.length;
            final char[] reversed = new char[length];

            for (int i = 0; i < length; i++) {
                reversed[i] = complementary(letters[i]);
            }

            helix.strand2 = makeStrand(modelImpl, reversed, DOWN);
        }

        return helix;
    }

    /**
     * Calculates the coordinates for a single strand.
     * @param model The DNA/ RNA model.
     * @param letters The DNA/ RNA sequence as char array.
     * @param direction Either {@link AbstractModel#UP} = 1 or {@link AbstractModel#DOWN} = -1, which depends on which strand is being calculated.
     * @return The coordinates of a single strand.
     */
    private static List<Nucleotide> makeStrand(AbstractModel model, char[] letters, int direction) {
        final int helixTurn = model.helixTurn();
        final int elevation = model.elevation();

        final Value sugar = model.getSugarData();
        final Value phosphate = model.getPhosphateData();

        int theta = 0;
        int z = 0;

        final List<Nucleotide> nucleotides = new ArrayList<>(letters.length);

        for (char letter : letters) {
            final Value next = model.eval(letter);

            final List<CylinderCoords> sugarCoords = sugar.eval(theta, z, direction);
            final List<CylinderCoords> phosphateCoords = phosphate.eval(theta, z, direction);
            final List<CylinderCoords> baseCoords = next.eval(theta, z, direction);

            nucleotides.add(new Nucleotide(letter, sugarCoords, phosphateCoords, baseCoords));

            theta += helixTurn;
            z += elevation;
        }

        return nucleotides;
    }
}
