package net.sourceforge.fidocadj.circuit.model;

import java.util.*;

import net.sourceforge.fidocadj.circuit.controllers.UndoActions;
import net.sourceforge.fidocadj.geom.*;
import net.sourceforge.fidocadj.layers.*;
import net.sourceforge.fidocadj.primitives.*;
import net.sourceforge.fidocadj.graphic.*;

/**
	Database of the FidoCadJ drawing. This is the "model" in the 
	model/view/controller pattern. Offers methods to modify its contents,
	but they are relatively low level and database-oriented. More high-level
	operations can be done via the controllers operating on this class.
	
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
    along with FidoCadJ.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2007-2014 by Davide Bucci
</pre>
    
    @author Davide Bucci
*/

public class DrawingModel
{   
    // ************* DRAWING *************
    
    // Array used to determine which layer is used in the drawing.
    public boolean[] layersUsed;   

    // Higher priority layer used in the drawing.
    public int maxLayer;
    // True if only pads should be drawn.
    public boolean drawOnlyPads;
    
    // Positive if during the redraw step only a particular layer should be
    // drawn
    public int drawOnlyLayer;
    
    private int exportBorder;

    // Font and size to be used for the text associated to the macros.
    private String macroFont;
    private int macroFontSize;
    
    // True if the drawing characteristics have been modified. This implies
    // that during the first redraw a in-depth calculation of all coordinates
    // will be done. For performance reasons, this is indeed done only when
    // necessary.
    public boolean changed;		// TODO: should be private
    
    // ******* PRIMITIVE DATABASE ********
    
    // Vector containing all primitives in the drawing.
    private Vector<GraphicPrimitive> primitiveVector;
    // Vector containing all layers used in the drawing.
    public Vector<LayerDesc> layerV;

    // Library of macros loaded.
    private Map<String, MacroDesc> library;
  
    /** The standard constructor. Not so much interesting, apart for the
        fact that it allocates memory of a few internal objects and reset all
        state flags.
    */
    public DrawingModel()
    {
        setPrimitiveVector(new Vector<GraphicPrimitive>(25));
        layerV=new Vector<LayerDesc>(LayerDesc.MAX_LAYERS);
        library=new TreeMap<String, MacroDesc>();
        macroFont = "Courier New";
        
        drawOnlyPads=false;
        drawOnlyLayer=-1;
        layersUsed = new boolean[LayerDesc.MAX_LAYERS];
        exportBorder=0;
        changed=true;
    }
    
    /** Get the first selected primitive
        @return the selected primitive, null if none.
    */
    public GraphicPrimitive getFirstSelectedPrimitive()
    {
        for (GraphicPrimitive g: primitiveVector) {
            if (g.getSelected())
                return g;
        }
        return null;
    }
    
    /** Apply an action to all elements contained in the model.
    	@tt the method containing the action to be performed 
    */
    public void applyToAllElements(ProcessElementsInterface tt)
    {
    	for (GraphicPrimitive g:primitiveVector){
    		tt.doAction(g);
    	}
    }
    
    /** Apply an action to selected elements contained in the model.
    	@tt the method containing the action to be performed
    */
    public void applyToSelectedElements(ProcessElementsInterface tt)
    {
    	for (GraphicPrimitive g:primitiveVector){
    		if (g.getSelected())
    			tt.doAction(g);
    	}
    }    
    
    /** Get the layer description vector
        @return a vector of LayerDesc describing layers.
    */
    public Vector<LayerDesc> getLayers()
    {
        return layerV;
    }
    
    /** Set the layer description vector.
        @param v a vector of LayerDesc describing layers.
    */
    public void setLayers(final Vector<LayerDesc> v)
    {
        layerV=v;
        applyToAllElements(new ProcessElementsInterface()
        {
        	public void doAction(GraphicPrimitive g)
        	{
        		if (g instanceof PrimitiveMacro) {
            		((PrimitiveMacro) g).setLayers(v);
            	}
        	}
        });
        changed=true;
    }
    
