/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License. 
 **To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/3.0/ 
 *or send a letter to Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
 *****************************************************************************/
package ab.vision;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.jbox2d.common.Vec2;

public class Vision {
    private BufferedImage image;
    private VisionMBR visionMBR = null;
    private VisionRealShape visionRealShape = null;

    public Vision(BufferedImage image)
    {
            this.image = image;
    }

    public List<ABObject> findBirdsMBR()
    {
            if (visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            } 
            return visionMBR.findBirds();

    }
    /**
     * @return a list of MBRs of the blocks in the screenshot. Blocks: Stone, Wood, Ice
     * */
    public List<ABObject> findBlocksMBR()
    {
            if (visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            }
            return visionMBR.findBlocks();
    }

    public List<ABObject> findTNTs()
    {
            if(visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            }
            return visionMBR.findTNTs();
    }
    public List<ABObject> findPigsMBR()
    {
            if (visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            }
            return visionMBR.findPigs();
    }
    public List<ABObject> findPigsRealShape()
    {
            if(visionRealShape == null)
            {
                    visionRealShape = new VisionRealShape(image);
            }

            return visionRealShape.findPigs();
    } 
    public List<ABObject> findBirdsRealShape()
    {
            if(visionRealShape == null)
            {
                    visionRealShape = new VisionRealShape(image);
            }

            return visionRealShape.findBirds();
    }

    public List<ABObject> findHills()
    {
            if(visionRealShape == null)
            {
                    visionRealShape = new VisionRealShape(image);
            }

            return visionRealShape.findHills();
    } 


    public Rectangle findSlingshotMBR()
    {
            if (visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            }
            return visionMBR.findSlingshotMBR();
    }
    public List<Point> findTrajPoints()
    {
            if (visionMBR == null)
            {
                    visionMBR = new VisionMBR(image);
            }
            return visionMBR.findTrajPoints();
    }
    /**
     * @return a list of real shapes (represented by Body.java) of the blocks in the screenshot. Blocks: Stone, Wood, Ice 
     * */
    public List<ABObject> findBlocksRealShape()
    {
            if(visionRealShape == null)
            {
                    visionRealShape = new VisionRealShape(image);
            }
            List<ABObject> allBlocks = visionRealShape.findObjects();

            return allBlocks;
    }
    public VisionMBR getMBRVision()
    {
            if(visionMBR == null)
                    visionMBR = new VisionMBR(image);
            return visionMBR;
    }
    
    public VisionRealShape getRealVision()
    {
        if (visionRealShape == null)
        {
            visionRealShape = new VisionRealShape(image);
        }
        return visionRealShape;
    }

