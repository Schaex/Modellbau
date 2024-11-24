package com.pfaff.maximilian.model;

class A_DNA extends AbstractModel {
    private static final String[] namesPhosphate = {"O1", "O2", "O3", "P1", "O4"};
    private static final int[][] coordsPhosphate = {{958, 744, -287}, {996, 679, -510}, {769, 739, -453}, {892, 692, -406}, {870, 601, -333}};
    private static final String[] namesDeoxyribose = {"C5", "O4", "C4", "C3", "C2", "C1"};
    private static final int[][] coordsDeoxyribose = {{991, 554, -319}, {922, 440, -186}, {977, 466, -313}, {891, 422, -417}, {881, 334, -350}, {859, 359, -204}};
    private static final String[] namesAdenine = {"N9", "C8", "N7", "C5", "N6", "C6", "N1", "C2", "N3", "C4"};
    private static final int[][] coordsAdenine = {{716, 361, -162}, {640, 457, -161}, {518, 448, -119}, {522, 298, -91}, {319, 148, -13}, {449, 167, -43}, {540, 53, -26}, {665, 84, -56}, {725, 173, -102}, {652, 267, -117}};
    private static final String[] namesGuanine = {"N9", "C8", "N7", "C5", "O6", "C6", "N1", "N2", "C2", "N3", "C4"};
    private static final int[][] coordsGuanine = {{716, 361, -162}, {638, 457, -160}, {514, 446, -117}, {520, 294, -90}, {327, 132, -11}, {444, 164, -41}, {546, 53, -27}, {771, 7, -37}, {678, 81, -57}, {730, 174, -103}, {652, 266, -117}};
    private static final String[] namesCytosine = {"N1", "C6", "C5", "N4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsCytosine = {{716, 361, -162}, {655, 463, -166}, {528, 491, -129}, {330, 345, -46}, {457, 355, -84}, {541, 235, -80}, {761, 192, -117}, {669, 259, -119}};
    private static final String[] namesThymine = {"N1", "C6", "Me", "C5", "O4", "C4", "N3", "O2", "C2"};
    private static final int[][] coordsThymine = {{716, 361, -162}, {655, 464, -167}, {498, 657, -133}, {530, 493, -130}, {330, 355, -47}, {448, 364, -83}, {539, 243, -82}, {756, 190, -115}, {671, 260, -120}};

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
        return 256;
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
