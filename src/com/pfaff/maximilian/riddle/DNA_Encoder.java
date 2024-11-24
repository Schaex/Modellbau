package com.pfaff.maximilian.riddle;

import java.util.Locale;
import java.util.Random;

public final class DNA_Encoder {
    private static final String STOP_CODON = "TAA";
    private static final Node[] NODES;

    static {
        // Alphabetically sorted array for quick access to letters
        NODES = new Node[]{
                new Node('A', "GCT", "GCC", "GCA", "GCG"),
                null,//new Node('B'),
                new Node('C', "TGT", "TGC"),
                new Node('D', "GAT", "GAC"),
                new Node('E', "GAA", "GAG"),
                new Node('F', "TTT", "TTC"),
                new Node('G', "GGT", "GGC", "GGA", "GGG"),
                new Node('H', "CAT", "CAC"),
                new Node('I', "ATT", "ATC", "ATA"),
                null,//new Node('J'),
                new Node('K', "AAA", "AAG"),
                new Node('L', "TTA", "TTG", "CTT", "CTC", "CTA", "CTG"),
                new Node('M', "ATG"),
                new Node('N', "AAT", "AAC"),
                new Node('O', "TAG"),
                new Node('P', "CCT", "CCC", "CCA", "CCG"),
                new Node('Q', "CAA", "CAG"),
                new Node('R', "CGT", "CGC", "CGA", "CGG", "AGA", "AGG"),
                new Node('S', "TCT", "TCC", "TCA", "TCG", "AGT", "AGC"),
                new Node('T', "ACT", "ACC", "ACA", "ACG"),
                new Node('U', "TGA"),
                new Node('V', "GTT", "GTC", "GTA", "GTG"),
                new Node('W', "TGG"),
                null,//new Node('X'),
                new Node('Y', "TAT", "TAC"),
                null,//new Node('Z')
        };
    }

    /**
     * @param peptide Amino acid sequence.
     * @return Possible sequence of respective coding DNA strand.
     */
    public static String encode(String peptide) {
        final char[] chars = peptide.toUpperCase(Locale.US).toCharArray();

        if (chars[0] != 'M') {
            throw new IllegalArgumentException("Peptide sequence must start with methionine (M)! Input: " + peptide);
        }

        final Random random = new Random();
        final StringBuilder builder = new StringBuilder();

        for (char c : chars) {
            if (c < 'A' || c > 'Z') {
                // Not a letter, e.g. comma or space
                builder.append(c);
            } else {
                final Node node = NODES[c - 'A'];
                final String[] codons = node.codons;
                final int index = codons.length == 1 ? 0 : random.nextInt(codons.length);

                builder.append(codons[index]);
            }
        }

        return builder + STOP_CODON;
    }

    /**
     * @param codingStrand DNA sequence of the coding strand.
     * @return Amino acid sequence after transcription and translation of the template strand.
     */
    public static String decode(String codingStrand) {
        final char[] chars = codingStrand.toUpperCase(Locale.US).toCharArray();

        final StringBuilder builder = new StringBuilder();

        int index = 0;

        while (index < chars.length) {
            final char c = chars[index++];

            if (c < 'A' || c > 'T') {
                // Not a letter, e.g. comma or space
                builder.append(c);
            } else {
                final String codon = "" + c + chars[index++] + chars[index++];
                final char aminoAcid = getAminoAcidFromCodon(codon);

                builder.append(aminoAcid);
            }
        }

        return builder.toString();
    }

    /**
     * Iterates over the array to find the node with the specified codon.
     * @param codon A three-letter code consisting of 'A', 'T', 'G' and 'C'.
     * @return The amino acid that is encoded with the codon, or the null character if it's the {@link DNA_Encoder#STOP_CODON}.
     */
    public static char getAminoAcidFromCodon(String codon) {
        // find the correct codon with brute force!
        for (Node node : NODES) {
            if (node == null) {
                continue;
            }

            for (String entry : node.codons) {
                if (codon.equals(entry)) {
                    return node.aminoAcid;
                }
            }
        }

        // The stop codon is the only acceptable case left
        if (codon.equals(STOP_CODON)) {
            return 0;
        }

        throw new IllegalArgumentException("Illegal codon: " + codon);
    }

    /**
     * Utility container for the mapping "one-letter code -> codon".
     * @param aminoAcid One-letter code of the amino acid.
     * @param codons Possible base triplets that encode the amino acid in the coding DNA strand.
     */
    private record Node(char aminoAcid, String... codons) {}

    private DNA_Encoder() {}
}