    /** Sets whether during the export a border should be added.
    @param b true if a border must be added.
    */
    public void setExportBorder(int b)
    {
    	exportBorder=b;
    }
    
    /** Get the current library
        @return a map String/String describing the current library.  
    */
    public Map<String, MacroDesc> getLibrary()
    {
        return library;
    }
    
    /** Specify the current library.
        @param l the new library (a String/String hash table)
    */
    public void setLibrary(Map<String, MacroDesc> l)
    {
        library=l;
        changed=true;
    }
    
    /** Resets the current library.
    */
    public void resetLibrary()
    {
        setLibrary(new TreeMap<String, MacroDesc>());
        changed=true;
    }
    
       
    /** Add a graphic primitive.
        @param p the primitive to be added.
        @param sort if true, sort the primitive layers
        @param ua if different from <pre>null</pre>, the operation will be
        	undoable.
    */
    public void addPrimitive(GraphicPrimitive p, boolean sort,
    	UndoActions ua)
    {   
        // The primitive database MUST be ordered. The idea is that we insert
        // primitives without ordering them and then we call a sorter.
        synchronized(this) {        
        	getPrimitiveVector().add(p);

        	// We check if the primitives should be sorted depending of 
        	// their layer
        	// If there are more than a few primitives to insert, it is wise to
        	// sort only once, at the end of the insertion process.
        	if (sort)
            	sortPrimitiveLayers();
            
        	// Check if it should be undoable.
        	if (ua!=null) {
        		ua.saveUndoState();
        		ua.setModified(true);
        		// We now have to track that something has changed. This 
        		// forces all the
        		// caching system used by the drawing routines to be refreshed.
        		changed=true;
        	}
        }
    }

    /** Set the font of all elements.
        @param f the font name
        @param tsize the size
        @param ua the undo controller or null if not useful.
    */
    public void setTextFont(String f, int tsize, UndoActions ua)
    {
        int size=tsize;

        macroFont=f;
        macroFontSize = size;

        for (GraphicPrimitive g:getPrimitiveVector()){
           g.setMacroFont(f, size);
        }
        changed=true;
        if(ua!=null) ua.setModified(true);
    }
    
    /** Get an array describing the state of selection of the objects.
    	@return a vector containing Boolean objects with the selection states
    		of all objects in the database.
    */
    public Vector<Boolean> getSelectionStateVector()
    {
        int i;
        Vector<Boolean> v = new Vector<Boolean>(getPrimitiveVector().size());
   
        for(GraphicPrimitive g : getPrimitiveVector()) {
        	if(g.getSelected()) {
        		v.add(Boolean.TRUE);
        	} else {
        		v.add(Boolean.FALSE);
        	}
        }
        return v;
    }
    
    /** Select/deselect all primitives.
    	@param state true if you want to select, false for deselect.  
    */
    public void setSelectionAll(boolean state)
    {
        for (GraphicPrimitive g: getPrimitiveVector()) {
            g.setSelected(state);
        }   
    }
    
    /** Sets the state of the objects in the database according to the given
    	vector.
    	@param v the vector containing the selection state of elements
    */
    public void setSelectionStateVector(Vector<Boolean> v)
    {
    	int i=0;
        
        for(GraphicPrimitive g : getPrimitiveVector()) {
        	g.setSelected(v.get(i++).booleanValue());
        }
    }
    
    /** Get the font of all macros.
        @return the font name
    */
    public String getTextFont()
    {   
        return macroFont;
    }
    
    /** Get the size of the font used for all macros.
        @return the font name
    */
    public int getTextFontSize()
    {   
        if(getPrimitiveVector().isEmpty())
        	return macroFontSize;
        
        // TODO: not very elegant piece of code.
        // Basically, we grab the settings of the very first object stored.	
     	int size=((GraphicPrimitive)getPrimitiveVector().get(0))
                   .getMacroFontSize();
                    
        if(size<=0) size=1;
        macroFontSize=size;
    
        return macroFontSize;
    }
   
