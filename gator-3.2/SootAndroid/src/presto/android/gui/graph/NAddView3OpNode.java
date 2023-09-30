/*
 * NAddView3OpNode.java
 * Created by Mattia Fazzini
 */
package presto.android.gui.graph;

import soot.SootMethod;
import soot.jimple.Stmt;
import soot.toolkits.scalar.Pair;

//mf: added
//AddView3: parent.addHeaderView(child, data, isSelectable)
//AddView3: parent.addHeaderView(child)
public class NAddView3OpNode extends NOpNode {
  public NAddView3OpNode(NVarNode parent, NVarNode child,
      Pair<Stmt, SootMethod> callSite, boolean artificial) {
    super(callSite, artificial);
    child.addEdgeTo(this);
    parent.addEdgeTo(this);
  }

  @Override
  public NVarNode getReceiver() {
    return (NVarNode) this.pred.get(1);
  }

  @Override
  public boolean hasReceiver() {
    return true;
  }

  @Override
  public NNode getParameter() {
    return this.pred.get(0);
  }

  @Override
  public boolean hasParameter() {
    return true;
  }

  // no getLhs()
  @Override
  public boolean hasLhs() {
    return false;
  }
}