import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // cell constructor
  Cell(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
  }

  // to flood adjacent cells if they have the same color and add them to a
  // flooded list
  void floodCells(Color color, ArrayList<Cell> floodList, ArrayList<Cell> seen) {
    this.flooded = true;
    if (!floodList.contains(this)) {
      floodList.add(this);
    }
    seen.add(this);

    if (this.top != null && !seen.contains(this.top)
        && (this.top.sameColor(color) || this.top.isFlooded())) {
      this.top.floodCells(color, floodList, seen);
    }
    if (this.bottom != null && !seen.contains(this.bottom)
        && (this.bottom.sameColor(color) || this.bottom.isFlooded())) {
      this.bottom.floodCells(color, floodList, seen);
    }
    if (this.left != null && !seen.contains(this.left)
        && (this.left.sameColor(color) || this.left.isFlooded())) {
      this.left.floodCells(color, floodList, seen);
    }
    if (this.right != null && !seen.contains(this.right)
        && (this.right.sameColor(color) || this.right.isFlooded())) {
      this.right.floodCells(color, floodList, seen);
    }
  }

  // to check if a Cell is the same color
  // as the color given
  boolean sameColor(Color newColor) {
    return this.color.equals(newColor);
  }

  // to check if a cell is flooded
  boolean isFlooded() {
    return this.flooded;
  }
}

