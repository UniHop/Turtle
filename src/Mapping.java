import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class Mapping {
    static BufferedImage[] Backgroundimages = setup.getTextureAtlas("res/tiles/TextureAtlasv20v20v.png");

    int colums = setup.GAME_WIDTH/50;
    int rows = setup.GAME_HEIGHT/50;

    int ticksperupdate = 40;
    int ticks = 0;

    int[] playerloc = new int[2];

    Areas[][] AreaArray;

    Mapping(int mapnumber, int playermapx, int playermapy){
        this.AreaArray = new Areas[colums][rows];
        this.playerloc[0] = playermapx;
        this.playerloc[1] = playermapy;

        /*
         * Case 1 : generate a flat empty map
         * Case 2 : generate Saved map
         * Case 3 : generate map based on seed
         */
        switch (mapnumber) {
            case 1 -> {
                int i = 0;
                for (Areas[] c : AreaArray) {
                    int j = 0;
                    for (Areas r : c) {
                        AreaArray[i][j] = new Areas(i, j, 10, 10, 13, true);
                        j++;
                    }
                    i++;
                }
            }
            case 2 -> make_map(this.playerloc);
            case 3 -> generateMap(this.playerloc);
        }
    }

    // moves the current index range forward
    public void update(int colum, int row){
        Areas area = AreaArray[colum][row];

        int index = -1;
        for(int i = 0; i < setup.TEXTUREGROUPS.size(); i++){
            if (Arrays.equals(new int[]{area.lower_index, area.upper_index, (area.change) ? 1 : 0}, setup.TEXTUREGROUPS.get(i))){
                index = i;
                break;
            }
        }
        if (index!=-1){
            index++;
            index = (index==setup.TEXTUREGROUPS.size())? 0 : index;
            area.lower_index = setup.TEXTUREGROUPS.get(index)[0];
            area.upper_index = setup.TEXTUREGROUPS.get(index)[1];
            area.change = setup.TEXTUREGROUPS.get(index)[2] == 1;
            area.changeState(true, area.lower_index, area.upper_index);
        }
    }

    public void draw(Graphics g){
        ticks ++;
        for(Areas[] a : this.AreaArray){
            for(Areas areas: a){
                areas.draw(g);
                if (ticks >= ticksperupdate){
                    areas.changeState(areas.change, areas.lower_index, areas.upper_index);
                }
            }
        }
        if (ticks > ticksperupdate){
            ticks = 0;
        }

    }

    // Saves the current map. Format is index of background, specific background
    public void save(){
        String path = "res/tiles/Map/map" + this.playerloc[0] + this.playerloc[1] + ".txt";

        File myFile = new File(path);

        System.out.println(myFile);

        // Try block to check if exception occurs
        try {

            FileOutputStream fileout = new FileOutputStream("src/"+path);
            ObjectOutputStream out = new ObjectOutputStream(fileout);
            out.writeObject(this.AreaArray);

            out.close();

            fileout.close();

            // Display message for successful execution of
            // program on the console
            System.out.println("File is created successfully with the content.");
        } catch (IOException e) {

            // Print the exception
            System.out.println(e.getMessage());
            try {

                System.out.println("Making new file");
                myFile = new File("src\\res\\tiles\\Map\\map" + this.playerloc[0] + this.playerloc[1] + ".txt");
                if (myFile.createNewFile()) {
                    System.out.println("Success");
                    save();
                } else {
                    System.out.println(":(");
                }
            } catch (IOException e2){
                System.out.println(e2.getMessage());
            }
        }
    }



    // get data from map
    public void make_map(int[] loc){
        File myFile = new File("src\\res\\tiles\\Map\\map" + loc[0] + loc[1] + ".txt");
        try {
            FileInputStream filein = new FileInputStream(myFile);
            ObjectInputStream in = new ObjectInputStream(filein);
            this.AreaArray = (Areas[][]) in.readObject();


        }catch (IOException e){
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void keypressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_ENTER){
            save();
        }
    }

    public void mousepressed(MouseEvent a){
        int x = a.getX();
        int y = a.getY();
        int colum = x/50;
        int row = y/50;
        update(colum, row);
    }


    public static BufferedImage get_image(int index){
        return Backgroundimages[index];

    }


    public void generateMap(int[] playerloc){
        long seed = (long) ((playerloc[0]+playerloc[1])/Math.PI);
        System.out.println(seed+"aaaaaaaaaaaaaaaaaaaaaa");
        System.out.println();
        OpenSimplexNoise simplex = new OpenSimplexNoise(seed);
        for(int x=0; x<setup.BLOCKS_WIDTH; x++){
            for(int y=0; y<setup.BLOCKS_HEIGHT; y++){
                double scale = 0.1;
                double texture = simplex.eval((x+playerloc[0]*20)*scale, (y+playerloc[1]*10)*scale);
                System.out.println(texture);
                if (texture<-0.3){
                    AreaArray[x][y] = new Areas (x, y, 10,10,13,true);
                }else if(texture<0){
                    AreaArray[x][y] = new Areas (x, y, 20,20,23,false);
                }else if(texture<0.2){
                    AreaArray[x][y] = new Areas (x, y, 30,30,33,false);
                }else if(texture<0.4){
                    AreaArray[x][y] = new Areas (x, y, 40,40,43,false);
                }else{
                    AreaArray[x][y] = new Areas (x, y, 50,50,53,false);
                }
            }
        }
    }
}