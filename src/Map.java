import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class Map extends JPanel {

	private static final long serialVersionUID = 1L;
	// width and height of the JPanel
	private int width, height;
	// width and height of the actual map grid
	private int gridWidth, gridHeight;
	// average of grid width and height
	private int avgGridDim;
	// the actual map grid
	private int[][] map;

	private static final int GRASS = 0;
	private static final int TREES = 1;
	private static final int WATER = 2;
	private static final int DESERT_SAND = 3;
	private static final int ROCK = 4;
	private static final int SAND = 5;
	private static final int SNOW = 6;
	private static final int TEMP = 7;

	private static final int NORTH = 0;
	private static final int WEST = 1;
	private static final int SOUTH = 2;
	private static final int EAST = 3;

	public Map(int width, int height, int gridWidth, int gridHeight) {
		this.width = width;
		this.height = height;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.avgGridDim = (gridWidth + gridHeight) / 2;
		this.map = new int[gridHeight][gridWidth];
		// set preferred size of the JPanel
		setPreferredSize(new Dimension(width, height));
		setBackground(new Color(0, 0, 0));
	}

	// set all map tiles to plains
	private void initMap() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				map[y][x] = GRASS;
			}
		}
	}

	// generates the map
	public void genMap() {
		initMap();
		int mountainInitSide = (Math.random() < 0.5) ? NORTH : SOUTH;
		boolean mountainGenSign = (Math.random() > 0.5) ? true : false;
		initMountainRange(mountainInitSide, mountainGenSign);
		genDesert(mountainInitSide, mountainGenSign);
		genMountains();
		genForests();
		genRiver(mountainGenSign);
		genLakes();
		genSpring();
		genRocks();
		genBeaches();
		genOases();
		genIceCaps();
	}

	// generates mountain range, taking a side of the map to start the range on
	private void initMountainRange(int side, boolean sign) {
		// distance on the x axis of the origin from the nearest side of the map
		int xOffset = (int) ((Math.random() * 0.15 * gridWidth) + (0.25 * gridWidth));
		// x and y positions of the origin of the mountain range
		int x = (sign) ? gridWidth - (xOffset + 1) : xOffset;
		int y = (side == NORTH) ? 0 : gridHeight - 1;
		// the side of the y axis of the origin
		boolean ySign = y > gridHeight / 2;
		// generates mountains until it reaches an edge of the map
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0) {
			// places a temporary tile to denote the location of the range
			map[y][x] = TEMP;
			double rand = Math.random();
			// chance to make the mountain range go north/south
			if (rand < 0.65) {
				y += (ySign) ? -1 : 1;
				// chance to make the mountain range go east/west
			} else {
				x += (sign == rand < 0.92) ? 1 : -1;
			}
		}
	}

	// completes the generation of the mountain range by spreading rock from all
	// "temp" tiles
	private void genMountains() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (map[y][x] == TEMP) {
					spreadMountain(x, y, (int) (Math.random() * avgGridDim / 16 + avgGridDim / 16));
				}
			}
		}
	}

	// spreads a mountain from a certain point
	private void spreadMountain(int x, int y, int i) {
		if (i < 0) {
			return;
		}
		// sets the current tile to be forest
		map[y][x] = ROCK;
		// checks if adjacent tiles are plains, and if so, recursively calls the
		// function to spread mountain until i reaches 0, with a chance of
		// stopping before that to create more irregular shapes
		if (y > 0) {
			if ((map[y - 1][x] == GRASS || map[y - 1][x] == DESERT_SAND) && Math.random() < i * 0.75) {
				spreadMountain(x, y - 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x > 0) {
			if ((map[y][x - 1] == GRASS || map[y][x - 1] == DESERT_SAND) && Math.random() < i * 0.75) {
				spreadMountain(x - 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (y < gridHeight - 1) {
			if ((map[y + 1][x] == GRASS || map[y + 1][x] == DESERT_SAND) && Math.random() < i * 0.75) {
				spreadMountain(x, y + 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x < gridWidth - 1) {
			if ((map[y][x + 1] == GRASS || map[y][x + 1] == DESERT_SAND) && Math.random() < i * 0.75) {
				spreadMountain(x + 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
	}

	// generates river
	private void genRiver(boolean sign) {
		// the x and y positions of the start of the river
		int x;
		int y;
		// 30% chance for river to start at the top or bottom of the map
		if (Math.random() < 0.3) {
			// equal chances of the river starting at the top or bottom
			if (Math.random() < 0.5) {
				y = 0;
			} else {
				y = gridHeight - 1;
			}
			// makes the river start near the corner of the map
			int xOffset = (int) (Math.random() * 0.4 * gridWidth);
			// sets the initial x based on where the mountains are
			x = (sign) ? xOffset : gridWidth - (xOffset + 1);
			// 70% chance for river to start on the side of the map opposite the
			// mountain range
		} else {
			// sets initial x to the side of the map opposite the mountain range
			x = (sign) ? 0 : gridWidth - 1;
			// sets the initial y near one of the corners of the map
			int yOffset = (int) (Math.random() * 0.3 * gridHeight);
			y = (Math.random() < 0.5) ? yOffset : gridHeight - (yOffset + 1);
		}
		// the location of the river origin on the x axis. true is on the east,
		// false is on the west
		boolean xSign = x > gridWidth / 2;
		// the location of the river origin on the y axis. true is on the south,
		// false is on the north
		boolean ySign = y > gridHeight / 2;
		// whether or not the river has reached a point near the center of the
		// map, used to make the river curve away from the mountain range
		boolean turn = false;
		// generates the river by changing the current x and y positions and
		// setting them to be water. the river tends to move more north/south,
		// and has a curve
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0) {
			map[y][x] = WATER;
			// makes the river curve away from the mountain range
			if ((xSign && x < gridWidth * 0.6) || (!xSign && x > gridWidth * 0.4)) {
				turn = true;
			}
			// 60% chance to make the river go north/south
			if (Math.random() < 0.6) {
				y += (ySign) ? -1 : 1;
				// 40% chance to make the river go east/west, reversing
				// direction if turn is true
			} else {
				x += (xSign == turn) ? 1 : -1;
			}
		}
	}

	// generates the desert next to the mountains
	private void genDesert(int side, boolean sign) {
		// checks if the mountain range starts on the northern edge of the map
		if (side == NORTH) {
			// starts the desert on the northeast corner of the map
			if (sign) {
				desertify(gridWidth - 1, 0);
				// starts the desert on the northeast corner of the map
			} else {
				desertify(0, 0);
			}
			// runs if the mountain range starts on the southern edge of the map
		} else {
			// starts the desert on the southeast corner of the map
			if (sign) {
				desertify(gridWidth - 1, gridHeight - 1);
				// starts the desert on the southwest corner of the map
			} else {
				desertify(0, gridHeight - 1);
			}
		}
	}

	// spreads desert until it reaches mountains
	private void desertify(int x, int y) {
		// sets current tile to be desert
		map[y][x] = DESERT_SAND;
		// checks if adjacent tiles are mountain or desert, and if not,
		// recursively calls the function to spread desert around the area
		// enclosed by the mountain range and the map edges
		if (y > 0) {
			if (map[y - 1][x] != TEMP && map[y - 1][x] != DESERT_SAND) {
				desertify(x, y - 1);
			}
		}
		if (x > 0) {
			if (map[y][x - 1] != TEMP && map[y][x - 1] != DESERT_SAND) {
				desertify(x - 1, y);
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] != TEMP && map[y + 1][x] != DESERT_SAND) {
				desertify(x, y + 1);
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] != TEMP && map[y][x + 1] != DESERT_SAND) {
				desertify(x + 1, y);
			}
		}
	}

	// generates forests
	private void genForests() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// small chance to start a forest on plains
				if (map[y][x] == GRASS && Math.random() < 0.025) {
					growForest(x, y, (int) (Math.random() * Math.pow(avgGridDim, 0.4)) + 1);
				}
			}
		}
	}

	// spreads forests
	private void growForest(int x, int y, int i) {
		if (i < 0) {
			return;
		}
		// sets the current tile to be forest
		map[y][x] = TREES;
		// checks if adjacent tiles are plains, and if so, recursively calls the
		// function to spread forest until i reaches 0, with a chance of
		// stopping before that to create more irregular shapes
		if (y > 0) {
			if (map[y - 1][x] == GRASS && Math.random() < i * 0.75) {
				growForest(x, y - 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x > 0) {
			if (map[y][x - 1] == GRASS && Math.random() < i * 0.75) {
				growForest(x - 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] == GRASS && Math.random() < i * 0.75) {
				growForest(x, y + 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] == GRASS && Math.random() < i * 0.75) {
				growForest(x + 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
	}

	// generate patches of mountain
	private void genRocks() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// small chance to place a rock on tiles that aren't water, to
				// prevent ruining the river
				if (map[y][x] != WATER && Math.random() < 0.03) {
					map[y][x] = ROCK;
				}
			}
		}
	}

	// generates lakes
	private void genLakes() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// small chance to start a lake in plains or forests or deserts
				if ((map[y][x] == GRASS || map[y][x] == TREES) && Math.random() < 3 * Math.pow(avgGridDim, -1.6)) {
					placeLake(x, y, (int) (Math.random() * Math.pow(avgGridDim, 0.3)) + 1);
				} else if (map[y][x] == DESERT_SAND && Math.random() < 3 * Math.pow(avgGridDim, -1.6)) {
					placeLake(x, y, (int) (Math.random() * Math.pow(avgGridDim, 0.2)) + 1);
				}
			}
		}
	}

	// places a lake
	private void placeLake(int x, int y, int i) {
		if (i < 0) {
			return;
		}
		// sets the current tile to be forest
		map[y][x] = WATER;
		// checks if adjacent tiles are water, and if so, recursively calls the
		// function to spread water until i reaches 0, with a chance of
		// stopping before that to create more irregular shapes
		if (y > 0) {
			if (map[y - 1][x] != ROCK && map[y - 1][x] != WATER && Math.random() < i * 0.75) {
				placeLake(x, y - 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x > 0) {
			if (map[y][x - 1] != ROCK && map[y][x - 1] != WATER && Math.random() < i * 0.75) {
				placeLake(x - 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] != ROCK && map[y + 1][x] != WATER && Math.random() < i * 0.75) {
				placeLake(x, y + 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] != ROCK && map[y][x + 1] != WATER && Math.random() < i * 0.75) {
				placeLake(x + 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
	}

	// generate oases nest to water in the desert
	private void genOases() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// if the tile is adjacent to water
				boolean adj = getAdjacentTile(x, y, 0) == WATER || getAdjacentTile(x, y, 1) == WATER
						|| getAdjacentTile(x, y, 2) == WATER || getAdjacentTile(x, y, 3) == WATER;
				// small chance to place plains or forest on tiles that are next
				// to water
				if (map[y][x] == DESERT_SAND && adj && Math.random() < 0.7) {
					map[y][x] = (Math.random() < 0.4) ? GRASS : TREES;
				}
			}
		}
	}

	// places sand next to water
	private void genBeaches() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// if the tile is adjacent to water
				boolean adj = getAdjacentTile(x, y, 0) == WATER || getAdjacentTile(x, y, 1) == WATER
						|| getAdjacentTile(x, y, 2) == WATER || getAdjacentTile(x, y, 3) == WATER;
				// chance to place sand on tiles that are next
				// to water
				if ((map[y][x] == GRASS || map[y][x] == TREES) && adj && Math.random() < 0.6) {
					map[y][x] = SAND;
				}
			}
		}
	}

	// places ice caps on mountain tops
	private void genIceCaps() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// if the tile is only adjacent to snow and rock
				boolean adj = (getAdjacentTile(x, y, NORTH) == SNOW || getAdjacentTile(x, y, NORTH) == ROCK)
						&& (getAdjacentTile(x, y, WEST) == SNOW || getAdjacentTile(x, y, WEST) == ROCK)
						&& (getAdjacentTile(x, y, SOUTH) == SNOW || getAdjacentTile(x, y, SOUTH) == ROCK)
						&& (getAdjacentTile(x, y, EAST) == SNOW || getAdjacentTile(x, y, EAST) == ROCK);
				// if the tiles corner neighbors are all rock or snow
				boolean cor = (getCornerTile(x, y, NORTH) == SNOW || getCornerTile(x, y, NORTH) == ROCK)
						&& (getCornerTile(x, y, WEST) == SNOW || getCornerTile(x, y, WEST) == ROCK)
						&& (getCornerTile(x, y, SOUTH) == SNOW || getCornerTile(x, y, SOUTH) == ROCK)
						&& (getCornerTile(x, y, EAST) == SNOW || getCornerTile(x, y, EAST) == ROCK);
				// chance to place snow on tiles that are only neighboring
				// rock and snow
				if ((map[y][x] == ROCK) && adj && cor && Math.random() < 0.5) {
					map[y][x] = SNOW;
				}
			}
		}
	}

	// has a chance to create rivers flowing from the mountain range
	private void genSpring() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// if the tile is only adjacent to snow and rock
				boolean adj = (getAdjacentTile(x, y, NORTH) == SNOW || getAdjacentTile(x, y, NORTH) == ROCK)
						&& (getAdjacentTile(x, y, WEST) == SNOW || getAdjacentTile(x, y, WEST) == ROCK)
						&& (getAdjacentTile(x, y, SOUTH) == SNOW || getAdjacentTile(x, y, SOUTH) == ROCK)
						&& (getAdjacentTile(x, y, EAST) == SNOW || getAdjacentTile(x, y, EAST) == ROCK);
				// if the tiles corner neighbors are all rock or snow
				boolean cor = (getCornerTile(x, y, NORTH) == SNOW || getCornerTile(x, y, NORTH) == ROCK)
						&& (getCornerTile(x, y, WEST) == SNOW || getCornerTile(x, y, WEST) == ROCK)
						&& (getCornerTile(x, y, SOUTH) == SNOW || getCornerTile(x, y, SOUTH) == ROCK)
						&& (getCornerTile(x, y, EAST) == SNOW || getCornerTile(x, y, EAST) == ROCK);
				// chance to start a river on a tile that is snow or rock
				if ((map[y][x] == ROCK || map[y][x] == SNOW) && adj && cor && Math.random() < 0.3 / avgGridDim) {
					carveRiver(x, y);
				}
			}
		}
	}

	private void carveRiver(int x, int y) {
		boolean flip = (Math.random() < 0.2);
		// the location of the river origin on the x axis. true is on the east,
		// false is on the west
		boolean xSign = x > gridWidth / 2;
		// generates the river by changing the current x and y positions and
		// setting them to be "temp" tiles so it can differentiate between when
		// it runs into itself and when it runs into a lake or river
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0 && map[y][x] != WATER) {
			map[y][x] = TEMP;
			// 40% chance to make the river go north/south
			if (Math.random() < 0.4) {
				// greater chance for the river to flow south
				y += (Math.random() < 0.25) ? -1 : 1;
				// 60% chance to make the river go east/west
			} else {
				x += (xSign != flip) ? -1 : 1;
			}
		}
		// replaces all "temp" tiles with water
		for (int yi = 0; yi < gridHeight; yi++) {
			for (int xi = 0; xi < gridWidth; xi++) {
				if (map[yi][xi] == TEMP) {
					map[yi][xi] = WATER;
				}
			}
		}
	}

	// returns the value of the tile adjacent to the given tile in the given
	// direction
	private int getAdjacentTile(int x, int y, int direction) {
		if (direction == NORTH && y > 0) {
			return map[y - 1][x];
		} else if (direction == WEST && x < gridWidth - 1) {
			return map[y][x + 1];
		} else if (direction == SOUTH && y < gridHeight - 1) {
			return map[y + 1][x];
		} else if (direction == EAST && x > 0) {
			return map[y][x - 1];
		} else {
			return map[y][x];
		}
	}

	// returns the value of the tile at the corner of the given tile in the
	// given direction
	private int getCornerTile(int x, int y, int direction) {
		if (direction == NORTH && y > 0 && x < gridWidth - 1) {
			return map[y - 1][x + 1];
		} else if (direction == EAST && x < gridWidth - 1 && y < gridHeight - 1) {
			return map[y + 1][x + 1];
		} else if (direction == SOUTH && y < gridHeight - 1 && x > 0) {
			return map[y + 1][x - 1];
		} else if (direction == WEST && x > 0 && y > 0) {
			return map[y - 1][x - 1];
		} else {
			return map[y][x];
		}
	}

	// updates the size of the JPanel
	public void updateSize() {
		this.width = getWidth();
		this.height = getHeight();
	}

	// renders the map
	public void draw(Graphics g) {
		updateSize();
		int tileSize = Math.min(width / gridWidth, height / gridHeight);
		int xOffset = (width - (tileSize * gridWidth)) / 2;
		int yOffset = (height - (tileSize * gridHeight)) / 2;
		Graphics2D g2 = (Graphics2D) g;
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// sets color depending on the biome of each tile
				switch (map[y][x]) {
				case GRASS:
					g2.setColor(new Color(60, 255, 60));
					break;
				case TREES:
					g2.setColor(new Color(20, 120, 20));
					break;
				case WATER:
					g2.setColor(new Color(0, 200, 255));
					break;
				case DESERT_SAND:
					g2.setColor(new Color(230, 230, 150));
					break;
				case ROCK:
					g2.setColor(new Color(120, 120, 120));
					break;
				case SAND:
					g2.setColor(new Color(255, 240, 160));
					break;
				case SNOW:
					g2.setColor(new Color(240, 240, 240));
					break;
				default:
					g2.setColor(getBackground());
					break;
				}
				// draws a rectangle to represent each tile
				g2.fillRect(xOffset + (x * tileSize), yOffset + (y * tileSize), tileSize, tileSize);
			}
		}
		g2.setColor(Color.black);
		g2.fillRect(0, 0, getWidth(), yOffset);
		g2.fillRect(0, getHeight() - yOffset, xOffset, yOffset);
		g2.fillRect(0, yOffset, xOffset, getHeight() - (yOffset * 2));
		g2.fillRect(getWidth() - xOffset, yOffset, xOffset, getHeight() - (yOffset * 2));
	}

}
