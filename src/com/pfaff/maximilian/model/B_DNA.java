package com.pfaff.maximilian.model;

class B_DNA extends AbstractModel {
    private static final String[] namesPhosphate = {"O1", "O2", "O3", "P1", "O4"};
    private static final int[][] coordsPhosphate = {{875, 974, 363}, {1020, 911, 186}, {882, 1033, 129}, {891, 952, 208}, {773, 880, 183}};
    private static final String[] namesDeoxyribose = {"C5", "O4", "C4", "C3", "C2", "C1"};
    private static final int[][] coordsDeoxyribose = {{770, 798, 277}, {622, 660, 183}, {759, 699, 204}, {820, 699, 64}, {704, 732, -24}, {586, 674, 47}};
    private static final String[] namesAdenine = {"N9", "C8", "N7", "C5", "N6", "C6", "N1", "C2", "N3", "C4"};
    private static final int[][] coordsAdenine = {{463, 766, 42}, {484, 930, 50}, {395, 1054, 43}, {274, 940, 28}, {183, 1540, 14}, {141, 1072, 15}, {86, 401, 3}, {217, 306, 4}, {324, 470, 16}, {333, 705, 28}};
    private static final String[] namesGuanine = {"N9", "C8", "N7", "C5", "O6", "C6", "N1", "N2", "C2", "N3", "C4"};
    private static final int[][] coordsGuanine = {{463, 766, 42}, {482, 932, 50}, {392, 1057, 42}, {270, 940, 28}, {171, 1546, 13}, {139, 1093, 15}, {92, 379, 3}, {301, 42, -10}, {228, 287, 3}, {329, 467, 16}, {333, 703, 28}};
    private static final String[] namesCytosine = {"N1", "C6", "C5", "N4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsCytosine = {{463, 766, 42}, {499, 922, 52}, {435, 1070, 47}, {276, 1366, 27}, {294, 1100, 32}, {231, 839, 22}, {369, 479, 18}, {340, 674, 27}};
    private static final String[] namesThymine = {"N1", "C6", "Me", "C5", "O4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsThymine = {{463, 766, 42}, {501, 923, 52}, {540, 1198, 58}, {438, 1069, 47}, {282, 1363, 27}, {298, 1119, 32}, {236, 852, 23}, {364, 478, 18}, {342, 673, 27}};

    /**
     * @return The angle that the helix turns with each nucleotide times 10.
     */
    @Override
    protected int helixTurn() {
        return 360;
    }

    /**
     * @return The distance the helix rises with each nucleotide times 100.
     */
    @Override
    protected int elevation() {
        return 338;
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
        return new Value(namesDeoxyribose, coordsDeoxyribose);
    }

    private static final Value A = new Value(namesAdenine, coordsAdenine);
    private static final Value T = new Value(namesThymine, coordsThymine);
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
            case 't', 'T' -> T;
            case 'g', 'G' -> G;
            case 'c', 'C' -> C;
            default -> throw new IllegalStateException("Unexpected value: " + letterCode);
        };
    }
}