// to represent a FloodIt game
class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;
  int size;
  int numColor;
  int optimalTrials;
  int numOfPlays;
  Random rand;
  ArrayList<Cell> flooded;
  int timer;
  int count;

  // A list of all possible colors for the cells
  ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.RED, Color.CYAN, Color.YELLOW,
      Color.GREEN.brighter(), Color.ORANGE, Color.PINK, Color.MAGENTA.darker(), Color.LIGHT_GRAY));

  // the Scene size and cell size
  static final int SCENE_SIZE = 850;
  static final int CELL_SIZE = 25;

  // constructor taking in size and numColor as parameters
  FloodItWorld(int size, int numColor) {
    if (size < 2 || size > 26) {
      throw new IllegalArgumentException("The minimum possible size of the board is 2x2 and the "
          + "maximum possible size of the board is 26x26");
    }
    if (numColor > 2 && numColor < 9) {
      if (numColor > (size * size)) {
        this.numColor = size * size;
      }
      else {
        this.numColor = numColor;
      }
    }
    else {
      throw new IllegalArgumentException(
          "The minimum number of colors is 3 and the maximum number of colors is 8");
    }

    this.rand = new Random();
    this.size = size;
    this.board = this.initBoard();
    this.optimalTrials = (int) (Math.round((this.size * this.numColor) / 3.4));
    this.numOfPlays = 0;
    this.flooded = new ArrayList<Cell>();
    this.timer = 0;
    this.count = 0;
  }

  // Convenience constructor for initBoard() test
  FloodItWorld(Random rand) {
    this(2, 3);
    this.rand = rand;
  }

  // Convenience constructor for makeScene test
  FloodItWorld() {
    this.rand = new Random();
    this.size = 2;
    this.numColor = 3;
    this.board = new ArrayList<Cell>(Arrays.asList(new Cell(0, 0, Color.RED),
        new Cell(0, 1, Color.BLUE), new Cell(1, 0, Color.RED), new Cell(1, 1, Color.BLUE)));
    this.optimalTrials = 2;
    this.numOfPlays = 0;
    this.flooded = new ArrayList<Cell>();
    this.timer = 0;
    this.count = 0;
  }

  // convenience constructor for testing makeScene
  FloodItWorld(ArrayList<Cell> board, int numOfPlays, int optimalTrials, int timer) {
    this.rand = new Random();
    this.size = 2;
    this.numColor = 4;
    this.board = board;
    this.optimalTrials = optimalTrials;
    this.numOfPlays = numOfPlays;
    this.flooded = new ArrayList<Cell>();
    this.timer = timer;
    this.count = 0;
  }

  // create the board with size^2 cells and randomly color the cells
  ArrayList<Cell> initBoard() {
    ArrayList<Cell> startingBoard = new ArrayList<Cell>();
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        int colorIndex = this.rand.nextInt(this.numColor);
        Cell cell = new Cell(i, j, colors.get(colorIndex));
        startingBoard.add(cell);
      }
    }
    return startingBoard;
  }

  // to link adjacent cells in board
  void linkCells() {
    for (int i = 0; i < this.size; i++) {
      for (int j = 0; j < this.size; j++) {
        Cell cell = this.getCellAt(i, j);
        if (i > 0) {
          cell.left = this.getCellAt(i - 1, j);
        }
        if (j > 0) {
          cell.top = this.getCellAt(i, j - 1);
        }
        if (i < this.size - 1) {
          cell.right = this.getCellAt(i + 1, j);
        }
        if (j < this.size - 1) {
          cell.bottom = this.getCellAt(i, j + 1);
        }
      }
    }
  }

  // to get all the cells that need to be flooded
  // and change their color
  void flood() {
    ArrayList<Cell> seen = new ArrayList<Cell>();
    Cell first = this.board.get(0);
    first.floodCells(first.color, this.flooded, seen);
    int i = 1;
    while (i < this.flooded.size()) {
      Cell cell = this.flooded.get(i);
      cell.color = first.color;
      i++;
    }
  }

  // gets the cell at the given logical coordinates
  Cell getCellAt(int x, int y) {
    return this.board.get(x * this.size + y);
  }

  // to check if all the cells in the board
  // are flooded
  boolean gameOver() {
    for (Cell cell : this.board) {
      if (!cell.flooded) {
        return false;
      }
    }
    return true;
  }

  // to detect valid mouse clicks
  @Override
  public void onMouseClicked(Posn pos, String button) {
    int center = SCENE_SIZE / 2;
    int boardSize = (CELL_SIZE * this.size) / 2;
    if ((button.equals("LeftButton") && (pos.x > (center - boardSize))
        && (pos.x < (center + boardSize)))
        && ((pos.y >= (center - boardSize)) && (pos.y <= (center + boardSize)))) {
      int x = (int) Math
          .round((double) ((pos.x + boardSize - center - (double) (CELL_SIZE / 2)) / CELL_SIZE));
      int y = (int) Math
          .round((double) ((pos.y + boardSize - center - (double) (CELL_SIZE / 2)) / CELL_SIZE));
      Cell clicked = this.getCellAt(x, y);
      Cell first = this.getCellAt(0, 0);
      if (!first.color.equals(clicked.color)) {
        this.numOfPlays++;
        first.color = clicked.color;
      }
    }
  }

  // to manage the ticks of the bigbang
  @Override
  public void onTick() {
    this.linkCells();
    this.count++;
    if ((count % 10 == 0) && count > 0 && !this.gameOver()) {
      this.timer++;
    }
    this.flood();
  }

  // to reset the game if letter "r" is pressed
  public void onKeyEvent(String key) {
    if (key.equalsIgnoreCase("r")) {
      this.numOfPlays = 0;
      this.board = this.initBoard();
      this.timer = 0;
    }
  }

  // to draw the FloodIT World Scene
  @Override
  public WorldScene makeScene() {
    int center = SCENE_SIZE / 2;
    int boardSize = CELL_SIZE * this.size;
    String seconds = Integer.toString(this.timer % 60);
    String minutes = Integer.toString(this.timer / 60);
    String hours = Integer.toString(this.timer / 3600);
    WorldScene scene = new WorldScene(SCENE_SIZE, SCENE_SIZE);
    for (Cell cell : this.board) {
      int x = (cell.x * CELL_SIZE) + (CELL_SIZE / 2) - (boardSize / 2) + center;
      int y = (cell.y * CELL_SIZE) + (CELL_SIZE / 2) - (boardSize / 2) + center;
      scene.placeImageXY(new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, cell.color), x,
          y);
    }
    scene.placeImageXY(new TextImage("Flood-It", 50, Color.black), center, 40);
    scene.placeImageXY(new TextImage(
        Integer.toString(this.numOfPlays) + "/" + Integer.toString(this.optimalTrials), 50,
        Color.black), center, SCENE_SIZE - 70);
    if ((this.timer % 60) < 10) {
      seconds = "0" + Integer.toString(this.timer % 60);
    }
    if ((this.timer / 60) < 10) {
      minutes = "0" + Integer.toString(this.timer / 60);
    }
    if ((this.timer / 3600) < 10) {
      hours = "0" + Integer.toString(this.timer / 3600);
    }
    scene.placeImageXY(new TextImage(hours + ":" + minutes + ":" + seconds, 40, Color.black), 90,
        20);
    if (this.numOfPlays > this.optimalTrials) {
      scene.placeImageXY(new TextImage("You Lose!", 30, Color.black), center, SCENE_SIZE - 20);
    }
    if ((this.numOfPlays == this.optimalTrials) && !this.gameOver()) {
      scene.placeImageXY(new TextImage("You Lose!", 30, Color.black), center, SCENE_SIZE - 20);
    }
    if ((this.numOfPlays <= this.optimalTrials) && this.gameOver()) {
      scene.placeImageXY(new TextImage("You Win!", 30, Color.black), center, SCENE_SIZE - 20);
    }
    return scene;
  }
}

