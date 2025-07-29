public class Board {
    // Pixel dimensions of each cell
    private final int cellHPix; // Height of a cell in pixels
    private final int cellWPix; // Width of a cell in pixels

    // Number of cells horizontally and vertically
    private final int wCells; // Number of columns
    private final int hCells; // Number of rows

    // Image representing the board (could be drawn to)
    private final Img img;

    // Physical size of each cell in meters
    private final double cellHM; // Height of a cell in meters
    private final double cellWM; // Width of a cell in meters

    // Constructor with default physical cell size (1.0 meter)
    public Board(int cellHPix, int cellWPix,
                 int wCells, int hCells,
                 Img img) {
        this(cellHPix, cellWPix, wCells, hCells, img, 1.0, 1.0);
    }

    // Constructor that allows specifying physical cell dimensions
    public Board(int cellHPix, int cellWPix,
                 int wCells, int hCells,
                 Img img,
                 double cellHM, double cellWM) {
        this.cellHPix = cellHPix;
        this.cellWPix = cellWPix;
        this.wCells = wCells;
        this.hCells = hCells;
        this.img = img;
        this.cellHM = cellHM;
        this.cellWM = cellWM;
    }

    /* ------------ convenience ------------- */

    // Clone the board and make a deep copy of the image (if it exists)
    public Board cloneBoard() {
        Img newImg;
        if (img != null && img.get() != null) {
            // Make a copy of the image
            java.awt.image.BufferedImage copy = new java.awt.image.BufferedImage(
                    img.get().getColorModel(),
                    img.get().copyData(null),
                    img.get().isAlphaPremultiplied(),
                    null);
            BuffImg bi = new BuffImg();
            bi.setBufferedImage(copy);
            newImg = bi;
        } else {
            newImg = new BuffImg();
        }
        return new Board(cellHPix, cellWPix, wCells, hCells, newImg, cellHM, cellWM);
    }

    // Show the board's image (if one exists)
    public void show() {
        if (img != null) img.show();
    }

    /* ------------ conversions ------------- */

    /** Convert (x, y) in metres to board cell (row, col). */
    public int[] mToCell(double xM, double yM) {
        int col = (int) Math.round(xM / cellWM); // x => column
        int row = (int) Math.round(yM / cellHM); // y => row
        return new int[]{row, col};
    }

    // Same as mToCell, but returns a Moves.Pair object instead of array
    public Pair mToCellPair(double xM, double yM) {
        int col = (int) Math.round(xM / cellWM);
        int row = (int) Math.round(yM / cellHM);
        return new Pair(row, col);
    }

    /** Convert a cell (row, col) to its top-left corner in metres. */
    public double[] cellToM(int row, int col) {
       return new double[]{col * cellWM, row * cellHM};
    }

    // Overload of cellToM using Moves.Pair
    public double[] cellToM(Pair cell) {
        return cellToM(cell.r, cell.c);
    }

    /** Convert (x, y) in metres to pixel coordinates. */
    public int[] mToPix(double xM, double yM) {
        int xPx = (int) Math.round(xM / cellWM * cellWPix); // Convert x meters to pixels
        int yPx = (int) Math.round(yM / cellHM * cellHPix); // Convert y meters to pixels
        return new int[]{xPx, yPx};
    }

    /* ------------ getters ------------- */
    public int getCellHPix() { return cellHPix; }
    public int getCellWPix() { return cellWPix; }
    public int getWCells() { return wCells; }
    public int getHCells() { return hCells; }
    public Img getImg() { return img; }
}
