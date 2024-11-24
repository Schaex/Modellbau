package com.pfaff.maximilian.model;

class A_RNA extends AbstractModel {
    private static final String[] namesPhosphate = {"O1", "O2", "O3", "P1", "O4"};
    private static final int[][] coordsPhosphate = {{954, 728, -244}, {959, 712, -494}, {750, 766, -380}, {871, 705, -375}, {849, 601, -348}};
    private static final String[] namesRibose = {"C5", "O4", "C4", "C3", "O2", "C2", "C1"};
    private static final int[][] coordsRibose = {{975, 558, -335}, {915, 453, -177}, {968, 469, -310}, {886, 414, -402}, {1015, 302, -314}, {882, 332, -317}, {855, 369, -177}};
    private static final String[] namesAdenine = {"N9", "C8", "N7", "C5", "N6", "C6", "N1", "C2", "N3", "C4"};
    private static final int[][] coordsAdenine = {{712, 372, -139}, {639, 470, -133}, {515, 462, -95}, {514, 309, -75}, {308, 156, -5}, {439, 174, -34}, {528, 55, -24}, {654, 87, -54}, {715, 180, -93}, {644, 276, -101}};
    private static final String[] namesGuanine = {"N9", "C8", "N7", "C5", "O6", "C6", "N1", "N2", "C2", "N3", "C4"};
    private static final int[][] coordsGuanine = {{712, 372, -139}, {636, 470, -132}, {511, 460, -94}, {513, 305, -74}, {316, 139, -4}, {434, 171, -33}, {535, 55, -26}, {758, 7, -42}, {667, 85, -55}, {720, 180, -94}, {644, 275, -101}};
    private static final String[] namesCytosine = {"N1", "C6", "C5", "N4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsCytosine = {{712, 372, -139}, {653, 476, -138}, {526, 506, -103}, {323, 361, -31}, {450, 368, -67}, {532, 244, -68}, {751, 199, -106}, {661, 268, -104}};
    private static final String[] namesUracil = {"N1", "C6", "C5", "O4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsUracil = {{712, 372, -139}, {654, 477, -138}, {528, 508, -104}, {324, 371, -32}, {442, 378, -66}, {531, 252, -69}, {746, 196, -104}, {663, 269, -104}};

    /**
     * @return The angle that the helix turns with each nucleotide times 10.
     */
    @Override
    protected int helixTurn() {
        return 327;
    }

    /**
     * @return The distance the helix rises with each nucleotide times 100.
     */
    @Override
    protected int elevation() {
        return 281;
    }

    /**
     * @return The atom names and base coordinates of the phosphate.
     */
    @Override
    protected Value getPhosphateData() {
        return new Value(namesPhosphate, coordsPhosphate);
    }

    /**
     * @return The atom names and base coordinates of the ribose or deoxyribose.
     */
    @Override
    protected Value getSugarData() {
        return new Value(namesRibose, coordsRibose);
    }

    private static final Value A = new Value(namesAdenine, coordsAdenine);
    private static final Value U = new Value(namesUracil, coordsUracil);
    private static final Value G = new Value(namesGuanine, coordsGuanine);
    private static final Value C = new Value(namesCytosine, coordsCytosine);

    /**
     * @param letterCode One-letter code of a nucleic base.
     * @return The atom names and base coordinates of the given nucleic base.
     */
    @Override
    protected Value eval(char letterCode) {
        return switch (letterCode) {
            case 'a', 'A' -> A;
            case 'u', 'U' -> U;
            case 'g', 'G' -> G;
            case 'c', 'C' -> C;
            default -> throw new IllegalStateException("Unexpected value: " + letterCode);
        };
    }
}