// to represent examples and tests for FloodItWorld
class ExampleFloodIt {
  Cell one;
  Cell two;
  Cell three;
  Cell four;
  ArrayList<Cell> board;
  FloodItWorld world;
  FloodItWorld world2;
  FloodItWorld nonRandWorld;
  FloodItWorld gameOver;
  FloodItWorld gameWon;

  // to initialize Flood it games
  void initGame() {
    one = new Cell(0, 0, Color.RED);
    two = new Cell(1, 0, Color.GREEN);
    three = new Cell(0, 1, Color.BLUE);
    four = new Cell(1, 1, Color.ORANGE);
    board = new ArrayList<Cell>(Arrays.asList(one, three, two, four));

    // Examples of FloodIt using the three constructors
    world = new FloodItWorld(2, 3);
    world2 = new FloodItWorld(new Random(42));
    nonRandWorld = new FloodItWorld();
    gameOver = new FloodItWorld(board, 2, 2, 123);
    gameWon = new FloodItWorld(board, 3, 4, 40);
  }

  // to test the initBoard() method
  void testInitBoard(Tester t) {
    this.initGame();
    ArrayList<Cell> expected = new ArrayList<Cell>(Arrays.asList(new Cell(0, 0, Color.YELLOW),
        new Cell(0, 1, Color.RED), new Cell(1, 0, Color.RED), new Cell(1, 1, Color.YELLOW)));
    t.checkExpect(world2.initBoard(), expected);
  }

  // to test the linkCells() method
  void testLinkCells(Tester t) {
    this.initGame();
    this.world.linkCells();
    t.checkExpect(this.world.getCellAt(0, 0).left, null);
    t.checkExpect(this.world.getCellAt(0, 0).top, null);
    t.checkExpect(this.world.getCellAt(0, 0).right, world.getCellAt(1, 0));
    t.checkExpect(this.world.getCellAt(0, 0).bottom, world.getCellAt(0, 1));

    t.checkExpect(this.world.getCellAt(0, 1).left, null);
    t.checkExpect(this.world.getCellAt(0, 1).top, world.getCellAt(0, 0));
    t.checkExpect(this.world.getCellAt(0, 1).right, world.getCellAt(1, 1));
    t.checkExpect(this.world.getCellAt(0, 1).bottom, null);

    t.checkExpect(this.world.getCellAt(1, 0).left, world.getCellAt(0, 0));
    t.checkExpect(this.world.getCellAt(1, 0).top, null);
    t.checkExpect(this.world.getCellAt(1, 0).right, null);
    t.checkExpect(this.world.getCellAt(1, 0).bottom, world.getCellAt(1, 1));

    t.checkExpect(this.world.getCellAt(1, 1).left, world.getCellAt(0, 1));
    t.checkExpect(this.world.getCellAt(1, 1).top, world.getCellAt(1, 0));
    t.checkExpect(this.world.getCellAt(1, 1).right, null);
    t.checkExpect(this.world.getCellAt(1, 1).bottom, null);
  }

  // to test the getCellAt()method
  void testGetCellAt(Tester t) {
    this.initGame();
    t.checkExpect(this.nonRandWorld.getCellAt(0, 0), new Cell(0, 0, Color.RED));
    t.checkExpect(this.nonRandWorld.getCellAt(1, 1), new Cell(1, 1, Color.BLUE));
    t.checkExpect(this.nonRandWorld.getCellAt(1, 0), new Cell(1, 0, Color.RED));
    t.checkExpect(this.nonRandWorld.getCellAt(0, 1), new Cell(0, 1, Color.BLUE));
  }

  // to test constructor exceptions
  void testExceptions(Tester t) {
    FloodItWorld test = new FloodItWorld(2, 8);
    t.checkConstructorException(
        new IllegalArgumentException("The minimum possible size of the board is 2x2 and the "
            + "maximum possible size of the board is 26x26"),
        "FloodItWorld", 1, 8);
    t.checkConstructorException(
        new IllegalArgumentException("The minimum possible size of the board is 2x2 and the "
            + "maximum possible size of the board is 26x26"),
        "FloodItWorld", 27, 8);
    t.checkConstructorException(
        new IllegalArgumentException(
            "The minimum number of colors is 3 and the " + "maximum number of colors is 8"),
        "FloodItWorld", 26, 2);
    t.checkConstructorException(
        new IllegalArgumentException(
            "The minimum number of colors is 3 and the " + "maximum number of colors is 8"),
        "FloodItWorld", 26, 9);
    t.checkExpect(test.numColor, 4);
  }

