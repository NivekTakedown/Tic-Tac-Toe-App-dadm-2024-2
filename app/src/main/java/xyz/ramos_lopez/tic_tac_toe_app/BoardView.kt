package xyz.ramos_lopez.tic_tac_toe_app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BoardView : View {

    // Constante para el grosor de las líneas del tablero
    private val mBoardLineWidth = 6

    // Bitmaps para las imágenes de X y O
    private lateinit var mHumanBitmap: Bitmap
    private lateinit var mComputerBitmap: Bitmap

    // Objeto Paint para dibujar las líneas y los símbolos
    private lateinit var mPaint: Paint

    // Referencia al juego
    private lateinit var mGame: TicTacToeGame

    // Listener para movimientos
    private var mMoveListener: MoveListener? = null

    // Constructores
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initialize()
    }

    private fun initialize() {
        // Cargar las imágenes desde los recursos
        mHumanBitmap = BitmapFactory.decodeResource(resources, R.drawable.x_img)
        mComputerBitmap = BitmapFactory.decodeResource(resources, R.drawable.o_img)

        // Inicializar el objeto Paint
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun setGame(game: TicTacToeGame) {
        mGame = game
    }

    fun setMoveListener(listener: MoveListener) {
        mMoveListener = listener
    }

    interface MoveListener {
        fun onMoveMade()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Obtener las dimensiones del View
        val boardWidth = width
        val boardHeight = height

        // Definir el color y grosor de las líneas del tablero
        mPaint.color = Color.LTGRAY
        mPaint.strokeWidth = mBoardLineWidth.toFloat()

        // Calcular el ancho y alto de cada celda
        val cellWidth = boardWidth / 3
        val cellHeight = boardHeight / 3

        // Dibujar las líneas verticales del tablero
        canvas.drawLine(cellWidth.toFloat(), 0f, cellWidth.toFloat(), boardHeight.toFloat(), mPaint)
        canvas.drawLine((2 * cellWidth).toFloat(), 0f, (2 * cellWidth).toFloat(), boardHeight.toFloat(), mPaint)

        // Dibujar las líneas horizontales del tablero
        canvas.drawLine(0f, cellHeight.toFloat(), boardWidth.toFloat(), cellHeight.toFloat(), mPaint)
        canvas.drawLine(0f, (2 * cellHeight).toFloat(), boardWidth.toFloat(), (2 * cellHeight).toFloat(), mPaint)

        // Dibujar las imágenes de X y O según el estado del juego
        if (::mGame.isInitialized) {
            for (i in 0 until TicTacToeGame.BOARD_SIZE) {
                val col = i % 3
                val row = i / 3

                // Definir los límites de la imagen dentro de la celda
                val left = (col * cellWidth).toFloat() + mBoardLineWidth
                val top = (row * cellHeight).toFloat() + mBoardLineWidth
                val right = ((col + 1) * cellWidth).toFloat() - mBoardLineWidth
                val bottom = ((row + 1) * cellHeight).toFloat() - mBoardLineWidth

                val pieza = mGame.getBoardValue(i)
                when (pieza) {
                    TicTacToeGame.HUMAN_PLAYER -> {
                        // Dibujar la imagen de X
                        canvas.drawBitmap(
                            mHumanBitmap,
                            null, // Fuente completa del bitmap
                            RectF(left, top, right, bottom), // Destino donde dibujar
                            null
                        )
                    }
                    TicTacToeGame.COMPUTER_PLAYER -> {
                        // Dibujar la imagen de O
                        canvas.drawBitmap(
                            mComputerBitmap,
                            null, // Fuente completa del bitmap
                            RectF(left, top, right, bottom), // Destino donde dibujar
                            null
                        )
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            val cellWidth = width / 3
            val cellHeight = height / 3

            val col = (x / cellWidth).toInt()
            val row = (y / cellHeight).toInt()

            val pos = row * 3 + col

            if (::mGame.isInitialized && mGame.getBoardValue(pos) == TicTacToeGame.OPEN_SPOT) {
                // Usuario realizó un movimiento
                mGame.setMove(TicTacToeGame.HUMAN_PLAYER, pos)
                invalidate() // Redibujar el tablero

                // Notificar al listener (MainActivity) que se realizó un movimiento
                mMoveListener?.onMoveMade()
            }

            return true
        }

        return false
    }
}
