package net.sourceforge.fidocadj.export;

import java.util.*;
import java.io.*;

import net.sourceforge.fidocadj.globals.Globals;
import net.sourceforge.fidocadj.layers.LayerDesc;
import net.sourceforge.fidocadj.primitives.Arrow;
import net.sourceforge.fidocadj.graphic.DimensionG;
import net.sourceforge.fidocadj.graphic.ColorInterface;
import net.sourceforge.fidocadj.graphic.PointDouble;


/** Export in a LaTeX drawing using the pgf (Portable Graphic File) packet.
    The file should be compatible with at least the 0.65 version of the
    pgf packet.

    Here is an example of a file inclusion, in LaTeX:<br>

    -------------------------------------------------
    <pre>
    \\documentclass[10pt,a4paper]{scrreprt}
    \\usepackage[italian]{babel}
    \\usepackage{pgf}

    \\begin{document}
    \\input{prova.pgf}
    \\end{document}
    </pre>
    -------------------------------------------------<br>

    since the prova.pgf file (generated by FidoCadJ) already contains the
    \begin{pgfpicture} and \end{pgfpicture} commands in order to work in the
    correct environment.
    The text is not formatted, since the user should be able to use LaTeX
    commands inside FidoCadJ to be parsed when the exported pgf file is included
    and compiled with LaTeX or pdfLaTeX.

<pre>
    This file is part of FidoCadJ.

    FidoCadJ is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FidoCadJ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FidoCadJ. If not,
    @see <a href=http://www.gnu.org/licenses/>http://www.gnu.org/licenses/</a>.

    Copyright 2008-2023 by Davide Bucci
</pre>
    @author Davide Bucci
*/

public class ExportPGF implements ExportInterface
{
    private final FileWriter fstream;
    private BufferedWriter out;
    private List layerV;
    private ColorInterface actualColor;
    private int currentDash;
    private double actualWidth;
    private float dashPhase;
    private float currentPhase=-1;
    // Dash patterns
    private String sDash[];

/*
    static final String dash[]={"{5.0pt}{10pt}", "{2.5pt}{2.5pt}",
        "{1.0pt}{1.0pt}", "{1.0pt}{2.5pt}", "{1.0pt}{2.5pt}{2.5pt}{2.5pt}"};
*/

    /** Set the multiplication factor to be used for the dashing.
        @param u the factor.
    */
    public void setDashUnit(double u)
    {
        sDash = new String[Globals.dashNumber];

        // If the line width has been changed, we need to update the
        // stroke table

        // The first entry is non dashed
        sDash[0]="";

        // Resize the dash sizes depending on the current zoom size.
        String dashArrayStretched;
        // Then, the dashed stroke styles are created.
        for(int i=1; i<Globals.dashNumber; ++i) {
            // Prepare the resized dash array.
            dashArrayStretched = "";
            for(int j=0; j<Globals.dash[i].length;++j) {
                dashArrayStretched+=(Globals.dash[i][j]*(float)u/2.0f);
                if(j<Globals.dash[i].length-1)
                    dashArrayStretched+="pt}{";
            }
            sDash[i]="{"+dashArrayStretched+"pt}";
        }
    }

    /** Set the "phase" in output units of the dashing style.
        For example, if a dash style is composed by a line followed by a space
        of equal size, a phase of 0 indicates that the dash starts with the
        line.
        @param p the phase, in output units.
    */
    public void setDashPhase(float p)
    {
        dashPhase=p;
    }