  // to test the makeScene method
  void testMakeScene(Tester t) {
    this.initGame();
    WorldScene scene = new WorldScene(850, 850);
    scene.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.RED), 412, 412);
    scene.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.BLUE), 412, 437);
    scene.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.RED), 437, 412);
    scene.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.BLUE), 437, 437);
    scene.placeImageXY(new TextImage("Flood-It", 50, Color.black), 425, 40);
    scene.placeImageXY(new TextImage("0/2", 50, Color.black), 425, 780);
    scene.placeImageXY(new TextImage("00:00:00", 40, Color.black), 90, 20);

    t.checkExpect(this.nonRandWorld.makeScene(), scene);

    one.color = three.color;
    one.flooded = true;
    two.color = three.color;
    two.flooded = true;
    three.flooded = true;
    WorldScene scene2 = new WorldScene(850, 850);
    scene2.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.BLUE), 412, 412);
    scene2.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.BLUE), 412, 437);
    scene2.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.BLUE), 437, 412);
    scene2.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE), 437, 437);
    scene2.placeImageXY(new TextImage("Flood-It", 50, Color.black), 425, 40);
    scene2.placeImageXY(new TextImage("2/2", 50, Color.black), 425, 780);
    scene2.placeImageXY(new TextImage("00:02:03", 40, Color.black), 90, 20);
    scene2.placeImageXY(new TextImage("You Lose!", 30, Color.black), 425, 830);
    t.checkExpect(this.gameOver.makeScene(), scene2);
    one.color = four.color;
    one.flooded = true;
    two.color = four.color;
    two.flooded = true;
    three.color = four.color;
    three.flooded = true;
    four.flooded = true;
    WorldScene scene3 = new WorldScene(850, 850);
    scene3.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE), 412, 412);
    scene3.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE), 412, 437);
    scene3.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE), 437, 412);
    scene3.placeImageXY(new RectangleImage(25, 25, OutlineMode.SOLID, Color.ORANGE), 437, 437);
    scene3.placeImageXY(new TextImage("Flood-It", 50, Color.black), 425, 40);
    scene3.placeImageXY(new TextImage("3/4", 50, Color.black), 425, 780);
    scene3.placeImageXY(new TextImage("00:00:40", 40, Color.black), 90, 20);
    scene3.placeImageXY(new TextImage("You Win!", 30, Color.black), 425, 830);
    t.checkExpect(this.gameWon.makeScene(), scene3);
  }

  // to test onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.initGame();
    ArrayList<Cell> board = this.gameOver.board;
    this.gameOver.onKeyEvent("o");
    t.checkExpect(this.gameOver.board, board);
    t.checkExpect(this.gameOver.numOfPlays, 2);
    t.checkExpect(this.gameOver.numColor, 4);
    t.checkExpect(this.gameOver.size, 2);
    this.gameOver.onKeyEvent("r");
    t.checkExpect(this.gameOver.board.equals(board), false);
    t.checkExpect(this.gameOver.numOfPlays, 0);
    t.checkExpect(this.gameOver.numColor, 4);
    t.checkExpect(this.gameOver.size, 2);

  }

  // to test gameOver method
  void testGameOver(Tester t) {
    this.initGame();
    t.checkExpect(this.gameOver.gameOver(), false);
    t.checkExpect(this.gameWon.gameOver(), false);
    one.color = four.color;
    one.flooded = true;
    two.color = four.color;
    two.flooded = true;
    three.color = four.color;
    three.flooded = true;
    four.flooded = true;
    t.checkExpect(this.gameOver.gameOver(), true);
    t.checkExpect(this.gameWon.gameOver(), true);
  }

  // to test onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.initGame();
    this.gameOver.onMouseClicked(new Posn(430, 430), "RightButton");
    t.checkExpect(this.gameOver.board, this.gameOver.board);
    this.gameOver.onMouseClicked(new Posn(450, 450), "LeftButton");
    t.checkExpect(this.gameOver.board, this.gameOver.board);
    this.gameOver.onMouseClicked(new Posn(415, 415), "LeftButton");
    t.checkExpect(this.gameOver.board.get(0).color, Color.RED);
    this.gameOver.onMouseClicked(new Posn(435, 435), "LeftButton");
    t.checkExpect(this.gameOver.board.get(0).color, Color.ORANGE);
  }

  // to test the method sameColor
  void testSameColor(Tester t) {
    this.initGame();
    t.checkExpect(this.one.sameColor(Color.GREEN), false);
    t.checkExpect(this.one.sameColor(Color.RED), true);
    t.checkExpect(this.two.sameColor(Color.RED), false);
    t.checkExpect(this.two.sameColor(Color.GREEN), true);
    t.checkExpect(this.three.sameColor(Color.GREEN), false);
    t.checkExpect(this.three.sameColor(Color.BLUE), true);
    t.checkExpect(this.four.sameColor(Color.GREEN), false);
    t.checkExpect(this.four.sameColor(Color.ORANGE), true);
  }

  // to test the method isFlooded
  void testIsFlooded(Tester t) {
    this.initGame();
    t.checkExpect(this.one.isFlooded(), false);
    t.checkExpect(this.three.isFlooded(), false);
    t.checkExpect(this.two.isFlooded(), false);
    t.checkExpect(this.four.isFlooded(), false);
    this.one.flooded = true;
    this.two.flooded = true;
    this.three.flooded = true;
    this.four.flooded = true;
    t.checkExpect(this.one.isFlooded(), true);
    t.checkExpect(this.three.isFlooded(), true);
    t.checkExpect(this.two.isFlooded(), true);
    t.checkExpect(this.four.isFlooded(), true);
  }

  // to test the method flood cells
  void testFloodCells(Tester t) {
    this.initGame();
    this.gameWon.linkCells();
    this.one.floodCells(Color.GREEN, this.gameWon.flooded, new ArrayList<Cell>());
    t.checkExpect(this.gameWon.flooded, new ArrayList<Cell>(Arrays.asList(this.one, this.two)));
    this.one.floodCells(Color.BLUE, this.gameWon.flooded, new ArrayList<Cell>());
    t.checkExpect(this.gameWon.flooded,
        new ArrayList<Cell>(Arrays.asList(this.one, this.two, this.three)));
    this.one.floodCells(Color.ORANGE, this.gameWon.flooded, new ArrayList<Cell>());
    t.checkExpect(this.gameWon.flooded,
        new ArrayList<Cell>(Arrays.asList(this.one, this.two, this.three, this.four)));
  }

  // to test the flood method
  void testFlood(Tester t) {
    this.initGame();
    this.gameWon.linkCells();
    this.one.color = Color.GREEN;
    this.gameWon.flood();
    t.checkExpect(this.gameWon.flooded, new ArrayList<Cell>(Arrays.asList(this.one, this.two)));
    t.checkExpect(this.one.flooded, true);
    t.checkExpect(this.two.flooded, true);
    t.checkExpect(this.three.flooded, false);
    t.checkExpect(this.four.flooded, false);
    this.one.color = Color.ORANGE;
    this.gameWon.flood();
    t.checkExpect(this.gameWon.flooded,
        new ArrayList<Cell>(Arrays.asList(this.one, this.two, this.four)));
    t.checkExpect(this.two.color, Color.ORANGE);
    t.checkExpect(this.three.flooded, false);
    t.checkExpect(this.four.flooded, true);
    this.one.color = Color.BLUE;
    this.gameWon.flood();
    t.checkExpect(this.gameWon.flooded,
        new ArrayList<Cell>(Arrays.asList(this.one, this.two, this.four, this.three)));
    t.checkExpect(this.two.color, Color.BLUE);
    t.checkExpect(this.four.color, Color.BLUE);
    t.checkExpect(this.three.flooded, true);
    t.checkExpect(this.gameWon.gameOver(), true);

  }

  // test the onTick method
  void testOnTick(Tester t) {
    this.initGame();
    t.checkExpect(this.world.timer, 0);
    for (int i = 0; i < 10; i++) {
      this.world.onTick();
    }
    t.checkExpect(world.timer, 1);

    this.initGame();
    this.world.timer = 4;
    t.checkExpect(this.world.timer, 4);
    for (int i = 0; i < 10; i++) {
      this.world.onTick();
    }
    t.checkExpect(this.world.timer, 5);

    // set the board to have a single color
    this.world.board.forEach(c -> c.color = this.world.colors.get(0));

    // make sure the game is not over yet
    t.checkExpect(this.world.gameOver(), false);

    // call the ontick method
    world.onTick();

    // make sure the game is over now that the time has run out
    t.checkExpect(this.world.gameOver(), true);
  }

  // to play FloodIt
  void testFloodIt(Tester t) {
    FloodItWorld starterWorld = new FloodItWorld(5, 4);
    int sceneSize = FloodItWorld.SCENE_SIZE;
    starterWorld.bigBang(sceneSize, sceneSize, 0.1);
  }
}