    public String detectShootingBirdSpecies()
    {
        Rectangle sling = this.getRealVision().findSling(); //this.findSlingshotMBR();

        if (sling == null) return "UNKNOWN_BIRD";

        final int[][] redBirds = {{165, 19, 51}, {136, 1, 29}, {214, 0, 45}, {211, 0, 44}, {208, 0, 44}, {204, 0, 42}, {200, 0, 41}, {195, 0, 41}, {193, 0, 40}, {187, 0, 39}, {185, 0, 38}, {168, 0, 35}, {163, 0, 34}, {156, 2, 31}, {154, 0, 32}, {141, 4, 28}, {131, 0, 27}, {104, 0, 21}, {100, 0, 21}, {92, 0, 19}, {87, 0, 18}};
        final int[][] blueBirds = {{97, 167, 194}, {94, 163, 189}, {89, 153, 177}, {88, 103, 83}, {86, 143, 162}, {64, 107, 124}, {50, 83, 96}, {99, 170, 197}, {96, 108, 113}, {95, 164, 190}, {78, 134, 154}, {68, 98, 109}};
        final int[][] yellowBirds = {{243, 223, 54}, {242, 221, 41}, {241, 219, 32}, {238, 217, 31}, {219, 199, 29}, {218, 201, 48}, {200, 181, 26}, {154, 135, 20}, {140, 131, 53}, {120, 109, 16}, {245, 232, 111}, {243, 230, 115}, {241, 220, 37}, {238, 212, 30}, {234, 200, 28}, {216, 200, 57}, {202, 184, 26}, {201, 182, 26}, {150, 136, 20}, {219, 197, 28}, {118, 110, 38}};
        final int[][] whiteBirds = {{253, 251, 235}, {245, 237, 157}, {243, 241, 223}, {236, 233, 203}, {232, 229, 199}, {226, 223, 194}, {216, 215, 210}, {205, 199, 132}, {165, 163, 142}, {225, 222, 193}, {248, 242, 183}, {247, 240, 170}, {246, 238, 158}, {241, 234, 154}, {237, 234, 206}, {229, 226, 197}, {225, 218, 144}};
        final int[][] blackBirds = {{67, 67, 67}, {63, 63, 63}, {61, 61, 61}, {23, 23, 23}, {17, 17, 17}, {13, 13, 13}, {10, 5, 2}, {8, 3, 1}, {1, 1, 1}, {13, 9, 0}, {66, 66, 66}, {62, 62, 62}, {22, 22, 22}, {18, 18, 18}, {4, 4, 4}, {3, 2, 1}};

        int[] probably = {0,0,0,0,0};

        for (int x=sling.x; x<(sling.x+sling.width); ++x)
        {
            for (int y=sling.y; y<(sling.y+sling.height/2); ++y)
            {
                Color col = new Color(image.getRGB(x,y));
                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                for (int i=0; i<redBirds.length; ++i)
                {
                    if ((r == redBirds[i][0]) && (g == redBirds[i][1]) && (b == redBirds[i][2]))
                    {
                        //return "RED_BIRD";
                        probably[0]++;
                        break;
                    }
                }

                for (int i=0; i<blueBirds.length; ++i)
                {
                    if ((r == blueBirds[i][0]) && (g == blueBirds[i][1]) && (b == blueBirds[i][2]))
                    {
                        //return "BLUE_BIRD";
                        probably[1]++;
                        break;
                    }
                }

                for (int i=0; i<yellowBirds.length; ++i)
                {
                    if ((r == yellowBirds[i][0]) && (g == yellowBirds[i][1]) && (b == yellowBirds[i][2]))
                    {
                        //return "YELLOW_BIRD";
                        probably[2]++;
                        break;
                    }
                }

                for (int i=0; i<whiteBirds.length; ++i)
                {
                    if ((r == whiteBirds[i][0]) && (g == whiteBirds[i][1]) && (b == whiteBirds[i][2]))
                    {
                        //return "WHITE_BIRD";
                        probably[3]++;
                        break;
                    }
                }

                for (int i=0; i<blackBirds.length; ++i)
                {
                    if ((r == blackBirds[i][0]) && (g == blackBirds[i][1]) && (b == blackBirds[i][2]))
                    {
                        //return "BLACK_BIRD";
                        probably[4]++;
                        break;
                    }
                }
            }
        }

        int maxProb = 0;
        int maxI = 0;
        for (int i=0; i<probably.length; ++i)
        {
            if (probably[i] > maxProb)
            {
                maxProb = probably[i];
                maxI = i;
            }
        }
        /*
        System.out.print("probably:");
        for (int i=0; i<probably.length; ++i)
        {
                System.out.print(" "+probably[i]);
        }
        System.out.println();
        */
        if (maxProb > 0)
        {
            switch (maxI)
            {
                case 0: return "RED_BIRD";
                case 1: return "BLUE_BIRD";
                case 2: return "YELLOW_BIRD";
                case 3: return "WHITE_BIRD";
                case 4: return "BLACK_BIRD";
            }
        }

        return "UNKNOWN_BIRD";
    }
    
    public int findGround(BufferedImage screenshot) //returns y of ground
    {
        int counter;
        int nHeight = screenshot.getHeight();
	int nWidth = screenshot.getWidth();

        for (int y = nHeight-1; y >= 0; y--)
        {
            counter = 0;
            for (int x = 0; x < nWidth; x++)
            {
                //final int c = screenshot.getRGB(x,y);
                Color col = new Color(screenshot.getRGB(x,y));
                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                //if (c == gr1 || c == gr2 || c == gr3 || c == gr4)
                if ((r >= 87 && r <= 90) && (g >= 161 && g <= 165) && (b >= 8 && b <= 10)) 
                {
                    counter++;
                }
            }
            
            if (counter > 20) //more than 20 pixels like this
            {
                return y;
            }
        }
        return 384; //default
    }
    
