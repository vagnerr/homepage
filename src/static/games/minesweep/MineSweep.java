import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import java.net.*;


public class MineSweep extends Applet {
  int size = 10;	/*(10x10 grid)*/
  int mines = 10;
  MinePanel field;
  BorderLayout bordl;
  Button modebut;
  TextField modetext;
  Image markmine;
  Image blanksq;
  Image amine;

  public void init(){// throws java.net.MalformedURLException
    try{
//      markmine = getImage(getCodeBase(),"butmark.gif");
//      blanksq = getImage(getCodeBase(),"butblank.gif");
//      amine = getImage(getCodeBase(),"mine.gif");
      markmine = getImage(getDocumentBase(),"butmark.gif");
      blanksq = getImage(getDocumentBase(),"butblank.gif");
      amine = getImage(getDocumentBase(),"mine.gif");
    } catch (NullPointerException e) {
      // We are obviously not running from within a browser
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      markmine = toolkit.getImage("butmark.gif");
      blanksq = toolkit.getImage("butblank.gif");
      amine = toolkit.getImage("mine.gif");
    }
  }

  public MineSweep(){
    setLayout(bordl = new BorderLayout());
    add("Center", field = new MinePanel(size,this,mines));

    Panel p = new Panel();
    p.setLayout(new FlowLayout());
    add("North", p);
    p.add(new Button("Reset"));
    TextField Textf = new TextField("score:",10);
    Textf.setEditable(false);
    p.add(Textf);
    p.add(modebut = new Button("Mark"));
    modetext = new TextField("Mode: Sweep",11);
    modetext.setEditable(false);
    p.add(modetext);
  }

  public boolean action(Event e, Object arg) {
    if ("Reset".equals(arg)){
      field.reset();  //reset minefield
      modebut.setLabel("Mark");
    } else if ("Mark".equals(arg)){
      field.set_mark_mode(true);
      modebut.setLabel("Sweep");
      modetext.setText("Mode: Mark");
    } else if ("Sweep".equals(arg)){
      field.set_mark_mode(false);
      modebut.setLabel("Mark");
      modetext.setText("Mode: Sweep");
    }
    return true;
  }

  public static void main(String args[]) throws java.net.MalformedURLException{

    Frame f = new Frame("MineSweep");
    MineSweep minesweep = new MineSweep();
    minesweep.init();
    minesweep.start();


    f.add("Center", minesweep);
    f.resize(400,400);
    f.show();
  }
}
