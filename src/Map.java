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
	}

	// generates mountain range, taking a side of the map to start the range on
	private void genMountains(int side, boolean sign) {
		int xOffset = (int) ((Math.random() * 0.15 * gridWidth) + (0.25 * gridWidth));
		int x = (sign) ? gridWidth - (xOffset + 1) : xOffset;
		int y = (side == 0) ? 0 : gridHeight - 1;
		int i = 0;
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0) {
			map[y][x] = MOUNTAIN;
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
			if (Math.random() < Math.pow(0.9, i / 2)) {
				if (side == 0) {
					y += 1;
				} else {
					y -= 1;
				}
			} else if (Math.random() < 1 - Math.pow(0.2, i)) {
				if (sign) {
					x += 1;
				} else {
					x -= 1;
				}
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
		int x;
		int y;
		if (Math.random() < 0.3) {
			if (Math.random() < 0.5) {
				y = 0;
			} else {
				y = gridHeight - 1;
			}
			int xOffset = (int) (Math.random() * 0.4 * gridWidth);
			x = (sign) ? xOffset : gridWidth - (xOffset + 1);
		} else {
			x = (sign) ? 0 : gridWidth - 1;
			int yOffset = (int) (Math.random() * 0.3 * gridWidth);
			y = (Math.random() < 0.5) ? yOffset : gridHeight - (yOffset + 1);
		}
		boolean xSign = x > gridWidth / 2;
		boolean ySign = y > gridHeight / 2;
		boolean turn = false;
		while (x < gridWidth && x >= 0 && y < gridHeight && y >= 0) {
			map[y][x] = WATER;
			if ((xSign && x < gridWidth*0.6) || (!xSign && x > gridWidth*0.4)) {turn = true;}
			if (Math.random() < 0.6) {
				y += (ySign) ? -1 : 1;
			} else {
				x += (xSign == turn) ? 1 : -1;
			}
		}
	}

	// generates the desert next to the mountains
	private void genDesert(int side, boolean sign) {
		if (side == 0) {
			if (sign) {
				desertify(gridWidth - 1, 0);
			} else {
				desertify(0, 0);
			}
		} else {
			if (sign) {
				desertify(gridWidth - 1, gridHeight - 1);
			} else {
				desertify(0, gridHeight - 1);
			}
		}
	}

	// spreads desert until it reaches mountains
	private void desertify(int x, int y) {
		map[y][x] = DESERT;
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
	
	//generates forests
	private void genForests() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (map[y][x] == PLAINS && Math.random() < 0.025) {
					growForest(x, y, (int) (Math.random() * 0.2 * gridWidth) + 1);
				}
			}
		}
	}
	
	//spreads forests
	private void growForest(int x, int y, int i) {
		map[y][x] = FOREST;
		if (y > 0) {
			if (map[y - 1][x] == PLAINS && Math.random() < i*0.75) {
				growForest(x, y - 1, i-1);
			}
		}
		if (x > 0) {
			if (map[y][x - 1] == PLAINS && Math.random() < i*0.75) {
				growForest(x - 1, y, i-1);
			}
		}
		if (y < gridHeight - 1) {
			if (map[y + 1][x] == PLAINS && Math.random() < i*0.75) {
				growForest(x, y + 1, i-1);
			}
		}
		if (x < gridWidth - 1) {
			if (map[y][x + 1] == PLAINS && Math.random() < i*0.75) {
				growForest(x + 1, y, i-1);
			}
		}
	}
	
	//generate patches of montain
	public void genRocks() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (map[y][x] != WATER && Math.random() < 0.03) {
					map[y][x] = MOUNTAIN;
				}
			}
		}
	}
	
	//generates lakes
	private void genLakes() {
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				if (((map[y][x] == PLAINS || map[y][x] == FOREST) && Math.random() < 0.01) || map[y][x] == DESERT && Math.random() < 0.005) {
					placeLake(x, y, (int) (Math.random() * 4) + 1);
				}
			}
		}
	}
	
	//places a lake
		private void placeLake(int x, int y, int i) {
			map[y][x] = WATER;
			if (y > 0) {
				if (map[y-1][x] != MOUNTAIN && map[y-1][x] != WATER && Math.random() < i*0.75) {
					placeLake(x, y - 1, i-1);
				}
			}
			if (x > 0) {
				if (map[y][x-1] != MOUNTAIN && map[y][x-1] != WATER && Math.random() < i*0.75) {
					placeLake(x - 1, y, i-1);
				}
			}
			if (y < gridHeight - 1) {
				if (map[y+1][x] != MOUNTAIN && map[y+1][x] != WATER && Math.random() < i*0.75) {
					placeLake(x, y + 1, i-1);
				}
			}
			if (x < gridWidth - 1) {
				if (map[y][x+1] != MOUNTAIN && map[y][x+1] != WATER && Math.random() < i*0.75) {
					placeLake(x + 1, y, i-1);
				}
			}
		}
	
	// renders the map
	public void draw(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
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
				g2.fillRect(x * (width / gridWidth), y * (height / gridHeight), (width / gridWidth),
						(height / gridHeight));
			}
		}

	}

}
