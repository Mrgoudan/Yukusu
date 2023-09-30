package android.support.test.espresso.search;

import android.view.View;

/**
 * Created by mattia on 11/10/17.
 */

public class ScoredHierarchyTarget {

    private double score = 0;
    private View view;
    private TargetViewModel viewModel;

    public ScoredHierarchyTarget(double score, View view, TargetViewModel viewModel){
        this.score=score;
        this.view=view;
        this.viewModel=viewModel;
    }

    public double getScore(){
        return score;
    }

    public View getView(){
        return view;
    }

    public TargetViewModel getViewModel(){
        return viewModel;
    }
}
