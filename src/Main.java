import javax.swing.JFrame;

public class Main {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Map");
		Map map = new Map(512, 512, 256, 128);
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setContentPane(map);
		frame.pack();
		
		map.genMap();
		while(true) {
			map.draw(map.getGraphics());
		}
	}
	
}
