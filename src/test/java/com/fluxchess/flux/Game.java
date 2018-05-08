import java.io.FileWriter;

import java.io.IOException;

import java.util.ArrayList;

import java.util.Random;





public class Game {

	Board b;

	private static String state;

	static int myColor, activePlayer;

	private static float myTime, oTime;

	private Opening mr;

	static ArrayList<String> myHistory = new ArrayList<String>();

	static ArrayList<String> oHistory = new ArrayList<String>();

	

	public Game() throws IOException{

		myHistory = new ArrayList<String>();

		oHistory = new ArrayList<String>();

		b=new Board();

		b.initBoard();

		mr = new Opening("openings.txt"); 

		Game.setMyColor(ChessColors.Black);

		Game.setActivePlayer(ChessColors.White);

		Game.setState("normal");

	}

	public Game(int EngColor) throws IOException{

		myHistory = new ArrayList<String>();

		oHistory = new ArrayList<String>();

		Game.setMyColor(EngColor);

		Game.setActivePlayer(ChessColors.White);

		Game.setState("normal");

		b=new Board();

		b.initBoard();

		mr = new Opening("openings.txt"); 

	}

	Piesa getPiesa(){

		ArrayList<Piesa> a = b.selectPiecesOfType((char)'N',myColor);

		a.addAll(b.selectPiecesOfType((char)'p', myColor));

		a.addAll(b.selectPiecesOfType((char)'R', myColor));

		a.addAll(b.selectPiecesOfType((char)'B', myColor));

		a.addAll(b.selectPiecesOfType((char)'B', myColor));

		a.addAll(b.selectPiecesOfType((char)'K', myColor));

		a.addAll(b.selectPiecesOfType((char)'Q', myColor));

		Random r=new Random();

		

		if (a.size() != 0){

			return a.get(r.nextInt(a.size()));

		}

		

		return null;

	}

	

	String getNextMove(){

		String move = mr.getNextMove(); 

	//	write("Mutarea este: " + move);

		if(move != null){

			MoveSan.Decode(move, b);

			myHistory.add(move);

			return move;

		}

		else {

			Mutare m = Negamax.nextMove(b);

			Position prev = m.p.p;

			boolean capt;

			if (!b.isFree(m.pos))

				capt = true;

			else

				capt = false;

			b.move(m.p, m.pos);

			move = MoveSan.Code(b, m.p, prev, capt);//to be encoded to SAN

			myHistory.add(move);

			return move;

		}

	}

	

	void processMove(String move){

	//	write("mutare noua");

		oHistory.add(move);

		if (move.charAt(move.length() - 1) == '#'){

			try {

				Comunicare.output("resign");

				Comunicare.input();

			} catch (IOException e) {

				e.printStackTrace();

			}

		}

		else

			MoveSan.Decode(move, b);

	}

	

	void showGameBoard(){

		b.printBoard(new String());

	}

	

	static void write(String s){

		 Comunicare.output(s);

		}

	

	public static void setMyColor(int s){

		myColor=s;

	}

	

	public static int getMyColor(){

		return myColor;

	}

	

	public static void setActivePlayer(int s){

		Game.activePlayer = s;

	}

	

	public static void changeActivePlayer(){

		Game.activePlayer *= -1;

	}

	

	public static int getActivePlayer(){

		return Game.activePlayer;

	}

	

	public static void setState(String s){

		state=s;

	}

	

	public static String getState(){

		return state;

	}

	

	public static void setMyTime(float x){

		myTime = x;

	}

	

	public static float getMyTime(){

		return myTime;

	}

	

	public static void setOTime(float x){

		oTime = x;

	}

	

	public static float getOTime(){

		return oTime;

	}

	

	public static void debug(String s){

		FileWriter fw;

		try {

			fw = new FileWriter("out3.txt",true);

			fw.write(s + "\n");

			fw.write("\n");

			fw.close();

		} catch (IOException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}

	}

	

}