    public List<Rectangle> findSupport(BufferedImage screenshot)
    {
        int nHeight = screenshot.getHeight();
	int nWidth = screenshot.getWidth();
        
        List<Point> candidatePixels = new ArrayList<Point>();
        for (int x = 0; x < nWidth; x++)
        {
                for (int y = 0; y < nHeight; y++)
                {
                        Color col = new Color(screenshot.getRGB(x,y));
                        final int r = col.getRed();
                        final int g = col.getGreen();
                        final int b = col.getBlue();

                        if ((r == 184 && g == 113 && b == 56) ||
                                (r == 230 && g == 157 && b == 99) ||
                                (r == 147 && g == 90 && b == 44) ||
                                (r == 140 && g == 86 && b == 42) ||
                                (r == 215 && g == 143 && b == 86) ||
                                (r == 180 && g == 123 && b == 79) ||
                                (r == 192 && g == 131 && b == 84)
                                )
                        {
                                candidatePixels.add(new Point(x, y));
                        }
                }
        }

        List<Rectangle> sups = new ArrayList<Rectangle>();
        Rectangle sup;

        for (int i=0; i<candidatePixels.size(); ++i)
        {
            sup = new Rectangle(candidatePixels.get(i).x, candidatePixels.get(i).y, 1, 1);
            sups.add(sup);
            candidatePixels.remove(i); i--;

            boolean grew = true;
            while (grew)
            {
                grew = false;
                for (int j=0; j<candidatePixels.size(); ++j)
                {
                    Point cp = candidatePixels.get(j);
                    if (sup.contains(cp))
                    {
                        candidatePixels.remove(j); j--;
                        continue;
                    }

                    //left
                    if ((sup.x - cp.x == 1) && (cp.y >= sup.y) && (cp.y <= sup.y+sup.height))
                    {
                        sup.x -= 1;
                        sup.width += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }

                    //right
                    if ((cp.x - (sup.x+sup.width) == 1) && (cp.y >= sup.y) && (cp.y <= sup.y+sup.height))
                    {
                        sup.width += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }

                    //top
                    if ((sup.y - cp.y == 1) && (cp.x >= sup.x) && (cp.x <= sup.x+sup.width))
                    {
                        sup.y -= 1;
                        sup.height += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }

                    //bottom
                    if ((cp.y - (sup.y+sup.height) == 1) && (cp.x >= sup.x) && (cp.x <= sup.x+sup.width))
                    {
                        sup.height += 1;
                        candidatePixels.remove(j); j--;
                        grew = true;
                        continue;
                    }
                }
            }
        }

        return sups;
    }
    
