import java.awt.*;

class MinePanel extends Panel {

  MineSweep parent=null;
  int size=0;
  int mines=0;
  boolean markmode = false;
    

  MinePanel(int sz, MineSweep p, int mine){
    parent=p;
    size = sz;
    mines = mine;

    setLayout(new GridLayout(size,size));
    for (int i=0 ; i < size ; i++){
      for (int j=0 ; j < size ; j++){
	add(new MnPanel(j+1,i+1,this));
      }
    }
    // create some mines
    create_mines();

  }

  void create_mines(){
    for (int mc = 0 ; mc < mines ; mc++){
      boolean ndu = false;
      MnPanel mp;
//      MButton mb;
      while (!ndu){
	int ploc = (int)Math.rint(Math.random()*((size*size)-1));
	mp = (MnPanel)getComponent(ploc);
      
	if (!mp.check()){
	  mp.set_mine(true);
	  ndu = true;
	}
      }
    }
  }

  int check_around(int x, int y){
    int score=0;
    for (int i=y-2 ; i <= y ; i++){
      for (int j=x-2 ; j <= x ; j++){
	if ((i >= 0) && (i < size) && (j >= 0) && (j < size) &&
	    !((i==y-1) && (j==x-1))){
	  MnPanel mb = (MnPanel)getComponent((i*size)+j);
	  if (mb.check()){
	    /*we have a mine next door*/
	    score++;
	  }
	}
      }
    }
    return score;
  }

  int marked_around(int x, int y){
    int score=0;
    for (int i=y-2 ; i <= y ; i++){
      for (int j=x-2 ; j <= x ; j++){
	if ((i >= 0) && (i < size) && (j >= 0) && (j < size) &&
	    !((i==y-1) && (j==x-1))){
	  MnPanel mb = (MnPanel)getComponent((i*size)+j);
	  if (mb.is_marked()){
	    /*we have a mine next door*/
	    score++;
	  }
	}
      }
    }
    return score;
  }

  void sweep_round(int x, int y){
    if (marked_around(x,y)>=check_around(x,y)){
      // enough squares around have been marked to be
      // "safe" to clear the rest
      if (markmode){
	markmode = false; //switch of mark mode while we clear
	clear_around(x,y);
	markmode = true;  // set back
      }else{
	clear_around(x,y);
      }
    }
  }

  void clear_around(int x, int y){
    for (int i=y-2 ; i <= y ; i++){
      for (int j=x-2 ; j <= x ; j++){
	if ((i >= 0) && (i < size) && (j >= 0) && (j < size) &&
	    !((i==y-1) && (j==x-1))){
	  MnPanel mb = (MnPanel)getComponent((i*size)+j);
	  mb.clear_square();
	}
      }
    }
  }

  void reset(){
    for (int i =0 ; i < size*size; i++){
      MnPanel mb = (MnPanel)getComponent(i);
      mb.reset();
    }
    create_mines();    //make some new mines. 
    markmode=false;    // make sure m,ark mode and button reset
  }

  void clear_board(){
    for (int i =0 ; i < size*size; i++){
      MnPanel mb = (MnPanel)getComponent(i);
      mb.clear_square();   //Should be a specific clear function
    }
  }

  void set_mark_mode(boolean opt){
    markmode = opt;
  }
  boolean get_mark_mode(){
    return markmode;
  }


  public Dimension prefferedSize(){
    return new Dimension(300,300);
  }
}


