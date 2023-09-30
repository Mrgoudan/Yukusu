package android.support.test.espresso.search;

import java.util.Set;

/**
 * Created by mattia on 7/12/17.
 */

public class GraphUtils {

    public static String getWindowFromNodeId(int nodeId){
        String query = "MATCH (a {nodeId:"+nodeId+"})<-[r:hasChild|hasRoot*]-(b) RETURN last(collect(b))";
        return query;
    }

    public static String getViewFromText(String text){
        String query = "MATCH (v:View) WHERE v.text='"+text+"' RETURN v";
        return query;
    }

    public static String getView(){
        //String query = "MATCH (v:View) RETURN v";
        String query = "MATCH (n:View) WHERE n.text<>\"\" OR n.imageReference<>\"\" OR n.resourceIdReference<>\"\"  RETURN n";
        return query;
    }

    public static String getPathsFromWindowToView(String name, String type, int nodeId){
        String query = "MATCH (a {fullClassName:'"+name+"', type:'"+type+"'}),(b {nodeId:"+nodeId+"}), p = allShortestPaths((a)-[r:hasRoot|hasDescendant|click|longClick|openOptions*]->(b)) RETURN p, extract(x IN rels(p)| type(x)) AS types";
        return query;
    }
}
