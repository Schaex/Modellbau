package com.pfaff.maximilian.model;

import com.pfaff.maximilian.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class PDB {
    // Account for differences of names between the script and the PDB format
    private static final String[] PHOSPHATE_NAMES = {"O5'", "OP1", "OP2", "P", "O3'"};

    /**
     * Our model - Group D14
     */
    public static void ourModel() throws IOException {
        createModelPdbFile(AbstractModel.Model.A_DNA, "TCCCCGGGGA");
    }

    /**
     * Opens a dialog to select a directory where the file will be saved in, creates the PDB model and dumps it inside said file.
     * @param model The DNA/ RNA model.
     * @param query The DNA/ RNA sequence.
     */
    public static void createModelPdbFile(AbstractModel.Model model, String query) throws IOException {
        final File dir = FileUtil.openDir();

        if (dir == null) {
            return;
        }

        final String fileName = String.format(Locale.US, "Model_%s_%s.pdb", model, query);
        final File file = FileUtil.resolveUniqueFilePath(dir.toPath(), fileName).toFile();

        final List<String> lines = createModelPdbFileContent(model, query);

        FileUtil.dumpToFile(file, lines);
    }

    /**
     * Prepares a complete dump of the model.
     * @param model The DNA/ RNA model.
     * @param query The DNA/ RNA sequence.
     * @return List of lines that make up the PDB file.
     */
    public static List<String> createModelPdbFileContent(AbstractModel.Model model, String query) {
        final AbstractModel.Helix helix = AbstractModel.createModel(model, query);
        final List<String> lines = new ArrayList<>();

        final AtomicInteger atomNumber = new AtomicInteger(1);
        final AtomicInteger chainID = new AtomicInteger('A');
        final AtomicInteger resID = new AtomicInteger(1);

        final Consumer<List<AbstractModel.Nucleotide>> filler = (nucleotides) -> {
            final String chainIDString = " " + (char) chainID.get();

            for (AbstractModel.Nucleotide nucleotide : nucleotides) {
                final char letter = nucleotide.letter();
                boolean thymine = letter == 'T';

                String residue_chain_resID = "D" + letter + chainIDString;
                final String currentResID = Integer.toString(resID.getAndIncrement());
                residue_chain_resID += getPadding(currentResID, 4) + currentResID;

                for (AbstractModel.CylinderCoords coords : nucleotide.sugar()) {
                    final String[] xyz = calculateXYZ(coords);
                    final String line = formatLine(atomNumber.getAndIncrement(),
                            coords.atom() + "'", residue_chain_resID, xyz);

                    lines.add(line);
                }

                final List<AbstractModel.CylinderCoords> baseCoords = nucleotide.base();

                if (thymine) {
                    for (int i = 0; i < baseCoords.size(); i++) {
                        final AbstractModel.CylinderCoords coords = baseCoords.get(i);

                        final String atomName = i == 2 ? "C7" : coords.atom();
                        final String[] xyz = calculateXYZ(coords);
                        final String line = formatLine(atomNumber.getAndIncrement(),
                                atomName, residue_chain_resID, xyz);

                        lines.add(line);
                    }
                } else {
                    for (AbstractModel.CylinderCoords coords : baseCoords) {
                        final String[] xyz = calculateXYZ(coords);
                        final String line = formatLine(atomNumber.getAndIncrement(),
                                coords.atom(), residue_chain_resID, xyz);

                        lines.add(line);
                    }
                }

                final List<AbstractModel.CylinderCoords> phosphateCoords = nucleotide.phosphate();

                for (int i = 0; i < phosphateCoords.size(); i++) {
                    final AbstractModel.CylinderCoords coords = phosphateCoords.get(i);
                    final String atomName = PHOSPHATE_NAMES[i];
                    final String[] xyz = calculateXYZ(coords);
                    final String line = formatLine(atomNumber.getAndIncrement(),
                            atomName, residue_chain_resID, xyz);

                    lines.add(line);
                }
            }
        };

        filler.accept(helix.strand1);

        final List<AbstractModel.Nucleotide> otherStrand = helix.strand2;
        if (otherStrand != null) {
            chainID.incrementAndGet();
            filler.accept(otherStrand);
        }

        return lines;
    }

    private static final DecimalFormat FLOAT_FORMAT = new DecimalFormat("0.000", new DecimalFormatSymbols(Locale.US));

    /**
     * Calculates x, y and z coordinates from a set of cylindrical coordinates.
     * @param coords The set of cylindrical coordinates with radius, angle and height.
     * @return An array of string representations of the cartesian coordinates with three decimal places.
     */
    static String[] calculateXYZ(AbstractModel.CylinderCoords coords) {
        final double r = coords.radius();
        final double theta = Math.toRadians(coords.theta());

        return new String[]{
                FLOAT_FORMAT.format(r * Math.cos(theta)),
                FLOAT_FORMAT.format(r * Math.sin(theta)),
                FLOAT_FORMAT.format(coords.height())
        };
    }

    /**
     * Formats the data so that it can be parsed by e.g. PyMOL, padding where necessary.
     * @param atomNumber Current unique number of the atom inside the file.
     * @param atomName three-character short name of the atom in the context of a nucleic acid.
     * @param residue_chain_resID Residue name (e.g. "DA" for adenine) + chain ID (A or B, depending on the strand) +
     *                            residue ID (each nucleotide has its own number inside the file)
     * @param xyz Array of cartesian coordinates for the current atom, obtained from {@link PDB#calculateXYZ(AbstractModel.CylinderCoords)}.
     * @return A line representing the current atom that can be parsed by a simulation tool like PyMOL or Coot.
     */
    static String formatLine(int atomNumber, String atomName, String residue_chain_resID, String[] xyz) {
        final String atomNumberString = Integer.toString(atomNumber);
        final String atomNumberPadding = getPadding(atomNumberString, 7);
        final String atomNamePadding = getPadding(atomName, 5);
        final String xPadding = getPadding(xyz[0], 12);
        final String yPadding = getPadding(xyz[1], 8);
        final String zPadding = getPadding(xyz[2], 8);
        final String atomSymbol = switch (atomName.charAt(0)) {
            case 'C' -> "C";
            case 'H' -> "H";
            case 'N' -> "N";
            case 'O' -> "O";
            case 'P' -> "P";
            default -> throw new IllegalStateException("Unexpected value: " + atomName.charAt(0));
        };

        return String.join("", "ATOM", atomNumberPadding, atomNumberString, "  ",
                atomName, atomNamePadding, residue_chain_resID,
                xPadding, xyz[0], yPadding, xyz[1], zPadding, xyz[2],
                "  1.00  0.00           ", atomSymbol);
    }

    // Preallocate padding
    private static final String[] SPACE_PADDING = new String[16];

    /**
     * Determines how much padding is needed to extend the input string to the target length.
     * @param str Input string.
     * @param targetLength Length of the string after padding has been applied.
     * @return A white space string, consisting of only spaces, or the empty string "".
     */
    static String getPadding(String str, int targetLength) {
        return SPACE_PADDING[targetLength - str.length()];
    }

    static {
        for (int i = 0; i < SPACE_PADDING.length; i++) {
            SPACE_PADDING[i] = " ".repeat(i);
        }

        FLOAT_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    private PDB() {}
}
