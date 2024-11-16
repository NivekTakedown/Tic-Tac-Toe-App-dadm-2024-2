package xyz.ramos_lopez.tic_tac_toe_app

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var game: TicTacToeGame
    private lateinit var boardButtons: Array<Button>
    private lateinit var infoTextView: TextView
    private lateinit var newGameButton: Button

    // Variables para el marcador
    private var humanScore = 0
    private var tieScore = 0
    private var computerScore = 0

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
                    else -> throw IllegalStateException("Índice de botón no válido")
                }
            )
        }

        infoTextView = findViewById(R.id.information)
        newGameButton = findViewById(R.id.new_game_button)

        // Configurar el botón de nuevo juego
        newGameButton.setOnClickListener {
            startNewGame()
        }

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
        infoTextView.text = getString(R.string.first_human)
        updateScoreboard()
    }

    private fun updateScoreboard() {
        // Actualiza el marcador en la pantalla
        findViewById<TextView>(R.id.human_score).text = "Human: $humanScore"
        findViewById<TextView>(R.id.tie_score).text = "Ties: $tieScore"
        findViewById<TextView>(R.id.computer_score).text = "Computer: $computerScore"
    }

    private inner class ButtonClickListener(private val location: Int) : View.OnClickListener {
        override fun onClick(view: View) {
            if (boardButtons[location].isEnabled) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location)
                var winner = game.checkForWinner()
                if (winner == 0) {
                    infoTextView.text = getString(R.string.turn_computer)
                    val move = game.getComputerMove()
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                    winner = game.checkForWinner()
                }

                infoTextView.text = when (winner) {
                    0 -> getString(R.string.turn_human)
                    1 -> {
                        tieScore++  // Incrementar el marcador de empates
                        getString(R.string.result_tie)
                    }
                    2 -> {
                        humanScore++  // Incrementar el marcador de humanos
                        getString(R.string.result_human_wins)
                    }
                    else -> {
                        computerScore++  // Incrementar el marcador de la computadora
                        getString(R.string.result_computer_wins)
                    }
                }

                // Deshabilitar todos los botones solo cuando el juego haya terminado
                if (winner != 0) {
                    boardButtons.forEach { button ->
                        button.isEnabled = false
                    }
                }

                updateScoreboard()  // Actualizar el marcador
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