    public static int[][] groundColors = {{54, 35, 21}, {53, 35, 21}, {52, 34, 20}, {53, 35, 20}, {58, 37, 21}, {155, 115, 86}, {154, 115, 85}, {56, 37, 21}, {150, 111, 82}, {147, 109, 81}, {138, 101, 73}, {137, 101, 74}, {134, 99, 72}, {133, 97, 70}, {132, 97, 70}, {127, 93, 67}, {123, 89, 64}, {122, 89, 63}, {120, 87, 62}, {117, 85, 60}, {102, 73, 51}, {60, 39, 23}, {94, 67, 47}, {91, 65, 44}, {80, 55, 36}, {77, 53, 34}, {59, 38, 21}, {69, 45, 26}, {58, 38, 21}, {66, 43, 25}, {64, 41, 23}, {63, 41, 23}, {62, 41, 23}, {61, 39, 23}, {60, 39, 22}, {58, 39, 24}, {57, 37, 21}, {56, 37, 20}, {55, 35, 20}, {54, 35, 20}, {54, 35, 19}, {51, 33, 18}, {49, 31, 18}, {48, 31, 17}, {47, 29, 16}, {46, 29, 16}, {53, 35, 19}, {61, 40, 23}, {64, 41, 24}, {49, 31, 17}, {163, 122, 91}, {160, 120, 89}, {159, 118, 88}, {158, 118, 87}, {157, 116, 87}, {125, 92, 67}, {146, 108, 79}, {143, 106, 78}, {139, 102, 75}, {133, 98, 71}, {130, 96, 70}, {47, 30, 16}, {127, 92, 66}, {126, 92, 66}, {125, 92, 66}, {124, 90, 64}, {119, 86, 61}, {157, 117, 87}, {108, 78, 54}, {104, 74, 52}, {101, 72, 50}, {99, 70, 48}, {95, 68, 47}, {90, 64, 44}, {67, 45, 28}, {82, 56, 36}, {158, 118, 88}, {77, 54, 36}, {69, 48, 31}, {66, 44, 26}, {65, 42, 24}, {64, 42, 24}, {62, 40, 23}, {61, 40, 22}, {59, 38, 22}, {58, 38, 23}, {56, 36, 20}, {55, 36, 20}, {54, 34, 19}, {53, 34, 19}, {52, 34, 19}, {50, 32, 18}, {49, 32, 18}, {48, 30, 17}, {47, 30, 17}, {57, 36, 20}, {50, 32, 17}, {49, 32, 17}, {137, 101, 73}, {53, 33, 19}, {73, 49, 30}, {54, 35, 21}, {50, 33, 18}, {53, 35, 19}, {56, 35, 20}, {99, 71, 50}, {54, 34, 19}, {53, 34, 20}, {51, 34, 19}, {55, 35, 20}, {97, 70, 49}, {69, 46, 28}, {53, 35, 21}, {162, 121, 90}, {161, 121, 90}, {159, 119, 89}, {58, 37, 22}, {60, 38, 22}, {155, 115, 85}, {56, 37, 22}, {151, 113, 83}, {50, 32, 19}, {51, 32, 17}, {147, 109, 81}, {146, 109, 80}, {142, 105, 77}, {140, 103, 75}, {75, 52, 33}, {136, 101, 73}, {55, 36, 21}, {132, 97, 70}, {58, 38, 23}, {130, 95, 68}, {129, 95, 68}, {128, 93, 67}, {127, 93, 67}, {126, 93, 66}, {124, 91, 65}, {123, 89, 64}, {56, 36, 20}, {120, 87, 62}, {119, 87, 62}, {114, 83, 60}, {113, 81, 58}, {112, 81, 58}, {107, 77, 54}, {105, 75, 52}, {59, 38, 21}, {61, 39, 22}, {60, 39, 22}, {59, 39, 23}, {58, 38, 22}, {58, 39, 23}, {92, 65, 45}, {90, 63, 41}, {86, 61, 41}, {84, 59, 40}, {83, 57, 36}, {56, 38, 22}, {77, 53, 34}, {75, 53, 35}, {73, 51, 33}, {72, 49, 31}, {71, 47, 27}, {70, 47, 29}, {69, 47, 29}, {68, 47, 29}, {67, 45, 27}, {65, 41, 24}, {64, 41, 23}, {63, 41, 23}, {62, 41, 25}, {61, 39, 23}, {60, 39, 21}, {59, 39, 22}, {58, 37, 21}, {57, 37, 21}, {56, 37, 21}, {55, 37, 21}, {54, 35, 19}, {54, 35, 20}, {52, 33, 19}, {51, 33, 18}, {50, 31, 17}, {49, 31, 18}, {48, 31, 17}, {47, 29, 16}, {46, 29, 16}, {53, 35, 20}, {63, 41, 24}, {61, 41, 25}, {67, 43, 25}, {52, 34, 18}, {128, 94, 68}, {61, 40, 23}, {120, 88, 64}, {65, 44, 27}, {66, 44, 26}, {63, 43, 26}, {52, 34, 20}, {53, 34, 18}, {64, 41, 24}, {63, 43, 27}, {123, 90, 64}, {109, 78, 55}, {64, 43, 27}, {89, 62, 40}, {62, 42, 25}, {69, 46, 27}, {127, 93, 66}, {90, 63, 43}, {163, 122, 91}, {162, 122, 91}, {158, 118, 88}, {156, 116, 86}, {153, 114, 84}, {152, 114, 84}, {149, 110, 81}, {147, 110, 81}, {146, 108, 79}, {144, 106, 78}, {126, 92, 66}, {140, 104, 76}, {125, 92, 66}, {133, 98, 71}, {130, 96, 69}, {129, 94, 69}, {128, 94, 67}, {127, 92, 66}, {69, 45, 27}, {129, 94, 68}, {123, 90, 65}, {121, 88, 62}, {67, 44, 27}, {118, 86, 62}, {114, 82, 58}, {113, 82, 57}, {111, 80, 56}, {57, 37, 20}, {109, 78, 54}, {108, 78, 55}, {56, 37, 23}, {105, 76, 53}, {101, 72, 51}, {99, 70, 48}, {98, 70, 49}, {97, 70, 48}, {96, 68, 48}, {95, 68, 47}, {50, 31, 18}, {92, 66, 46}, {91, 64, 44}, {49, 31, 17}, {89, 62, 41}, {67, 45, 28}, {85, 60, 40}, {113, 83, 59}, {137, 101, 74}, {79, 54, 34}, {77, 52, 32}, {76, 52, 35}, {75, 52, 34}, {74, 50, 30}, {73, 50, 31}, {70, 48, 30}, {47, 30, 17}, {68, 44, 26}, {67, 44, 25}, {66, 44, 27}, {65, 42, 24}, {64, 42, 24}, {63, 40, 23}, {62, 40, 23}, {61, 40, 22}, {60, 40, 24}, {59, 38, 22}, {58, 38, 21}, {57, 38, 22}, {56, 36, 21}, {55, 36, 20}, {54, 36, 20}, {53, 34, 19}, {52, 34, 19}, {51, 32, 18}, {50, 32, 18}, {49, 32, 18}, {48, 30, 17}, {47, 30, 16}, {75, 51, 31}, {52, 33, 18}, {51, 33, 19}, {63, 42, 27}, {62, 42, 26}, {51, 32, 18}, {56, 36, 19}, {49, 32, 16}, {69, 48, 30}, {53, 33, 18}, {72, 49, 31}, {55, 35, 19}, {50, 33, 18}, {121, 87, 62}, {53, 35, 20}, {59, 39, 21}, {71, 49, 32}, {74, 51, 34}, {76, 51, 31}, {54, 34, 19}, {52, 34, 19}, {61, 39, 21}, {60, 39, 23}, {59, 39, 23}, {113, 82, 58}, {68, 46, 29}, {162, 121, 90}, {161, 121, 90}, {59, 37, 21}, {58, 37, 21}, {156, 115, 86}, {155, 115, 85}, {56, 37, 20}, {151, 113, 83}, {50, 32, 17}, {97, 69, 47}, {147, 109, 80}, {146, 109, 81}, {145, 107, 78}, {144, 107, 78}, {142, 105, 76}, {141, 105, 77}, {139, 103, 75}, {138, 101, 75}, {137, 101, 74}, {136, 101, 72}, {135, 99, 71}, {55, 36, 20}, {132, 97, 70}, {58, 38, 21}, {130, 95, 68}, {129, 95, 69}, {128, 93, 67}, {127, 93, 67}, {126, 93, 66}, {125, 91, 66}, {51, 33, 19}, {57, 36, 21}, {56, 36, 21}, {120, 87, 62}, {117, 85, 60}, {115, 83, 60}, {113, 81, 57}, {111, 79, 55}, {159, 119, 88}, {105, 75, 53}, {66, 45, 28}, {78, 53, 33}, {101, 71, 48}, {100, 71, 49}, {99, 71, 50}, {60, 39, 21}, {59, 39, 22}, {58, 38, 22}, {93, 67, 46}, {91, 63, 41}, {88, 61, 40}, {84, 59, 40}, {83, 57, 36}, {82, 57, 38}, {80, 55, 36}, {79, 53, 34}, {60, 38, 22}, {76, 53, 35}, {75, 53, 34}, {74, 51, 33}, {73, 49, 31}, {72, 49, 30}, {59, 38, 22}, {69, 45, 27}, {68, 47, 29}, {67, 45, 28}, {66, 43, 24}, {65, 41, 24}, {64, 41, 23}, {63, 39, 22}, {62, 39, 22}, {61, 39, 22}, {60, 39, 22}, {57, 38, 22}, {58, 37, 20}, {57, 37, 21}, {56, 37, 21}, {55, 35, 20}, {54, 35, 20}, {53, 35, 19}, {52, 33, 18}, {51, 33, 18}, {50, 31, 17}, {49, 31, 17}, {48, 31, 16}, {47, 29, 16}, {46, 27, 16}, {103, 74, 51}, {63, 41, 23}, {62, 41, 25}, {64, 42, 23}, {61, 41, 24}, {65, 42, 25}, {103, 73, 51}, {65, 43, 26}, {123, 89, 63}, {63, 40, 22}, {62, 40, 23}, {121, 88, 62}, {60, 40, 24}, {51, 34, 18}, {64, 40, 23}, {65, 44, 27}, {62, 41, 24}, {145, 107, 79}, {53, 34, 18}, {100, 71, 50}, {64, 41, 24}, {51, 34, 19}, {142, 105, 77}, {54, 35, 19}, {63, 42, 26}, {62, 42, 26}, {67, 43, 25}, {51, 32, 17}, {69, 46, 28}, {127, 93, 66}, {48, 31, 17}, {163, 122, 91}, {46, 29, 16}, {161, 120, 89}, {160, 120, 89}, {159, 118, 88}, {71, 47, 28}, {146, 108, 80}, {143, 106, 78}, {126, 92, 65}, {125, 92, 65}, {136, 100, 73}, {132, 96, 70}, {129, 94, 68}, {47, 30, 17}, {127, 92, 66}, {126, 92, 66}, {125, 92, 66}, {123, 90, 64}, {86, 60, 40}, {121, 88, 63}, {66, 44, 26}, {115, 84, 59}, {114, 84, 60}, {113, 82, 59}, {111, 80, 57}, {57, 37, 20}, {109, 78, 54}, {56, 37, 22}, {105, 74, 51}, {104, 74, 51}, {103, 74, 52}, {97, 70, 49}, {51, 31, 17}, {92, 66, 45}, {90, 62, 41}, {89, 62, 43}, {69, 45, 26}, {87, 62, 42}, {86, 60, 41}, {83, 58, 39}, {113, 83, 59}, {137, 101, 73}, {80, 54, 34}, {49, 30, 16}, {75, 52, 33}, {74, 50, 31}, {94, 66, 45}, {71, 48, 30}, {70, 46, 27}, {69, 46, 29}, {68, 44, 25}, {67, 44, 27}, {66, 42, 24}, {65, 42, 24}, {64, 42, 24}, {63, 40, 23}, {62, 40, 22}, {61, 40, 22}, {60, 38, 21}, {59, 38, 21}, {48, 30, 16}, {57, 36, 20}, {56, 36, 20}, {55, 36, 22}, {54, 36, 20}, {53, 34, 19}, {52, 34, 18}, {51, 32, 19}, {50, 32, 18}, {49, 32, 18}, {48, 30, 17}, {47, 30, 16}, {46, 28, 16}, {45, 28, 16}, {44, 28, 16}, {75, 51, 31}, {76, 51, 32}, {54, 36, 22}, {55, 36, 21}, {57, 37, 22}, {52, 33, 19}, {51, 33, 17}, {123, 89, 64}, {63, 42, 25}, {62, 42, 25}, {64, 41, 22}, {65, 41, 23}, {120, 88, 63}, {50, 32, 17}, {56, 36, 19}, {72, 49, 30}, {55, 35, 19}, {71, 49, 32}, {133, 97, 71}, {53, 34, 18}, {51, 34, 19}, {60, 39, 23}, {162, 121, 90}, {161, 121, 90}, {159, 119, 88}, {157, 117, 87}, {57, 37, 23}, {152, 113, 83}, {51, 32, 18}, {147, 109, 80}, {138, 101, 73}, {135, 99, 72}, {55, 36, 21}, {133, 97, 70}, {132, 97, 69}, {128, 93, 67}, {127, 93, 66}, {125, 91, 65}, {124, 91, 65}, {123, 89, 64}, {122, 89, 63}, {56, 36, 21}, {120, 87, 62}, {112, 81, 57}, {107, 77, 54}, {159, 119, 87}, {105, 75, 52}, {104, 75, 52}, {102, 73, 51}, {100, 71, 49}, {61, 39, 21}, {98, 69, 47}, {60, 39, 21}, {94, 67, 46}, {84, 59, 40}, {79, 53, 34}, {77, 53, 34}, {73, 49, 30}, {72, 49, 32}, {59, 38, 22}, {66, 45, 28}, {65, 41, 23}, {64, 41, 23}, {63, 41, 23}, {62, 41, 24}, {61, 39, 22}, {60, 39, 22}, {57, 38, 22}, {58, 37, 21}, {57, 37, 21}, {65, 42, 25}, {55, 35, 20}, {54, 35, 19}, {53, 35, 19}, {52, 33, 18}, {51, 33, 18}, {50, 33, 18}, {49, 31, 17}, {48, 31, 17}, {47, 29, 16}, {46, 29, 16}, {62, 41, 23}, {64, 42, 23}, {64, 43, 26}, {63, 40, 22}, {62, 40, 23}, {64, 41, 24}, {51, 34, 18}, {124, 90, 65}, {54, 35, 20}, {54, 35, 21}, {63, 42, 25}, {127, 93, 67}, {163, 122, 91}, {158, 118, 87}, {156, 116, 86}, {71, 47, 28}, {151, 112, 83}, {146, 108, 79}, {143, 106, 78}, {139, 102, 74}, {135, 98, 72}, {134, 98, 71}, {131, 96, 70}, {129, 94, 68}, {128, 94, 67}, {126, 92, 66}, {124, 90, 64}, {123, 90, 64}, {121, 88, 63}, {67, 44, 27}, {116, 84, 60}, {58, 37, 20}, {111, 80, 57}, {102, 74, 52}, {101, 72, 50}, {94, 66, 45}, {93, 66, 45}, {92, 66, 43}, {48, 31, 16}, {132, 97, 71}, {77, 54, 35}, {71, 48, 31}, {70, 48, 30}, {68, 46, 29}, {67, 44, 25}, {65, 42, 24}, {64, 40, 22}, {63, 40, 23}, {62, 40, 22}, {48, 30, 17}, {60, 38, 21}, {59, 38, 21}, {58, 38, 21}, {57, 36, 20}, {56, 36, 20}, {55, 36, 20}, {53, 34, 19}, {52, 34, 19}, {51, 32, 17}, {50, 32, 18}, {48, 30, 16}, {47, 30, 16}, {45, 28, 16}, {51, 33, 17}, {151, 112, 82}, {111, 80, 56}, {97, 69, 47}, {64, 41, 22}, {50, 32, 18}, {151, 113, 83}, {55, 35, 19}, {54, 35, 20}, {59, 39, 24}, {155, 115, 85}, {135, 98, 72}, {159, 119, 88}, {156, 117, 86}, {57, 37, 20}, {153, 113, 83}, {151, 113, 82}, {150, 111, 82}, {149, 111, 81}, {148, 109, 81}, {145, 107, 79}, {144, 107, 78}, {142, 105, 77}, {140, 103, 75}, {135, 99, 72}, {55, 36, 22}, {133, 97, 70}, {128, 93, 67}, {127, 93, 67}, {126, 91, 65}, {125, 91, 65}, {123, 89, 64}, {122, 89, 63}, {116, 83, 59}, {115, 83, 60}, {114, 83, 58}, {113, 81, 58}, {112, 81, 57}, {110, 79, 55}, {107, 77, 53}, {105, 75, 52}, {104, 75, 52}, {102, 73, 50}, {62, 39, 22}, {61, 39, 21}, {98, 69, 47}, {95, 67, 46}, {94, 67, 45}, {82, 57, 37}, {73, 49, 30}, {59, 38, 22}, {67, 43, 25}, {66, 43, 25}, {65, 43, 26}, {64, 41, 23}, {63, 41, 23}, {62, 41, 23}, {61, 39, 22}, {60, 39, 22}, {57, 38, 22}, {58, 37, 21}, {57, 37, 21}, {55, 35, 20}, {54, 35, 21}, {53, 35, 19}, {52, 33, 19}, {51, 33, 18}, {50, 33, 18}, {49, 31, 17}, {48, 31, 17}, {46, 29, 16}, {64, 43, 26}, {63, 40, 22}, {62, 40, 23}, {82, 57, 36}, {64, 41, 24}, {124, 90, 65}, {63, 42, 24}, {143, 106, 77}, {49, 31, 16}, {163, 122, 91}, {161, 120, 89}, {160, 120, 89}, {155, 116, 86}, {151, 112, 83}, {144, 106, 78}, {143, 106, 78}, {141, 104, 76}, {140, 104, 76}, {139, 102, 75}, {137, 100, 72}, {135, 100, 72}, {134, 98, 71}, {128, 94, 68}, {126, 92, 66}, {125, 92, 65}, {124, 90, 64}, {123, 90, 64}, {121, 88, 63}, {65, 44, 28}, {118, 86, 62}, {116, 84, 60}, {113, 82, 58}, {111, 80, 55}, {102, 74, 51}, {96, 68, 46}, {50, 31, 17}, {92, 66, 43}, {49, 31, 18}, {86, 60, 41}, {82, 56, 36}, {73, 48, 29}, {72, 48, 28}, {71, 48, 31}, {92, 66, 45}, {68, 44, 26}, {67, 44, 25}, {66, 42, 24}, {65, 42, 24}, {64, 42, 24}, {63, 40, 23}, {62, 40, 22}, {60, 38, 21}, {59, 38, 21}, {57, 36, 20}, {56, 36, 20}, {55, 36, 20}, {53, 34, 19}, {52, 34, 19}, {51, 32, 19}, {50, 32, 17}, {49, 32, 16}, {47, 30, 16}, {45, 28, 16}, {55, 36, 19}, {51, 33, 19}, {151, 112, 82}};
    