    /** Constructor

        @param f the File object in which the export should be done.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public ExportPGF (File f) throws IOException
    {
        actualColor=null;
        fstream = new FileWriter(f);
    }

    /** Called at the beginning of the export phase. Ideally, in this routine
        there should be the code to write the header of the file on which
        the drawing should be exported.

        @param totalSize the size of the image. Useful to calculate for example
        the bounding box.
        @param la a vector describing the attributes of each layer.
        @param grid the grid size. This is useful when exporting to another
            drawing program having some kind of grid concept. You might use
            this value to synchronize FidoCadJ's grid with the one used by
            the target.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportStart(DimensionG totalSize, List<LayerDesc> la,
        int grid)
        throws IOException
    {

        // We need to save layers informations, since we will use them later.

        layerV=la;
        int i;
        out = new BufferedWriter(fstream);
        LayerDesc l;
        ColorInterface c;

        int wi=totalSize.width;
        int he=totalSize.height;

        // A basic header of the PGF file

        out.write("\\begin{pgfpicture}{0cm}{0cm}{"+(wi)+
            "pt}{"+(he)+"pt}\n"
            +"% Created by FidoCadJ ver. "+Globals.version
            +", export filter by Davide Bucci\n");
        out.write("\\pgfsetxvec{\\pgfpoint{"+1+"pt}{0pt}}\n");
        out.write("\\pgfsetyvec{\\pgfpoint{0pt}{"+1+"pt}}\n");
        out.write("\\pgfsetroundjoin \n\\pgfsetroundcap\n");
        out.write("\\pgftranslateto{\\pgfxy(0,"+he+")}\n");
        out.write("\\begin{pgfmagnify}{1}{-1}\n");
        out.write("% Layer color definitions\n");
        for(i=0; i<layerV.size();++i) {
            l=(LayerDesc)layerV.get(i);
            c=l.getColor();
            out.write("\\definecolor{layer"+i+"}{rgb}{"+
                +Math.round(100.0*c.getRed()/255.0)/100.0+","
                +Math.round(100.0*c.getGreen()/255.0)/100.0+ ","
                +Math.round(100.0*c.getBlue()/255.0)/100.0 +"}\n");
        }
        out.write("% End of color definitions\n");
        actualColor=null;
    }

    /** Called at the end of the export phase.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportEnd()
        throws IOException
    {
        out.write("\\end{pgfmagnify}\n");
        out.write("\\end{pgfpicture}");
        out.close();

    }

    /** Called when exporting an Advanced Text primitive.

        @param x the x position of the beginning of the string to be written.
        @param y the y position of the beginning of the string to be written.
        @param sizex the x size of the font to be used.
        @param sizey the y size of the font to be used.
        @param fontname the font to be used.
        @param isBold true if the text should be written with a boldface font.
        @param isMirrored true if the text should be mirrored.
        @param isItalic true if the text should be written with an italic font.
        @param orientation angle of orientation (degrees).
        @param layer the layer that should be used.
        @param text the text that should be written.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportAdvText (int x, int y, int sizex, int sizey,
        String fontname, boolean isBold, boolean isMirrored, boolean isItalic,
        int orientation, int layer, String text)
        throws IOException
    {
        registerColorSize(layer, -1.0);


        String path;

        /*  THIS VERSION OF TEXT EXPORT IS NOT COMPLETE! IN PARTICULAR,
            MIRRORING EFFECTS, ANGLES AND A PRECISE SIZE CONTROL IS NOT
            HANDLED at ALL!
            This is somehow wanted, since the main use of the PGF export is
            for inserting LaTeX commands inside drawings meant for use in a
            LaTeX documents. So this is something LaTeX should do, and it is
            not a businnes for FidoCadJ.
        */

        out.write("\\begin{pgfmagnify}{1}{-1}\n");
        out.write("\\pgfputat{\\pgfxy("+x+","+(-y)+")}{\\pgfbox[left,top]{");
        out.write(text);
        out.write("}}\n");

