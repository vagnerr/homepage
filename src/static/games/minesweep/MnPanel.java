import java.awt.*;

class MnPanel extends Panel {
//  MButton button;
//  MPanel backpanel;
  MinePanel parent;
  int x=-1;
  int y=-1;
  boolean ismine=false;
  boolean cleared=false;
  boolean marked=false;
  String name="?";
//  CardLayout cl;

  MnPanel(int xnum,int ynum,MinePanel p){
    super();    
    x = xnum;
    y = ynum;
    parent=p;

//    setLayout(cl = new CardLayout());
//    add("one",button = new MButton(this));
//    add("two",backpanel = new MPanel(this));
    return;
  }
  
  public void setLabel(String nam){
    name=nam;
  }
  
  public String getLabel(){
    return name;
  }  

  public void paint(Graphics g) {
    if (name == "M"){
      g.drawImage(parent.parent.markmine,0,0,this);
    }else if (name == "?"){
      g.drawImage(parent.parent.blanksq,0,0,this);
    }else if (name == "X"){
      g.drawImage(parent.parent.amine,0,0,this);
    }else{
      g.drawString(name, 0, 10);
    }
  }

  public Dimension preferredSize() {
    return new Dimension(30, 30);
  }

  public void set_mine(boolean im){
    ismine = im;
  }

  public boolean mouseDown(java.awt.Event event, int x, int y) {
    System.out.println("Mnpanel.mouseDown");
    if (!cleared){//we are still a button
      clear_square(event);
    }else{
      // act as panel
      if ((event.modifiers & Event.META_MASK) != 0){   // looking for right mouse button
	      System.out.println("MPanel.mouseDown:Right Click");
      } else if ((event.modifiers & Event.ALT_MASK) != 0){
	      System.out.println("MPanel.mouseDown:Middle Click");
        sweep_round(); // (will only clear if enough marked)
      } else {
	      System.out.println("MPanel.mouseDown:Left Click");
      }
    }
    return true;
  }

  public boolean action(Event e, Object arg) {
    System.out.println("MButton.action: button pressed, Event = " + e);
    clear_square(e);
    return true;
  }

  void clear_square(Event e){
    if ((e.modifiers & Event.META_MASK) != 0){   // looking for right mouse button
      System.out.println("MButton.clear_square:Right button?");
      marked = !marked;    // Toggle for now rather than tristate
      if (marked){
	      setLabel("M");
      }else{
	      setLabel("?");
      }
    } else if ((e.modifiers & Event.ALT_MASK)!= 0){
      System.out.println("MButton.clear_square:Middle Button? (Do nothing)");
    }else{
      System.out.println("MButton.clear_square:Left Button");
      clear_square();
    }
    repaint();
  }

  public void clear_square(){
    int around = 0;
    if (!is_marked()){
      if (!cleared){
        cleared = true;
        around = check_around();
	      if (!ismine){
	        if (around > 0){
	          setLabel("." + around);
	        }else{
	          setLabel("-");
	          clear_around();
	        }
	      }else{
	        setLabel("X");
	        clear_board();     // You loose show all

	      }
	      repaint();
      }
    }
  }

  public boolean check(){
    return ismine;
  }

  boolean is_marked(){
    return marked;
  }

  void reset(){
    ismine=false;
    cleared=false;
    marked=false;
    setLabel("?");  //remove any labels set in previous game
    repaint();
  }



  int check_around(){
    return parent.check_around(x,y);
  }

  void clear_around(){
    parent.clear_around(x,y);
  }

  void sweep_round(){
    parent.sweep_round(x,y);
  }

  void clear_board(){
    parent.clear_board();
  }

  boolean get_mark_mode(){
    return parent.get_mark_mode();
  }

//  boolean is_marked(){
//    return button.is_marked();
//  }

//  boolean check(){
//    return button.check();
//  }

//  void clear_square(){
//    button.clear_square();
//  }

//  void set_mine(boolean opt){
//    button.set_mine(opt);
//  }

//  public Dimension preferredSize() {
//    return new Dimension(30, 30);
//  }

//  void rmbut(){
//    cl.last(this);
//    backpanel.repaint();
//  }

//  void reset(){
//    cl.first(this);
//    button.reset();
//  }

//  String getLabel(){
//    return button.getLabel();
//  }


}

