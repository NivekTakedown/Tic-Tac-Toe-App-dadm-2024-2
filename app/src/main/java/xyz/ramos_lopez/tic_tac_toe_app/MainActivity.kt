package xyz.ramos_lopez.tic_tac_toe_app

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    companion object {
        private const val DIALOG_DIFFICULTY_ID = 0
        private const val DIALOG_QUIT_ID = 1
        private const val DIALOG_ABOUT_ID = 2
    }

    private lateinit var game: TicTacToeGame
    private lateinit var infoTextView: TextView

    private var humanScore = 0
    private var tieScore = 0
    private var computerScore = 0

    private lateinit var boardView: BoardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        // Inicializar el juego
        game = TicTacToeGame()

        // Obtener referencias a las vistas
        infoTextView = findViewById(R.id.information)

        boardView = findViewById(R.id.board)
        boardView.setGame(game)
        boardView.setMoveListener(object : BoardView.MoveListener {
            override fun onMoveMade() {
                onHumanMove()
            }
        })

        startNewGame()
    }

    private fun startNewGame() {
        game.clearBoard()
        boardView.isEnabled = true
        boardView.invalidate()
        infoTextView.text = getString(R.string.first_human)
        updateScoreboard()
    }

    private fun updateScoreboard() {
        // Actualiza el marcador en la pantalla
        findViewById<TextView>(R.id.human_score).text = "Human: $humanScore"
        findViewById<TextView>(R.id.tie_score).text = "Ties: $tieScore"
        findViewById<TextView>(R.id.computer_score).text = "Computer: $computerScore"
    }

    private fun onHumanMove() {
        var winner = game.checkForWinner()
        if (winner == 0) {
            infoTextView.text = getString(R.string.turn_computer)
            val move = game.getComputerMove()
            if (move != -1) {
                game.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                boardView.invalidate()
            }
            winner = game.checkForWinner()
        }

        handleWinner(winner)
    }

    private fun handleWinner(winner: Int) {
        infoTextView.text = when (winner) {
            0 -> getString(R.string.turn_human)
            1 -> {
                tieScore++
                getString(R.string.result_tie)
            }
            2 -> {
                humanScore++
                getString(R.string.result_human_wins)
            }
            else -> {
                computerScore++
                getString(R.string.result_computer_wins)
            }
        }

        if (winner != 0) {
            // Juego terminado
            boardView.isEnabled = false
        }
        updateScoreboard()
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
}
