package yakusu.android.test;
/**
 * Created by mattia on 8/11/17.
 */

public class SelectorCoordinate extends Selector {

    private int x;
    private int y;

    public SelectorCoordinate(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

	@Override
	public String getEspressoCode() {
		//$T == ViewMatchers
		String statement = "$T.withCoordinate($L,$L)";
		return statement;
	}
}
