/*
 * NMenuItemInflNode.java - part of the GATOR project
 *
 * Copyright (c) 2014, 2015 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE in the
 * root directory.
 */

package presto.android.gui.graph;

import java.util.HashMap;
import java.util.Iterator;

public class NMenuItemInflNode extends NInflNode {
  
  /**
   * Attributes of a menu item.
   */
  public HashMap<String, String> attrs;

  @Override
  public String toString() {
    String p = "";
    if (parents == null) {
      p = "*]";
    } else if (parents.size() == 1) {
      p = parents.iterator().next().id + "]";
    } else {
      for (NNode n : parents) {
        p += n.id + ";";
      }
      p += "]";
    }
    
    //mf: logging
//    String text = "";
//    Iterator<NNode> textNodeIterator = getTextNodes();
//    while(textNodeIterator.hasNext()){
//    	NNode currTextNode = textNodeIterator.next();
//    	if(currTextNode instanceof NStringConstantNode){
//    		NStringConstantNode stringConstantNode = (NStringConstantNode) currTextNode;
//    		text = text + "#" + stringConstantNode.value;
//    	}
//    	else if (currTextNode instanceof NStringIdNode) {
//    		text = text + "#" + "here";
//    		
////          Integer stringId = ((NStringIdNode)textNode).getIdValue();
////          if (stringId == null) {
////            return null;
////          }
////          return xml.getStringValue(stringId);
//    	}
//    }
//    return "MenuItemINFL[" + c + "," + text + "," + (idNode == null ? "*" : idNode) + "," + p + id;
    return "MenuItemINFL[" + c + "," + (idNode == null ? "*" : idNode) + "," + p + id;
  }
  
  public boolean showAsAction() {
    for (String attr : attrs.keySet()) {
      if (attr.contains(":showAsAction")) {
        return true;
      }  
    }
    return false;
  }
}
