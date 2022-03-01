package com.oddinstitute.memorygametemp

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


/** Tasks
 *  Change the layout so that the game appears on the op and the restart button is immediately below it
 *  Refactor the code
 *  Rename attributes one at a time (without refactoring) and learn what they do
 *  Add comments to all parts of the app
 *  Find out when the user wins the game
 *  Change the grid size from 4 to a dynamic value
 *  Make the tile animation work
*/



class MainActivity : AppCompatActivity(),
                    GameFragmentListener
{
    var thisIsSecondTap: Boolean = false
    lateinit var tile1: Tile
    lateinit var tile2: Tile
    var gameIsActive = true
   // val foundTiles: ArrayList<Tile> = ArrayList()


    override fun tileTapped(tile: Tile, index: Int)
    {
        // CHANGE THIS IN HERE
        if (tile.tileStatus != Status.UNKNOWN || !gameIsActive)
            return

        tile.tileStatus = Status.FLIPPED
        tile.updateTile()


        if (!thisIsSecondTap)
        {
            tile1 = tile
            thisIsSecondTap = true
        }
        else
        {
            tile2 = tile
            thisIsSecondTap = false

            gameIsActive = false
            compare()
        }
    }


    // task -> Move this to the Fragment class
    override fun makeTiles(): ArrayList<Tile>
    {
        val tilesArr: ArrayList<Tile> = ArrayList()

        for (i in 1..16)
        {
            var num = i
            if (num > 8)
                num -= 8

            val newTile = Tile(this, num)

            newTile.updateTile()
            tilesArr.add(newTile)
        }

        tilesArr.shuffle()
        return tilesArr
    }


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        restartGame()

        findViewById<Button>(R.id.restartButton).setOnClickListener {
            restartGame()
        }
    }


    fun compare()
    {
        Handler(Looper.getMainLooper()).postDelayed(
                {
                    if (tile1.value == tile2.value)
                    {
                        tile1.tileStatus = Status.FOUND
                        tile1.updateTile()

                        tile2.tileStatus = Status.FOUND
                        tile2.updateTile()

//                        foundTiles.add(tile1)
//                        foundTiles.add(tile2)


                    }
                    else
                    {
                        tile1.tileStatus = Status.UNKNOWN
                        tile1.updateTile()

                        tile2.tileStatus = Status.UNKNOWN
                        tile2.updateTile()
                    }

                    gameIsActive = true
                }, 800)

    }

    fun restartGame()
    {
        gameIsActive = true
//        foundTiles.clear()
        thisIsSecondTap = false

        val currentGameFrag = supportFragmentManager.findFragmentByTag("game")
        if (currentGameFrag != null)
            supportFragmentManager.beginTransaction().remove(currentGameFrag).commit()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.gameLayout, GameFragment.newInstance(4),"game")
            .commit()
    }
}





interface GameFragmentListener
{
    /*** Can you put this function inside the Fragment class */
    fun makeTiles(): ArrayList<Tile>
    fun tileTapped(tile: Tile, index: Int)
}


class GameFragment(var grid: Int) : Fragment()
{
    private lateinit var listener: GameFragmentListener

    companion object
    {
        fun newInstance(gridSize: Int): GameFragment
        {
            return GameFragment(gridSize)
        }
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        if (context is GameFragmentListener)
        {
            listener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        val fragView = inflater.inflate(R.layout.fragment_game,
                                        container,
                                        false)

        val activity = activity as Context
        val recyclerView: RecyclerView = fragView.findViewById(R.id.gameRv)
        recyclerView.layoutManager = GridLayoutManager(activity, grid)
        recyclerView.adapter = GameRecyclerAdapter(listener.makeTiles(), listener)

        return fragView
    }
}



class MyViewHolder(frameLayout: SquareFrameLayout) :
        RecyclerView.ViewHolder(frameLayout)

internal class GameRecyclerAdapter(private val inputDataSet: ArrayList<Tile>,
                                   private val listener: GameFragmentListener) :
        RecyclerView.Adapter<MyViewHolder>()
{
    lateinit var tileContainer: FrameLayout


    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder
    {
        val frameLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layout,
                     parent,
                     false) as SquareFrameLayout


        val newViewHolder = MyViewHolder(frameLayout)

        tileContainer = newViewHolder.itemView.findViewById(R.id.tileContainer)

        return newViewHolder
    }

    override fun getItemCount(): Int = inputDataSet.size

    override fun onBindViewHolder(holder: MyViewHolder,
                                  position: Int)
    {
        val thisTile: Tile = inputDataSet[position]

        tileContainer.setBackgroundColor(Color.LTGRAY)

        val myParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT)

        thisTile.layoutParams = myParams
        myParams.setMargins(5)
        thisTile.gravity = Gravity.CENTER
        thisTile.textSize = 24F


        tileContainer.addView(thisTile)


        tileContainer.setOnClickListener {

            listener.tileTapped(thisTile,
                                position)
        }
    }

}

class SquareFrameLayout : FrameLayout
{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        setMeasuredDimension(width, width)
    }
}



enum class Status
{
    UNKNOWN, FLIPPED, FOUND
}

data class Tile(var myContext: Context,
                var value: Int) : TextView(myContext)
{
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width = measuredWidth
        setMeasuredDimension(width, width)
    }

    var tileStatus: Status = Status.UNKNOWN


    fun updateTile()
    {
        when (tileStatus)
        {
            Status.UNKNOWN ->
            {
                this@Tile.text = "❓"
                this@Tile.setBackgroundColor(Color.DKGRAY)
            }
            Status.FLIPPED ->
            {
                this@Tile.text = this@Tile.value.toString()
                this@Tile.setBackgroundColor(Color.YELLOW)
            }
            Status.FOUND ->
            {
                this@Tile.text = "🙂"
                this@Tile.setBackgroundColor(Color.GREEN)
            }
        }

        //
        //        val objAnim1 = ObjectAnimator.ofFloat(this,
        //                                              "scaleX",
        //                                              1f,
        //                                              0f)
        //        val objAnim2 = ObjectAnimator.ofFloat(this,
        //                                              "scaleX",
        //                                              0f,
        //                                              1f)
        //        objAnim1.duration = 250
        //        objAnim2.duration = 250
        //
        //        objAnim1.interpolator = DecelerateInterpolator()
        //        objAnim2.interpolator = AccelerateDecelerateInterpolator()
        //        objAnim1.addListener(
        //                object : AnimatorListenerAdapter()
        //                {
        //                    override fun onAnimationEnd(animation: Animator)
        //                    {
        //                        super.onAnimationEnd(animation)
        //
        //                        when (tileStatus)
        //                        {
        //                            Status.UNKNOWN ->
        //                            {
        //                                this@Tile.text = "❓"
        //                                this@Tile.setBackgroundColor(Color.DKGRAY)
        //                            }
        //                            Status.FLIPPED ->
        //                            {
        //                                this@Tile.text = this@Tile.value.toString()
        //                                this@Tile.setBackgroundColor(Color.YELLOW)
        //                            }
        //                            Status.FOUND ->
        //                            {
        //                                this@Tile.text = "🙂"
        //                                this@Tile.setBackgroundColor(Color.GREEN)
        //                            }
        //                        }
        //
        //                        objAnim2.start()
        //                    }
        //                })
        //        objAnim1.start()

    }
}

