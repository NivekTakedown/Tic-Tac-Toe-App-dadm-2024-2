package xyz.ramos_lopez.tic_tac_toe_app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var game: TicTacToeGame
    // Botones que forman el tablero
    private lateinit var boardButtons: Array<Button>
    // Texto informativo
    private lateinit var infoTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        boardButtons = Array(TicTacToeGame.BOARD_SIZE) { index ->
            findViewById<Button>(
                when (index) {
                    0 -> R.id.one
                    1 -> R.id.two
                    2 -> R.id.three
                    3 -> R.id.four
                    4 -> R.id.five
                    5 -> R.id.six
                    6 -> R.id.seven
                    7 -> R.id.eight
                    8 -> R.id.nine
                    else -> throw IllegalStateException("Invalid button index")
                }
            )
        }

        infoTextView = findViewById(R.id.information)
        game = TicTacToeGame()
        startNewGame()
    }

    private fun startNewGame() {
        game.clearBoard()
        boardButtons.forEachIndexed { i, button ->
            button.apply {
                text = ""
                isEnabled = true
                setOnClickListener(ButtonClickListener(i))
            }
        }
        // El humano va primero
        infoTextView.text = "You go first."
    }

    // Manejador de clicks en los botones del tablero
    private inner class ButtonClickListener(private val location: Int) : View.OnClickListener {
        override fun onClick(view: View) {
            if (boardButtons[location].isEnabled) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location)
                // Si no hay ganador aún, deja que la computadora haga su movimiento
                var winner = game.checkForWinner()
                if (winner == 0) {
                    infoTextView.text = "It's Android's turn."
                    val move = game.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    winner = game.checkForWinner()
                }

                infoTextView.text = when (winner) {
                    0 -> "It's your turn."
                    1 -> "It's a tie!"
                    2 -> "You won!"
                    else -> "Android won!"
                }
            }
        }
    }

    private fun setMove(player: Char, location: Int) {
        game.setMove(player, location)
        boardButtons[location].apply {
            isEnabled = false
            text = player.toString()
            setTextColor(
                when (player) {
                    TicTacToeGame.HUMAN_PLAYER -> Color.rgb(0, 200, 0)
                    else -> Color.rgb(200, 0, 0)
                }
            )
        }
    }
}