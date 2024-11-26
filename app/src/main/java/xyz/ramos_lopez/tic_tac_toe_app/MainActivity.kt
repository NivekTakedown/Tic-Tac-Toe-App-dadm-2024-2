package xyz.ramos_lopez.tic_tac_toe_app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : Activity() {
    companion object {
        private const val DIALOG_DIFFICULTY_ID = 0
        private const val DIALOG_QUIT_ID = 1
        private const val DIALOG_ABOUT_ID = 2  // Añade esta constante
    }

    private lateinit var game: TicTacToeGame
    private lateinit var boardButtons: Array<Button>
    private lateinit var infoTextView: TextView

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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_game -> {
                startNewGame()
                return true
            }
            R.id.ai_difficulty -> {
                showDialog(DIALOG_DIFFICULTY_ID)
                return true
            }
            R.id.quit -> {
                showDialog(DIALOG_QUIT_ID)
                return true
            }
            R.id.about -> {
                showDialog(DIALOG_ABOUT_ID)
                return true
            }
        }
        return false
    }
    override fun onCreateDialog(id: Int): Dialog {
        val builder = AlertDialog.Builder(this)

        return when (id) {
            DIALOG_DIFFICULTY_ID -> {
                builder.setTitle(R.string.difficulty_choose)
                val levels = arrayOf(
                    getString(R.string.difficulty_easy),
                    getString(R.string.difficulty_harder),
                    getString(R.string.difficulty_expert)
                )

                // Obtener el nivel de dificultad actual
                val selected = when (game.getDifficultyLevel()) {
                    TicTacToeGame.DifficultyLevel.Easy -> 0
                    TicTacToeGame.DifficultyLevel.Harder -> 1
                    TicTacToeGame.DifficultyLevel.Expert -> 2
                }

                builder.setSingleChoiceItems(levels, selected) { dialog, item ->
                    dialog.dismiss()

                    // Establecer el nuevo nivel de dificultad
                    game.setDifficultyLevel(
                        when (item) {
                            0 -> TicTacToeGame.DifficultyLevel.Easy
                            1 -> TicTacToeGame.DifficultyLevel.Harder
                            else -> TicTacToeGame.DifficultyLevel.Expert
                        }
                    )

                    // Mostrar el nivel seleccionado
                    Toast.makeText(
                        applicationContext,
                        levels[item],
                        Toast.LENGTH_SHORT
                    ).show()
                }
                builder.create()
            }

            DIALOG_QUIT_ID -> {
                builder.setMessage(R.string.quit_question)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        this@MainActivity.finish()
                    }
                    .setNegativeButton(R.string.no, null)
                builder.create()
            }

            DIALOG_ABOUT_ID -> {
                val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val layout = inflater.inflate(R.layout.about_dialog, null)
                builder.setView(layout)
                    .setPositiveButton(R.string.ok, null)
                builder.create()
            }

            else -> super.onCreateDialog(id)
        }
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
