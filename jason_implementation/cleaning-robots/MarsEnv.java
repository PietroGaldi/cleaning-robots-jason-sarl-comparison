import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;
import java.util.Collection;

public class MarsEnv extends Environment {

    public static final int GSize = 15; // grid size
    public static final int GARB  = 16; // garbage code in grid model

    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");
    public static final Literal g3 = Literal.parseLiteral("garbage(r3)");
    public static final Literal g4 = Literal.parseLiteral("garbage(r4)");

    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;

    private long startTime;
    private boolean timerPrinted = false;

    @Override
    public void init(String[] args) {
        super.init(args);
        startTime = System.nanoTime();
        model = new MarsModel();
        //view  = new MarsView(model);
        //model.setView(view);
        updatePercepts();
    }

    @Override
    public synchronized boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot(ag);
            } else if (action.getFunctor().equals("move_towards")) {
                int x = (int)((NumberTerm)action.getTerm(0)).solve();
                int y = (int)((NumberTerm)action.getTerm(1)).solve();
                model.moveTowards(ag,x,y);
            } else if (action.equals(pg)) {
                model.pickGarb(ag);
            } else if (action.equals(dg)) {
                model.dropGarb(ag);
            } else if (action.equals(bg)) {
                model.burnGarb();
                if(model.isClean() && !timerPrinted){
                    timerPrinted = true;
                    long endTime = System.nanoTime();
                    double duration = (endTime - startTime) / 1000000.0;
                    logger.info("Execution time: " + duration + " ms");
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        //try {
        //   Thread.sleep(200);
        //} catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    @Override
    public synchronized Collection<Literal> getPercepts(String ag){
        return super.getPercepts(ag);
    }

    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
        Location r3Loc = model.getAgPos(2);
        Location r4Loc = model.getAgPos(3);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
        Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
        Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");

        addPercept(pos1);
        addPercept(pos2);
        addPercept(pos3);
        addPercept(pos4);

        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r2Loc)) {
            addPercept(g2);
        }
        if(model.hasObject(GARB, r3Loc)) {
            addPercept(g3);
        }
        if(model.hasObject(GARB, r4Loc)) {
            addPercept(g4);
        }
    }


    class MarsModel extends GridWorldModel {

        public static final int MErr = 2; // max error in pick garb
        int[] nerr = new int[4]; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not
        boolean r3HasGarb = false;
        boolean r4HasGarb = false;

        Random random = new Random(System.currentTimeMillis());

        private MarsModel() {
            super(GSize, GSize, 4);

            // initial location of agents
            try {
                setAgPos(0, 0, 0);

                Location r2Loc = new Location(GSize/2, GSize/2);
                setAgPos(1, r2Loc);

                setAgPos(2, 5, 0);
                setAgPos(3, 10, 0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // initial location of garbage
            add(GARB, 0, 2);
            add(GARB, 1, 6);
            add(GARB, 2, 10);
            add(GARB, 3, 13);
            add(GARB, 4, 4);
            add(GARB, 5, 1);
            add(GARB, 6, 5);
            add(GARB, 6, 14);
            add(GARB, 8, 9);
            add(GARB, 9, 12);
            add(GARB, 10, 1);
            add(GARB, 11, 4);
            add(GARB, 12, 8);
            add(GARB, 13, 11);
            add(GARB, 14, 14);
        }

        private int getAgIdFromName(String ag){
            if(ag.equals("r1")) return 0;
            if(ag.equals("r2")) return 1;
            if(ag.equals("r3")) return 2;
            if(ag.equals("r4")) return 3;
            return -1;
        }

        boolean isClean(){
        return countObjects(GARB)==0 && !r1HasGarb && !r3HasGarb && !r4HasGarb && !hasObject(GARB, getAgPos(1));
        }

        void nextSlot(String ag) throws Exception {
            int agId = getAgIdFromName(ag);
            Location agLoc = getAgPos(agId);

            int minX, maxX;
            if(ag.equals("r1")){
                minX = 0;
                maxX = 4;
            }
            else if(ag.equals("r3")){
                minX = 5;
                maxX = 9;
            }
            else if(ag.equals("r4")){
                minX = 10;
                maxX = 14;
            }
            else return;

            agLoc.x++;
            if(agLoc.x > maxX){
                agLoc.x = minX;
                agLoc.y++;
                if(agLoc.y >= getHeight()){
                    agLoc.y = 0;
                }
            }

            setAgPos(agId, agLoc);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void moveTowards(String ag, int x, int y) throws Exception {
            int agId = getAgIdFromName(ag);
            Location agLoc = getAgPos(agId);
            if (agLoc.x < x)
                agLoc.x++;
            else if (agLoc.x > x)
                agLoc.x--;
            if (agLoc.y < y)
                agLoc.y++;
            else if (agLoc.y > y)
                agLoc.y--;
            setAgPos(agId, agLoc);
            setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void pickGarb(String ag) {
            int agId = getAgIdFromName(ag);
            // r1 location has garbage
            if (hasObject(GARB, getAgPos(agId))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr[agId] == MErr) {
                    remove(GARB, getAgPos(agId));
                    nerr[agId] = 0;
                    if(agId == 0) r1HasGarb = true;
                    if(agId == 2) r3HasGarb = true;
                    if(agId == 3) r4HasGarb = true;
                } else {
                    nerr[agId]++;
                }
            }
        }

        void dropGarb(String ag) {
            int agId = getAgIdFromName(ag);
            if (agId == 0 && r1HasGarb) {
                r1HasGarb = false;
                add(GARB, getAgPos(0));
            }
            else if (agId == 2 && r3HasGarb) {
                r3HasGarb = false;
                add(GARB, getAgPos(2));
            }
            else if(agId == 3 && r4HasGarb){
                r4HasGarb = false;
                add(GARB, getAgPos(3));
            }
        }

        void burnGarb() {
            // r2 location has garbage
            if (hasObject(GARB, getAgPos(1))) {
                remove(GARB, getAgPos(1));
            }
        }
    }

    class MarsView extends GridWorldView {

        public MarsView(MarsModel model) {
            super(model, "Mars World", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            case MarsEnv.GARB:
                drawGarb(g, x, y);
                break;
            }
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            String label = "R"+(id+1);
            c = Color.blue;
            if (id == 0) {
                c = Color.yellow;
                if (((MarsModel)model).r1HasGarb) {
                    label += " - G";
                    c = Color.orange;
                }
            } else if (id == 2) {
               c = Color.cyan;
               if (((MarsModel)model).r3HasGarb) {
                   label += " - G";
                   c = Color.orange;
               }
           } else if (id == 3) {
               c = Color.green;
               if (((MarsModel)model).r4HasGarb) {
                   label += " - G";
                   c = Color.orange;
               }
           }
            super.drawAgent(g, x, y, c, -1);
            if (id == 0) {
                g.setColor(Color.black);
            } else {
                g.setColor(Color.white);
            }
            super.drawString(g, x, y, defaultFont, label);
            repaint();
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }

    }
}

