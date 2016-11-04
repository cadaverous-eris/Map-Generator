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

	private static final int PLAINS = 0;
	private static final int FOREST = 1;
	private static final int WATER = 2;
	private static final int DESERT = 3;
	private static final int MOUNTAIN = 4;

	private static final int NORTH = 0;
	private static final int EAST = 1;
	private static final int SOUTH = 2;
	private static final int WEST = 2;

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
				map[y][x] = PLAINS;
			}
		}
	}

	// generates the map
	public void genMap() {
		initMap();
		int mountainInitSide = (Math.random() < 0.5) ? 0 : 1;
		boolean mountainGenSign = (Math.random() > 0.5) ? true : false;
		genMountains(mountainInitSide, mountainGenSign);
		genDesert(mountainInitSide, mountainGenSign);
		genForests();
		genRiver(mountainGenSign);
		genRocks();
		genLakes();
		genOases();
	}

	// generates mountain range, taking a side of the map to start the range on
	private void genMountains(int side, boolean sign) {
		// distance from the nearest side of the map on the x axis
		int xOffset = (int) ((Math.random() * 0.15 * gridWidth) + (0.25 * gridWidth));
		// x and y positions of the origin of the mountain range
		int x = (sign) ? gridWidth - (xOffset + 1) : xOffset;
		int y = (side == 0) ? 0 : gridHeight - 1;
		// # of times the loop has run
		int i = 0;
		// generates mountains until it reaches an edge of the map
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0) {
			map[y][x] = MOUNTAIN;
			// chance to place mountains around the currently selected tile
			if (x < gridWidth - 1 && Math.random() < 0.7) {
				map[y][x + 1] = MOUNTAIN;
			}
			if (x > 0 && Math.random() < 0.7) {
				map[y][x - 1] = MOUNTAIN;
			}
			if (y < gridHeight - 1 && Math.random() < 0.7) {
				map[y + 1][x] = MOUNTAIN;
			}
			if (y > 0 && Math.random() < 0.7) {
				map[y - 1][x] = MOUNTAIN;
			}
			// gives a high probability for the mountain range to go
			// north/south, which decreases over time
			if (Math.random() < Math.pow(0.9, i / 2)) {
				if (side == 0) {
					y += 1;
				} else {
					y -= 1;
				}
				// gives a high probability for the mountain range to go east,
				// which decreases over time
			} else if (Math.random() < 1 - Math.pow(0.2, i)) {
				if (sign) {
					x += 1;
				} else {
					x -= 1;
				}
				// a small, but increasing chance for the mountain range to go
				// back toward the closest side on the x axis to it's origin
			} else {
				if (!sign) {
					x += 1;
				} else {
					x -= 1;
				}
			}
			i++;
		}
	}

	// generates river
	private void genRiver(boolean sign) {
		// the x and y positions of the start of the river
		int x;
		int y;
		// 30% chance for river to start at the top or bottom of the map
		if (Math.random() < 0.3) {
			// equal chances of the river starting at thetop or bottom
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
		if (side == 0) {
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
		map[y][x] = DESERT;
		// checks if adjacent tiles are mountain or desert, and if not,
		// recursively calls the function to spread desert around the area
		// enclosed by the mountain range and the map edges
		if (y > 0) {
			if (map[y - 1][x] != MOUNTAIN && map[y - 1][x] != DESERT) {
				desertify(x, y - 1);
			}
		}
		if (x > 0) {
			if (map[y][x - 1] != MOUNTAIN && map[y][x - 1] != DESERT) {
				desertify(x - 1, y);
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] != MOUNTAIN && map[y + 1][x] != DESERT) {
				desertify(x, y + 1);
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] != MOUNTAIN && map[y][x + 1] != DESERT) {
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
				if (map[y][x] == PLAINS && Math.random() < 0.025) {
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
		map[y][x] = FOREST;
		// checks if adjacent tiles are plains, and if so, recursively calls the
		// function to spread forest until i reaches 0, with a chance of
		// stopping before that to create more irregular shapes
		if (y > 0) {
			if (map[y - 1][x] == PLAINS && Math.random() < i * 0.75) {
				growForest(x, y - 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x > 0) {
			if (map[y][x - 1] == PLAINS && Math.random() < i * 0.75) {
				growForest(x - 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] == PLAINS && Math.random() < i * 0.75) {
				growForest(x, y + 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] == PLAINS && Math.random() < i * 0.75) {
				growForest(x + 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
	}

	// generate patches of montain
	public void genRocks() {
		// cycles through all tiles
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				// small chance to place a rock on tiles that aren't water, to
				// prevent ruining the river
				if (map[y][x] != WATER && Math.random() < 0.03) {
					map[y][x] = MOUNTAIN;
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
				if ((map[y][x] == PLAINS || map[y][x] == FOREST) && Math.random() < 3 * Math.pow(avgGridDim, -1.6)) {
					placeLake(x, y, (int) (Math.random() * Math.pow(avgGridDim, 0.3)) + 1);
				} else if (map[y][x] == DESERT && Math.random() < 3 * Math.pow(avgGridDim, -1.6)) {
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
			if (map[y - 1][x] != MOUNTAIN && map[y - 1][x] != WATER && Math.random() < i * 0.75) {
				placeLake(x, y - 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x > 0) {
			if (map[y][x - 1] != MOUNTAIN && map[y][x - 1] != WATER && Math.random() < i * 0.75) {
				placeLake(x - 1, y, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] != MOUNTAIN && map[y + 1][x] != WATER && Math.random() < i * 0.75) {
				placeLake(x, y + 1, i - ((Math.random() < 0.65) ? 1 : (Math.random() < 0.6) ? 2 : 0));
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] != MOUNTAIN && map[y][x + 1] != WATER && Math.random() < i * 0.75) {
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
				if (map[y][x] == DESERT && adj && Math.random() < 0.7) {
					map[y][x] = (Math.random() < 0.4) ? PLAINS : FOREST;
				}
			}
		}
	}

	private int getAdjacentTile(int x, int y, int direction) {
		if (direction == 0 && y > 0) {
			return map[y - 1][x];
		} else if (direction == 1 && x < gridWidth - 1) {
			return map[y][x + 1];
		} else if (direction == 2 && y < gridHeight - 1) {
			return map[y + 1][x];
		} else if (direction == 3 && x > 0) {
			return map[y][x - 1];
		} else {
			return map[y][x];
		}

	}

	//updates the size of the JPanel
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
				case PLAINS:
					g2.setColor(new Color(60, 255, 60));
					break;
				case FOREST:
					g2.setColor(new Color(20, 120, 20));
					break;
				case WATER:
					g2.setColor(new Color(0, 200, 255));
					break;
				case DESERT:
					g2.setColor(new Color(230, 230, 150));
					break;
				case MOUNTAIN:
					g2.setColor(new Color(120, 120, 120));
					break;
				default:
					g2.setColor(getBackground());
					break;
				}
				// draws a rectangle to represent each tile
				g2.fillRect(xOffset + (x * tileSize), yOffset + (y * tileSize), tileSize, tileSize);
			}
		}

	}

}