    /** Performs a sort of the primitives on the basis of their layer.
        The sorting metod adopted is the Shell sort. By the practical point
        of view, this seems to be rather good even for large drawings. This is
        because the primitive list is always more or less already ordered.
    */
    public void sortPrimitiveLayers()
    {
        int i;
        GraphicPrimitive t,g,gg;
        boolean cont=true;
        maxLayer = 0;
        
        // Indexes
        int j,k,l;
        // Swap temporary variable
        GraphicPrimitive s;
        
        // Shell sort. This is a farly standard implementation
        for(l = getPrimitiveVector().size()/2; l>0; l/=2) {
            for(j = l; j< getPrimitiveVector().size(); ++j) {
                for(i=j-l; i>=0; i-=l) {
                    if(((GraphicPrimitive)getPrimitiveVector().get(i+l)).layer>=
                      ((GraphicPrimitive)getPrimitiveVector().get(i)).layer)
                        break;
                    else {
                        // Swap
                        s = (GraphicPrimitive)getPrimitiveVector().get(i);
                        getPrimitiveVector().set(i,
                        	getPrimitiveVector().get(i+l));
                        getPrimitiveVector().set(i+l, s);
                    }
                }
            }
        }
    
        // Since for sorting we need to analyze all the primitives in the 
        // database, this is a good place to calculate which layers are
        // used. We thus start by resetting the array.
        maxLayer = -1;
        k=0;
        
        for (l=0; l<LayerDesc.MAX_LAYERS; ++l) {
            layersUsed[l] = false;
            
            for (i=k; i<getPrimitiveVector().size(); ++i){
                g=(GraphicPrimitive)getPrimitiveVector().get(i);
                
                // We keep track of the maximum layer number used in the 
                // drawing.
                if (g.layer>maxLayer)
                        maxLayer = g.layer;
                
                if (g.containsLayer(l)) {
                    layersUsed[l]=true;
                    k=i;
                    for (int z = 0; z<l; ++z) layersUsed[z]=true;
                    break;
                }                
            }
        }
    }
    /** Get the maximum layer which contains something. This value is updated
        after a redraw. This is tracked for efficiency reasons.
        
        @return the maximum layer number.
    */
    final public int getMaxLayer()
    {
        return maxLayer;
    }    
    
    /** Returns true if the specified layer is contained in the schematic
        being drawn. The analysis is done when the schematics is created, so
        the results of this method are ready before the redraw step.
        
        @return true if the specified layer is contained in the drawing.
    */
    public boolean containsLayer(int l)
    {
        return layersUsed[l];
    }

    
    /** Returns true if there is no drawing in memory
        @return true if the drawing is empty.
    */
    public boolean isEmpty()
    {
        return getPrimitiveVector().isEmpty();
    }
    
    /** Set the change state of the class. Changed just means that we want 
    	to  recalculate everything in deep during the following redraw.
    	This is different from being "modified", since "modified" implies 
    	that the current drawing has not been saved yet. 
    
        @param c if true, force a deep recalculation of all primitive 
            parameters at the first redraw.
    
    */
    public final void setChanged(boolean c)
    {
        changed=c;
    }

	/** Obtains a vector containing all elements.
		@return the vector containing all graphical objects.
	*/
	public Vector<GraphicPrimitive> getPrimitiveVector() 
	{
		return primitiveVector;
	}

	/** Sets a vector containing all elements.
		@param primitiveVector the vector containing all graphical objects.
	*/
	public void setPrimitiveVector(Vector<GraphicPrimitive> primitiveVector) 
	{
		this.primitiveVector = primitiveVector;
	}

    /** Specify that the drawing process should only draw holes of the pcb
        pad
        
        @param pd it is true if only holes should be drawn   
    */  
    public void setDrawOnlyPads(boolean pd)
    {
        drawOnlyPads=pd;
    }
    
    /** Set the layer to be drawn. If it is negative, draw all layers.
        @param la the layer to be drawn.    
    */
    public void setDrawOnlyLayer(int la)
    {
        drawOnlyLayer=la;
    }
}

