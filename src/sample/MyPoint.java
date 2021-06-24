/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample;

public class MyPoint<T, U> {
    private T x;
    private U y;

    public MyPoint(T x, U y){
        this.x = x;
        this.y = y;
    }

    public void setX (T x){
        this.x = x;
    }

    public void setY (U y){
        this.y = y;
    }

    public T getX (){
        return x;
    }

    public U getY (){
        return y;
    }
}