        out.write("\\end{pgfmagnify}\n");


    }

    /** Called when exporting a Bézier primitive.

        @param x1 the x position of the first point of the trace.
        @param y1 the y position of the first point of the trace.
        @param x2 the x position of the second point of the trace.
        @param y2 the y position of the second point of the trace.
        @param x3 the x position of the third point of the trace.
        @param y3 the y position of the third point of the trace.
        @param x4 the x position of the fourth point of the trace.
        @param y4 the y position of the fourth point of the trace.
        @param layer the layer that should be used.

                // from 0.22.1

        @param arrowStart specify if an arrow is present at the first point.
        @param arrowEnd specify if an arrow is present at the second point.
        @param arrowStyle the style of the arrow.
        @param arrowLength total lenght of arrows (if present).
        @param arrowHalfWidth half width of arrows (if present).
        @param dashStyle dashing style.
        @param strokeWidth the width of the pen to be used when drawing.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportBezier (int x1, int y1,
        int x2, int y2,
        int x3, int y3,
        int x4, int y4,
        int layer,
        boolean arrowStart,
        boolean arrowEnd,
        int arrowStyle,
        int arrowLength,
        int arrowHalfWidth,
        int dashStyle,
        double strokeWidth)
        throws IOException
    {
        registerColorSize(layer, strokeWidth);
        registerDash(dashStyle);

        if (arrowStart) {
            PointPr p=exportArrow(x1, y1, x2, y2, arrowLength,
                arrowHalfWidth, arrowStyle);
            // This fixes issue #172
            // If the arrow length is negative, the arrow extends
            // outside the line, so the limits must not be changed.
            if(arrowLength>0) {
                x1=(int)Math.round(p.x);
                y1=(int)Math.round(p.y);
            }
        }
        if (arrowEnd) {
            PointPr p=exportArrow(x4, y4, x3, y3, arrowLength,
                arrowHalfWidth, arrowStyle);
            // Fix #172
            if(arrowLength>0) {
                x4=(int)Math.round(p.x);
                y4=(int)Math.round(p.y);
            }
        }

        out.write("\\pgfmoveto{\\pgfxy("+x1+","+y1+")} \n"+
            "\\pgfcurveto{\\pgfxy("+x2+","+y2+")}{\\pgfxy("+x3+","+y3+
            ")}{\\pgfxy("+x4+","+y4+")}\n"+
            "\\pgfstroke\n");
    }

    /** Called when exporting a Connection primitive.
        @param x the x position of the position of the connection.
        @param y the y position of the position of the connection.
        @param layer the layer that should be used.
        @param node_size the sieze of the connection in logical units.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportConnection (int x, int y, int layer, double node_size)
        throws IOException
    {
        registerColorSize(layer, .33);

        out.write("\\pgfcircle[fill]{\\pgfxy("+x+","+y+")}{"+
            node_size/2.0+"pt}");

    }

    /** Called when exporting a Line primitive.

        @param x1 the x position of the first point of the segment.
        @param y1 the y position of the first point of the segment.
        @param x2 the x position of the second point of the segment.
        @param y2 the y position of the second point of the segment.

        @param layer the layer that should be used.

        // from 0.22.1

        @param arrowStart specify if an arrow is present at the first point.
        @param arrowEnd specify if an arrow is present at the second point.
        @param arrowStyle the style of the arrow.
        @param arrowLength total lenght of arrows (if present).
        @param arrowHalfWidth half width of arrows (if present).
        @param dashStyle dashing style.
        @param strokeWidth the width of the pen to be used when drawing.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportLine (double x1, double y1,
        double x2, double y2,
        int layer,
        boolean arrowStart,
        boolean arrowEnd,
        int arrowStyle,
        int arrowLength,
        int arrowHalfWidth,
        int dashStyle,
        double strokeWidth)
        throws IOException
    {
        registerColorSize(layer, strokeWidth);
        registerDash(dashStyle);
        double xstart=x1, ystart=y1;
        double xend=x2, yend=y2;

        if (arrowStart) {
            PointPr p=exportArrow(x1, y1, x2, y2, arrowLength,
                arrowHalfWidth, arrowStyle);
            // This fixes issue #172
            // If the arrow length is negative, the arrow extends
            // outside the line, so the limits must not be changed.
            if(arrowLength>0) {
                xstart=p.x;
                ystart=p.y;
            }
        }
        if (arrowEnd) {
            PointPr p=exportArrow(x2, y2, x1, y1, arrowLength,
                arrowHalfWidth, arrowStyle);
            // Fix #172
            if(arrowLength>0) {
                xend=p.x;
                yend=p.y;
            }
        }
        out.write("\\pgfline{\\pgfxy("+xstart+","+ystart+")}{\\pgfxy("+
            xend+","+yend+")}\n");
    }

    /** Called when exporting an arrow.
        @param x position of the tip of the arrow.
        @param y position of the tip of the arrow.
        @param xc direction of the tip of the arrow.
        @param yc direction of the tip of the arrow.
        @param l length of the arrow.
        @param h width of the arrow.
        @param style style of the arrow.
        @return the coordinates of the base of the arrow.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public PointPr exportArrow(double x, double y, double xc, double yc,
        double l, double h,
        int style)
        throws IOException
    {
        double s;
        double alpha;
        double x0;
        double y0;
        double x1;
        double y1;
        double x2;
        double y2;

        // At first we need the angle giving the direction of the arrow
        // a little bit of trigonometry :-)

        if (x==xc)
            alpha = Math.PI/2.0+(y-yc<0?0:Math.PI);
        else
            alpha = Math.atan((double)(y-yc)/(double)(x-xc));

        alpha += x-xc>0?0:Math.PI;

        // Then, we calculate the points for the polygon
        x0 = x - l*Math.cos(alpha);
        y0 = y - l*Math.sin(alpha);

        x1 = x0 - h*Math.sin(alpha);
        y1 = y0 + h*Math.cos(alpha);

        x2 = x0 + h*Math.sin(alpha);
        y2 = y0 - h*Math.cos(alpha);

        out.write("\\pgfmoveto{\\pgfxy("+x+","+ y+")}\n");
        out.write("\\pgflineto{\\pgfxy("+x1+","+y1+")}\n");
        out.write("\\pgflineto{\\pgfxy("+x2+","+y2+")}\n");


        out.write("\\pgfclosepath \n");

        if ((style & Arrow.flagEmpty) == 0)
            out.write("\\pgffill \n");
        else
            out.write("\\pgfqstroke \n");

        if ((style & Arrow.flagLimiter) != 0) {
            double x3;
            double y3;
            double x4;
            double y4;
            x3 = x - h*Math.sin(alpha);
            y3 = y + h*Math.cos(alpha);

            x4 = x + h*Math.sin(alpha);
            y4 = y - h*Math.cos(alpha);
            out.write("\\pgfline{\\pgfxy("+x3+","+y3+")}{\\pgfxy("+
                x4+","+y4+")}\n");
        }
        return new PointPr(x0,y0);
    }

    /** Called when exporting a Macro call.
        This function can just return false, to indicate that the macro should
        be rendered by means of calling the other primitives. Please note that
        a macro does not have a reference layer, since it is defined by its
        components.

        @param x the x position of the position of the macro.
        @param y the y position of the position of the macro.
        @param isMirrored true if the macro is mirrored.
        @param orientation the macro orientation in degrees.
        @param macroName the macro name.
        @param macroDesc the macro description, in the FidoCad format.
        @param name the shown name.
        @param xn coordinate of the shown name.
        @param yn coordinate of the shown name.
        @param value the shown value.
        @param xv coordinate of the shown value.
        @param yv coordinate of the shown value.
        @param font the used font.
        @param fontSize the size of the font to be used.
        @param m the library.
        @return true if the export is done by the function, false if the
            macro should be expanded into primitives.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public boolean exportMacro(int x, int y, boolean isMirrored,
        int orientation, String macroName, String macroDesc,
        String name, int xn, int yn, String value, int xv, int yv, String font,
        int fontSize, Map m)
        throws IOException
    {
        // The macro will be expanded into primitives.
        return false;
    }

    /** Called when exporting an Oval primitive. Specify the bounding box.

        @param x1 the x position of the first corner
        @param y1 the y position of the first corner
        @param x2 the x position of the second corner
        @param y2 the y position of the second corner
        @param isFilled it is true if the oval should be filled

        @param layer the layer that should be used
        @param dashStyle dashing style
        @param strokeWidth the width of the pen to be used when drawing
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportOval(int x1, int y1, int x2, int y2,
        boolean isFilled, int layer, int dashStyle, double strokeWidth)
        throws IOException
    {
        registerColorSize(layer, strokeWidth);
        registerDash(dashStyle);

        out.write("\\pgfellipse["+(isFilled?"fillstroke":"stroke")+
            "]{\\pgfxy("+(x1+x2)/2.0+","+(y1+y2)/2.0+")}{\\pgfxy("+
            Math.abs(x2-x1)/2.0+",0)}{\\pgfxy(0,"+Math.abs(y2-y1)/2.0+")}\n");


    }

    /** Called when exporting a PCBLine primitive.

        @param x1 the x position of the first point of the segment.
        @param y1 the y position of the first point of the segment.
        @param x2 the x position of the second point of the segment.
        @param y2 the y position of the second point of the segment.
        @param width the width ot the line.
        @param layer the layer that should be used.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportPCBLine(int x1, int y1, int x2, int y2, int width,
        int layer)
        throws IOException
    {
        registerColorSize(layer, width);
        // This avoids that some of the exported lines are dashed!
        registerDash(0);

        out.write("\\pgfline{\\pgfxy("+x1+","+y1+")}{\\pgfxy("+
            x2+","+y2+")}\n");
    }

    /** Called when exporting a PCBPad primitive.

        @param x the x position of the pad.
        @param y the y position of the pad.
        @param style the style of the pad (0: oval, 1: square, 2: rounded
            square.)
        @param six the x size of the pad.
        @param siy the y size of the pad.
        @param indiam the hole internal diameter.
        @param layer the layer that should be used.
        @param onlyHole true if only the hole should be exported.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportPCBPad(int x, int y, int style, int six, int siy,
        int indiam, int layer, boolean onlyHole)
        throws IOException
    {
        double xdd;
        double ydd;

        if(onlyHole) {
            // ... then, drill the hole!
            if(!actualColor.equals(actualColor.white())) {
                actualColor=actualColor.white();
                out.write("\\color{white}\n");
            }

            out.write("\\pgfellipse[fillstroke"+
                "]{\\pgfxy("+x+","+y+")}{\\pgfxy("+
                (indiam/2)+",0)}{\\pgfxy(0,"+
                (indiam/2)+")}\n");
        } else {
            // At first, draw the pad...
            registerColorSize(layer, .33);
            switch (style) {
                case 1: // Square pad
                    xdd=(double)x-six/2.0;
                    ydd=(double)y-siy/2.0;
                    out.write("\\pgfrect[fillstroke"+
                        "]{\\pgfxy("+xdd+","+ydd+")}{\\pgfxy("+
                        six+","+
                        siy+")}\n");
                    break;
                case 2: // Rounded pad
                    xdd=(double)x-six/2.0;
                    ydd=(double)y-siy/2.0;
                    out.write("\\pgfrect[fillstroke"+
                        "]{\\pgfxy("+xdd+","+ydd+")}{\\pgfxy("+
                        six+","+
                        siy+")}\n");
                    break;
                case 0: // Oval pad
                default:
                    out.write("\\pgfellipse[fillstroke"+
                        "]{\\pgfxy("+x+","+y+")}{\\pgfxy("+
                        (six/2.0)+",0)}{\\pgfxy(0,"+
                        (siy/2.0)+")}\n");
                    break;
            }
        }
    }

    /** Called when exporting a Polygon primitive.

        @param vertices array containing the position of each vertex.
        @param nVertices number of vertices.
        @param isFilled true if the polygon is filled.
        @param layer the layer that should be used.
        @param dashStyle dashing style.
        @param strokeWidth the width of the pen to be used when drawing.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportPolygon(PointDouble[] vertices, int nVertices,
        boolean isFilled, int layer, int dashStyle, double strokeWidth)
        throws IOException
    {
        registerColorSize(layer, strokeWidth);
        registerDash(dashStyle);

        String fill_pattern="";
        int i;

        out.write("\\pgfmoveto{\\pgfxy("+vertices[0].x+","+
            vertices[0].y+")}\n");
        for (i=1; i<nVertices; ++i) {
            out.write("\\pgflineto{\\pgfxy("+vertices[i].x+
                ","+vertices[i].y+")}\n");

        }
        out.write("\\pgfclosepath \n");
        if(isFilled)
            out.write("\\pgffill \n");
        else
            out.write("\\pgfqstroke \n");

    }

    /** Called when exporting a Curve primitive.

        @param vertices array containing the position of each vertex.
        @param nVertices number of vertices.
        @param isFilled true if the polygon is filled.
        @param isClosed true if the curve is closed.
        @param layer the layer that should be used.
        @param arrowStart specify if an arrow is present at the first point.
        @param arrowEnd specify if an arrow is present at the second point.
        @param arrowStyle the style of the arrow.
        @param arrowLength total lenght of arrows (if present).
        @param arrowHalfWidth half width of arrows (if present).
        @param dashStyle dashing style.
        @param strokeWidth the width of the pen to be used when drawing.
        @return false if the curve should be rendered using a polygon, true
            if it is handled by the function.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public boolean exportCurve(PointDouble[] vertices, int nVertices,
        boolean isFilled, boolean isClosed, int layer,
        boolean arrowStart,
        boolean arrowEnd,
        int arrowStyle,
        int arrowLength,
        int arrowHalfWidth,
        int dashStyle,
        double strokeWidth)
        throws IOException
    {
        return false;
    }

    /** Called when exporting a Rectangle primitive.

        @param x1 the x position of the first corner.
        @param y1 the y position of the first corner.
        @param x2 the x position of the second corner.
        @param y2 the y position of the second corner.
        @param isFilled it is true if the rectangle should be filled.

        @param layer the layer that should be used.
        @param dashStyle dashing style.
        @param strokeWidth the width of the pen to be used when drawing.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    public void exportRectangle(int x1, int y1, int x2, int y2,
        boolean isFilled, int layer, int dashStyle, double strokeWidth)
        throws IOException
    {
        registerColorSize(layer, strokeWidth);
        registerDash(dashStyle);

        out.write("\\pgfmoveto{\\pgfxy("+x1+","+y1+")}\n");
        out.write("\\pgflineto{\\pgfxy("+x2+","+y1+")}\n");
        out.write("\\pgflineto{\\pgfxy("+x2+","+y2+")}\n");
        out.write("\\pgflineto{\\pgfxy("+x1+","+y2+")}\n");

        out.write("\\pgfclosepath \n");
        if(isFilled)
            out.write("\\pgffill \n");
        else
            out.write("\\pgfqstroke \n");
    }


    /** Check if there has been a change in the actual color and stroke width.
        if yes, change accordingly.
        @param layer the layer number (used for the color specification).
        @param strokeWidth (nothing is specified if non positive).
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    private void registerColorSize(int layer, double strokeWidth)
        throws IOException
    {
        LayerDesc l=(LayerDesc)layerV.get(layer);
        ColorInterface c=l.getColor();
        if(!c.equals(actualColor)) {
            actualColor=c;
            out.write("\\color{layer"+layer+"}\n");
        }
        if (strokeWidth > 0 && actualWidth!=strokeWidth) {
            out.write("\\pgfsetlinewidth{"+strokeWidth
                +"pt}\n");
            actualWidth = strokeWidth;
        }
    }
    /** Check if there has been a change in the actual dash style if yes,
        change accordingly.
        @param dashStyle the wanted dashing style.
        @throws IOException if a disaster happens, i.e. a file can not be
            accessed.
    */
    private void registerDash(int dashStyle)
        throws IOException
    {
        if(currentDash!=dashStyle ||currentPhase!=dashPhase) {
            currentDash=dashStyle;
            currentPhase=dashPhase;
            if(dashStyle==0)
                out.write("\\pgfsetdash{}{0pt}\n");
            else
                out.write("\\pgfsetdash{"+sDash[dashStyle]+"}{"+dashPhase+
                    "pt}\n");
        }
    }
}