    public List<List<Vec2> > findSupportGround2(BufferedImage screenshot, Rectangle sling)
    {
        int nHeight = screenshot.getHeight();
	int nWidth = screenshot.getWidth();
        
        List<Rectangle> polys = new ArrayList<Rectangle>();
        List<Vec2> frontier = new ArrayList<Vec2>();

        boolean[][] groundPixels = new boolean[nWidth][nHeight];
        for (int x=0; x<nWidth; ++x)
        {
            for (int y=0; y<nHeight; ++y)
            {
                groundPixels[x][y] = false;
            }
        }

        for (int x = 0; x < nWidth; x++)
        {
            for (int y = 0; y < nHeight; y++)
            {
                if ((sling != null) && (x > sling.x-sling.width/2.0f) && (x < sling.x+sling.width*3.0f/2.0f) && (y < sling.y+sling.height)) continue;

                Color col = new Color(screenshot.getRGB(x,y));
                final int r = col.getRed();
                final int g = col.getGreen();
                final int b = col.getBlue();

                for (int i=0; i<groundColors.length; ++i)
                {
                    if ((r == groundColors[i][0]) && (g == groundColors[i][1]) && (b == groundColors[i][2]))
                    {
                        groundPixels[x][y] = true;
                        break;
                    }
                }
            }
        }

        for (int x=0; x<nWidth; ++x)
        {
            for (int y=0; y<nHeight; ++y)
            {
                if (!groundPixels[x][y]) continue;

                int sum = 0;
                for (int i=(int)Math.max(0,x-1); i<=(int)Math.min(nWidth-1, x+1); ++i)
                {
                    for (int j=(int)Math.max(0,y-1); j<=(int)Math.min(nHeight-1, y+1); ++j)
                    {
                        if (!groundPixels[i][j]) sum++;
                    }
                }

                if (sum>2)
                {
                    frontier.add(new Vec2(x, y));
                    //polys.add(new Rectangle(x, y, 1, 1));
                }
            }
        }

        List<List<Vec2> > polygons = new ArrayList<List<Vec2> >();

        while (frontier.size()>0)
        {
            List<Vec2> polygon = new ArrayList<Vec2>();
            polygon.add(frontier.get(0));
            frontier.remove(0);
            boolean foundContinuation = true;

            while (foundContinuation)
            {
                Vec2 lastPixel = polygon.get(polygon.size()-1);
                float closestDist = -1.0f;
                int closestIndex = -1;

                for (int i=0; i<frontier.size(); ++i)
                {
                    float dist = (lastPixel.sub(frontier.get(i))).length();

                    if (((closestDist < 0) || (dist < closestDist)) && (dist < 10.0f))
                    {
                            closestDist = dist;
                            closestIndex = i;
                    }
                }

                if (closestDist < 0)
                {
                    foundContinuation = false;
                    break;
                }

                polygon.add(frontier.get(closestIndex));
                frontier.remove(closestIndex);
            }

            if (polygon.size() > 20)
            {
                    polygons.add(polygon);
            }
        }

        /*
        System.out.println("Found " + polygons.size() + " polygons !");
        for (int i=0; i<polygons.size(); ++i)
        {
            System.out.println("Polygon "+i+" has "+polygons.get(i).size()+" points");
        }
        */

        //clean polygons (keep only corners)
        for (int i=0; i<polygons.size(); ++i)
        {
            List<Vec2> poly = polygons.get(i);

            for (int j=0; j<poly.size()-2; ++j)
            {
                float d1 = (poly.get(j+1).sub(poly.get(j))).length();
                float d2 = (poly.get(j+2).sub(poly.get(j+1))).length();
                float dt = (poly.get(j+2).sub(poly.get(j))).length();

                if (Math.abs(dt - (d1+d2)) < 0.001) //collinear
                {
                    poly.remove(j+1);
                    j--;
                }
            }
        }

        /*
        System.out.println("Cleaned " + polygons.size() + " polygons !");
        for (int i=0; i<polygons.size(); ++i)
        {
            System.out.println("Clean poly "+i+" has "+polygons.get(i).size()+" points");
        }
        */

        return polygons;
    }
}
